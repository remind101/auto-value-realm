package com.remind101.auto.value.realm;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

@AutoService(AutoValueExtension.class)
public class AutoValueRealmExtension extends AutoValueExtension {
    private static final String TO_REALM_OBJECT_METHOD_NAME = "toRealmObject";
    private static final String TO_MODEL_METHOD_NAME = "toModel";

    private static final List<String> SUPPORTED_TYPES = Arrays.asList(String.class.getName(), Date.class.getName(), byte[].class.getName(), Boolean.class.getName(), Byte.class.getName(), Short.class.getName(), Integer.class.getName(), Float.class.getName(), Long.class.getName(), Double.class.getName());

    @Override
    public boolean applicable(Context context) {
        for (ExecutableElement method : context.abstractMethods()) {
            if (method.getSimpleName().toString().equals(TO_REALM_OBJECT_METHOD_NAME)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<ExecutableElement> consumeMethods(Context context) {
        for (ExecutableElement method : context.abstractMethods()) {
            if (method.getSimpleName().toString().equals(TO_REALM_OBJECT_METHOD_NAME)) {
                return Collections.singleton(method);
            }
        }
        return Collections.emptySet();
    }

    @Override
    public String generateClass(Context context, String className, String classToExtend, boolean isFinal) {
        verifyInput(context);
        createRealmObjectClass(context);

        String packageName = context.packageName();
        TypeSpec subclass = TypeSpec.classBuilder(className)
                .addModifiers(isFinal ? Modifier.FINAL : Modifier.ABSTRACT)
                .superclass(ClassName.get(packageName, classToExtend))
                .addMethod(createAutoValueConstructor(context))
                .addMethod(createToRealmObjectMethod(context))
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, subclass).build();
        return javaFile.toString();
    }

    private void verifyInput(Context context) {
        for (Map.Entry<String, ExecutableElement> entry : context.properties().entrySet()) {
            TypeMirror returnType = entry.getValue().getReturnType();
            if (!returnType.getKind().isPrimitive() && !SUPPORTED_TYPES.contains(returnType.toString())) {
                throw new IllegalArgumentException(context.autoValueClass().getSimpleName().toString() + "." + entry.getKey() +" is of a non supported type: " + entry.getValue().getReturnType().toString());
            }
        }
    }

    private void createRealmObjectClass(Context context) {
        TypeSpec.Builder realmObjectClassBuilder = TypeSpec.classBuilder(getRealmObjectType(context).simpleName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get("io.realm", "RealmObject"))
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AvRealmModel.class), getAvObjectType(context)));

        for (Map.Entry<String, ExecutableElement> property : context.properties().entrySet()) {
            boolean isPrimaryKey = property.getValue().getAnnotation(AvPrimaryKey.class) != null;
            boolean isIndex = property.getValue().getAnnotation(AvIndex.class) != null;
            TypeName propertyType = TypeName.get(property.getValue().getReturnType());
            FieldSpec.Builder fieldBuilder = FieldSpec.builder(propertyType, property.getKey())
                    .addModifiers(Modifier.PRIVATE);
            if (isPrimaryKey) {
                fieldBuilder.addAnnotation(ClassName.get("io.realm.annotations", "PrimaryKey"));
            }
            if (isIndex) {
                fieldBuilder.addAnnotation(ClassName.get("io.realm.annotations", "Index"));
            }
            MethodSpec setter = MethodSpec.methodBuilder(getSetterName(property.getKey()))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(propertyType, property.getKey())
                    .addStatement("this.$N = $N", property.getKey(), property.getKey())
                    .build();
            realmObjectClassBuilder.addField(fieldBuilder.build());
            realmObjectClassBuilder.addMethod(setter);
        }

        realmObjectClassBuilder.addMethod(createRealmToModelMethod(context));
        JavaFile file = JavaFile.builder(context.packageName(), realmObjectClassBuilder.build()).build();
        try {
            file.writeTo(context.processingEnvironment().getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static MethodSpec createAutoValueConstructor(Context context) {
        List<ParameterSpec> params = new ArrayList<>();
        for (Map.Entry<String, ExecutableElement> entry : context.properties().entrySet()) {
            TypeName typeName = TypeName.get(entry.getValue().getReturnType());
            params.add(ParameterSpec.builder(typeName, entry.getKey()).build());
        }

        StringBuilder body = new StringBuilder("super(");
        for (int i = context.properties().size(); i > 0; i--) {
            body.append("$N");
            if (i > 1) body.append(", ");
        }
        body.append(")");

        return MethodSpec.constructorBuilder()
                .addParameters(params)
                .addStatement(body.toString(), context.properties().keySet().toArray())
                .build();
    }

    private MethodSpec createRealmToModelMethod(Context context) {
        StringBuilder returnStatement = new StringBuilder("return new $T(");
        String[] propertyNames = context.properties().keySet().toArray(new String[context.properties().size()]);
        for (int i = 0; i < context.properties().size(); i++) {
            if (i != 0) {
                returnStatement.append(", ");
            }
            returnStatement.append(propertyNames[i]);
        }
        returnStatement.append(")");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(TO_MODEL_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(getAvObjectType(context))
                .addStatement(returnStatement.toString(), getAvImplType(context));
        return builder.build();
    }

    private MethodSpec createToRealmObjectMethod(Context context) {
        TypeName realmObjectType = getRealmObjectType(context);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(TO_REALM_OBJECT_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(realmObjectType)
                .addStatement("$T realmObject = new $T()", realmObjectType, realmObjectType);

        for (Map.Entry<String, ExecutableElement> property : context.properties().entrySet()) {
            builder.addStatement("realmObject.$N($N())", getSetterName(property.getKey()), property.getValue().getSimpleName().toString());
        }

        builder.addStatement("return realmObject");
        return builder.build();
    }

    private String getSetterName(String propertyName) {
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private ClassName getRealmObjectType(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.autoValueClass().getSimpleName());
        Element element = context.autoValueClass();
        while (element.getEnclosingElement() != null && element.getEnclosingElement().getKind().isClass()) {
            element = element.getEnclosingElement();
            builder.insert(0, element.getSimpleName() + "_");
        }
        return ClassName.get(context.packageName(), "$Realm" + builder.toString());
    }

    private ClassName getAvObjectType(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.autoValueClass().getSimpleName());
        Element element = context.autoValueClass();
        while (element.getEnclosingElement() != null && element.getEnclosingElement().getKind().isClass()) {
            element = element.getEnclosingElement();
            builder.insert(0, element.getSimpleName() + ".");
        }
        return ClassName.get(context.packageName(), builder.toString());
    }

    private ClassName getAvImplType(Context context) {
        StringBuilder builder = new StringBuilder();
        builder.append(context.autoValueClass().getSimpleName());
        Element element = context.autoValueClass();
        while (element.getEnclosingElement() != null && element.getEnclosingElement().getKind().isClass()) {
            element = element.getEnclosingElement();
            builder.insert(0, element.getSimpleName() + "_");
        }
        return ClassName.get(context.packageName(), "AutoValue_" + builder.toString());
    }
}
