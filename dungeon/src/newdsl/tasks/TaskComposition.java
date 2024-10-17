package newdsl.tasks;

import java.util.List;

public class TaskComposition {
    private String id;
    private List<TaskCompositionSubtask> subtasks;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TaskCompositionSubtask> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<TaskCompositionSubtask> subtasks) {
        this.subtasks = subtasks;
    }
}
