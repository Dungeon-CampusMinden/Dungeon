package parser;

import antlr.main.DungeonDSLLexer;
import antlr.main.DungeonDSLParser;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Stack;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
// importing all required classes from symbolTable will be to verbose
// CHECKSTYLE:OFF: AvoidStarImport
import parser.AST.*;
// CHECKSTYLE:ON: AvoidStarImport

/**
 * This class converts the {@link ParseTree} created by the antlr parser into an AST. While walking
 * the parse tree, the astStack is used to combine multiple Nodes into more complex ones. This works
 * in a bottom-up fashion: in a specific exit-method (of the DungeonDSLListener interface) we use
 * the invariant, that we combined all child-nodes of the specific rule. Therefore, they are in
 * reverse order on the astStack and can be added as children to a {@link Node} (or a
 * specialization), representing the currently exited rule.
 */
// we need to provide visitor methods for many node classes, so the method count and the class data
// abstraction coupling
// will be high naturally
@SuppressWarnings({"methodcount", "classdataabstractioncoupling"})
public class DungeonASTConverter implements antlr.main.DungeonDSLListener {

    Stack<parser.AST.Node> astStack;

    /** Constructor */
    public DungeonASTConverter() {
        astStack = new Stack<>();
    }

    /**
     * Walk the passed parseTree and create an AST from it
     *
     * @param parseTree The ParseTree to walk
     * @return Root Node of the AST.
     */
    public parser.AST.Node walk(ParseTree parseTree) {
        astStack = new Stack<>();
        ParseTreeWalker.DEFAULT.walk(this, parseTree);
        return astStack.peek();
    }

    @Override
    public void enterProgram(DungeonDSLParser.ProgramContext ctx) {}

    /**
     * Pops all remaining AST-Nodes from the stack (they will be in reverse order) and adds all as
     * children to the root node of the program
     *
     * @param ctx the parse tree
     */
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
    public void enterDefinition(DungeonDSLParser.DefinitionContext ctx) {}

    @Override
    public void exitDefinition(DungeonDSLParser.DefinitionContext ctx) {}

    @Override
    public void enterFn_def(DungeonDSLParser.Fn_defContext ctx) {}

    @Override
    public void exitFn_def(DungeonDSLParser.Fn_defContext ctx) {
        // pop everything (depending on ctx) and create fnDefNode
        Node stmtList = Node.NONE;
        if (ctx.stmt_list() != null) {
            // no stmt list
            stmtList = astStack.pop();
        }

        Node retType = Node.NONE;
        if (ctx.ret_type_def() != null) {
            retType = astStack.pop();
        }

        Node paramDefList = Node.NONE;
        if (ctx.param_def_list() != null) {
            paramDefList = astStack.pop();
        }

        Node functionName = astStack.pop();

        var funcDefNode = new FuncDefNode(functionName, paramDefList, retType, stmtList);
        astStack.push(funcDefNode);
    }

    @Override
    public void enterStmt(DungeonDSLParser.StmtContext ctx) {}

    @Override
    public void exitStmt(DungeonDSLParser.StmtContext ctx) {
        // just let it bubble up, we don't need to store the information, that it is a stmt
    }

    @Override
    public void enterStmt_list(DungeonDSLParser.Stmt_listContext ctx) {}

    @Override
    public void exitStmt_list(DungeonDSLParser.Stmt_listContext ctx) {
        // condense to actual list of stmt's
        if (ctx.stmt_list() == null) {
            // trivial stmt definition list (one stmt)
            var innerStmt = astStack.pop();

            var list = new ArrayList<Node>(1);
            list.add(innerStmt);

            var stmtList = new Node(Node.Type.StmtList, list);
            astStack.push(stmtList);
        } else {
            // rhs stmt list is on stack
            var rhsList = astStack.pop();
            assert (rhsList.type == Node.Type.StmtList);

            var leftStmt = astStack.pop();

            var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
            childList.add(leftStmt);
            childList.addAll(rhsList.getChildren());

            var stmtList = new Node(Node.Type.StmtList, childList);
            astStack.push(stmtList);
        }
    }

    @Override
    public void enterRet_type_def(DungeonDSLParser.Ret_type_defContext ctx) {}

    @Override
    public void exitRet_type_def(DungeonDSLParser.Ret_type_defContext ctx) {
        Node retTypeId = astStack.pop();

        // remove the arrow
        astStack.pop();
        astStack.push(retTypeId);
    }

