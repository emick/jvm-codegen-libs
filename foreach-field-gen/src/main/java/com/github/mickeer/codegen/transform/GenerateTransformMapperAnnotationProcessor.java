package com.github.mickeer.codegen.transform;

import com.github.mickeer.codegen.common.AbstractFieldProcessor;
import com.github.mickeer.codegen.util.FieldGenReflectionUtil;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.List;
import java.util.function.Consumer;

/**
 * Annotation processor for {@link GenerateTransformMapper}.
 */
@AutoService(Processor.class)
public class GenerateTransformMapperAnnotationProcessor extends AbstractFieldProcessor {

    public GenerateTransformMapperAnnotationProcessor() {
        super(GenerateTransformMapper.class);
    }

    @Override
    protected TypeSpec.Builder process(Element element, List<Element> sourceFields) {
        TypeSpec.Builder mapperBuilder = TypeSpec.classBuilder(element.getSimpleName() + "FieldMapper")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        var elementType = TypeName.get(element.asType());

        mapperBuilder.addField(elementType, "source", Modifier.PRIVATE);
        mapperBuilder.addMethod(MethodSpec.constructorBuilder()
                .addParameter(ParameterSpec.builder(elementType, "source").build())
                .addStatement("this.source = source")
                .build());

        sourceFields.forEach(f -> mapperBuilder.addMethod(createFieldMappingMethod(elementType, f)));
        mapperBuilder.addMethod(createMapAllToMethod(elementType, sourceFields));

        return mapperBuilder;
    }

    private static MethodSpec createMapAllToMethod(TypeName elementType, List<Element> sourceFields) {
        var method = MethodSpec.methodBuilder("mapAllTo")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(ParameterSpec.builder(elementType, "target").build());

        sourceFields.forEach(f -> method.addStatement(getSettingMethodName(f) + "(" +
                "source, " +
                "($T)" +
                FieldGenReflectionUtil.class.getCanonicalName() + ".getFieldValue(source, \"" + f.getSimpleName() + "\"), " +
                "value -> " + FieldGenReflectionUtil.class.getCanonicalName() + ".setFieldValue(target, \"" + f.getSimpleName() + "\", value)" +
                ")", ClassName.get(f.asType())));

        return method.build();

    }

    private MethodSpec createFieldMappingMethod(TypeName elementType, Element field) {
        ParameterizedTypeName setterType = ParameterizedTypeName.get(ClassName.get(Consumer.class), TypeName.get(field.asType()));

        return MethodSpec.methodBuilder(getSettingMethodName(field))
                .addModifiers(Modifier.ABSTRACT, Modifier.PROTECTED)
                .addParameter(elementType, "source")
                .addParameter(ParameterSpec.builder(TypeName.get(field.asType()), "sourceFieldValue")
                        .build())
                .addParameter(ParameterSpec.builder(setterType, "setter").build())
                .build();
    }

    private static String getSettingMethodName(Element element) {
        String fieldName = element.getSimpleName().toString();
        String capitalizedName = capitalize(fieldName);
        return "set" + capitalizedName;
    }
}
