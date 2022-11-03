package parser;

import static org.junit.Assert.assertEquals;

import helpers.Helpers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import parser.AST.IdNode;
import parser.AST.Node;
import starter.DesktopLauncher;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DesktopLauncher.class})
public class TestDungeonASTConverter {

    @Test
    public void testSimpleDotDef() {
        String program = "graph g { a -- b }";

        var ast = Helpers.getASTFromString(program);

        // TODO: checking the structure in this way is very verbose and
        // gets old quickly..
        var dot_def = ast.getChild(0);
        assertEquals(dot_def.type, Node.Type.DotDefinition);

        var id = dot_def.getChild(0);
        assertEquals(id.type, Node.Type.Identifier);
        var idNode = (IdNode) id;
        assertEquals("g", idNode.getName());
    }
}
