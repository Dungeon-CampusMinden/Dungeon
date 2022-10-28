package parser;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import java.util.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import parser.AST.*;

public class DungeonASTConverter implements antlr.main.DungeonDSLListener {

    Stack<parser.AST.Node> astStack;

    public DungeonASTConverter() {
        astStack = new Stack<>();
    }

    public parser.AST.Node walk(ParseTree parseTree) {
        astStack = new Stack<>();
        ParseTreeWalker.DEFAULT.walk(this, parseTree);
        return astStack.peek();
    }

    @Override
    public void enterProgram(DungeonDSLParser.ProgramContext ctx) {}

    @Override
    public void exitProgram(DungeonDSLParser.ProgramContext ctx) {
        int symbolCount = astStack.size();
        LinkedList<Node> nodes = new LinkedList<>();
        for (int i = 0; i < symbolCount; i++) {
            var node = astStack.pop();
            nodes.addFirst(node);
        }

        var programNode = new Node(Node.Type.Program, new ArrayList<Node>(nodes));
        astStack.push(programNode);
    }

    @Override
    public void enterObj_def(DungeonDSLParser.Obj_defContext ctx) {}

    @Override
    public void exitObj_def(DungeonDSLParser.Obj_defContext ctx) {}

    @Override
    public void enterProperty_def(DungeonDSLParser.Property_defContext ctx) {}

    @Override
    public void exitProperty_def(DungeonDSLParser.Property_defContext ctx) {}

    @Override
    public void enterStmt(DungeonDSLParser.StmtContext ctx) {}

    @Override
    public void exitStmt(DungeonDSLParser.StmtContext ctx) {}

    @Override
    public void enterDot_def(DungeonDSLParser.Dot_defContext ctx) {}

    @Override
    public void exitDot_def(DungeonDSLParser.Dot_defContext ctx) {
        // check, whether all edge_ops are correct for graph type
        DotDefNode.Type graphType =
                ctx.graph_type.getText().equals("graph")
                        ? DotDefNode.Type.graph
                        : DotDefNode.Type.digraph;

        EdgeOpNode.Type edgeOpType =
                graphType == DotDefNode.Type.graph
                        ? EdgeOpNode.Type.doubleLine
                        : EdgeOpNode.Type.arrow;

        // if dot_stmt_list is not empty, it will be on stack
        Node stmtList = Node.NONE;
        if (ctx.dot_stmt_list() != null) {
            stmtList = astStack.pop();
            assert (stmtList.type == Node.Type.DotStmtList);
        }

        for (Node dotStmtList : stmtList.getChildren()) {
            for (Node dotStmt : dotStmtList.getChildren()) {
                if (dotStmt.type == Node.Type.DotEdgeRHS
                        && !((EdgeRhsNode) dotStmt).getEdgeOpType().equals(edgeOpType)) {
                    // TODO: sensible syntax error message
                    System.out.println("Wrong syntax");
                }
            }
        }

        // graph ID will be on stack
        Node idNode = astStack.pop();

        // create dotDefNode and directly add stmts as list
        DotDefNode dotDef = new DotDefNode(graphType, idNode, stmtList.getChildren());
        astStack.push(dotDef);
    }

