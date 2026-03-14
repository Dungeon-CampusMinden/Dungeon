package blockly.dgir.compiler.java.transformations;

import blockly.dgir.compiler.java.EmitContext;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class LogicalBinaryToConditional extends ModifierVisitor<EmitContext> {
  @Override
  public Visitable visit(BinaryExpr binaryExpr, EmitContext arg) {
    // First, recursively lower children (bottom-up transformation)
    BinaryExpr visited = (BinaryExpr) super.visit(binaryExpr, arg);

    BinaryExpr.Operator op = visited.getOperator();
    Expression left = visited.getLeft();
    Expression right = visited.getRight();

    return switch (op) {
      // A && B  →  (A ? B : false)
      case AND -> new EnclosedExpr(new ConditionalExpr(left, right, new BooleanLiteralExpr(false)));

      // A || B  →  (A ? true : B)
      case OR -> new EnclosedExpr(new ConditionalExpr(left, new BooleanLiteralExpr(true), right));

      // Leave all other binary expressions untouched
      default -> visited;
    };
  }
}
