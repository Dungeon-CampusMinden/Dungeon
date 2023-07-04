package quest;

public abstract class QuestContent {
    private final String content;
    private final IQuestContentType type;

    public QuestContent(String content, IQuestContentType type) {
        this.content = content;
        this.type = type;
    }

    public String content() {
        return content;
    }

    public IQuestContentType type() {
        return type;
    }

    @Override
    public String toString() {
        return content;
    }
}
