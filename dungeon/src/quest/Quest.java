package quest;

public abstract class Quest {
    private final QuestContent[] content;
    private final QuestContent task;

    protected Quest(QuestContent[] content, QuestContent task) {
        this.content = content;
        this.task = task;
    }

    public QuestContent[] content() {
        return content;
    }

    public QuestContent task() {
        return task;
    }

    @Override
    public String toString() {
        return task.toString();
    }
}
