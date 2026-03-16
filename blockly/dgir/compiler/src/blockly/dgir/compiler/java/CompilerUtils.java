package blockly.dgir.compiler.java;

import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.DataKey;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.declarations.ResolvedDeclaration;
import com.github.javaparser.resolution.model.typesystem.LazyType;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import dgir.core.ir.Type;
import dgir.dialect.builtin.BuiltinTypes;
import dgir.dialect.str.StrTypes;
import org.jetbrains.annotations.NotNull;

import java.util.IdentityHashMap;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class CompilerUtils {
  private static final IdentityHashMap<ResolvedType, Type> resolvedToType = new IdentityHashMap<>();

  public static @NotNull Optional<TokenRange> mergeTokenRanges(
      @NotNull Optional<TokenRange> first, @NotNull Optional<TokenRange> second) {
    if (first.isEmpty()) {
      return second;
    }
    return second
        .map(javaTokens -> new TokenRange(first.get().getBegin(), javaTokens.getEnd()))
        .or(() -> first);
  }

  /** Returns the first available token range from the given source anchors. */
  public static @NotNull Optional<TokenRange> tokenRangeFrom(@NotNull Node... anchors) {
    for (Node anchor : anchors) {
      Optional<TokenRange> tokenRange = anchor.getTokenRange();
      if (tokenRange.isPresent()) {
        return tokenRange;
      }
    }
    return Optional.empty();
  }

  /** Assigns the first available source token range to a generated node. */
  public static <T extends Node> @NotNull T setTokenRangeFrom(
      @NotNull T node, @NotNull Node... anchors) {
    tokenRangeFrom(anchors).ifPresent(node::setTokenRange);
    return node;
  }

  /** Assigns a token range when present and leaves the node unchanged otherwise. */
  public static <T extends Node> @NotNull T setTokenRange(
      @NotNull T node, @NotNull Optional<TokenRange> tokenRange) {
    tokenRange.ifPresent(node::setTokenRange);
    return node;
  }

  public static boolean containsLocalFlag(@NotNull Statement body, @NotNull String flagName) {
    if (body instanceof BlockStmt blockStmt) {
      for (Statement stmt : blockStmt.getStatements()) {
        if (!stmt.isExpressionStmt()) {
          continue;
        }
        Expression expression = stmt.asExpressionStmt().getExpression();
        if (!expression.isVariableDeclarationExpr()) {
          continue;
        }
        for (VariableDeclarator variable : expression.asVariableDeclarationExpr().getVariables()) {
          if (flagName.equals(variable.getNameAsString())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static <T extends ResolvedDeclaration, B extends Resolvable<T>>
      @NotNull Optional<T> resolve(@NotNull B target, @NotNull EmitContext context) {
    T resolved;
    try {
      resolved = target.resolve();
    } catch (UnsolvedSymbolException e) {
      context.emitError((Node) target, "Failed to resolve " + e.getName() + ": " + e.getMessage());
      return Optional.empty();
    }
    return Optional.ofNullable(resolved);
  }

  public record TypeInfo(@NotNull Type type, @NotNull ResolvedType resolvedType) {}

  public static @NotNull Optional<TypeInfo> resolveType(
      @NotNull com.github.javaparser.ast.type.Type target, @NotNull EmitContext context) {
    ResolvedType resolved;
    try {
      resolved = target.resolve();
    } catch (UnsolvedSymbolException e) {
      context.emitError(target, "Failed to resolve type: " + e.getName() + ": " + e.getMessage());
      return Optional.empty();
    }
    Type type = fromAstType(resolved, target, context).orElse(null);
    if (type != null) return Optional.of(new TypeInfo(type, resolved));
    return Optional.empty();
  }

  public static @NotNull Optional<Type> fromAstType(
      @NotNull com.github.javaparser.ast.type.Type type,
      @NotNull Node site,
      @NotNull EmitContext context) {
    Optional<TypeInfo> resolvedType = resolveType(type, context);
    if (resolvedType.isEmpty()) {
      return Optional.empty();
    }
    resolvedToType.put(resolvedType.get().resolvedType, resolvedType.get().type);
    return fromAstType(resolvedType.get().resolvedType, site, context);
  }

  public static @NotNull Optional<Type> fromAstType(
      @NotNull ResolvedType type, @NotNull Node site, @NotNull EmitContext context) {
    if (type instanceof LazyType lazyType) type = lazyType.getType();
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
    @NotNull ResolvedType finalType = type;
    result.ifPresent(
        value -> {
          resolvedToType.put(finalType, value);
        });
    return result;
  }
}
