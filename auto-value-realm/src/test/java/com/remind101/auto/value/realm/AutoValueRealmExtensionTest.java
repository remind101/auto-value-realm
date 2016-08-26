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
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    public abstract $RealmTest toRealmObject();\n"
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
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    abstract Object getFoo();"
                + "    public abstract $RealmTest toRealmObject();\n"
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
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    abstract int getValue();\n"
                + "    @Override public abstract $RealmTest toRealmObject();\n"
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
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "import java.lang.String;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    abstract int getValue();\n"
                + "    abstract String getName();\n"
                + "    abstract boolean getIsValid();\n"
                + "    @Override public abstract $RealmTest toRealmObject();\n"
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
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "import com.remind101.auto.value.realm.AvPrimaryKey;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    @AvPrimaryKey abstract int getValue();\n"
                + "    @Override public abstract $RealmTest toRealmObject();\n"
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

    @Test
    public void testIndex() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "import com.remind101.auto.value.realm.AvIndex;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    @AvIndex abstract int getValue();\n"
                + "    @Override public abstract $RealmTest toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject = JavaFileObjects.forSourceString("test/$RealmTest", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import io.realm.annotations.Index;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmTest extends RealmObject implements AvRealmModel<Test> {\n"
                + "    @Index\n"
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
    public void testPrimaryKeyAndIndex() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvIndex;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "import com.remind101.auto.value.realm.AvPrimaryKey;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    @AvPrimaryKey @AvIndex abstract int getValue();\n"
                + "    @Override public abstract $RealmTest toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject = JavaFileObjects.forSourceString("test/$RealmTest", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import io.realm.annotations.Index;\n"
                + "import io.realm.annotations.PrimaryKey;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmTest extends RealmObject implements AvRealmModel<Test> {\n"
                + "    @PrimaryKey\n"
                + "    @Index\n"
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
    public void testInnerClass() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    abstract int getValue();\n"
                + "    @Override public abstract $RealmTest toRealmObject();\n"
                + "    @AutoValue public static abstract class Inner implements AvModel<$RealmTest_Inner> {\n"
                + "        abstract long getCount();\n"
                + "        @Override public abstract $RealmTest_Inner toRealmObject();\n"
                + "    }\n"
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

        JavaFileObject expectedRealmObjectInner = JavaFileObjects.forSourceString("test/$RealmTest_Inner", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmTest_Inner extends RealmObject implements AvRealmModel<Test.Inner> {\n"
                + "    private long count;\n"
                + "\n"
                + "    public void setCount(long count) {\n"
                + "        this.count = count;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Test.Inner toModel() {\n"
                + "        return new AutoValue_Test_Inner(count);\n"
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

        JavaFileObject expectedSourceInner = JavaFileObjects.forSourceString("test/AutoValue_Test_Inner", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Test_Inner extends $AutoValue_Test_Inner {\n"
                + "    AutoValue_Test_Inner(long count) {\n"
                + "        super(count);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmTest_Inner toRealmObject() {\n"
                + "        $RealmTest_Inner realmObject = new $RealmTest_Inner();\n"
                + "        realmObject.setCount(getCount());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject, expectedSource, expectedRealmObjectInner, expectedSourceInner);
    }

    @Test
    public void testOneToOneRelationship() throws Exception {
        JavaFileObject source = JavaFileObjects.forSourceString("test.Test", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Test implements AvModel<$RealmTest> {\n"
                + "    abstract Inner getValue();\n"
                + "    @Override public abstract $RealmTest toRealmObject();\n"
                + "    @AutoValue public static abstract class Inner implements AvModel<$RealmTest_Inner> {\n"
                + "        abstract long getCount();\n"
                + "        @Override public abstract $RealmTest_Inner toRealmObject();\n"
                + "    }\n"
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
                + "    private $RealmTest_Inner value;\n"
                + "\n"
                + "    public void setValue($RealmTest_Inner value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Test toModel() {\n"
                + "        return new AutoValue_Test(value.toModel());\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObjectInner = JavaFileObjects.forSourceString("test/$RealmTest_Inner", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmTest_Inner extends RealmObject implements AvRealmModel<Test.Inner> {\n"
                + "    private long count;\n"
                + "\n"
                + "    public void setCount(long count) {\n"
                + "        this.count = count;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Test.Inner toModel() {\n"
                + "        return new AutoValue_Test_Inner(count);\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource = JavaFileObjects.forSourceString("test/AutoValue_Test", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Test extends $AutoValue_Test {\n"
                + "    AutoValue_Test(Test.Inner value) {\n"
                + "        super(value);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmTest toRealmObject() {\n"
                + "        $RealmTest realmObject = new $RealmTest();\n"
                + "        realmObject.setValue(getValue().toRealmObject());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSourceInner = JavaFileObjects.forSourceString("test/AutoValue_Test_Inner", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Test_Inner extends $AutoValue_Test_Inner {\n"
                + "    AutoValue_Test_Inner(long count) {\n"
                + "        super(count);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmTest_Inner toRealmObject() {\n"
                + "        $RealmTest_Inner realmObject = new $RealmTest_Inner();\n"
                + "        realmObject.setCount(getCount());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject, expectedSource, expectedRealmObjectInner, expectedSourceInner);
    }

    @Test
    public void testOneToOneRelationshipDifferentFiles() throws Exception {
        JavaFileObject source1 = JavaFileObjects.forSourceString("test.Foo", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Foo implements AvModel<$RealmFoo> {\n"
                + "    abstract Bar getBar();\n"
                + "    @Override public abstract $RealmFoo toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject source2 = JavaFileObjects.forSourceString("test.Bar", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Bar implements AvModel<$RealmBar> {\n"
                + "    abstract int getValue();\n"
                + "    @Override public abstract $RealmBar toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject1 = JavaFileObjects.forSourceString("test/$RealmFoo", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmFoo extends RealmObject implements AvRealmModel<Foo> {\n"
                + "    private $RealmBar bar;\n"
                + "\n"
                + "    public void setBar($RealmBar bar) {\n"
                + "        this.bar = bar;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Foo toModel() {\n"
                + "        return new AutoValue_Foo(bar.toModel());\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject2 = JavaFileObjects.forSourceString("test/$RealmBar", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmBar extends RealmObject implements AvRealmModel<Bar> {\n"
                + "    private int value;\n"
                + "\n"
                + "    public void setValue(int value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Bar toModel() {\n"
                + "        return new AutoValue_Bar(value);\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource1 = JavaFileObjects.forSourceString("test/AutoValue_Foo", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Foo extends $AutoValue_Foo {\n"
                + "    AutoValue_Foo(Bar bar) {\n"
                + "        super(bar);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmFoo toRealmObject() {\n"
                + "        $RealmFoo realmObject = new $RealmFoo();\n"
                + "        realmObject.setBar(getBar().toRealmObject());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource2 = JavaFileObjects.forSourceString("test/AutoValue_Bar", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Bar extends $AutoValue_Bar {\n"
                + "    AutoValue_Bar(int value) {\n"
                + "        super(value);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmBar toRealmObject() {\n"
                + "        $RealmBar realmObject = new $RealmBar();\n"
                + "        realmObject.setValue(getValue());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source1, source2))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject1, expectedSource1, expectedRealmObject2, expectedSource2);
    }

    @Test
    public void testOneToOneRelationshipDifferentPackages() throws Exception {
        JavaFileObject source1 = JavaFileObjects.forSourceString("test.Foo", ""
                + "package test;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "import test2.Bar;\n"
                + "@AutoValue public abstract class Foo implements AvModel<$RealmFoo> {\n"
                + "    abstract Bar getBar();\n"
                + "    @Override public abstract $RealmFoo toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject source2 = JavaFileObjects.forSourceString("test2.Bar", ""
                + "package test2;\n"
                + "import com.google.auto.value.AutoValue;\n"
                + "import com.remind101.auto.value.realm.AvModel;\n"
                + "@AutoValue public abstract class Bar implements AvModel<$RealmBar> {\n"
                + "    abstract int getValue();\n"
                + "    @Override public abstract $RealmBar toRealmObject();\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject1 = JavaFileObjects.forSourceString("test/$RealmFoo", ""
                + "package test;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "import test2.$RealmBar;\n"
                + "\n"
                + "public class $RealmFoo extends RealmObject implements AvRealmModel<Foo> {\n"
                + "    private $RealmBar bar;\n"
                + "\n"
                + "    public void setBar($RealmBar bar) {\n"
                + "        this.bar = bar;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Foo toModel() {\n"
                + "        return new AutoValue_Foo(bar.toModel());\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedRealmObject2 = JavaFileObjects.forSourceString("test2/$RealmBar", ""
                + "package test2;\n"
                + "\n"
                + "import com.remind101.auto.value.realm.AvRealmModel;\n"
                + "import io.realm.RealmObject;\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class $RealmBar extends RealmObject implements AvRealmModel<Bar> {\n"
                + "    private int value;\n"
                + "\n"
                + "    public void setValue(int value) {\n"
                + "        this.value = value;\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final Bar toModel() {\n"
                + "        return new AutoValue_Bar(value);\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource1 = JavaFileObjects.forSourceString("test/AutoValue_Foo", ""
                + "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "import test2.Bar;\n"
                + "\n"
                + "final class AutoValue_Foo extends $AutoValue_Foo {\n"
                + "    AutoValue_Foo(Bar bar) {\n"
                + "        super(bar);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmFoo toRealmObject() {\n"
                + "        $RealmFoo realmObject = new $RealmFoo();\n"
                + "        realmObject.setBar(getBar().toRealmObject());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        JavaFileObject expectedSource2 = JavaFileObjects.forSourceString("test/AutoValue_Bar", ""
                + "package test2;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "final class AutoValue_Bar extends $AutoValue_Bar {\n"
                + "    AutoValue_Bar(int value) {\n"
                + "        super(value);\n"
                + "    }\n"
                + "\n"
                + "    @Override\n"
                + "    public final $RealmBar toRealmObject() {\n"
                + "        $RealmBar realmObject = new $RealmBar();\n"
                + "        realmObject.setValue(getValue());\n"
                + "        return realmObject;\n"
                + "    }\n"
                + "}\n"
        );

        assertAbout(javaSources())
                .that(Arrays.asList(source1, source2))
                .processedWith(new AutoValueProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedRealmObject1, expectedSource1, expectedRealmObject2, expectedSource2);
    }
}
