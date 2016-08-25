package com.remind101.auto.value.realm;

import com.google.auto.service.AutoService;
import com.google.auto.value.extension.AutoValueExtension;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;

@AutoService(AutoValueExtension.class)
public class AutoValueRealmExtension extends AutoValueExtension {
    private static final String TO_REALM_OBJECT_METHOD_NAME = "toRealmObject";

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
                .addModifiers(Modifier.ABSTRACT)
                .superclass(ClassName.get(packageName, classToExtend))
                .addMethod(createToRealmObjectMethod(packageName, context.autoValueClass().getSimpleName().toString()))
                .build();
        JavaFile javaFile = JavaFile.builder(packageName, subclass).build();
        return javaFile.toString();
    }

    private void createRealmObjectClass(Context context) {
        TypeSpec realmObjectClass = TypeSpec.classBuilder(getRealmObjectClassName(context.autoValueClass().getSimpleName().toString()))
                .addModifiers(Modifier.FINAL)
                .build();


        JavaFile file = JavaFile.builder(context.packageName(), realmObjectClass).build();
        try {
            file.writeTo(context.processingEnvironment().getFiler());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private MethodSpec createToRealmObjectMethod(String packageName, String autoValueClassName) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(TO_REALM_OBJECT_METHOD_NAME)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .returns(ClassName.get(packageName, getRealmObjectClassName(autoValueClassName)))
                .addStatement("return null");
        return builder.build();
    }

    private String getRealmObjectClassName(String autoValueClassName) {
        return "$Realm" + autoValueClassName;
    }
}