    @Override
    public void enterParam_def(DungeonDSLParser.Param_defContext ctx) {}

    @Override
    public void exitParam_def(DungeonDSLParser.Param_defContext ctx) {
        // topmost id on stack: id of parameter
        var id = astStack.pop();
        assert id.type == Node.Type.Identifier;

        // after that: type id
        var typeId = astStack.pop();
        assert typeId.type == Node.Type.Identifier;

        var paramNode = new ParamDefNode(typeId, id);
        astStack.push(paramNode);
    }

    @Override
    public void enterParam_def_list(DungeonDSLParser.Param_def_listContext ctx) {}

    @Override
    public void exitParam_def_list(DungeonDSLParser.Param_def_listContext ctx) {
        // condense down to list of param def nodes
        if (ctx.param_def_list() == null) {
            // trivial parameter definition list
            var innerParamDef = astStack.pop();
            assert (innerParamDef.type == Node.Type.ParamDef);

            var list = new ArrayList<Node>(1);
            list.add(innerParamDef);

            var paramDefList = new Node(Node.Type.ParamDefList, list);
            astStack.push(paramDefList);
        } else {
            // rhs paramDefList is on stack
            var rhsList = astStack.pop();
            assert (rhsList.type == Node.Type.ParamDefList);

            var leftParamDef = astStack.pop();
            assert (leftParamDef.type == Node.Type.ParamDef);

            var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
            childList.add(leftParamDef);
            childList.addAll(rhsList.getChildren());

            var paramDefList = new Node(Node.Type.ParamDefList, childList);
            astStack.push(paramDefList);
        }
    }

    @Override
    public void enterGame_obj_def(DungeonDSLParser.Game_obj_defContext ctx) {}

    @Override
    public void exitGame_obj_def(DungeonDSLParser.Game_obj_defContext ctx) {
        // if we have a component definition list, it will be on the stack
        var componentDefList = Node.NONE;
        if (ctx.component_def_list() != null) {
            componentDefList = astStack.pop();
            assert componentDefList.type == Node.Type.ComponentDefinitionList;
        }

        // id will be on the stack
        var idNode = astStack.pop();
        assert idNode.type == Node.Type.Identifier;

        var gameObjectDefinition = new GameObjectDefinitionNode(idNode, componentDefList);
        astStack.push(gameObjectDefinition);
    }

    @Override
    public void enterComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {}

    @Override
    public void exitComponent_def_list(DungeonDSLParser.Component_def_listContext ctx) {
        // TODO: add tests for this
        if (ctx.component_def_list() == null) {
            // trivial component definition list
            var innerComponentList = astStack.pop();
            assert (innerComponentList.type == Node.Type.AggregateValueDefinition);

            var list = new ArrayList<Node>(1);
            list.add(innerComponentList);

            var componentDefList = new Node(Node.Type.ComponentDefinitionList, list);
            astStack.push(componentDefList);
        } else {
            // rhs componentDefList is on stack
            var rhsList = astStack.pop();
            assert (rhsList.type == Node.Type.ComponentDefinitionList);

            var leftComponentDef = astStack.pop();
            assert (leftComponentDef.type == Node.Type.AggregateValueDefinition);

            var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
            childList.add(leftComponentDef);
            childList.addAll(rhsList.getChildren());

            var componentDefList = new Node(Node.Type.ComponentDefinitionList, childList);
            astStack.push(componentDefList);
        }
    }

    @Override
    public void enterAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {}

    @Override
    public void exitAggregate_value_def(DungeonDSLParser.Aggregate_value_defContext ctx) {
        // if we have a propertyDefList, it will be on the stack
        var propertyDefListNode = Node.NONE;
        if (ctx.property_def_list() != null) {
            propertyDefListNode = astStack.pop();
            assert propertyDefListNode.type == Node.Type.PropertyDefinitionList;
        }

        // id of the component will be on the stack
        var idNode = astStack.pop();
        assert idNode.type == Node.Type.Identifier;

        var componentDefinitionNode = new AggregateValueDefinitionNode(idNode, propertyDefListNode);
        astStack.push(componentDefinitionNode);
    }

    @Override
    public void enterGrammar_type_obj_def(DungeonDSLParser.Grammar_type_obj_defContext ctx) {}

