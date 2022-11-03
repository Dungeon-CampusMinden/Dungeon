package parser;

import static org.junit.Assert.assertEquals;

import helpers.Helpers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import parser.AST.*;
import starter.DesktopLauncher;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DesktopLauncher.class})
public class TestDungeonASTConverter {

    // TODO: checking the structure in this way is very verbose and
    // gets old quickly, should create more comfortable way of testing AST-Structure..
    @Test
    public void testSimpleDotDef() {
        String program = "graph g { a -- b }";

        var ast = Helpers.getASTFromString(program);

        var dot_def = ast.getChild(0);
        assertEquals(dot_def.type, Node.Type.DotDefinition);

        var id = dot_def.getChild(0);
        assertEquals(id.type, Node.Type.Identifier);
        var idNode = (IdNode) id;
        assertEquals("g", idNode.getName());

        var edgeStmt = dot_def.getChild(1);
        assertEquals(edgeStmt.type, Node.Type.DotEdgeStmt);
        var edgeStmtNode = (EdgeStmtNode) edgeStmt;

        var lhsId = edgeStmtNode.getLhsId();
        assertEquals(lhsId.type, Node.Type.Identifier);
        var lhsIdNode = (IdNode) lhsId;
        assertEquals(lhsIdNode.getName(), "a");

        var rhsStmts = edgeStmtNode.getRhsStmts();
        assertEquals(rhsStmts.size(), 1);

        var rhsEdge = rhsStmts.get(0);
        assertEquals(rhsEdge.type, Node.Type.DotEdgeRHS);

        var rhsEdgeNode = (EdgeRhsNode) rhsEdge;
        var edgeOp = rhsEdgeNode.getEdgeOpNode();
        assertEquals(edgeOp.type, Node.Type.DotEdgeOp);

        var edgeOpNode = (EdgeOpNode)edgeOp;
        assertEquals(edgeOpNode.getEdgeOpType(), EdgeOpNode.Type.doubleLine);

        var rhsId = rhsEdgeNode.getIdNode();
        assertEquals(rhsId.type, Node.Type.Identifier);

        var rhsIdNode = (IdNode)rhsId;
        assertEquals(rhsIdNode.getName(), "b");
    }
}
