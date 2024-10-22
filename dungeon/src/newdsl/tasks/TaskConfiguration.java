package newdsl.tasks;

import newdsl.graph.TaskDependencyGraph;

public class TaskConfiguration {
    private String id;
    private TaskDependencyGraph graph;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public TaskDependencyGraph getGraph() {
        return graph;
    }

    public void setGraph(TaskDependencyGraph graph) {
        this.graph = graph;
    }
}
