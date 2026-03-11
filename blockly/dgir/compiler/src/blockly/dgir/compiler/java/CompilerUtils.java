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
import dgir.dialect.str.StrTypes;
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.Optional;

public class CompilerUtils {
  private static final IdentityHashMap<ResolvedType, Type> resolvedToType = new IdentityHashMap<>();

  public static <T extends ResolvedDeclaration, B extends Resolvable<T>> Optional<T> resolve(
      @NotNull B target, @NotNull EmitContext context) {
    T resolved;
    try {
      resolved = target.resolve();
    } catch (UnsolvedSymbolException e) {
      context.emitError((Node) target, "Failed to resolve " + e.getName());
      return Optional.empty();
    }
    return Optional.ofNullable(resolved);
  }

  public record TypeInfo(@NotNull Type type, @NotNull ResolvedType resolvedType) {}

  public static Optional<TypeInfo> resolveType(
      @NotNull com.github.javaparser.ast.type.Type target, @NotNull EmitContext context) {
    ResolvedType resolved;
    try {
      resolved = target.resolve();
    } catch (UnsolvedSymbolException e) {
      context.emitError(target, "Failed to resolve type: " + e.getName());
      return Optional.empty();
    }
    Type type = fromAstType(resolved, target, context).orElse(null);
    if (type != null) return Optional.of(new TypeInfo(type, resolved));
    return Optional.empty();
  }

  public static Optional<Type> fromAstType(
      @NotNull com.github.javaparser.ast.type.Type type, Node site, @NotNull EmitContext context) {
    Optional<TypeInfo> resolvedType = resolveType(type, context);
    if (resolvedType.isEmpty()) {
      context.emitError(site, "Failed to resolve type " + type + " for " + site);
      return Optional.empty();
    }

    resolvedToType.put(resolvedType.get().resolvedType, resolvedType.get().type);

    Optional<Type> result = fromAstType(resolvedType.get().resolvedType, site, context);
    if (result.isEmpty())
      context.emitError(site, "Failed to get dgir type for ast type " + type + " for " + site);
    return result;
  }

  public static Optional<Type> fromAstType(
      @NotNull ResolvedType type, Node site, @NotNull EmitContext context) {
    if (resolvedToType.containsKey(type)) return Optional.of(resolvedToType.get(type));

    Optional<Type> result;
    switch (type) {
      case ResolvedPrimitiveType primitiveType -> {
        result =
            Optional.ofNullable(
                switch (primitiveType) {
                  case BOOLEAN -> BuiltinTypes.IntegerT.BOOL;
                  case BYTE -> BuiltinTypes.IntegerT.INT8;
                  case CHAR -> BuiltinTypes.IntegerT.UINT16;
                  case SHORT -> BuiltinTypes.IntegerT.INT16;
                  case INT -> BuiltinTypes.IntegerT.INT32;
                  case LONG -> BuiltinTypes.IntegerT.INT64;
                  case FLOAT -> BuiltinTypes.FloatT.FLOAT32;
                  case DOUBLE -> BuiltinTypes.FloatT.FLOAT64;
                });
      }
      case ResolvedReferenceType referenceType -> {
        result =
            Optional.ofNullable(
                switch (referenceType.describe()) {
                  case "java.lang.String" -> StrTypes.StringT.INSTANCE;
                  default -> {
                    context.emitError(
                        site, "Unsupported reference type: " + referenceType.describe());
                    yield null;
                  }
                });
      }
      default -> {
        context.emitError(site, "Unsupported type: " + type.describe());
        return Optional.empty();
      }
    }
    result.ifPresent(
        value -> {
          resolvedToType.put(type, value);
        });
    return result;
  }

  public static @NotNull EmitResult<Value> emitImplicitCastIfNeeded(
      @NotNull Value source,
      @NotNull ResolvedType sourceType,
      @NotNull ResolvedType targetType,
      boolean isLiteralAssignment,
      @NotNull EmitContext context,
      @NotNull Node site) {
    if (sourceType.describe().equals(targetType.describe())
        || sourceType.describe().equals("java.lang.Object")
        || targetType.describe().equals("java.lang.Object")) return EmitResult.of(source);
    boolean override = false;
    // Allow implicit cast for literal assignments that are valid in Java, e.g. char c = 65; or byte
    // b = 100; short = 'b'
    if (isLiteralAssignment)
      override =
          switch (targetType.describe()) {
            case "byte", "short", "char" ->
                switch (sourceType.describe()) {
                  case "int", "char" -> true;
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
