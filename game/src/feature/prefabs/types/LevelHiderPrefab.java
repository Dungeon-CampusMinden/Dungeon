package feature.prefabs.types;

import engine.Entity;
import engine.utils.Point;
import feature.level.visibility.LevelHideFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import java.util.List;

/** Client-side rectangular level-hide region. */
public final class LevelHiderPrefab extends Prefab {

  private static final PrefabProperty<Point> FIRST_CORNER =
      PrefabProperty.point("firstCorner", "First Corner", new Point(0, 0));
  private static final PrefabProperty<Point> SECOND_CORNER =
      PrefabProperty.point("secondCorner", "Second Corner", new Point(1, 1));
  private static final PrefabProperty<Float> TRANSITION_SIZE =
      PrefabProperty.floating("transitionSize", "Transition Size", 2f, 0f, Float.MAX_VALUE);

  /** Creates the level-hider definition. */
  public LevelHiderPrefab() {
    super(
        "level-hider",
        "Level Hider",
        PrefabSide.CLIENT,
        List.of(FIRST_CORNER, SECOND_CORNER, TRANSITION_SIZE));
  }

  @Override
  protected void validate(PrefabInstance instance) {
    Point first = value(instance, FIRST_CORNER);
    Point second = value(instance, SECOND_CORNER);
    if (first.x() == second.x() || first.y() == second.y()) {
      throw new IllegalArgumentException("level hider must have a positive width and height");
    }
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Point first = value(instance, FIRST_CORNER);
    Point second = value(instance, SECOND_CORNER);
    Point bottomLeft = new Point(Math.min(first.x(), second.x()), Math.min(first.y(), second.y()));
    Point topRight = new Point(Math.max(first.x(), second.x()), Math.max(first.y(), second.y()));
    Entity hider = context.createEntity(instance.name());
    LevelHideFactory.createLevelHide(
        hider,
        bottomLeft,
        topRight.x() - bottomLeft.x(),
        topRight.y() - bottomLeft.y(),
        value(instance, TRANSITION_SIZE));
    return List.of(hider);
  }

  @Override
  public void renderEditorFeedback(
      PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Point first = value(instance, FIRST_CORNER);
    Point second = value(instance, SECOND_CORNER);
    feedback.point(first, "Corner 1");
    feedback.point(second, "Corner 2");
    feedback.rectangle(first, second);
  }
}
