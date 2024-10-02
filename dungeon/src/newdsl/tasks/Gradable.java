package newdsl.tasks;

import java.util.Set;

public interface Gradable<T> {
    public float gradeTask(Set<T> answers);

    public boolean pass(float points);

    public String correctAnswersAsString();
}
