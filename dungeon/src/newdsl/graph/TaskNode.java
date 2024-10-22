package newdsl.graph;

import newdsl.tasks.Task;

public class TaskNode {
    public static TaskNode NONE = new TaskNode(null);

    private static int _idx;
    private final int idx;
    private final Task task;

    public TaskNode(Task task) {
        this.task = task;
        this.idx = _idx++;
    }

    public static TaskNode getNONE() {
        return NONE;
    }

    public static void setNONE(TaskNode NONE) {
        TaskNode.NONE = NONE;
    }

    public Task getTask() {
        return task;
    }

    public static int get_idx() {
        return _idx;
    }

    public static void set_idx(int _idx) {
        TaskNode._idx = _idx;
    }

    public int getIdx() {
        return idx;
    }
}
