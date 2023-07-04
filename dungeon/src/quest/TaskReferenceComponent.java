package quest;

import core.Component;
import core.Entity;

/** Create connection between Entity and a {@link QuestContent} */
public final class TaskReferenceComponent extends Component {

    private final Quest quest;
    private final QuestContent content;

    public TaskReferenceComponent(
            final Entity entity, final QuestContent content, final Quest quest) {
        super(entity);
        this.content = content;
        this.quest = quest;
    }

    public QuestContent content() {
        return content;
    }

    public Quest quest() {
        return quest;
    }
}
