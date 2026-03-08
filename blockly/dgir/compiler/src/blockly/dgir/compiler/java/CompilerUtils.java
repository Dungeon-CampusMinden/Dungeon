package blockly.dgir.compiler.java;

import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
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
      @NotNull EmitContext context,
      boolean isLiteralAssignment) {
    if (sourceType.equals(targetType)) return EmitResult.of(source);
    boolean override = false;
    // Allow implicit cast for literal assignments that are valid in Java, e.g. char c = 65; or byte
    // b = 100; short = 'b'
    if (isLiteralAssignment)
      override =
          switch (targetType.describe()) {
            case "byte", "short", "char" ->
                switch (sourceType.describe()) {
                  case "char", "int" -> true;
                  default -> false;
                };
            default -> false;
          };
    if (targetType.isAssignableBy(sourceType) || override) {
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
}
