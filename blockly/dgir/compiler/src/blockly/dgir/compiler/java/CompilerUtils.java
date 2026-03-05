package blockly.dgir.compiler.java;

import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedDeclaration;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedType;
import dgir.core.ir.Type;
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
      case CHAR, BYTE -> Optional.of(BuiltinTypes.IntegerT.INT8);
      case SHORT -> Optional.of(BuiltinTypes.IntegerT.INT16);
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
}
