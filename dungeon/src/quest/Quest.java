package quest;

/**
 * Quest is a collection of {@link QuestContent} Objects. The {@link #task} represents the Quest
 * itself, it contains the quest text as content In {@link #content} all the other quest related
 * things are stored. For example this can be different anwers for a question.
 */
public abstract class Quest {
    private final QuestContent[] content;
    private final QuestContent task;
    private final IContentType type;

    /**
     * Create a new Quest
     *
     * @param content possible answer objects
     * @param task Object that stores the task text
     */
    protected Quest(QuestContent[] content, QuestContent task, IContentType type) {
        this.content = content;
        this.task = task;
        this.type = type;
    }

    public QuestContent[] content() {
        return content;
    }

    public QuestContent task() {
        return task;
    }

    public IContentType type() {
        return type;
    }

    @Override
    public String toString() {
        return task.toString();
    }
}
