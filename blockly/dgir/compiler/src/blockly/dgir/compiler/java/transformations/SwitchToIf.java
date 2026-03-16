package blockly.dgir.compiler.java.transformations;

import blockly.dgir.compiler.java.EmitContext;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static blockly.dgir.compiler.java.CompilerUtils.mergeTokenRanges;
import static blockly.dgir.compiler.java.CompilerUtils.setTokenRange;
import static blockly.dgir.compiler.java.CompilerUtils.setTokenRangeFrom;

/**
 * A JavaParser {@link ModifierVisitor} that rewrites:
 *
 * <ul>
 *   <li><b>Switch statements</b> → chains of {@code if / else if / else} blocks. {@code break},
 *       {@code return}, and {@code yield} inside cases are handled correctly.
 *   <li><b>Switch expressions</b> → nested ternary (conditional) expressions. Both arrow-style
 *       ({@code case X -> expr}) and traditional colon-style cases are supported.
 * </ul>
 *
 * <p>Usage:
 *
 * <pre>{@code
 * CompilationUnit cu = StaticJavaParser.parse(source);
 * cu.accept(new SwitchRewriteVisitor(), null);
 * System.out.println(cu);
 * }</pre>
 *
 * <p>Limitations / known caveats:
 *
 * <ul>
 *   <li>Fall-through between colon-style cases is converted to duplicated else-if branches
 *       (semantically equivalent, but more verbose).
 *   <li>Pattern-matching cases (Java 21+) are not supported and are left untouched.
 *   <li>Guarded patterns ({@code case X when expr}) are not supported.
 * </ul>
 */
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SwitchToIf extends ModifierVisitor<@NotNull EmitContext> {

  // -----------------------------------------------------------------------
  // Entry points
  // -----------------------------------------------------------------------

  @Override
  public Visitable visit(SwitchStmt switchStmt, @NotNull EmitContext arg) {
    // First recurse so inner switches are rewritten before outer ones
    super.visit(switchStmt, arg);
    return rewriteSwitchStatement(switchStmt);
  }

  @Override
  public Visitable visit(SwitchExpr switchExpr, @NotNull EmitContext arg) {
    super.visit(switchExpr, arg);
    return rewriteSwitchExpression(switchExpr);
  }

  @Override
  public Visitable visit(ReturnStmt returnStmt, @NotNull EmitContext arg) {
    super.visit(returnStmt, arg);

    if (returnStmt.getExpression().isEmpty()) {
      return returnStmt;
    }

    Expression expr = returnStmt.getExpression().get();
    if (!(expr instanceof SwitchExpr sw)) {
      return returnStmt;
    }

    // If expression lowering could not reduce to ternary, fall back to statement lowering.
    Optional<Statement> fallback = rewriteSwitchExpressionToIfElseReturns(sw);
    return fallback.orElse(returnStmt);
  }

  // -----------------------------------------------------------------------
  // Switch Statement → if / else if / else chain
  // -----------------------------------------------------------------------

  /**
   * Converts a {@link SwitchStmt} into an equivalent if-else chain wrapped in a {@link BlockStmt}.
   *
   * <p>Algorithm:
   *
   * <ol>
   *   <li>Group {@link SwitchEntry} entries into "case groups" – consecutive entries that share the
   *       same body (fall-through).
   *   <li>For each group build an {@code if} condition that ORs all labels together.
   *   <li>Strip trailing {@code break} statements (they are meaningless inside an {@code if}).
   *   <li>The {@code default} group, if present, becomes the final {@code else} branch.
   * </ol>
   */
  private @NotNull Statement rewriteSwitchStatement(@NotNull SwitchStmt sw) {
    Expression selector = sw.getSelector();
    List<SwitchEntry> entries = sw.getEntries();

    // ── 1. Build case groups (handle fall-through) ──────────────────────
    List<CaseGroup> groups = buildCaseGroups(entries);

    // ── 2. Assemble the if-else chain ────────────────────────────────────
    Statement result = null; // built bottom-up

    // Iterate in reverse so we can nest else branches
    for (int i = groups.size() - 1; i >= 0; i--) {
      CaseGroup group = groups.get(i);
      BlockStmt body = stripTrailingBreak(group.body.clone());

      if (group.isDefault) {
        // default → else { body }
        if (body.getStatements().isEmpty()) {
          result = null; // empty default, nothing to generate
        } else {
          result = body;
        }
      } else {
        // case A, B, C → if (sel == A || sel == B || sel == C)
        Expression condition = buildEqualityCondition(selector, group.labels);
        result =
            setTokenRange(
                new IfStmt(condition, body, result == null ? null : result.clone()),
                group.sourceRange);
      }
    }

    if (result == null) {
      return new BlockStmt(); // degenerate: nothing generated
    }

    // Wrap in a block so the result is a single statement
    BlockStmt wrapper = setTokenRangeFrom(new BlockStmt(new NodeList<>()), sw);
    wrapper.addStatement(result);
    return wrapper;
  }

  // -----------------------------------------------------------------------
  // Switch Expression → nested ternary expression
  // -----------------------------------------------------------------------

  /**
   * Converts a {@link SwitchExpr} into a nested {@link ConditionalExpr} (ternary chain).
   *
   * <p>Both arrow-style and colon-style switch expressions are supported. {@code yield} statements
   * inside blocks are unwrapped to extract their value.
   */
  private @NotNull Expression rewriteSwitchExpression(@NotNull SwitchExpr sw) {
    Expression selector = sw.getSelector();
    List<SwitchEntry> entries = sw.getEntries();

    // Collect (condition, value) pairs and an optional default value
    List<CaseBranch> branches = new ArrayList<>();
    Expression defaultValue = null;

    // Again we need to handle fall-through groups in switch expressions too
    List<CaseGroup> groups = buildCaseGroups(entries);

    for (CaseGroup group : groups) {
      Optional<Expression> value = extractYieldValue(group.body);
      if (value.isEmpty()) {
        // Could not extract a single expression for ternary conversion.
        return sw; // give up, leave untouched
      }

      if (group.isDefault) {
        defaultValue = value.get();
      } else {
        Expression condition = buildEqualityCondition(selector, group.labels);
        branches.add(new CaseBranch(condition, value.get(), group.sourceRange));
      }
    }

    if (defaultValue == null) {
      // A switch expression must be exhaustive; if we cannot find a default we bail out
      return sw;
    }

    // Build nested ternary from the back (last branch → innermost)
    Expression ternary = defaultValue;
    for (int i = branches.size() - 1; i >= 0; i--) {
      CaseBranch branch = branches.get(i);
      ternary =
          setTokenRange(
              new ConditionalExpr(branch.condition, branch.value, ternary), branch.sourceRange);
    }

    return ternary;
  }

  /**
   * Converts a {@link SwitchExpr} into an if/else chain that returns from each branch.
   *
   * <p>This is used as a fallback when ternary conversion is not possible (for example when a case
   * body has multiple statements before a final {@code yield}).
   */
  private @NotNull Optional<Statement> rewriteSwitchExpressionToIfElseReturns(
      @NotNull SwitchExpr sw) {
    Expression selector = sw.getSelector();
    List<CaseGroup> groups = buildCaseGroups(sw.getEntries());

    Statement result = null;
    for (int i = groups.size() - 1; i >= 0; i--) {
      CaseGroup group = groups.get(i);
      Optional<BlockStmt> body = toReturnBody(group.body);
      if (body.isEmpty()) {
        return Optional.empty();
      }

      if (group.isDefault) {
        result = body.get().getStatements().isEmpty() ? null : body.get();
      } else {
        Expression condition = buildEqualityCondition(selector, group.labels);
        result =
            setTokenRange(
                new IfStmt(condition, body.get(), result == null ? null : result.clone()),
                group.sourceRange);
      }
    }

    if (result == null) {
      return Optional.empty();
    }

    BlockStmt wrapper = setTokenRangeFrom(new BlockStmt(new NodeList<>()), sw);
    wrapper.addStatement(result);
    return Optional.of(wrapper);
  }

  // -----------------------------------------------------------------------
  // Helpers – grouping
  // -----------------------------------------------------------------------

  /**
   * Groups switch entries by merging consecutive fall-through entries (those whose body is empty)
   * with the next non-empty entry.
   *
   * <p>Example:
   *
   * <pre>
   *   case A:
   *   case B:
   *     doSomething(); break;
   *   case C:
   *     doOther(); break;
   * </pre>
   *
   * yields two groups: {labels=[A,B], body=doSomething()} and {labels=[C], body=doOther()}.
   */
  private @NotNull List<CaseGroup> buildCaseGroups(@NotNull List<SwitchEntry> entries) {
    List<CaseGroup> groups = new ArrayList<>();
    List<Expression> pendingLabels = new ArrayList<>();
    boolean pendingIsDefault = false;
    Optional<TokenRange> pendingSourceRange = Optional.empty();

    for (SwitchEntry entry : entries) {
      boolean isDefault = entry.getLabels().isEmpty();

      if (isDefault) {
        pendingIsDefault = true;
      } else {
        pendingLabels.addAll(entry.getLabels());
      }
      pendingSourceRange = mergeTokenRanges(pendingSourceRange, entry.getTokenRange());

      NodeList<Statement> stmts = entry.getStatements();
      boolean isEmpty = stmts.isEmpty();

      NodeList<Statement> stmtClones =
          stmts.stream().map(Statement::clone).collect(NodeList.toNodeList());

      if (!isEmpty) {
        BlockStmt body = setTokenRange(new BlockStmt(stmtClones), pendingSourceRange);
        CaseGroup group = new CaseGroup(body);
        group.labels = new ArrayList<>(pendingLabels);
        group.isDefault = pendingIsDefault;
        group.sourceRange = pendingSourceRange;
        groups.add(group);

        pendingLabels = new ArrayList<>();
        pendingIsDefault = false;
        pendingSourceRange = Optional.empty();
      }
      // else: empty body → fall-through, accumulate the label
    }

    // Flush any dangling labels (case with empty body at end, e.g. case X: in default)
    if (!pendingLabels.isEmpty() || pendingIsDefault) {
      CaseGroup group =
          new CaseGroup(
              setTokenRangeFrom(new BlockStmt(new NodeList<>()), pendingLabels.getLast()));
      group.labels = pendingLabels;
      group.isDefault = pendingIsDefault;
      group.sourceRange = pendingSourceRange;
      groups.add(group);
    }

    return groups;
  }

  // -----------------------------------------------------------------------
  // Helpers – condition building
  // -----------------------------------------------------------------------

  /**
   * Builds {@code selector == label} for a single label, or {@code selector == A || selector == B}
   * for multiple labels. String/enum labels use {@code .equals()} instead of {@code ==}.
   */
  private @NotNull Expression buildEqualityCondition(
      @NotNull Expression selector, @NotNull List<Expression> labels) {
    if (labels.isEmpty()) {
      throw new IllegalArgumentException("buildEqualityCondition called with empty label list");
    }

    Expression condition = null;
    for (Expression label : labels) {
      Expression cmp = buildSingleEquality(selector, label);
      // First expression is the initial condition after that it gets ORed with the next comparisons
      condition =
          condition == null
              ? cmp
              : setTokenRange(
                  new BinaryExpr(condition, cmp, BinaryExpr.Operator.OR),
                  mergeTokenRanges(condition.getTokenRange(), cmp.getTokenRange()));
    }
    return condition;
  }

  /**
   * Produces the appropriate equality check.
   *
   * <ul>
   *   <li>Primitive / null literals → {@code ==}
   *   <li>String literals → {@code selector.equals(label)}
   *   <li>Other (enum constants, etc.) → {@code ==} (identity is correct for enums)
   * </ul>
   */
  private @NotNull Expression buildSingleEquality(
      @NotNull Expression selector, @NotNull Expression label) {
    if (label instanceof StringLiteralExpr) {
      // selector.equals("literal")
      MethodCallExpr equalsCall =
          new MethodCallExpr(selector.clone(), "equals", new NodeList<>(label.clone()));
      return setTokenRangeFrom(equalsCall, label);
    }
    if (label instanceof NullLiteralExpr) {
      // selector == null
      return setTokenRangeFrom(
          new BinaryExpr(selector.clone(), new NullLiteralExpr(), BinaryExpr.Operator.EQUALS),
          label);
    }
    // Primitive literals, enum constants, etc.
    return setTokenRangeFrom(
        new BinaryExpr(selector.clone(), label.clone(), BinaryExpr.Operator.EQUALS), label);
  }

  // -----------------------------------------------------------------------
  // Helpers – body manipulation
  // -----------------------------------------------------------------------

  /**
   * Returns a new {@link BlockStmt} with any trailing {@link BreakStmt} (no label) removed. Handles
   * arrow-style single-statement blocks that are just a break.
   */
  private @NotNull BlockStmt stripTrailingBreak(@NotNull BlockStmt block) {
    NodeList<Statement> stmts = block.getStatements();
    while (!stmts.isEmpty()) {
      Statement last = stmts.getLast().orElseThrow();
      if (isUnlabeledBreak(last)) {
        stmts.removeLast();
      } else {
        break;
      }
    }
    return block;
  }

  private boolean isUnlabeledBreak(@NotNull Statement stmt) {
    if (stmt instanceof BreakStmt b) {
      return b.getLabel().isEmpty();
    }
    return false;
  }

  /**
   * Extracts a single {@link Expression} value from a case body to use in a ternary. Handles:
   *
   * <ul>
   *   <li>Arrow expression: single {@link ExpressionStmt} → the expression itself
   *   <li>Block with a single {@code yield expr;} → the yielded expression
   *   <li>Block with a single {@code return expr;} → the returned expression
   *   <li>Single {@link ReturnStmt} directly in body → the return value
   * </ul>
   *
   * Returns {@code null} if the value cannot be extracted.
   */
  private @NotNull Optional<Expression> extractYieldValue(@NotNull BlockStmt body) {
    BlockStmt normalizedBody = unwrapSwitchRuleBlock(body);
    NodeList<Statement> stmts = normalizedBody.getStatements();

    if (stmts.size() == 1) {
      Statement only = stmts.getFirst().orElseThrow();

      // Arrow style: case X -> someExpr;
      switch (only) {
        case ExpressionStmt es -> {
          return Optional.ofNullable(es.getExpression().clone());
        }
        // yield expr;
        case YieldStmt ys -> {
          return Optional.ofNullable(ys.getExpression().clone());
        }
        // return expr;  (in switch expressions inside a method)
        case ReturnStmt rs -> {
          return Optional.ofNullable(rs.getExpression().map(Expression::clone).orElse(null));
        }
        default -> {}
      }
    }

    return Optional.empty(); // cannot reduce to a single expression
  }

  /** Converts a switch-expression case body into a block that ends in {@code return expr;}. */
  private @NotNull Optional<BlockStmt> toReturnBody(@NotNull BlockStmt body) {
    BlockStmt rewritten = unwrapSwitchRuleBlock(body);
    NodeList<Statement> stmts = rewritten.getStatements();
    if (stmts.isEmpty()) {
      return Optional.empty();
    }

    Statement last = stmts.getLast().orElseThrow();

    switch (last) {
      case YieldStmt ys -> {
        stmts.set(
            stmts.size() - 1, setTokenRangeFrom(new ReturnStmt(ys.getExpression().clone()), ys));
        return Optional.of(rewritten);
      }
      case ReturnStmt rs -> {
        return rs.getExpression().isPresent() ? Optional.of(rewritten) : Optional.empty();
      }
      // Arrow expression case: `case X -> expr;`
      case ExpressionStmt es when stmts.size() == 1 -> {
        stmts.set(0, setTokenRangeFrom(new ReturnStmt(es.getExpression().clone()), es));
        return Optional.of(rewritten);
      }
      default -> {}
    }

    return Optional.empty();
  }

  /**
   * JavaParser models `case X -> { ... }` as a single {@link BlockStmt} statement in the entry. For
   * lowering we normalize this shape by unwrapping that one nested block.
   */
  private @NotNull BlockStmt unwrapSwitchRuleBlock(@NotNull BlockStmt body) {
    if (body.getStatements().size() == 1 && body.getStatement(0) instanceof BlockStmt nested) {
      return nested.clone();
    }
    return body.clone();
  }

  // -----------------------------------------------------------------------
  // Internal data structures
  // -----------------------------------------------------------------------

  /** A group of one or more case labels sharing the same body (fall-through group). */
  private static class CaseGroup {
    @NotNull List<Expression> labels = new ArrayList<>();
    boolean isDefault = false;
    @NotNull BlockStmt body;
    @NotNull Optional<TokenRange> sourceRange = Optional.empty();

    public CaseGroup(@NotNull BlockStmt body) {
      this.body = body;
    }
  }

  /** A single arm of a switch expression: condition → value. */
  private record CaseBranch(
      @NotNull Expression condition,
      @NotNull Expression value,
      @NotNull Optional<TokenRange> sourceRange) {}
}
