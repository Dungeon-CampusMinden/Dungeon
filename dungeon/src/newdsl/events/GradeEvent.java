package newdsl.events;

import newdsl.tasks.Answer;

import java.util.Set;

public class GradeEvent<T extends Answer> {
    private final String taskId;
    private final Set<T> answers;

    public GradeEvent(String taskId, Set<T> answers) {
        this.taskId = taskId;
        this.answers = answers;
    }


    public String getTaskId() {
        return taskId;
    }

    public Set<T> getAnswers() {
        return answers;
    }
}