    @Override
    public void exitGrammar_type_obj_def(DungeonDSLParser.Grammar_type_obj_defContext ctx) {
        var propertyDefList = Node.NONE;
        if (ctx.property_def_list() != null) {
            propertyDefList = astStack.pop();
            assert (propertyDefList.type == Node.Type.PropertyDefinitionList);
        }

        // id on stack
        var id = astStack.pop();
        assert (id.type == Node.Type.Identifier);

        // type specifier on stack
        var typeSpecifier = astStack.pop();
        assert (typeSpecifier.type == Node.Type.Identifier);

        var objectDef =
                new ObjectDefNode(
                        typeSpecifier, id, propertyDefList, ObjectDefNode.Type.GrammarBuiltInType);
        astStack.push(objectDef);
    }

    @Override
    public void enterOther_type_obj_def(DungeonDSLParser.Other_type_obj_defContext ctx) {}

    @Override
    public void exitOther_type_obj_def(DungeonDSLParser.Other_type_obj_defContext ctx) {
        var propertyDefList = Node.NONE;
        if (ctx.property_def_list() != null) {
            propertyDefList = astStack.pop();
            assert (propertyDefList.type == Node.Type.PropertyDefinitionList);
        }

        // id on stack
        var id = astStack.pop();
        assert (id.type == Node.Type.Identifier);

        // type specifier (ID) on stack
        var typeSpecifier = astStack.pop();
        assert (typeSpecifier.type == Node.Type.Identifier);

        var objectDef =
                new ObjectDefNode(
                        typeSpecifier, id, propertyDefList, ObjectDefNode.Type.GrammarBuiltInType);
        astStack.push(objectDef);
    }

    @Override
    public void enterProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {}

    @Override
    public void exitProperty_def_list(DungeonDSLParser.Property_def_listContext ctx) {
        // TODO: add tests for this
        if (ctx.property_def_list() == null) {
            // trivial property definition
            var innerPropertyDef = astStack.pop();
            assert (innerPropertyDef.type == Node.Type.PropertyDefinition);

            var list = new ArrayList<Node>(1);
            list.add(innerPropertyDef);

            var propertyDefList = new Node(Node.Type.PropertyDefinitionList, list);
            astStack.push(propertyDefList);
        } else {
            // rhs propertyDefList is on stack
            var rhsList = astStack.pop();
            assert (rhsList.type == Node.Type.PropertyDefinitionList);

            var leftPropertyDef = astStack.pop();
            assert (leftPropertyDef.type == Node.Type.PropertyDefinition);

            var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
            childList.add(leftPropertyDef);
            childList.addAll(rhsList.getChildren());

            var propertyDefList = new Node(Node.Type.PropertyDefinitionList, childList);
            astStack.push(propertyDefList);
        }
    }

    @Override
    public void enterProperty_def(DungeonDSLParser.Property_defContext ctx) {}

    @Override
    public void exitProperty_def(DungeonDSLParser.Property_defContext ctx) {
        // stmt on stack
        var stmtNode = astStack.pop();

        // ID (lhs) is on stack
        var id = astStack.pop();
        assert (id.type == Node.Type.Identifier);

        var propertyDefNode = new PropertyDefNode(id, stmtNode);
        astStack.push(propertyDefNode);
    }

    @Override
    public void enterFunc_call(DungeonDSLParser.Func_callContext ctx) {}

    @Override
    public void exitFunc_call(DungeonDSLParser.Func_callContext ctx) {
        // TODO: test this
        // if there are parameters, a paramList will be on stack
        var paramList = Node.NONE;
        if (ctx.param_list() != null) {
            paramList = astStack.pop();
            assert paramList.type == Node.Type.ParamList;
        }

        // function id will be on stack
        var funcId = astStack.pop();
        assert funcId.type == Node.Type.Identifier;

        var funcCallNode = new FuncCallNode(funcId, paramList);
        astStack.push(funcCallNode);
    }

    @Override
    public void enterParam_list(DungeonDSLParser.Param_listContext ctx) {}

    @Override
    public void exitParam_list(DungeonDSLParser.Param_listContext ctx) {
        if (ctx.param_list() == null) {
            // trivial param
            var innerParam = astStack.pop();
            var list = new ArrayList<Node>(1);
            list.add(innerParam);

            var paramList = new Node(Node.Type.ParamList, list);
            astStack.push(paramList);
        } else {
            // rhs paramlist is on stack
            var rhsList = astStack.pop();
            assert (rhsList.type == Node.Type.ParamList);

            var leftParam = astStack.pop();
            var childList = new ArrayList<Node>(rhsList.getChildren().size() + 1);
            childList.add(leftParam);
            childList.addAll(rhsList.getChildren());

            var paramList = new Node(Node.Type.ParamList, childList);
            astStack.push(paramList);
        }
    }

