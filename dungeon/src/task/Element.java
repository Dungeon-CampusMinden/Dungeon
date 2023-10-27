package task;

/**
 * Generic storage for a single value that is a {@link TaskContent}.
 *
 * @param <T>
 */
public class Element<T> extends TaskContent {
    private final T content;

    /**
     * Create a new element that can be used as {@link TaskContent}.
     *
     * <p>This content will not automatically register itself with the task. Call {@link
     * Task#addContent(TaskContent)} for that.
     *
     * @param task Task to which this content belongs.
     * @param content Content to store in this element.
     */
    public Element(Task task, T content) {
        super(task);
        this.content = content;
    }

    /**
     * @return The content of this element.
     */
    public T content() {
        return content;
    }

    @Override
    public String toString() {
        return content.toString();
    }
}
