package blockly.dgir.compiler.java;

import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import dgir.core.ir.Type;
import dgir.core.ir.Value;
import dgir.dialect.arith.ArithOps;
import dgir.dialect.builtin.BuiltinTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Map;
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
      boolean isPrimitiveVariableAssignment,
      @NotNull EmitContext context) {
    if (sourceType.equals(targetType)) return EmitResult.of(source);
    if (isImplicitlyAssignable(sourceType, targetType, isPrimitiveVariableAssignment)) {
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

  public static final @NotNull @Unmodifiable Map<String, Integer> WIDENING_ORDER =
      Map.of(
          "byte", 1,
          "short", 2,
          "char", 2, // char can be widened to int but not to short since it is 16bit unsigned
          "int", 3,
          "long", 4,
          "float", 5,
          "double", 6);

  public static boolean isImplicitlyAssignable(
      @NotNull ResolvedType source,
      @NotNull ResolvedType target,
      boolean isPrimitiveVariableAssignment) {
    // Exact match
    if (source.equals(target)) return true;

    // Both must be primitives for implicit widening
    if (!source.isPrimitive() || !target.isPrimitive()) return false;

    // During primitive variable assignment, we allow narrowing of integers to the smaller integer
    // types
    // This is to allow for code like `byte b = 1;` without requiring an explicit cast, since the
    // literal `1` is of type `int` by default. However, in other contexts (e.g. method argument
    // passing), we require an explicit cast for narrowing conversions to avoid accidental data
    // loss.
    if (isPrimitiveVariableAssignment) {
      String sourceDesc = source.asPrimitive().describe();
      switch (target.asPrimitive().describe()) {
        case "byte", "short", "char" -> {
          switch (sourceDesc) {
            case "char", "int" -> {
              return true;
            }
          }
        }
      }
    }
    return WIDENING_ORDER.getOrDefault(source.asPrimitive().describe(), Integer.MAX_VALUE)
        <= WIDENING_ORDER.getOrDefault(target.asPrimitive().describe(), Integer.MAX_VALUE);
  }

  public static boolean isReferenceAssignable(
      @NotNull ResolvedType source, @NotNull ResolvedType target) {
    if (!source.isReferenceType() || !target.isReferenceType()) return false;

    ResolvedReferenceType srcRef = source.asReferenceType();
    ResolvedReferenceType tgtRef = target.asReferenceType();

    // Same type
    if (srcRef.getQualifiedName().equals(tgtRef.getQualifiedName())) return true;

    // Check full ancestor chain (superclasses + interfaces)
    return srcRef.getAllAncestors().stream()
        .anyMatch(a -> a.getQualifiedName().equals(tgtRef.getQualifiedName()));
  }
}