    @Override
    public void enterDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx) {}

    @Override
    public void exitDot_stmt_list(DungeonDSLParser.Dot_stmt_listContext ctx) {
        // if there is a rhs dot_stmt_list, it will be on top of stack
        Node rhsStmtList = Node.NONE;
        if (ctx.dot_stmt_list() != null) {
            rhsStmtList = astStack.pop();
        }

        // lhsStmt will be on top of stack
        Node lhsStmt = astStack.pop();

        // flatten list, condense all stmt's to one list
        ArrayList<Node> stmtListChildren = new ArrayList<>(rhsStmtList.getChildren().size() + 1);
        stmtListChildren.add(lhsStmt);
        stmtListChildren.addAll(rhsStmtList.getChildren());

        Node stmtListNode = new Node(Node.Type.DotStmtList, stmtListChildren);
        astStack.push(stmtListNode);
    }

    @Override
    public void enterDot_stmt(DungeonDSLParser.Dot_stmtContext ctx) {}

    @Override
    public void exitDot_stmt(DungeonDSLParser.Dot_stmtContext ctx) {
        // just let it bubble up, no need to simplify
    }

    @Override
    public void enterDot_assign_stmt(DungeonDSLParser.Dot_assign_stmtContext ctx) {}

    @Override
    public void exitDot_assign_stmt(DungeonDSLParser.Dot_assign_stmtContext ctx) {}

    @Override
    public void enterDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx) {}

    @Override
    public void exitDot_edge_stmt(DungeonDSLParser.Dot_edge_stmtContext ctx) {
        var attr_list = Node.NONE;
        if (ctx.dot_attr_list() != null) {
            attr_list = astStack.pop();
            assert (attr_list.type == Node.Type.DotAttrList);
        }

        LinkedList<Node> rhsEdges = new LinkedList<>();
        for (int i = 0; i < ctx.dot_edge_RHS().size(); i++) {
            var rhs = astStack.pop();
            assert (rhs.type == Node.Type.DotEdgeRHS);
            rhsEdges.addFirst(rhs);
        }

        var lhsId = astStack.pop();
        assert (lhsId.type == Node.Type.Identifier);

        var edgeStmtNode = new EdgeStmtNode(lhsId, new ArrayList<>(rhsEdges), attr_list);
        astStack.push(edgeStmtNode);
    }

    @Override
    public void enterDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx) {}

    @Override
    public void exitDot_edge_RHS(DungeonDSLParser.Dot_edge_RHSContext ctx) {
        // ID will be identifier on stack
        var idNode = astStack.pop();

        // edge_op will be on stack
        var edgeOp = astStack.pop();

        var edgeRhs = new EdgeRhsNode(edgeOp, idNode);

        astStack.push(edgeRhs);
    }

    @Override
    public void enterDot_attr_stmt(DungeonDSLParser.Dot_attr_stmtContext ctx) {}

    @Override
    public void exitDot_attr_stmt(DungeonDSLParser.Dot_attr_stmtContext ctx) {}

    @Override
    public void enterDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx) {}

    @Override
    public void exitDot_node_stmt(DungeonDSLParser.Dot_node_stmtContext ctx) {}

    @Override
    public void enterDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx) {}

    @Override
    public void exitDot_attr_list(DungeonDSLParser.Dot_attr_listContext ctx) {}

    @Override
    public void enterDot_a_list(DungeonDSLParser.Dot_a_listContext ctx) {}

    @Override
    public void exitDot_a_list(DungeonDSLParser.Dot_a_listContext ctx) {}

    @Override
    public void enterDot_edge_op(DungeonDSLParser.Dot_edge_opContext ctx) {}

    @Override
    public void exitDot_edge_op(DungeonDSLParser.Dot_edge_opContext ctx) {
        var inner = astStack.pop();
        assert (inner.type == Node.Type.Arrow || inner.type == Node.Type.DoubleLine);
        EdgeOpNode.Type edgeOpNodeType = EdgeOpNode.Type.NONE;
        if (inner.type == Node.Type.Arrow) {
            edgeOpNodeType = EdgeOpNode.Type.arrow;
        } else {
            edgeOpNodeType = EdgeOpNode.Type.doubleLine;
        }
        var node = new EdgeOpNode(inner, edgeOpNodeType);
        astStack.push(node);
    }

    private SourceFileReference getSourceFileReference(TerminalNode node) {
        var symbol = node.getSymbol();
        var line = symbol.getLine();
        var column = symbol.getCharPositionInLine();
        return new SourceFileReference(line, column);
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        var nodeType = node.getSymbol().getType();
        if (nodeType == DungeonDSLLexer.ID) {
            var idNode = new IdNode(node.getText(), getSourceFileReference(node));
            astStack.push(idNode);
        } else if (nodeType == DungeonDSLLexer.ARROW) {
            var arrowNode = new Node(Node.Type.Arrow, getSourceFileReference(node));
            astStack.push(arrowNode);
        } else if (nodeType == DungeonDSLLexer.DOUBLE_LINE) {
            var doubleLineNode = new Node(Node.Type.DoubleLine, getSourceFileReference(node));
            astStack.push(doubleLineNode);
        }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {}

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {}

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {}
}
