package rooms.morphco.level;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabRegistry;
import feature.prefabs.PrefabSide;
import java.util.List;

/**
 * An object of the Morph &amp; Co. room, placed and moved in the level editor.
 *
 * <p>The instance name becomes the entity name the room code attaches its behaviour to (see {@link
 * MorphCoObjects#on(String, java.util.function.Consumer)}).
 */
public final class MorphCoObjectPrefab extends Prefab {

  /** Registry ID of the prefab. */
  public static final String TYPE = "morphco-object";

  private static final String DISPLAY_NAME = "Morph & Co. object";

  /** Bottom-left corner of the object. */
  public static final PrefabProperty<Point> POSITION =
      PrefabProperty.point("position", "Position", new Point(0, 0));

  /** Kind of object, which decides its look. */
  public static final PrefabProperty<String> KIND =
      PrefabProperty.selection("kind", "Object", "server", MorphCoObjects.kinds());

  /** Crate index, belt length, or click-area size in tenths of a tile. */
  public static final PrefabProperty<Integer> NUMBER =
      PrefabProperty.integer("number", "Number (crate, belt length, click size)", 0, 0, 40);

  private static final List<PrefabProperty<?>> PROPERTIES = List.of(POSITION, KIND, NUMBER);

  /** Creates the editor definition. */
  public MorphCoObjectPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES);
  }

  /**
   * Creates a typed view of an authored object.
   *
   * @param level owning level
   * @param name instance name
   */
  public MorphCoObjectPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, PROPERTIES, level, name);
  }

  /** Registers the prefab before any server, client, or editor level parsing. */
  public static void register() {
    if (PrefabRegistry.find(TYPE).isEmpty())
      PrefabRegistry.register(new MorphCoObjectPrefab(), MorphCoObjectPrefab::new);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    return MorphCoObjects.build(
        value(instance, KIND), instance.name(), value(instance, POSITION), value(instance, NUMBER));
  }

  @Override
  public void renderEditorFeedback(
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    feedback.point(value(instance, POSITION), instance.name());
  }
}
