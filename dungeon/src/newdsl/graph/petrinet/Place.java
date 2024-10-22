package newdsl.graph.petrinet;

import newdsl.tasks.Task;
import newdsl.tasks.TaskState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Place {
    private int tokenCount = 0;
    private final Map<Task, TaskState> observe = new HashMap<>();
    private final Map<Task, TaskState> changeStateOnTokenAdd = new HashMap<>();
    private final Set<Transition> transition = new HashSet<>();

    public void observe(Task task, TaskState state) {
        if (!changeStateOnTokenAdd.isEmpty())
            throw new RuntimeException("A Place cannot observe and activate Tasks at the same time.");
        task.registerPlace(this);
        observe.put(task, state);
    }


    public void notify(Task task, TaskState state) {
        if (observe.get(task) == state) placeToken();
    }

    public void changeStateOnTokenAdd(Task task, TaskState state) {
        if (!observe.isEmpty())
            throw new RuntimeException("A Place cannot observe and activate Tasks at the same time.");
        changeStateOnTokenAdd.put(task, state);
    }


    public void placeToken() {
        tokenCount++;
        changeStateOnTokenAdd.forEach(Task::state);
        transition.forEach(transition -> transition.notify(this));
    }


    public void removeToken() {
        tokenCount = Math.max(0, tokenCount - 1);
        transition.forEach(transition -> transition.notify(this));
    }

    public void register(Transition observer) {
        this.transition.add(observer);
    }

    public int tokenCount() {
        return tokenCount;
    }
}
