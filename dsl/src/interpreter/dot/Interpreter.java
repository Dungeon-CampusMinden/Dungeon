package interpreter.dot;

import interpreter.DSLInterpreter;
import parser.ast.*;
// CHECKSTYLE:ON: AvoidStarImport

import task.quizquestion.SingleChoice;

import taskdependencygraph.TaskDependencyGraph;
// CHECKSTYLE:OFF: AvoidStarImport
import taskdependencygraph.TaskEdge;
import taskdependencygraph.TaskNode;

import java.util.*;

public class Interpreter implements AstVisitor<TaskNode> {
    private final DSLInterpreter dslInterpreter;
    // how to build graph?
    // - need nodes -> hashset, quasi symboltable
    Dictionary<String, TaskNode> graphNodes = new Hashtable<>();

    // - need edges (between two nodes)
    //      -> hashset with string-concat of Names with edge_op as key
    Dictionary<String, TaskEdge> graphEdges = new Hashtable<>();

    ArrayList<TaskDependencyGraph> graphs = new ArrayList<>();

    public Interpreter(DSLInterpreter dslInterpreter) {
        this.dslInterpreter = dslInterpreter;
    }

    /**
     * Parses a dot definition and creates a {@link TaskDependencyGraph} from it
     *
     * @param dotDefinition The DotDefNode to parse as a graph
     * @return The {@link TaskDependencyGraph} object created from the dotDefinition
     */
    public TaskDependencyGraph getGraph(DotDefNode dotDefinition) {
        graphNodes = new Hashtable<>();
        graphEdges = new Hashtable<>();

        dotDefinition.accept(this);

        // sort edges
        var edgeIter = graphEdges.elements().asIterator();
        ArrayList<TaskEdge> edgeList = new ArrayList<>(graphEdges.size());

        while (edgeIter.hasNext()) {
            edgeList.add(edgeIter.next());
        }

        Collections.sort(edgeList);

        // sort nodes
        var nodeIter = graphNodes.elements().asIterator();
        ArrayList<TaskNode> nodeList = new ArrayList<>(graphNodes.size());

        while (nodeIter.hasNext()) {
            nodeList.add(nodeIter.next());
        }

        Collections.sort(nodeList);

        return new TaskDependencyGraph(edgeList, nodeList);
    }

    @Override
    public TaskNode visit(Node node) {
        // traverse down..
        for (Node child : node.getChildren()) {
            if (child.type == Node.Type.DotDefinition) {
                var graph = getGraph((DotDefNode) child);
                graphs.add(graph);
            } else {
                child.accept(this);
            }
        }
        return null;
    }

    @Override
    public TaskNode visit(IdNode node) {
        String name = node.getName();
        // TODO: resolve name as task definition (see:
        var task = node.accept(dslInterpreter);

        // https://github.com/Programmiermethoden/Dungeon/issues/520)
        // lookup and create, if not present previously
        if (graphNodes.get(name) == null) {
            graphNodes.put(name, new TaskNode(new SingleChoice("")));
        }

        // return Dot-Node
        return graphNodes.get(name);
    }

    @Override
    public TaskNode visit(DotDefNode node) {
        this.graphEdges = new Hashtable<>();
        this.graphNodes = new Hashtable<>();

        for (Node edgeStmt : node.getStmtNodes()) {
            edgeStmt.accept(this);
        }

        return null;
    }

    @Override
    public TaskNode visit(DotEdgeStmtNode node) {
        // TODO: add handling of edge-attributes

        // node will contain all edge definitions
        //var lhsDotNode = node.getLhsId().accept(this);
        var lhsDotNode = node.getIds().get(0).accept(this);
        TaskNode rhsDotNode = null;

        List<Node> rhsIds = node.getIds().subList(1, node.getIds().size());

        for (Node id : rhsIds) {
            assert (id.type.equals(Node.Type.Identifier));

            //EdgeRhsNode edgeRhs = (EdgeRhsNode) edge;
            rhsDotNode = id.accept(this);

            // TODO: parse dependency type correctly (see:
            // https://github.com/Programmiermethoden/Dungeon/issues/520)
            TaskEdge.Type edgeType = TaskEdge.Type.sequence;

            var graphEdge = new TaskEdge(edgeType, lhsDotNode, rhsDotNode);
            graphEdges.put(graphEdge.name(), graphEdge);

            lhsDotNode = rhsDotNode;
        }

        return null;
    }
}
