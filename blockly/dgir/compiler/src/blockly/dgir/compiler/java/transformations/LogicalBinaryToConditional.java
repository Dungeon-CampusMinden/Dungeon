package blockly.dgir.compiler.java.transformations;

import blockly.dgir.compiler.java.EmitContext;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.jetbrains.annotations.NotNull;

import static blockly.dgir.compiler.java.CompilerUtils.setTokenRangeFrom;

public class LogicalBinaryToConditional extends ModifierVisitor<EmitContext> {
  @Override
  public Visitable visit(BinaryExpr binaryExpr, @NotNull EmitContext arg) {
    // First, recursively lower children (bottom-up transformation)
    BinaryExpr visited = (BinaryExpr) super.visit(binaryExpr, arg);

    BinaryExpr.Operator op = visited.getOperator();
    Expression left = visited.getLeft();
    Expression right = visited.getRight();

    return switch (op) {
      // A && B  →  (A ? B : false)
      case AND -> {
        BooleanLiteralExpr falseExpr = setTokenRangeFrom(new BooleanLiteralExpr(false), visited);
        ConditionalExpr conditional =
            setTokenRangeFrom(new ConditionalExpr(left, right, falseExpr), visited);
        yield setTokenRangeFrom(new EnclosedExpr(conditional), visited);
      }

      // A || B  →  (A ? true : B)
      case OR -> {
        BooleanLiteralExpr trueExpr = setTokenRangeFrom(new BooleanLiteralExpr(true), visited);
        ConditionalExpr conditional =
            setTokenRangeFrom(new ConditionalExpr(left, trueExpr, right), visited);
        yield setTokenRangeFrom(new EnclosedExpr(conditional), visited);
      }

      // Leave all other binary expressions untouched
      default -> visited;
    };
  }
}
