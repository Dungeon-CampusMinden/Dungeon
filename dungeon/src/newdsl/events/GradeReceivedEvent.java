package newdsl.events;

import newdsl.tasks.Answer;
import newdsl.tasks.Task;

import java.util.Set;

public class GradeReceivedEvent {
    private final float points;
    private final Task task;

    public GradeReceivedEvent(float points, Task t) {
        this.points = points;
        this.task = t;
    }

    public float getPoints() {
        return points;
    }

    public Task getTask() {
        return task;
    }
}
