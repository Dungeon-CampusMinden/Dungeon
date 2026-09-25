package feature.prefabs.types;

import engine.Entity;
import engine.components.PositionComponent;
import engine.level.elements.ILevel;
import engine.utils.Point;
import engine.utils.Vector2;
import feature.components.CollideComponent;
import feature.prefabs.Prefab;
import feature.prefabs.PrefabCreationContext;
import feature.prefabs.PrefabEditorFeedback;
import feature.prefabs.PrefabInstance;
import feature.prefabs.PrefabProperty;
import feature.prefabs.PrefabSide;
import feature.prefabs.Region;
import java.util.List;

/** Server-side invisible rectangle with a solid collider and no other interactions. */
public final class InvisibleWallPrefab extends Prefab {

  private static final String TYPE = "invisible-wall";
  private static final String DISPLAY_NAME = "Invisible Wall";
  private static final PrefabProperty<Region> REGION =
      PrefabProperty.region("region", "Region", new Region(new Point(0, 0), new Point(1, 1)));

  /** Creates the invisible-wall definition. */
  public InvisibleWallPrefab() {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, List.of(REGION));
  }

  /**
   * Creates a bound view for one authored invisible-wall instance.
   *
   * @param level owning level
   * @param name authored instance name
   */
  public InvisibleWallPrefab(ILevel level, String name) {
    super(TYPE, DISPLAY_NAME, PrefabSide.SERVER, List.of(REGION), level, name);
  }

  @Override
  public List<Entity> create(PrefabCreationContext context, PrefabInstance instance) {
    Region region = value(instance, REGION);
    Point bottomLeft = region.bottomLeft();
    float width = region.topRight().x() - bottomLeft.x();
    float height = region.topRight().y() - bottomLeft.y();
    if (width == 0f || height == 0f) return List.of();

    Entity wall = context.createEntity(instance.name());
    wall.add(new PositionComponent(bottomLeft));
    wall.add(new CollideComponent(Vector2.ZERO, Vector2.of(width, height)));
    return List.of(wall);
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
