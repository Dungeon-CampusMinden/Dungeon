package newdsl.graph;

import newdsl.ast.ASTNodes;
import newdsl.interpreter.Environment;
import newdsl.tasks.TaskComposition;
import newdsl.tasks.TaskCompositionSubtask;

import java.util.*;

public class TaskConfigGraphBuilder {

    private static TaskEdge.EdgeType getEdgeType(boolean isRequired) {
        return isRequired ? TaskEdge.EdgeType.subtask_mandatory : TaskEdge.EdgeType.subtask_optional;
    }

    // resolve task compositions
    public static Graph resolveGraph(Graph graph, Environment env) {
        List<String> taskIds = graph.nodes.keySet().stream().toList();

        // Wieso gibt es hier einen Fehler?
        taskIds.forEach((n) -> {
            Object compositionCandidate = env.get(n);
            if (compositionCandidate instanceof TaskComposition) {
                // 1. remove composition node
                graph.nodes.remove(n);

                // 2. resolve the composition to its entries
                List<TaskCompositionSubtask> tasks = ((TaskComposition) compositionCandidate).getSubtasks();

                // 3. update accordingly
                tasks.forEach(task -> {
                    // 3.1 add graph node
                    graph.nodes.put(task.getId(), new GraphNode(task.getId()));

                    // 3.2 add the edges for the task themselves
                    for (int i = tasks.indexOf(task); i < tasks.size() - 1; i++) {
                        TaskCompositionSubtask from = tasks.get(i);
                        TaskCompositionSubtask to = tasks.get(i + 1);

                        graph.edges.add(new GraphEdge(getEdgeType(from.isRequired()), from.getId(), to.getId()));
                    }

                    // 3.3 find the ones pointing to n, redirect to entry #1
                    List<GraphEdge> dirtyEdgesTo = graph.edges.stream().filter(e -> e.toId.equals(n)).toList();
                    dirtyEdgesTo.forEach(edge -> {
                        graph.edges.remove(edge);
                        graph.edges.add(new GraphEdge(edge.type, edge.fromId, task.getId()));
                    });

                    // 3.4 find the ones going out from n, make it go out from entry #n
                    List<GraphEdge> dirtyEdgesFrom = graph.edges.stream().filter(e -> e.fromId.equals(n)).toList();
                    dirtyEdgesFrom.forEach(edge -> {
                        graph.edges.remove(edge);
                        graph.edges.add(new GraphEdge(getEdgeType(task.isRequired()), task.getId(), edge.toId));
                    });
                });
            }
        });

        return graph;
    }

    public static void buildGraph(ASTNodes.TaskConfigNode taskConfigNode, Graph graph) {
        graph.addNode(graph.root);
        processContent(null, taskConfigNode.taskConfigContentNode, graph, null, TaskEdge.EdgeType.sequence);
    }

    static void processContent(String currentId, ASTNodes.TaskConfigContentNode node, Graph graph, String danglingId, TaskEdge.EdgeType type) {
        String id = (String) node.id.value;

        if (currentId != null) {
            graph.addEdge(currentId, id, type);
        }

        if (node.followingBranch != null) {
            if (node.followingConfig != null) { // is there a config? if yes, it needs to be passed all the way until there is no branch, then be appended to the each branch
                String dangle = (String) node.followingConfig.id.value;
                processContent(id, node.followingBranch.correctBranch, graph, dangle, TaskEdge.EdgeType.conditional_correct);
                processContent(id, node.followingBranch.falseBranch, graph, dangle, TaskEdge.EdgeType.conditional_false);
            } else {
                processContent(id, node.followingBranch.correctBranch, graph, danglingId, TaskEdge.EdgeType.conditional_correct);
                processContent(id, node.followingBranch.falseBranch, graph, danglingId, TaskEdge.EdgeType.conditional_false);
            }

        } else if (node.followingConfig != null) { // process a simple config
            processContent(id, node.followingConfig, graph, null, TaskEdge.EdgeType.sequence);
        }

        if (node.followingBranch == null && danglingId != null) {
            graph.addEdge(id, danglingId, TaskEdge.EdgeType.sequence);
        }
    }

    public static class GraphNode {
        String id;
        List<GraphNode> neighbors;

        GraphNode(String id) {
            this.id = id;
            this.neighbors = new ArrayList<>();
        }
    }

    public static class GraphEdge {
        public String fromId;
        public String toId;
        public TaskEdge.EdgeType type;

        GraphEdge(TaskEdge.EdgeType type, String fromId, String toId) {
            this.fromId = fromId;
            this.toId = toId;
            this.type = type;
        }
    }

    public static class Graph {

        private String root;
        private Map<String, GraphNode> nodes;
        private List<GraphEdge> edges;

        public Graph(String root) {
            this.nodes = new HashMap<>();
            this.edges = new ArrayList<>();
            this.root = root;
        }

        private GraphNode getNode(String id) {
            return nodes.computeIfAbsent(id, GraphNode::new);
        }

        public Map<String, GraphNode> getNodes() {
            return this.nodes;
        }

        public String getRoot() {
            return root;
        }

        public void setRoot(String root) {
            this.root = root;
        }

        public void setNodes(Map<String, GraphNode> nodes) {
            this.nodes = nodes;
        }

        public List<GraphEdge> getEdges() {
            return edges;
        }

        public void setEdges(List<GraphEdge> edges) {
            this.edges = edges;
        }

        void addEdge(String fromId, String toId, TaskEdge.EdgeType type) {
            GraphNode fromNode = getNode(fromId);
            GraphNode toNode = getNode(toId);
            fromNode.neighbors.add(toNode);
            edges.add(new GraphEdge(type, fromId, toId));
        }

        void addNode(String fromId) {
            GraphNode fromNode = getNode(fromId);
            nodes.put(fromId, fromNode);
        }

    }
}
