package quest;

public abstract class QuestContent {
    private final String content;

    public QuestContent(String content) {
        this.content = content;
    }

    public String content() {
        return content;
    }

    @Override
    public String toString() {
        return content;
    }
}
