package dsltypeadapters;

import core.utils.components.draw.Animation;
import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;
import task.QuestItem;
import task.components.TaskContentComponent;

public class QuestItemAdapter {
    @DSLTypeAdapter(name = "quest_item")
    public static QuestItem buildQuestItem(
        @DSLTypeMember(name = "display_name") String displayName,
        @DSLTypeMember(name = "description") String description,
        @DSLTypeMember(name = "texture_path") String texturePath) {
        Animation animation = new Animation(texturePath);
        TaskContentComponent tcc = new TaskContentComponent();
        return new QuestItem(displayName, description, animation, tcc);
    }
}
