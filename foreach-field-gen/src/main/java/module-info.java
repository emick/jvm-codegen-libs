module foreach.field.gen {
    requires java.compiler;

    requires com.squareup.javapoet;
    requires com.google.auto.service;

    exports com.github.mickeer.codegen.fieldenum;
    exports com.github.mickeer.codegen.fieldvisitor;
    exports com.github.mickeer.codegen.transform;
    exports com.github.mickeer.codegen.util;
}