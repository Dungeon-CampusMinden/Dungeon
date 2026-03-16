package blockly.dgir.compiler.java.transformations;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;

import static blockly.dgir.compiler.java.CompilerUtils.*;

/**
 * Lowers loop control flow into explicit boolean data flow.
 *
 * <p>Per loop body we create two local flags: - {@code skip}: current loop iteration should not
 * execute remaining statements. - {@code skipBreak}: current loop should terminate after this
 * iteration.
 *
 * <p>The visitor argument/return ({@code maySkip}) models path-sensitive state while walking a
 * block: it is {@code true} once a prior statement in the same block may have set {@code skip}, so
 * every following statement is wrapped in {@code if (!skip)}.
 */
public class LoopLowering extends GenericVisitorAdapter<Boolean, Boolean> {
  @Override
  public Boolean visit(BlockStmt block, Boolean maySkip) {
    for (int i = 0; i < block.getStatements().size(); i++) {
      Statement stmt = block.getStatement(i);

      // Data-flow step 1: transform statement and compute maySkip after it.
      Boolean afterStmt = stmt.accept(this, maySkip);
      if (afterStmt == null) {
        afterStmt = maySkip;
      }

      // Data-flow step 2: if a previous statement may have skipped, guard the rest of the block.
      if (Boolean.TRUE.equals(maySkip)) {
        wrapInSkipGuard(block.getStatements(), i);
        if (stmt.getParentNode().isPresent()) stmt.getParentNode().get().accept(this, false);
        return afterStmt;
      }

      // Data-flow step 3: propagate state to the next sibling statement.
      maySkip = afterStmt;
    }
    return maySkip;
  }

  @Override
  public Boolean visit(BreakStmt n, Boolean maySkip) {
    // break -> skipBreak = true; skip = true; (guarded when we are already in a maybe-skipped
    // region)
    n.replace(createControlFlowFlagUpdate(n, true));
    return true; // always true after a break
  }

  @Override
  public Boolean visit(ContinueStmt n, Boolean maySkip) {
    // continue -> skip = true; (guarded when we are already in a maybe-skipped region)
    n.replace(createControlFlowFlagUpdate(n, false));
    return true; // always true after a continue
  }

  @Override
  public Boolean visit(IfStmt n, Boolean maySkip) {
    // Evaluate both control-flow branches independently and join by logical OR.
    Boolean maySkipThen = n.getThenStmt().accept(this, maySkip);
    maySkipThen = maySkipThen != null && maySkipThen;

    Boolean maySkipElse =
        n.getElseStmt()
            .map(e -> e.accept(this, maySkip))
            .orElse(maySkip); // no-else: else path inherits incoming maySkip
    maySkipElse = maySkipElse != null && maySkipElse;

    if (!n.getThenStmt().isBlockStmt()) {
      wrapInBlockStmt(n.getThenStmt());
    }
    if (n.getElseStmt().isPresent() && !n.getElseStmt().get().isBlockStmt()) {
      wrapInBlockStmt(n.getElseStmt().get());
    }

    return maySkipThen || maySkipElse;
  }

  @Override
  public Boolean visit(ForStmt n, Boolean maySkip) {
    // Ensure the loop update is a simple prefix increment in case it is just a postfix increment
    if (n.getUpdate().size() == 1) {
      n.getUpdate()
          .get(0)
          .ifUnaryExpr(
              unaryExpr -> {
                if (unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT) {
                  unaryExpr.setOperator(UnaryExpr.Operator.PREFIX_INCREMENT);
                } else if (unaryExpr.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT) {
                  unaryExpr.setOperator(UnaryExpr.Operator.PREFIX_DECREMENT);
                }
              });
    }

    // Each loop introduces a fresh local control-flow scope.
    BlockStmt loweredBody = ensureBlockBody(n.getBody());

    // Nested loop: its body gets its own local flags
    Boolean withSkip = loweredBody.accept(this, false);
    withSkip = withSkip != null && withSkip;
    if (withSkip) {
      ensureLoopFlags(loweredBody);
    }
    n.setBody(loweredBody);
    if (!n.getBody().isBlockStmt()) {
      wrapInBlockStmt(n.getBody());
    }
    // The outer maySkip is unchanged by a nested loop
    return maySkip;
  }

  @Override
  public Boolean visit(WhileStmt n, Boolean maySkip) {
    // While follows the same local flag scope as for.
    BlockStmt loweredBody = ensureBlockBody(n.getBody());

    boolean withSkip = loweredBody.accept(this, false);
    if (withSkip) {
      ensureLoopFlags(loweredBody);
    }
    n.setBody(loweredBody);
    if (!n.getBody().isBlockStmt()) {
      wrapInBlockStmt(n.getBody());
    }
    return maySkip;
  }

