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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

@AutoService(AutoValueExtension.class)
public class AutoValueRealmExtension extends AutoValueExtension {
    private static final String TO_REALM_OBJECT_METHOD_NAME = "toRealmObject";
    private static final String TO_MODEL_METHOD_NAME = "toModel";

    private static final List<String> SUPPORTED_TYPES = Arrays.asList(String.class.getName(), Date.class.getName(), byte[].class.getName(), Boolean.class.getName(), Byte.class.getName(), Short.class.getName(), Integer.class.getName(), Float.class.getName(), Long.class.getName(), Double.class.getName());
    private ClassName avRealmHelper = ClassName.get("com.remind101.auto.value.realm", "AvRealmHelper");

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
            if (!returnType.getKind().isPrimitive()
                    && !SUPPORTED_TYPES.contains(returnType.toString())
                    && !isOtherAvModel(context, entry.getValue())
                    && !isListOfOtherAvModel(context, entry.getValue())) {
                throw new IllegalArgumentException(context.autoValueClass().getSimpleName().toString() + "." + entry.getKey() +" is of a non supported type: " + entry.getValue().getReturnType().toString());
            }
        }
    }

    private void createRealmObjectClass(Context context) {
        TypeSpec.Builder realmObjectClassBuilder = TypeSpec.classBuilder(getRealmObjectType(context).simpleName())
                .addModifiers(Modifier.PUBLIC)
                .superclass(ClassName.get("io.realm", "RealmObject"))
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AvRealmModel.class), getAvObjectType(context)));

        // Create the fields and the setters
        for (Map.Entry<String, ExecutableElement> property : context.properties().entrySet()) {
            boolean isPrimaryKey = property.getValue().getAnnotation(AvPrimaryKey.class) != null;
            boolean isIndex = property.getValue().getAnnotation(AvIndex.class) != null;
            TypeName propertyType;
            if (isOtherAvModel(context, property.getValue())) {
                propertyType = getRealmTypeName(property.getValue().getReturnType());

            } else if (isListOfOtherAvModel(context, property.getValue())) {
                TypeName otherRealmType = getRealmTypeName(getListGenericType(property.getValue().getReturnType()));
                propertyType = ParameterizedTypeName.get(ClassName.get("io.realm", "RealmList"), otherRealmType);
            } else {
                propertyType = TypeName.get(property.getValue().getReturnType());
            }
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

    private TypeMirror getListGenericType(TypeMirror type) {
        if (!type.getKind().equals(TypeKind.DECLARED)) {
            throw new RuntimeException("Cannot find list generic type");
        }
        List<? extends TypeMirror> generics = ((DeclaredType) type).getTypeArguments();
        if (generics.size() != 1) {
            throw new RuntimeException("Unexpected number of generics");
        }
        return generics.get(0);
    }

    /**
     * Gives the TypeName of the Realm Model class of another AvModel than the one we are processing
     * @param otherAvType example: my.other.package.Foo.Inner
     * @return example: my.other.package.$RealmFoo_Inner
     */
    private TypeName getRealmTypeName(TypeMirror otherAvType) {
        String avTypeString = otherAvType.toString();
        int packageNameLength = -1;
        for (int i = 0; i < avTypeString.length(); i++) {
            if (Character.isUpperCase(avTypeString.charAt(i))) {
                packageNameLength = i;
                break;
            }
        }
        if (packageNameLength == -1) {
            throw new RuntimeException("Could not figure out package name for class " + avTypeString + ". This should never happen");
        }
        String enclosedName = avTypeString.substring(packageNameLength).replaceAll("\\.", "_");
        return ClassName.get(avTypeString.substring(0, packageNameLength - 1), "$Realm" + enclosedName);
    }

    private boolean isOtherAvModel(Context context, ExecutableElement getter) {
        TypeMirror avModel = context.processingEnvironment().getElementUtils().getTypeElement("com.remind101.auto.value.realm.AvModel").asType();
        return context.processingEnvironment().getTypeUtils().isAssignable(getter.getReturnType(), avModel);
    }

    private boolean isListOfOtherAvModel(Context context, ExecutableElement getter) {
        Types typeUtils = context.processingEnvironment().getTypeUtils();
        TypeElement list = context.processingEnvironment().getElementUtils().getTypeElement("java.util.List");
        TypeMirror avModel = context.processingEnvironment().getElementUtils().getTypeElement("com.remind101.auto.value.realm.AvModel").asType();
        TypeMirror parameterizedList = typeUtils.getDeclaredType(list, typeUtils.getWildcardType(avModel, null));
        return context.processingEnvironment().getTypeUtils().isSubtype(getter.getReturnType(), parameterizedList);
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
        List<String> arguments = new ArrayList<>();
        List<ClassName> externalClassesNames = new ArrayList<>();
        externalClassesNames.add(getAvImplType(context));
        for (Map.Entry<String, ExecutableElement> entry : context.properties().entrySet()) {
            String arg;
            if (isOtherAvModel(context, entry.getValue())) {
                arg = entry.getKey() + ".toModel()"; // We need to transform the field
            } else if (isListOfOtherAvModel(context, entry.getValue())) {
                externalClassesNames.add(avRealmHelper);
                arg = "$T.fromRealmModels(" + entry.getKey() + ")";
            } else {
                arg = entry.getKey(); // Just use the field
            }
            arguments.add(arg);
        }
        for (int i = 0; i < context.properties().size(); i++) {
            if (i != 0) {
                returnStatement.append(", ");
            }
            returnStatement.append(arguments.get(i));
        }
        returnStatement.append(")");
        MethodSpec.Builder builder = MethodSpec.methodBuilder(TO_MODEL_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(getAvObjectType(context))
                .addStatement(returnStatement.toString(), externalClassesNames.toArray());
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
            if (isOtherAvModel(context, property.getValue())) {
                builder.addStatement("realmObject.$N($N().toRealmObject())", getSetterName(property.getKey()), property.getValue().getSimpleName().toString());
            } else if (isListOfOtherAvModel(context, property.getValue())) {
                builder.addStatement("realmObject.$N($T.toRealmModels($N()))", getSetterName(property.getKey()), avRealmHelper, property.getValue().getSimpleName().toString());
            } else {
                builder.addStatement("realmObject.$N($N())", getSetterName(property.getKey()), property.getValue().getSimpleName().toString());
            }
        }

        builder.addStatement("return realmObject");
        return builder.build();
    }

    private String getSetterName(String propertyName) {
        return "set" + propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }

    private ClassName getRealmObjectType(Context context) {
        return ClassName.get(context.packageName(), "$Realm" + getClassNameWithEnclosingClasses(context.autoValueClass(), "_"));
    }

    private ClassName getAvObjectType(Context context) {
        return ClassName.get(context.packageName(), getClassNameWithEnclosingClasses(context.autoValueClass(), "."));
    }

    private ClassName getAvImplType(Context context) {
        return ClassName.get(context.packageName(), "AutoValue_" + getClassNameWithEnclosingClasses(context.autoValueClass(), "_"));
    }

    private String getClassNameWithEnclosingClasses(Element element, String separator) {
        StringBuilder builder = new StringBuilder();
        builder.append(element.getSimpleName());
        while (element.getEnclosingElement() != null && element.getEnclosingElement().getKind().isClass()) {
            element = element.getEnclosingElement();
            builder.insert(0, element.getSimpleName() + separator);
        }
        return builder.toString();
    }
}
