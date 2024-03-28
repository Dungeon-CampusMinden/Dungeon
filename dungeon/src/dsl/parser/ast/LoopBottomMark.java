package dsl.parser.ast;

import dsl.interpreter.DSLInterpreter;
import dsl.runtime.value.Value;
import dsl.semanticanalysis.symbol.Symbol;
import java.util.Iterator;

/**
 * WTF? (erster Satz KURZ).
 *
 * <p>This AST Node is used in the {@link DSLInterpreter} to mark the end of the execution of one
 * iteration of an {@link LoopStmtNode}. The {@link DSLInterpreter} checks, if another iteration of
 * the loop statement should be executed. This class stores the state of the loop (the iterator and
 * the symbols corresponding to the loop and counter variable).
 */
public class LoopBottomMark extends Node {
  private final LoopStmtNode loopStmtNode;
  private final Iterator<Value> internalIterator;
  private final Symbol loopVariableSymbol;
  private final Symbol counterVariableSymbol;

  /**
   * Constructor for bottom mark of {@link LoopStmtNode}s, which have no internal iterator and no
   * loop variables.
   *
   * @param loopStmtNode The {@link LoopStmtNode} of which the new instance will be the bottom mark.
   */
  public LoopBottomMark(LoopStmtNode loopStmtNode) {
    super(Type.LoopBottomMark);
    this.loopStmtNode = loopStmtNode;
    internalIterator = null;
    loopVariableSymbol = Symbol.NULL;
    counterVariableSymbol = Symbol.NULL;
  }

  /**
   * Constructor for bottom mark of {@link LoopStmtNode}s, which have an internal iterator and loop
   * and/or counter variables.
   *
   * @param loopStmtNode The {@link LoopStmtNode} of which the new instance will be the bottom mark.
   * @param internalIterator The {@link Iterator} for the iterable expression of the {@link
   *     LoopStmtNode}
   * @param loopVariableSymbol The {@link Symbol} for the loop variable, which is used to iterate
   *     over the iterable expression
   * @param counterVariableSymbol The {@link Symbol} for the counter variable, which will be used to
   *     count the iterations of the loop
   */
  public LoopBottomMark(
      LoopStmtNode loopStmtNode,
      Iterator<Value> internalIterator,
      Symbol loopVariableSymbol,
      Symbol counterVariableSymbol) {
    super(Type.LoopBottomMark);
    this.loopStmtNode = loopStmtNode;
    this.internalIterator = internalIterator;
    this.loopVariableSymbol = loopVariableSymbol;
    this.counterVariableSymbol = counterVariableSymbol;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public LoopStmtNode getLoopStmtNode() {
    return loopStmtNode;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Iterator<Value> getInternalIterator() {
    return internalIterator;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Symbol getLoopVariableSymbol() {
    return loopVariableSymbol;
  }

  /**
   * WTF? .
   *
   * @return foo
   */
  public Symbol getCounterVariableSymbol() {
    return counterVariableSymbol;
  }

  @Override
  public <T> T accept(AstVisitor<T> visitor) {
    return visitor.visit(this);
  }
}
