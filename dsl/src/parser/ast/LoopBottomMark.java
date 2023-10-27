package parser.ast;

import runtime.Value;
import semanticanalysis.Symbol;

import java.util.Iterator;

public class LoopBottomMark extends Node {
    private final LoopStmtNode loopStmtNode;
    private final Iterator<Value> internalIterator;
    private final Symbol loopVariableSymbol;
    private final Symbol counterVariableSymbol;

    public LoopBottomMark(LoopStmtNode loopStmtNode) {
        super(Type.LoopBottomMark);
        this.loopStmtNode = loopStmtNode;
        internalIterator = null;
        loopVariableSymbol = Symbol.NULL;
        counterVariableSymbol = Symbol.NULL;
    }

    public LoopBottomMark(LoopStmtNode loopStmtNode, Iterator<Value> internalIterator, Symbol loopVariableSymbol, Symbol counterVariableSymbol) {
        super(Type.LoopBottomMark);
        this.loopStmtNode = loopStmtNode;
        this.internalIterator = internalIterator;
        this.loopVariableSymbol = loopVariableSymbol;
        this.counterVariableSymbol = counterVariableSymbol;
    }

    public LoopStmtNode getLoopStmtNode() {
        return loopStmtNode;
    }

    public Iterator<Value> getInternalIterator() {
        return internalIterator;
    }

    public Symbol getLoopVariableSymbol () {
        return loopVariableSymbol;
    }

    public Symbol getCounterVariableSymbol () {
        return counterVariableSymbol;
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
