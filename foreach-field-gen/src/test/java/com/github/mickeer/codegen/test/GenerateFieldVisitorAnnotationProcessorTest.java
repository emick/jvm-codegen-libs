package com.github.mickeer.codegen.test;

import com.github.mickeer.codegen.fieldvisitor.GenerateFieldVisitor;
import com.github.mickeer.codegen.fieldvisitor.GenerateFieldVisitorAnnotationProcessor;
import com.github.mickeer.codegen.util.FieldGenReflectionUtil;
import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

public class GenerateFieldVisitorAnnotationProcessorTest {

    private static final String NEW_LINE = System.lineSeparator();
    private static final String REFLECTION_UTIL = FieldGenReflectionUtil.class.getCanonicalName();

    @Test
    public void shouldProcess() {
        JavaFileObject input = JavaFileObjects.forSourceString("com.example.A",
                Joiner.on(NEW_LINE).join(
                        "package com.example;",
                        "",
                        "import " + GenerateFieldVisitor.class.getCanonicalName() + ";",
                        "import java.util.ArrayDeque;",
                        "",
                        "@" + GenerateFieldVisitor.class.getSimpleName(),
                        "public class A {",
                        "    String myField;",
                        "    ArrayDeque<String> myField2;",
                        "}"));

        JavaFileObject output = JavaFileObjects.forSourceString("com.example.AFieldVisitor",
                Joiner.on(NEW_LINE).join(
                        "package com.example;",
                        "",
                        "import java.lang.String;",
                        "import java.util.ArrayDeque;",
                        "",
                        "public abstract class AFieldVisitor {",
                        "",
                        "  private A instance;",
                        "",
                        "  AFieldVisitor(A instance) {",
                        "    this.instance = instance;",
                        "  }",
                        "",
                        "  protected abstract void visitMyField(String value);",
                        "  protected abstract void visitMyField2(ArrayDeque<String> value);",
                        "",
                        "  public void visitAll() {",
                        "    visitMyField((String)" + REFLECTION_UTIL + ".getFieldValue(instance, \"myField\"));",
                        "    visitMyField2((ArrayDeque<String>)" + REFLECTION_UTIL + ".getFieldValue(instance, \"myField2\"));",
                        "  }",
                        "}"
                ));

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(List.of(input))
                .processedWith(new GenerateFieldVisitorAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(output);
    }
}