  @Override
  public Boolean visit(DoStmt n, Boolean maySkip) {
    // Do-while follows the same local flag scope as for/while.
    BlockStmt loweredBody = ensureBlockBody(n.getBody());

    boolean withSkip = loweredBody.accept(this, false);
    if (withSkip) {
      ensureLoopFlags(loweredBody);
    }
    n.setBody(loweredBody);
    if (!n.getBody().isBlockStmt()) {
      wrapInBlockStmt(n.getBody());
    }
    return maySkip;
  }

  @Override
  public Boolean visit(ExpressionStmt n, Boolean maySkip) {
    return maySkip; // plain statements don't change maySkip
  }

  private Statement createControlFlowFlagUpdate(Statement source, boolean isBreak) {
    // Flag writes are materialized as statements so later passes can consume them.
    BlockStmt updates = setTokenRangeFrom(new BlockStmt(), source);
    if (isBreak) {
      updates.addStatement(assignTrue("skipBreak", source));
    }
    updates.addStatement(assignTrue("skip", source));
    return updates;
  }

  private Statement assignTrue(String variableName, Statement source) {
    NameExpr lhs = setTokenRangeFrom(new NameExpr(variableName), source);
    BooleanLiteralExpr rhs = setTokenRangeFrom(new BooleanLiteralExpr(true), source);
    AssignExpr assignExpr = setTokenRangeFrom(new AssignExpr(lhs, rhs, AssignExpr.Operator.ASSIGN), source);
    return setTokenRangeFrom(new ExpressionStmt(assignExpr), source);
  }

  private BlockStmt ensureBlockBody(Statement stmt) {
    if (stmt.isBlockStmt()) {
      return stmt.asBlockStmt();
    }
    return setTokenRangeFrom(new BlockStmt(NodeList.nodeList(stmt)), stmt);
  }

  private void ensureLoopFlags(BlockStmt body) {
    // Keep transformation idempotent: only add missing declarations.
    if (!containsLocalFlag(body, "skip")) {
      body.addStatement(0, createFlagDeclaration("skip", body));
    }
    if (!containsLocalFlag(body, "skipBreak")) {
      body.addStatement(1, createFlagDeclaration("skipBreak", body));
    }
  }

  private Statement createFlagDeclaration(String name, Statement source) {
    VariableDeclarator decl =
        new VariableDeclarator(
            setTokenRangeFrom(PrimitiveType.booleanType(), source),
            name,
            setTokenRangeFrom(new BooleanLiteralExpr(false), source));
    setTokenRangeFrom(decl, source);
    VariableDeclarationExpr variableDeclarationExpr =
        setTokenRangeFrom(new VariableDeclarationExpr(decl), source);
    return setTokenRangeFrom(new ExpressionStmt(variableDeclarationExpr), source);
  }

  private UnaryExpr skipIsFalseCondition(Statement source) {
    NameExpr skipRef = setTokenRangeFrom(new NameExpr("skip"), source);
    return setTokenRangeFrom(
        new UnaryExpr(skipRef, UnaryExpr.Operator.LOGICAL_COMPLEMENT), source);
  }

  /**
   * Replace the original statements starting from startIndex with an if-guard that conditionally
   * executes them based on the skip flag.
   *
   * @param stmts the list of statements to modify
   * @param startIndex the index of the first statement to wrap
   */
  private void wrapInSkipGuard(NodeList<Statement> stmts, int startIndex) {
    if (stmts.isEmpty()) return;
    if (startIndex >= stmts.size()) return;
    Statement source = stmts.get(startIndex);
    NodeList<Statement> guardedStmts = new NodeList<>();
    for (int i = startIndex; i < stmts.size(); i++) {
      guardedStmts.add(stmts.get(i));
    }
    BlockStmt guardedBody = setTokenRangeFrom(new BlockStmt(guardedStmts), source);
    IfStmt guard = setTokenRangeFrom(new IfStmt(skipIsFalseCondition(source), guardedBody, null), source);
    stmts.subList(startIndex, stmts.size()).clear();
    stmts.add(guard);
  }

  private void wrapInBlockStmt(Statement stmt) {
    BlockStmt block = setTokenRangeFrom(new BlockStmt(NodeList.nodeList(stmt.clone())), stmt);
    stmt.replace(block);
  }
}
