package com.github.mickeer.codegen.fieldvisitor;

/**
 * For each class annotated with this annotation, an abstract class with name
 * postfixed with "FieldVisitor" is generated. The generated class contains
 * an abstract method per field of the annotated class.
 *
 * <p> E.g. for a class {@code MyClass} with a field {@code date}, a class
 * {@code MyClassFieldVisitor} is generated. The generated class contains
 * an abstract method {@code visitDate(Date)} which can be implemented
 * in extending class for customized visiting logic.
 *
 * <p> The intention of this is to provide way to iterate over all fields of a
 * class similar to reflection, but have type-safe access to the field values
 * and remain simpler to use than reflection. Also, any change to the annotated
 * class is automatically reflected in the generated class and thus any changes
 * such as adding, renaming or removing fields will trigger compilation errors,
 * signalling the developer to fix those.
 */
public @interface GenerateFieldVisitor {
}
