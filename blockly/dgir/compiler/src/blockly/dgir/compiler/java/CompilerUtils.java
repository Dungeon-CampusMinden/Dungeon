package blockly.dgir.compiler.java;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.*;
import dgir.core.ir.Type;
import dgir.core.ir.Value;
import dgir.dialect.arith.ArithOps;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class CompilerUtils {
  public static <T extends ResolvedDeclaration, B extends Resolvable<T>> Optional<T> resolve(
      @NotNull B target, @NotNull EmitContext context) {
    T resolved;
    try {
      resolved = target.resolve();
    } catch (UnsolvedSymbolException e) {
      context.emitError((Node) target, "Failed to resolve symbol: " + e.getName());
      return Optional.empty();
    }
    return Optional.ofNullable(resolved);
  }

  public static <T extends ResolvedType, B extends Resolvable<T>> Optional<T> resolveType(
      @NotNull B target, @NotNull EmitContext context) {
    T resolved;
    try {
      resolved = target.resolve();
    } catch (UnsolvedSymbolException e) {
      context.emitError((Node) target, "Failed to resolve symbol: " + e.getName());
      return Optional.empty();
    }
    return Optional.ofNullable(resolved);
  }

  public static Optional<Type> fromAstType(
      @NotNull ResolvedType type, Node site, @NotNull EmitContext context) {
    if (!type.isPrimitive()) {
      context.emitError(site, "Only primitive types are supported.");
      return Optional.empty();
    }
    ResolvedPrimitiveType primitiveType = type.asPrimitive();
    return switch (primitiveType) {
      case BOOLEAN -> Optional.of(BuiltinTypes.IntegerT.BOOL);
      case BYTE -> Optional.of(BuiltinTypes.IntegerT.INT8);
      case CHAR, SHORT -> Optional.of(BuiltinTypes.IntegerT.INT16);
      case INT -> Optional.of(BuiltinTypes.IntegerT.INT32);
      case LONG -> Optional.of(BuiltinTypes.IntegerT.INT64);
      case FLOAT -> Optional.of(BuiltinTypes.FloatT.FLOAT32);
      case DOUBLE -> Optional.of(BuiltinTypes.FloatT.FLOAT64);
      default -> {
        context.emitError(site, "Unsupported primitive type: " + primitiveType.describe());
        yield Optional.empty();
      }
    };
  }

  public static @NotNull EmitResult<Value> emitImplicitCastIfNeeded(
      @NotNull Node site,
      @NotNull Value source,
      @NotNull ResolvedType sourceType,
      @NotNull ResolvedType targetType,
      @NotNull EmitContext context) {
    if (sourceType.equals(targetType)) return EmitResult.of(source);
    if (targetType.isAssignableBy(sourceType)) {
      // Insert implicit cast
      Optional<Type> targetDgirType = fromAstType(targetType, site, context);
      return targetDgirType
          .map(
              type ->
                  EmitResult.of(
                      context
                          .insert(new ArithOps.CastOp(context.loc(site), source, type))
                          .getResult()))
          .orElseGet(
              () ->
                  EmitResult.failure(
                      context, site, "Could not resolve target type for implicit cast."));
    }
    return EmitResult.failure(
        context,
        site,
        "Type mismatch: cannot assign " + sourceType.describe() + " to " + targetType.describe());
  }

  /** Checks whether the target is target is accessible from the source. */
  public static boolean isAccessibleFrom(
      ResolvedReferenceTypeDeclaration from, ResolvedDeclaration target) {
    Optional<ResolvedTypeDeclaration> targetDeclaringType;
    AccessSpecifier targetAccessSpecifier;

    switch (target) {
      case ResolvedFieldDeclaration fieldDeclaration -> {
        targetDeclaringType =
            Optional.ofNullable(fieldDeclaration.declaringType())
                .map(Optional::of)
                .orElseThrow(
                    () ->
                        new IllegalStateException(
                            "Field declaration does not have a declaring type."));
        targetAccessSpecifier = fieldDeclaration.accessSpecifier();
      }
      case ResolvedMethodLikeDeclaration methodLikeDeclaration -> {
        targetDeclaringType = Optional.of(methodLikeDeclaration.declaringType());
        targetAccessSpecifier = methodLikeDeclaration.accessSpecifier();
      }
      case ResolvedTypeDeclaration typeDeclaration -> {
        targetDeclaringType = typeDeclaration.containerType().flatMap(r -> Optional.of((ResolvedTypeDeclaration) r));
        targetAccessSpecifier = getAccessSpecifier(typeDeclaration);
      }
      case ResolvedValueDeclaration valueDeclaration -> {
        targetDeclaringType = getDeclaringType(valueDeclaration.getType());
        targetAccessSpecifier = getAccessSpecifier(valueDeclaration.getType());
      }
      default ->
          throw new IllegalArgumentException(
              "Unsupported target type: " + target.getClass().getName());
    }
    return false;
  }

  public static Optional<ResolvedTypeDeclaration> getDeclaringType(ResolvedType type) {
    return Optional.ofNullable(
        switch (type) {
          case ResolvedArrayType arrayType ->
              getDeclaringType(arrayType.getComponentType()).orElse(null);
          case ResolvedReferenceType refType -> {
            var declaration =
                refType
                    .getTypeDeclaration()
                    .orElseThrow(
                        () ->
                            new IllegalStateException(
                                "Reference type does not have a declaration."));
            yield declaration.containerType().orElse(null);
          }
          case ResolvedPrimitiveType primitiveType -> null;
          default -> throw new IllegalStateException("Unexpected value: " + type);
        });
  }

  public static AccessSpecifier getAccessSpecifier(ResolvedTypeDeclaration declaration) {
    return switch (declaration){
      // We do not support type parameters.
      // case ResolvedTypeParameterDeclaration typeParameterDeclaration ->
      case ResolvedReferenceTypeDeclaration referenceTypeDeclaration -> getAccessSpecifier(referenceTypeDeclaration);
      default -> throw new IllegalStateException("Unexpected value: " + declaration);
    };
  }

  public static AccessSpecifier getAccessSpecifier(ResolvedReferenceTypeDeclaration declaration) {
    return switch (declaration) {
      case ResolvedAnnotationDeclaration annotationDeclaration ->
        ((AnnotationDeclaration) annotationDeclaration.toAst().orElseThrow())
          .getAccessSpecifier();
      case ResolvedClassDeclaration classDeclaration -> classDeclaration.accessSpecifier();
      case ResolvedEnumDeclaration enumDeclaration -> enumDeclaration.accessSpecifier();
      case ResolvedInterfaceDeclaration interfaceDeclaration ->
        interfaceDeclaration.accessSpecifier();
      case ResolvedRecordDeclaration recordDeclaration -> recordDeclaration.accessSpecifier();
      default -> throw new IllegalStateException("Unexpected value: " + declaration);
    };
  }

  public static AccessSpecifier getAccessSpecifier(ResolvedType resolvedType) {
    return switch (resolvedType) {
      case ResolvedArrayType arrayType -> getAccessSpecifier(arrayType.getComponentType());
      case ResolvedReferenceType refType ->
          getAccessSpecifier(refType.getTypeDeclaration().orElseThrow());
      case ResolvedPrimitiveType primitiveType ->
          AccessSpecifier.PUBLIC; // Primitive types are always public
      case ResolvedVoidType voidType -> AccessSpecifier.PUBLIC; // Void type is always public
      default -> throw new IllegalStateException("Unexpected value: " + );
    };
  }
}
