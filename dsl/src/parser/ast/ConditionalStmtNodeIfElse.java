package parser.ast;

import java.util.ArrayList;

public class ConditionalStmtNodeIfElse extends Node {
    public final int conditionIdx = 0;
    public final int stmtIfIdx = 1;
    public final int stmtElseIdx = 2;

    /**
     * Getter for the AstNode corresponding to the condition of the statement
     *
     * @return AstNode corresponding to the condition of the statement
     */
    public Node getCondition() {
        return this.getChild(conditionIdx);
    }

    /**
     * Getter for the AstNode corresponding to the stmtBlock in the if branch of the statement
     *
     * @return AstNode corresponding to the stmtBlock in the if branch of the statement
     */
    public Node getIfStmt() {
        return this.getChild(stmtIfIdx);
    }

    /**
     * Getter for the AstNode corresponding to the stmtBlock in the else branch of the statement
     *
     * @return AstNode corresponding to the stmtBlock in the else branch of the statement
     */
    public Node getElseStmt() {
        return this.getChild(stmtElseIdx);
    }

    /**
     * Constructor
     *
     * @param condition The AstNode corresponding to the condition
     * @param stmtIf The AstNode corresponding to the stmt in the if-branch
     * @param stmtElse The AstNode corresponding to the stmt in the else-branch
     */
    public ConditionalStmtNodeIfElse(Node condition, Node stmtIf, Node stmtElse) {
        super(Type.ConditionalStmtIfElse, new ArrayList<>(3));

        this.children.add(condition);
        this.children.add(stmtIf);
        this.children.add(stmtElse);
    }

    @Override
    public <T> T accept(AstVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
