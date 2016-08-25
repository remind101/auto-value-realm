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
                + "final class $RealmTest extends RealmObject implements AvRealmModel<Test> {\n"
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
}
