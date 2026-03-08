package blockly.dgir.compiler.java;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.model.typesystem.NullType;
import com.github.javaparser.resolution.types.*;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class Access {
  /**
   * Checks whether a type use is visible from the given type declaration context.
   *
   * <p>This models type use visibility (public/private/protected/package-private), including
   * accessibility of enclosing types.
   *
   * <p>Special case: primitive types, void type, and null type are always accessible and therefore
   * do not need to be checked for accessibility. This is because they are not declared and
   * therefore do not have an access specifier.
   *
   * @param from the type declaration context.
   * @param type the type use to check.
   * @return true if the type use is visible from the given type declaration context, false
   *     otherwise.
   */
  public static boolean isTypeUseAccessibleFrom(
      @NotNull ResolvedTypeDeclaration from, @NotNull ResolvedType type) {
    return isTypeUseAccessibleFrom(AccessContext.of(from), type);
  }

  /**
   * Checks whether a member declaration is visible from the given type declaration context.
   *
   * <p>This models declaration visibility (public/private/protected/package-private), including
   * accessibility of enclosing types.
   *
   * <p>Special case: non-field {@link ResolvedValueDeclaration} instances (for example local
   * variables, method parameters, and catch parameters) are always accessible from their
   * declaration context and therefore do not need to be checked for accessibility. This is because
   * their accessibility is already enforced by the JavaParser symbol solver during resolution.
   */
  public static boolean isDeclarationAccessibleFrom(
      @NotNull ResolvedTypeDeclaration from, @NotNull ResolvedDeclaration target) {
    AccessContext fromContext = AccessContext.of(from);

    // For locals/parameters/catch vars, scope is checked by resolution, so we can skip
    // accessibility checks here.
    if (target instanceof ResolvedValueDeclaration
        && !(target instanceof ResolvedFieldDeclaration)) {
      return true;
    }

    TargetContext targetContext = TargetContext.of(target);
    return isDeclarationAccessibleFrom(fromContext, targetContext);
  }

  private static boolean isTypeUseAccessibleFrom(
      @NotNull AccessContext from, @NotNull ResolvedType type) {
    return switch (type) {
      // Primitive types are always accessible.
      case ResolvedPrimitiveType ignored -> true;
      case ResolvedVoidType ignored -> true;
      case NullType ignored -> true;

      // Array types are accessible if their component type is accessible.
      case ResolvedArrayType arrayType ->
          isTypeUseAccessibleFrom(from, arrayType.getComponentType());
      // Reference types are accessible if their declaration is accessible and all type arguments
      // are accessible.
      case ResolvedReferenceType refType ->
          isTypeAccessibleFrom(from, refType.getTypeDeclaration().orElseThrow())
              && refType.typeParametersValues().stream()
                  .allMatch(typeArgument -> isTypeUseAccessibleFrom(from, typeArgument));
      // Wildcard types are accessible if they are unbounded or their bounded type is accessible.
      case ResolvedWildcard wildcard ->
          !wildcard.isBounded() || isTypeUseAccessibleFrom(from, wildcard.getBoundedType());
      // Type variables are accessible if all bounds are accessible.
      case ResolvedTypeVariable typeVariable ->
          typeVariable.asTypeParameter().getBounds().stream()
              .allMatch(bound -> isTypeUseAccessibleFrom(from, bound.getType()));
      // Union types are accessible if all elements are accessible.
      case ResolvedUnionType unionType ->
          unionType.getElements().stream()
              .allMatch(element -> isTypeUseAccessibleFrom(from, element));
      // No idea what this is but it only has one member so test away.
      case ResolvedLambdaConstraintType lambdaConstraintType ->
          isTypeUseAccessibleFrom(from, lambdaConstraintType.getBound());
      default -> throw new IllegalStateException("Unexpected value: " + type);
    };
  }

  private static boolean isDeclarationAccessibleFrom(
      @NotNull AccessContext from, @NotNull TargetContext target) {
    return switch (target.accessSpecifier()) {
      // For top-level declarations, the access specifier only controls package-level visibility, so
      // we check the declaring type (if any) for accessibility. For nested declarations, the access
      // specifier controls visibility from other types in the same package as well, so we check the
      // declaring type for all access specifiers.
      case PUBLIC ->
          target
              .declaringType()
              .map(declaringType -> isTypeAccessibleFrom(from, declaringType))
              .orElse(true);
      // Private members are only accessible within the same top-level type, so we check if the
      // declaring type of the target is the same as or nested within the declaring type of the
      // source.
      // E.g.
      // class A {
      //   private int x;
      //   class B {
      //     void f() {
      //       A a = new A();
      //     }
      //   }
      // }
      // In this example, A.x is accessible from A.B.f() because they share the same top-level type
      // A,
      // even though A.x is private.
      case PRIVATE -> {
        if (from.type().isEmpty() || target.declaringType().isEmpty()) {
          yield false;
        }
        yield haveSameTopLevelType(from.type().get(), target.declaringType().get());
      }
      // Protected members are accessible within the same package and from subtypes, so we check if
      // the declaring type of the target is accessible from the source and if the source is a
      // subtype
      // of the declaring type of the target when they are in different packages.
      case PROTECTED -> {
        if (target.declaringType().isEmpty()) {
          yield false;
        }
        ResolvedReferenceTypeDeclaration declaringType = target.declaringType().get();
        if (isSamePackage(from.packageName(), target.packageName())) {
          yield isTypeAccessibleFrom(from, declaringType);
        }
        yield from.type()
                .map(callerType -> isSameTypeOrSubtypeOf(callerType, declaringType))
                .orElse(false)
            && isTypeAccessibleFrom(from, declaringType);
      }
      // Package-private members are accessible within the same package, so we check if the source
      // and
      // target are in the same package and if the declaring type of the target is accessible from
      // the source.
      case NONE ->
          isSamePackage(from.packageName(), target.packageName())
              && target
                  .declaringType()
                  .map(declaringType -> isTypeAccessibleFrom(from, declaringType))
                  .orElse(true);
    };
  }

  /**
   * Checks whether a type is accessible from the given type declaration context.
   *
   * @param from the type declaration context.
   * @param type the type to check.
   * @return true if the type is accessible from the given type declaration context, false
   *     otherwise.
   */
  private static boolean isTypeAccessibleFrom(
      @NotNull AccessContext from, @NotNull ResolvedReferenceTypeDeclaration type) {
    // A type is accessible if it and all its enclosing types are accessible.
    TargetContext typeContext =
        new TargetContext(
            type.containerType(),
            Optional.ofNullable(type.getPackageName()),
            getAccessSpecifier(type));
    return isDeclarationAccessibleFrom(from, typeContext);
  }

  /**
   * Checks whether two types are in the same package.
   *
   * @param lhs 1st type to compare.
   * @param rhs 2nd type to compare.
   * @return True if the two types are in the same package, false otherwise.
   */
  private static boolean isSamePackage(
      @NotNull Optional<String> lhs, @NotNull Optional<String> rhs) {
    return lhs.isPresent() && rhs.isPresent() && lhs.get().equals(rhs.get());
  }

  /**
   * Checks whether two types have the same top-level type. This is used to determine accessibility
   * of private members, which are accessible within the same top-level type. For example, A and A.B
   * share the same top-level type A, while A and C do not share the same top-level type.
   *
   * @param a 1st type to compare.
   * @param b 2nd type to compare.
   * @return True if the two types have the same top-level type, false otherwise.
   */
  private static boolean haveSameTopLevelType(
      @NotNull ResolvedReferenceTypeDeclaration a, @NotNull ResolvedReferenceTypeDeclaration b) {
    ResolvedReferenceTypeDeclaration topLevelA = topLevelTypeOf(a);
    ResolvedReferenceTypeDeclaration topLevelB = topLevelTypeOf(b);
    return sameType(topLevelA, topLevelB);
  }

  /**
   * Returns the top-level type declaration of a type declaration. For example, A in A.B.C.D is the
   * top-level type declaration of D.
   *
   * @param declaration The type declaration to get the top-level type declaration of.
   * @return The top-level type declaration of the given type declaration.
   */
  private static @NotNull ResolvedReferenceTypeDeclaration topLevelTypeOf(
      @NotNull ResolvedReferenceTypeDeclaration declaration) {
    ResolvedReferenceTypeDeclaration current = declaration;
    while (current.containerType().isPresent()) {
      current = current.containerType().get();
    }
    return current;
  }

  /**
   * Checks whether a candidate type is the same as or a subtype of a given super type.
   *
   * @param candidate 1st type to compare.
   * @param superType 2nd type to compare.
   * @return True if the candidate type is the same as or a subtype of the super type, false
   */
  private static boolean isSameTypeOrSubtypeOf(
      @NotNull ResolvedReferenceTypeDeclaration candidate,
      @NotNull ResolvedReferenceTypeDeclaration superType) {
    // A type is a subtype of another type if it is the same type or if any of its ancestors is the
    // same type.
    if (sameType(candidate, superType)) {
      return true;
    }
    return candidate.getAllAncestors().stream()
        .map(ancestor -> ancestor.getTypeDeclaration().orElse(null))
        .filter(Objects::nonNull)
        .anyMatch(ancestorDecl -> sameType(ancestorDecl, superType));
  }

  /**
   * Checks whether two types are the same.
   *
   * @param lhs 1st type to compare.
   * @param rhs 2nd type to compare.
   * @return True if the two types are the same, false otherwise.
   */
  private static boolean sameType(
      @NotNull ResolvedReferenceTypeDeclaration lhs,
      @NotNull ResolvedReferenceTypeDeclaration rhs) {
    if (lhs.equals(rhs)) {
      return true;
    }
    String lhsName = lhs.getQualifiedName();
    String rhsName = rhs.getQualifiedName();
    return lhsName != null && lhsName.equals(rhsName);
  }

  /**
   * Context for validating accessibility of a declaration from a type declaration context. This
   * includes the declaring type (if any) and package of the source and target declarations, as well
   * as the access specifier of the target declaration.
   *
   * @param type
   * @param packageName
   */
  private record AccessContext(
      @NotNull Optional<ResolvedReferenceTypeDeclaration> type,
      @NotNull Optional<String> packageName) {
    static AccessContext of(@NotNull ResolvedTypeDeclaration declaration) {
      if (declaration instanceof ResolvedReferenceTypeDeclaration referenceDeclaration) {
        return new AccessContext(
            Optional.of(referenceDeclaration),
            Optional.ofNullable(referenceDeclaration.getPackageName()));
      }
      return new AccessContext(Optional.empty(), Optional.empty());
    }
  }

  /**
   * Context for validating accessibility of a target declaration from a type declaration context.
   * This includes the declaring type (if any) and package of the target declaration, as well as the
   * access specifier of the target declaration. For example, for a field declaration, the declaring
   * type is the class that declares the field, and the package is the package of that class. For a
   * top-level type declaration, the declaring type is empty, and the package is the package of the
   * type. For a nested type declaration, the declaring type is the enclosing type, and the package
   * is the package of the enclosing type. For example, for the following code:
   *
   * <pre>{@code
   * package com.example;
   * class A {
   *   private int x;
   *   class B {
   *     void f() {
   *       A a = new A();
   *     }
   *   }
   * }
   * }</pre>
   *
   * The target context for A.x would have declaringType = A, packageName = com.example, and
   * accessSpecifier = PRIVATE. The target context for A.B would have declaringType = A, packageName
   * = com.example, and accessSpecifier = NONE. The target context for A would have declaringType =
   * empty, packageName = com.example, and accessSpecifier = NONE.
   */
  private record TargetContext(
      @NotNull Optional<ResolvedReferenceTypeDeclaration> declaringType,
      @NotNull Optional<String> packageName,
      @NotNull AccessSpecifier accessSpecifier) {
    static TargetContext of(ResolvedDeclaration target) {
      return switch (target) {
        /*
         Field declarations are the only value declaration we have to check since every other
         value declaration is checked in {@link Access#isAccessibleFrom(ResolvedTypeDeclaration, ResolvedDeclaration)}.
         Furthermore, all primitive values should be handled before reaching this point and therefore also do not need to
         be handled here.
        */
        case ResolvedFieldDeclaration fieldDeclaration -> {
          ResolvedReferenceTypeDeclaration declaringType =
              Optional.ofNullable(fieldDeclaration.declaringType())
                  .filter(ResolvedReferenceTypeDeclaration.class::isInstance)
                  .map(ResolvedReferenceTypeDeclaration.class::cast)
                  .orElseThrow(
                      () ->
                          new IllegalStateException(
                              "Field declaration does not have a reference declaring type."));
          yield new TargetContext(
              Optional.of(declaringType),
              Optional.ofNullable(declaringType.getPackageName()),
              fieldDeclaration.accessSpecifier());
        }

        // Method like declarations include methods and constructors.
        case ResolvedMethodLikeDeclaration methodLikeDeclaration -> {
          ResolvedReferenceTypeDeclaration declaringType = methodLikeDeclaration.declaringType();
          yield new TargetContext(
              Optional.of(declaringType),
              Optional.ofNullable(declaringType.getPackageName()),
              methodLikeDeclaration.accessSpecifier());
        }

        // Type declarations include classes, interfaces, enums, records, and annotation types.
        case ResolvedReferenceTypeDeclaration referenceTypeDeclaration ->
            new TargetContext(
                referenceTypeDeclaration.containerType(),
                Optional.ofNullable(referenceTypeDeclaration.getPackageName()),
                getAccessSpecifier(referenceTypeDeclaration));
        default ->
            throw new IllegalArgumentException(
                "Unsupported target type: " + target.getClass().getName());
      };
    }
  }

  public static @NotNull AccessSpecifier getAccessSpecifier(
      @NotNull ResolvedReferenceTypeDeclaration declaration) {
    return switch (declaration) {
      case HasAccessSpecifier accessSpecifier -> accessSpecifier.accessSpecifier();
      // Why does an annotation declaration not have an access specifier???
      case ResolvedAnnotationDeclaration annotationDeclaration ->
          ((AnnotationDeclaration) annotationDeclaration.toAst().orElseThrow())
              .getAccessSpecifier();
      default -> throw new IllegalStateException("Unexpected value: " + declaration);
    };
  }
}