    @Override
    public void enterPrimary(DungeonDSLParser.PrimaryContext ctx) {}

    @Override
    public void exitPrimary(DungeonDSLParser.PrimaryContext ctx) {}

    @Override
    public void enterDot_def(DungeonDSLParser.Dot_defContext ctx) {}

    /**
     * Adds the Node, representing the dot_stmt_list of the dot definition, as child to a {@link
     * DotDefNode}. Validates consistency of all edge operators with the type of the defined graph
     * ('graph' or 'digraph').
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitDot_def(DungeonDSLParser.Dot_defContext ctx) {
        // check, whether all edge_ops are correct for graph type
        DotDefNode.Type graphType =
                ctx.graph_type.getText().equals("dslToGame/graph")
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

        // check consistency of used edge operators with graph type
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
        // if the ctx contains a dot_attr_list, the corresponding Node will
        // be on the astStack
        var attr_list = Node.NONE;
        if (ctx.dot_attr_list() != null) {
            attr_list = astStack.pop();
            assert (attr_list.type == Node.Type.DotAttrList);
        }

        // pop all DotEdgeRHS Nodes from the stack and add them to one list
        LinkedList<Node> rhsEdges = new LinkedList<>();
        for (int i = 0; i < ctx.dot_edge_RHS().size(); i++) {
            var rhs = astStack.pop();
            assert (rhs.type == Node.Type.DotEdgeRHS);
            rhsEdges.addFirst(rhs);
        }

        // get the first identifier of the statement (left-hand-side)
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
        // get the Node corresponding to the literal operator (arrow or double line)
        var inner = astStack.pop();
        assert (inner.type == Node.Type.Arrow || inner.type == Node.Type.DoubleLine);

        // determine EdgeOpType based on literal operator
        EdgeOpNode.Type edgeOpNodeType;
        if (inner.type == Node.Type.Arrow) {
            edgeOpNodeType = EdgeOpNode.Type.arrow;
        } else {
            edgeOpNodeType = EdgeOpNode.Type.doubleLine;
        }

        var node = new EdgeOpNode(inner.getSourceFileReference(), edgeOpNodeType);
        astStack.push(node);
    }

    private SourceFileReference getSourceFileReference(TerminalNode node) {
        var symbol = node.getSymbol();
        var line = symbol.getLine();
        var column = symbol.getCharPositionInLine();
        return new SourceFileReference(line, column);
    }

    /**
     * Convert the relevant antlr terminal nodes into {@link Node} objects for further usage by this
     * converter.
     *
     * @param node the terminal node
     */
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
        } else if (nodeType == DungeonDSLLexer.STRING_LITERAL) {
            // TODO: add test for this
            String value = node.getText();

            // trim leading and trailing quotes
            String trimmedValue = value.subSequence(1, value.length() - 1).toString();

            // escape sequences
            String escapedValue = trimmedValue.translateEscapes();

            var stringNode = new StringNode(escapedValue, getSourceFileReference(node));
            astStack.push(stringNode);
        } else if (nodeType == DungeonDSLLexer.NUM_DEC) {
            // TODO: add test for this
            float value = Float.parseFloat(node.getText());
            var numNode = new DecNumNode(value, getSourceFileReference(node));
            astStack.push(numNode);
        } else if (nodeType == DungeonDSLLexer.NUM) {
            // TODO: add test for this
            int value = Integer.parseInt(node.getText());
            var numNode = new NumNode(value, getSourceFileReference(node));
            astStack.push(numNode);
        } else if (nodeType == DungeonDSLLexer.TYPE_SPECIFIER) {
            String value = node.getText();
            var typeSpecifierNode = new IdNode(value, getSourceFileReference(node));
            astStack.push(typeSpecifierNode);
        }
    }

    @Override
    public void visitErrorNode(ErrorNode node) {}

    @Override
    public void enterEveryRule(ParserRuleContext ctx) {}

    @Override
    public void exitEveryRule(ParserRuleContext ctx) {}
}
