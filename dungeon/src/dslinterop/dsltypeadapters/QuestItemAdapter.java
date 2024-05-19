package dslinterop.dsltypeadapters;

import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;
import dsl.annotation.DSLTypeAdapter;
import dsl.annotation.DSLTypeMember;
import task.game.components.TaskContentComponent;
import task.game.content.QuestItem;

/** Typeadatper for creation of {@link QuestItem}. */
public class QuestItemAdapter {
  /**
   * Buildermethod for creating a {@link QuestItem}. Will create a new {@link TaskContentComponent}
   * and pass it to the ctor of {@link QuestItem}.
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
    Animation animation = Animation.fromSingleImage(new SimpleIPath(texturePath));
    TaskContentComponent tcc = new TaskContentComponent();
    return new QuestItem(animation, tcc);
  }
}
