package com.github.mickeer.codegen.test;

import com.github.mickeer.codegen.transform.GenerateTransformMapper;
import com.github.mickeer.codegen.transform.GenerateTransformMapperAnnotationProcessor;
import com.github.mickeer.codegen.util.FieldGenReflectionUtil;
import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;
import java.util.List;

public class GenerateTransformMapperAnnotationProcessorTest {

    private static final String NEW_LINE = System.lineSeparator();
    private static final String REFLECTION_UTIL = FieldGenReflectionUtil.class.getCanonicalName();

    @Test
    public void shouldProcess() {
        JavaFileObject input = JavaFileObjects.forSourceString("com.example.A",
                Joiner.on(NEW_LINE).join(
                        "package com.example;",
                        "",
                        "import " + GenerateTransformMapper.class.getCanonicalName() + ";",
                        "import java.util.ArrayDeque;",
                        "",
                        "@" + GenerateTransformMapper.class.getSimpleName(),
                        "public class A {",
                        "    String myField;",
                        "    ArrayDeque<String> myField2;",
                        "}"));

        JavaFileObject output = JavaFileObjects.forSourceString("com.example.AFieldMapper",
                Joiner.on(NEW_LINE).join(
                        "package com.example;",
                        "",
                        "import java.lang.String;",
                        "import java.util.ArrayDeque;",
                        "import java.util.function.Consumer;",
                        "",
                        "public abstract class AFieldMapper {",
                        "  private A source;",
                        "",
                        "  AFieldMapper(A source) {",
                        "    this.source = source;",
                        "  }",
                        "",
                        "  protected abstract void setMyField(A source, String sourceFieldValue, Consumer<String> setter);",
                        "  protected abstract void setMyField2(A source, ArrayDeque<String> sourceFieldValue, Consumer<ArrayDeque<String>> setter);",
                        "",
                        "  public void mapAllTo(A target) {",
                        "    setMyField(source, (String)" + REFLECTION_UTIL + ".getFieldValue(source, \"myField\"), value -> " + REFLECTION_UTIL + ".setFieldValue(target, \"myField\", value));",
                        "    setMyField2(source, (ArrayDeque<String>)" + REFLECTION_UTIL + ".getFieldValue(source, \"myField2\"), value -> " + REFLECTION_UTIL + ".setFieldValue(target, \"myField2\", value));",
                        "  }",
                        "}"
                ));

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(List.of(input))
                .processedWith(new GenerateTransformMapperAnnotationProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(output);
    }
}
