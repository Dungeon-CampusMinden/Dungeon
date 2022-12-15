package interpreter.dot;

import static org.junit.Assert.assertTrue;

import helpers.Helpers;
import org.junit.Test;
import parser.AST.DotDefNode;

public class TestDotInterpreter {

    /** Test, if the order of nodes returned by the node iterator is correct */
    @Test
    public void testNodeOrder() {
        String program = "graph g {\n" + "A -- B \n" + "B -- E -- D -- C \n" + "}";
        var ast = Helpers.getASTFromString(program);
        var dot_def = ast.getChild(0);

        Interpreter dotInterpreter = new Interpreter();
        var graph = dotInterpreter.getGraph((DotDefNode) dot_def);

        var nodeIter = graph.getNodeIterator();
        int iteration = 0;
        int lastNodeIdx = -1;
        while (nodeIter.hasNext()) {
            var node = nodeIter.next();
            assertTrue(
                    "Assertion not met, index "
                            + node.getIdx()
                            + " is smaller than last index "
                            + lastNodeIdx
                            + " iteration: "
                            + iteration,
                    node.getIdx() > lastNodeIdx);
            lastNodeIdx = node.getIdx();
            iteration++;
        }
    }

    /** Test, if the order of edges returned by the edge iterator is correct */
    @Test
    public void testEdgeOrder() {
        String program = "graph g {\n" + "A -- B \n" + "B -- E -- D -- C \n" + "}";
        var ast = Helpers.getASTFromString(program);
        var dot_def = ast.getChild(0);

        Interpreter dotInterpreter = new Interpreter();
        var graph = dotInterpreter.getGraph((DotDefNode) dot_def);

        var edgeIter = graph.getEdgeIterator();
        int iteration = 0;
        int lastEdgeIdx = -1;
        while (edgeIter.hasNext()) {
            var edge = edgeIter.next();
            assertTrue(
                    "Assertion not met, index "
                            + edge.getIdx()
                            + " is smaller than last index "
                            + lastEdgeIdx
                            + " iteration: "
                            + iteration,
                    edge.getIdx() > lastEdgeIdx);
            lastEdgeIdx = edge.getIdx();
            iteration++;
        }
    }
}
