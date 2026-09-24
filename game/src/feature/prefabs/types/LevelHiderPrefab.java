package feature.prefabs.types;

import engine.Entity;
import engine.level.elements.ILevel;
import engine.utils.Point;
import feature.level.visibility.LevelHideFactory;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import java.util.List;

/**
 * Client-side rectangular level-hide region.
 *
 * <p>Saved instances use the {@code region} property. Legacy instances with {@code firstCorner}
 * or {@code secondCorner} are skipped by the V3 loader rather than converted.
 */
public final class LevelHiderPrefab extends Prefab {

  private static final PrefabProperty<Region> REGION =
      PrefabProperty.region(
          "region", "Region", new Region(new Point(0, 0), new Point(1, 1)));
  private static final PrefabProperty<Float> TRANSITION_SIZE =
      PrefabProperty.floating("transitionSize", "Transition Size", 2f, 0f, Float.MAX_VALUE);

  /** Creates the level-hider definition. */
  public LevelHiderPrefab() {
    super(
        "level-hider",
        "Level Hider",
        PrefabSide.CLIENT,
        List.of(REGION, TRANSITION_SIZE));
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    var bottomLeft = region.bottomLeft();
    var topRight = region.topRight();
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
      ILevel level, PrefabInstance instance, PrefabEditorFeedback feedback, boolean selected) {
    Region region = value(instance, REGION);
    feedback.point(region.bottomLeft(), null);
    feedback.point(region.topRight(), null);
    feedback.rectangle(region.bottomLeft(), region.topRight());
    feedback.label(midpoint(region.bottomLeft(), region.topRight()), instance.name());
  }

  private static Point midpoint(Point first, Point second) {
    return new Point(first.x() * 0.5f + second.x() * 0.5f, first.y() * 0.5f + second.y() * 0.5f);
  }
}
