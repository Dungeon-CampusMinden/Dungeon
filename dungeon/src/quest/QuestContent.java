package quest;

/**
 * Basic class for a Content of Quests.
 *
 * <p>Each QuestContent is from a specific {@link IContentType}.
 *
 * <p>Also a QuestContent contains a String that represents the content in the real world (for
 * example the question text or an possible answer)
 */
public abstract class QuestContent {
    private final String content;
    private final IContentType type;

    /**
     * Create a new QuestContent
     *
     * @param content Content as String (for example the answer text)
     * @param type Type of the content (different for each task-type)
     */
    public QuestContent(String content, IContentType type) {
        this.content = content;
        this.type = type;
    }

    public String content() {
        return content;
    }

    public IContentType type() {
        return type;
    }

    @Override
    public String toString() {
        return content;
    }
}
