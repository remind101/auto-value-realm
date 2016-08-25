package com.remind101.auto.value.realm;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

@AutoService(AutoValueExtension.class)
public class AutoValueRealmExtension extends AutoValueExtension {
    private static final String TO_REALM_OBJECT_METHOD_NAME = "toRealmObject";
    private static final String TO_MODEL_METHOD_NAME = "toModel";

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
        createRealmObjectClass(context);

        String packageName = context.packageName();
        TypeSpec subclass = TypeSpec.classBuilder(className)
                .addModifiers(isFinal ? Modifier.FINAL : Modifier.ABSTRACT)
                .superclass(ClassName.get(packageName, classToExtend))
                .addMethod(createToRealmObjectMethod(context))
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, subclass).build();
        return javaFile.toString();
    }

    private void createRealmObjectClass(Context context) {
        TypeSpec realmObjectClass = TypeSpec.classBuilder(getRealmObjectType(context).simpleName())
                .addModifiers(Modifier.FINAL)
                .superclass(ClassName.get("io.realm", "RealmObject"))
                .addSuperinterface(ParameterizedTypeName.get(ClassName.get(AvRealmModel.class), getAvObjectType(context)))
                .addMethod(createRealmToModelMethod(context))
                .build();

        JavaFile file = JavaFile.builder(context.packageName(), realmObjectClass).build();
        try {
            file.writeTo(context.processingEnvironment().getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec createRealmToModelMethod(Context context) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(TO_MODEL_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(getAvObjectType(context))
                .addStatement("return new $T()", getAvImplType(context));
        return builder.build();
    }

    private MethodSpec createToRealmObjectMethod(Context context) {
        TypeName realmObjectType = getRealmObjectType(context);
        MethodSpec.Builder builder = MethodSpec.methodBuilder(TO_REALM_OBJECT_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(realmObjectType)
                .addStatement("$T realmObject = new $T()", realmObjectType, realmObjectType)
                .addStatement("return realmObject");
        return builder.build();
    }

    private ClassName getRealmObjectType(Context context) {
        return ClassName.get(context.packageName(), "$Realm" + context.autoValueClass().getSimpleName().toString());
    }

    private ClassName getAvObjectType(Context context) {
        return ClassName.get(context.packageName(), context.autoValueClass().getSimpleName().toString());
    }

    private ClassName getAvImplType(Context context) {
        return ClassName.get(context.packageName(), "AutoValue_" + context.autoValueClass().getSimpleName().toString());
    }
}
