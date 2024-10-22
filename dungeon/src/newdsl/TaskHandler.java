package newdsl;

import newdsl.graph.TaskDependencyGraph;
import newdsl.graph.TaskEdge;
import newdsl.graph.TaskNode;
import newdsl.tasks.Answer;
import newdsl.tasks.Task;
import newdsl.tasks.TaskState;

import java.util.*;

public class TaskHandler {

    private TaskDependencyGraph graph;
    private Task current;

    private List<Task> visited = new ArrayList<>();

    private static Optional<TaskNode> findRoot(ArrayList<TaskNode> nodes, ArrayList<TaskEdge> edges) {
        List<String> allIds = nodes.stream().map(n -> n.getTask().getId()).toList();
        List<String> idsWithPredecessors = new ArrayList<>();

        for (TaskEdge edge : edges) {
            idsWithPredecessors.add(edge.getEndNode().getTask().getId());
        }

        for (String id : allIds) {
            if (!idsWithPredecessors.contains(id)) {
                return nodes.stream().filter(n -> n.getTask().getId().equals(id)).findFirst();
            }
        }

        return Optional.empty();
    }

    public TaskHandler(TaskDependencyGraph graph) {
        this.graph = graph;
        findRoot(graph.getNodes(), graph.getEdges()).ifPresent(root -> current = root.getTask());
    }

    public Task getCurrent() {
        return current;
    }

    public <T extends Answer> boolean giveAnswers(Set<T> answers) {
        current.gradeTask(answers);
        return current.getState() == TaskState.FINISHED_CORRECT;
    }

    public void visitPreviousTask() {
        if (visited.isEmpty()) {
            return;
        }

        current = visited.getLast();
        visited.removeLast();

    }

    public <T extends Answer> void enterSolution(Set<T> givenAnswers) {
        current.gradeTask(givenAnswers);
    }

    public void visitNextTask() {
        List<TaskEdge> nextCandidates = graph.getEdges().stream().filter(e -> e.getStartNode().getTask().getId().equals(current.getId())).toList();

        if (nextCandidates.isEmpty()) {
            // nowhere to go
        }

        if (nextCandidates.size() == 1) { // is either a mandatory or an optional edge
            TaskEdge edge = nextCandidates.get(0);

            if (edge.getEdgeType() == TaskEdge.EdgeType.subtask_optional) { // you are free to go
                visited.add(current);
                current = edge.getEndNode().getTask();
            } else if (edge.getEdgeType() == TaskEdge.EdgeType.subtask_mandatory || edge.getEdgeType() == TaskEdge.EdgeType.sequence) {
                // you need to have finished the task
                if (current.getState() == TaskState.FINISHED_CORRECT || current.getState() == TaskState.FINISHED_WRONG) {
                    visited.add(current);
                    current = edge.getEndNode().getTask();
                }
            }
        }

        if (nextCandidates.size() == 2) { // is a conditional edge
            boolean isSolvedCorrect = current.getState() == TaskState.FINISHED_CORRECT;
            Optional<TaskEdge> edge = nextCandidates.stream().filter(e -> isSolvedCorrect ? e.getEdgeType() == TaskEdge.EdgeType.conditional_correct : e.getEdgeType() == TaskEdge.EdgeType.conditional_false).findFirst();

            if(edge.isPresent()){
                TaskEdge e = edge.get();
                visited.add(current);
                current = e.getEndNode().getTask();
            }
        }

    }


}
