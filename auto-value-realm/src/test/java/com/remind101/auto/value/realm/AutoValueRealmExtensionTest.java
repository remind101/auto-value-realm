package com.remind101.auto.value.realm;

import com.google.auto.value.processor.AutoValueProcessor;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import java.util.Arrays;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AutoValueRealmExtensionTest {
    @Test
    public void testEmptyClass() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "@AutoValue public abstract class Test {\n"
                + "    abstract $RealmTest toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject = JavaFileObjects.forSourceString("test/$RealmTest", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmTest extends RealmObject implements AvRealmModel<Test> {\n"
                + "    @Override\n"
                + "    public final Test toModel() {\n"
                + "        return new AutoValue_Test();\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "    AutoValue_Test() {\n"
                + "        super();\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmTest toRealmObject() {\n"
                + "        $RealmTest realmObject = new $RealmTest();\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject, expectedSource);
    }

    @Test
    public void testPropertyTypeNotSupported() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "@AutoValue public abstract class Test {\n"
                + "    abstract Object getFoo();"
                + "    abstract $RealmTest toRealmObject();\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source))
                .processedWith(new AutoValueProcessor())
                .failsToCompile()
                .withErrorContaining("Test.foo is of a non supported type: java.lang.Object");
    }

    @Test
    public void testSingleProperty() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "@AutoValue public abstract class Test {\n"
                + "    abstract int getValue();\n"
                + "    abstract $RealmTest toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject = JavaFileObjects.forSourceString("test/$RealmTest", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmTest extends RealmObject implements AvRealmModel<Test> {\n"
                + "    private int value;\n"
                + "\n"
                + "    public void setValue(int value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Test toModel() {\n"
                + "        return new AutoValue_Test(value);\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "    AutoValue_Test(int value) {\n"
                + "        super(value);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmTest toRealmObject() {\n"
                + "        $RealmTest realmObject = new $RealmTest();\n"
                + "        realmObject.setValue(getValue());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject, expectedSource);
    }

    @Test
    public void testMultipleProperties() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import java.lang.String;\n"
                + "@AutoValue public abstract class Test {\n"
                + "    abstract int getValue();\n"
                + "    abstract String getName();\n"
                + "    abstract boolean getIsValid();\n"
                + "    abstract $RealmTest toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject = JavaFileObjects.forSourceString("test/$RealmTest", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "import java.lang.String;\n"
                + "\n"
                + "public class $RealmTest extends RealmObject implements AvRealmModel<Test> {\n"
                + "    private int value;\n"
                + "    private String name;\n"
                + "    private boolean isValid;\n"
                + "\n"
                + "    public void setValue(int value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    public void setName(String name) {\n"
                + "        this.name = name;\n"
                + "    }\n"
                + "\n"
                + "    public void setIsValid(boolean isValid) {\n"
                + "        this.isValid = isValid;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Test toModel() {\n"
                + "        return new AutoValue_Test(value, name, isValid);\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "import java.lang.String;\n"
                + "\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "    AutoValue_Test(int value, String name, boolean isValid) {\n"
                + "        super(value, name, isValid);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmTest toRealmObject() {\n"
                + "        $RealmTest realmObject = new $RealmTest();\n"
                + "        realmObject.setValue(getValue());\n"
                + "        realmObject.setName(getName());\n"
                + "        realmObject.setIsValid(getIsValid());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject, expectedSource);
    }

    @Test
    public void testPrimaryKey() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvPrimaryKey;\n"
                + "@AutoValue public abstract class Test {\n"
                + "    @AvPrimaryKey abstract int getValue();\n"
                + "    abstract $RealmTest toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject = JavaFileObjects.forSourceString("test/$RealmTest", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import io.realm.annotations.PrimaryKey;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmTest extends RealmObject implements AvRealmModel<Test> {\n"
                + "    @PrimaryKey\n"
                + "    private int value;\n"
                + "\n"
                + "    public void setValue(int value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Test toModel() {\n"
                + "        return new AutoValue_Test(value);\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "    AutoValue_Test(int value) {\n"
                + "        super(value);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmTest toRealmObject() {\n"
                + "        $RealmTest realmObject = new $RealmTest();\n"
                + "        realmObject.setValue(getValue());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject, expectedSource);
    }
}
