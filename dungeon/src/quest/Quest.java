package quest;

import java.util.Set;

public abstract class Quest {
    private final Set<QuestContent> content;
    private final String questText;

    protected Quest(Set<QuestContent> content, String questText) {
        this.content = content;
        this.questText = questText;
    }

    public Set<QuestContent> content() {
        return content;
    }

    public String questText() {
        return questText;
    }

    @Override
    public String toString() {
        return questText;
    }
}
