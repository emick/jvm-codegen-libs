package com.github.mickeer.codegen.common;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract processor for generating a type (class or enum) from class and its fields.
 */
public abstract class AbstractFieldProcessor extends AbstractProcessor {

    private final Class<? extends Annotation> annotationClass;

    protected AbstractFieldProcessor(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        roundEnv.getElementsAnnotatedWith(annotationClass)
                .forEach(element -> tryProcess(element));
        return true; // Annotation is claimed
    }

    private void tryProcess(Element element) {
        try {
            process(element);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void process(Element element) throws IOException {
        if (element.getKind() != ElementKind.CLASS) {
            error(element, "Only classes may be annotated with @" + annotationClass.getSimpleName());
            return;
        }

        Element packageElement = element.getEnclosingElement();
        if (packageElement.getKind() != ElementKind.PACKAGE) {
            error(element, "Failed to locate package for @" + annotationClass.getSimpleName());
            return;
        }

        List<Element> sourceFields = element.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.FIELD)
                .collect(Collectors.toList());

        if (hasFieldNameConflicts(sourceFields)) {
            error(element, "Field names should not differ only by case");
            return;
        }

        var builder = process(element, sourceFields);

        String packageName = ((PackageElement) packageElement).getQualifiedName().toString();
        JavaFile javaFile = JavaFile.builder(packageName, builder.build())
                .build();

        javaFile.writeTo(processingEnv.getFiler());
    }

    private boolean hasFieldNameConflicts(List<Element> sourceFields) {
        var nameSet = sourceFields.stream()
                .map(el -> el.getSimpleName().toString().toUpperCase())
                .collect(Collectors.toSet());
        return nameSet.size() != sourceFields.size();
    }

    private void error(Element element, String s) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, s, element);
    }

    protected static String capitalize(String string) {
        return string.substring(0, 1).toUpperCase() + string.substring(1);
    }

    protected abstract TypeSpec.Builder process(Element element, List<Element> sourceFields);

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    public @Override
    Set<String> getSupportedAnnotationTypes() {
        return Set.of(annotationClass.getCanonicalName());
    }
}
