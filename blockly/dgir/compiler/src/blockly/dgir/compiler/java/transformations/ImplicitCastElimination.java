package blockly.dgir.compiler.java.transformations;

import blockly.dgir.compiler.java.EmitContext;
import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;

import java.util.Objects;
import java.util.Optional;

/**
 * Replaces implicit casts (e.g. byte a = 1) with explicit casts (e.g. byte a = (byte) 1). Also
 * casts function arguments to the expected type when the argument type does not match the parameter
 * type, and the argument is a literal or a simple expression (e.g. variable reference, field
 * access, method call without arguments).
 *
 * <p>Widening casts are also made explicit.
 *
 * <p>Returns true on failure. Otherwise null
 */
public class ImplicitCastElimination extends GenericVisitorAdapter<Boolean, EmitContext> {
  private Optional<Expression> createImplicitCastIfValid(
      Node n,
      ResolvedType targetType,
      ResolvedType valueType,
      Expression value,
      boolean isLiteralExpr) {
    if (targetType.describe().equals(valueType.describe())) return Optional.of(value);
    // In case primitive types should get cast to object do nothing for now but warn the user that
    // this is not expected behavior
    if (targetType.describe().equals("java.lang.Object")) {
      if (valueType.isPrimitive() && targetType.describe().equals("java.lang.Object")) {
        System.err.println(
            "Implicit cast from "
                + valueType.describe()
                + " to "
                + targetType.describe()
                + " in "
                + n.getClass().getSimpleName()
                + " should result in boxing but does not do anything right now.\n"
                + n);
        return Optional.of(value);
      } else if (valueType.describe().equals("java.lang.String")) {
        System.err.println(
            "Implicit cast from "
                + valueType.describe()
                + " to "
                + targetType.describe()
                + " in "
                + n.getClass().getSimpleName()
                + " should result in cast to object but does not do anything right now since we dont support polymorphism.\n"
                + n);
        return Optional.of(value);
      }
    }

    boolean override = false;
    // Allow implicit cast for literal assignments that are valid in Java, e.g. char c = 65; or byte
    // b = 100; short = 'b'
    if (isLiteralExpr)
      override =
          switch (targetType.describe()) {
            case "byte", "short", "char" ->
                switch (valueType.describe()) {
                  case "int", "char" -> true;
                  default -> false;
                };
            default -> false;
          };
    if (targetType.isAssignableBy(valueType) || override) {
      return Optional.of(
          new CastExpr(
              value.getTokenRange().orElse(null),
              StaticJavaParser.parseType(targetType.describe()),
              value.clone()));
    }
    return Optional.empty();
  }

  @Override
  public Boolean visit(AssignExpr n, EmitContext arg) {
    // Recursively visit child nodes first to ensure we eliminate implicit casts in nested
    // expressions.
    Boolean result = super.visit(n, arg);
    if (result != null) return result;

    ResolvedType targetType;
    try {
      if (n.getTarget() instanceof NameExpr nameExpr) {
        targetType = nameExpr.resolve().getType();
      } else {
        targetType = n.getTarget().calculateResolvedType();
      }
    } catch (Exception e) {
      arg.emitError(n, "Failed to resolve target: " + e.getMessage());
      return true;
    }
    ResolvedType valueType = n.getValue().calculateResolvedType();
    if (valueType == null) {
      arg.emitError(n, "Failed to resolve value type of " + n.getValue());
      return true;
    }
    var castExpr =
        createImplicitCastIfValid(
            n, targetType, valueType, n.getValue(), n.getValue().isLiteralExpr());
    if (castExpr.isPresent()) {
      if (!Objects.equals(castExpr.get(), n.getValue())) {
        boolean replaced = n.getValue().replace(castExpr.get());
        if (!replaced) {
          arg.emitError(n, "Failed to replace value of " + n);
          return true;
        }
      }
    } else {
      arg.emitError(n, "Failed to cast value of " + n);
      return true;
    }
    return null;
  }

  @Override
  public Boolean visit(VariableDeclarationExpr n, EmitContext arg) {
    // Recursively visit child nodes first to ensure we eliminate implicit casts in nested
    // expressions.
    Boolean result = super.visit(n, arg);
    if (result != null) return result;

    // Emit cast expressions for all variable initializers.
    try {
      n.getVariables()
          .forEach(
              var -> {
                ResolvedType targetType = var.getType().resolve();
                // If there is no initializer dont do anything.
                if (var.getInitializer().isEmpty()) return;
                ResolvedType valueType;
                try {
                  valueType = var.getInitializer().get().calculateResolvedType();
                } catch (Exception e) {
                  arg.emitError(
                      var.getInitializer().get(),
                      "Failed to resolve value type of " + var.getInitializer().get());
                  throw new RuntimeException();
                }
                var castExpr =
                    createImplicitCastIfValid(
                        var,
                        targetType,
                        valueType,
                        var.getInitializer().get(),
                        var.getInitializer().get().isLiteralExpr());
                if (castExpr.isPresent()) {
                  if (!Objects.equals(castExpr.get(), var.getInitializer().get())) {
                    boolean replaced = var.getInitializer().get().replace(castExpr.get());
                    if (!replaced) {
                      arg.emitError(
                          var.getInitializer().get(), "Failed to replace initializer of " + var);
                      throw new RuntimeException();
                    }
                  }
                } else {
                  arg.emitError(var.getInitializer().get(), "Failed to cast initializer of " + var);
                  throw new RuntimeException();
                }
              });
    } catch (Exception e) {
      return true;
    }
    return null;
  }

  @Override
  public Boolean visit(MethodCallExpr n, EmitContext arg) {
    // Recursively visit child nodes first to ensure we eliminate implicit casts in nested
    // expressions.
    Boolean result = super.visit(n, arg);
    if (result != null) return result;

    ResolvedMethodDeclaration targetMethod;
    try {
      targetMethod = n.resolve();
    } catch (Exception e) {
      arg.emitError(n, "Failed to resolve method call: " + e.getMessage());
      return true;
    }

    // Check if the caller arguments with the callee param types and emit casts if necessary
    int varargsIndex = -1;
    for (int i = 0; i < n.getArguments().size(); i++) {
      ResolvedType callArgType;
      if (n.getArguments().get(i) instanceof NameExpr nameExpr) {
        callArgType = nameExpr.resolve().getType();
      } else {
        callArgType = n.getArguments().get(i).calculateResolvedType();
      }

      ResolvedType targetType =
          targetMethod.getParam(varargsIndex == -1 ? i : varargsIndex).getType();
      if (varargsIndex == -1) {
        if (targetMethod.getParam(i).isVariadic()) {
          varargsIndex = i;
        }
      }

      if (varargsIndex != -1) {
        targetType = targetType.asArrayType().getComponentType();
      }

      var castExpr =
          createImplicitCastIfValid(n, targetType, callArgType, n.getArguments().get(i), false);
      if (castExpr.isPresent()) {
        if (!Objects.equals(castExpr.get(), n.getArguments().get(i))) {
          boolean replaced = n.getArgument(i).replace(castExpr.get());
          if (!replaced) {
            arg.emitError(n.getArgument(i), "Failed to replace argument " + i + " of " + n);
            return true;
          }
        }
      } else {
        arg.emitError(n, "Failed to cast argument " + i + " of " + n);
        return true;
      }
    }
    return null;
  }
}
