package dsltypeadapters;

import core.utils.components.draw.Animation;

import semanticanalysis.types.DSLTypeAdapter;
import semanticanalysis.types.DSLTypeMember;

import task.QuestItem;
import task.components.TaskContentComponent;

/** Typeadatper for creation of {@link QuestItem}s */
public class QuestItemAdapter {
    /**
     * Buildermethod for creating a {@link QuestItem}. Will create a new {@link
     * TaskContentComponent} and pass it to the ctor of {@link QuestItem}.
     *
     * @param displayName The displayName of the new {@link QuestItem}.
     * @param description The description of the new {@link QuestItem}.
     * @param texturePath The path to a texture, which will be used for the new {@link QuestItem}.
     * @return the newly created {@link QuestItem}.
     */
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
