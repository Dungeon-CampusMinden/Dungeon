package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;

/** Builds the data entries and map cells displayed by riddle 10. */
public final class SystemCoreEntityFactory {

  private SystemCoreEntityFactory() {}

  /**
   * Creates one visible module entry for the central counting exercise.
   *
   * @param point entry position
   * @param index array index represented by the entry
   * @param moduleName module name, or {@code null} for an empty entry
   * @return configured central module entry
   */
  public static Entity moduleEntry(Point point, int index, String moduleName) {
    Entity entity = new Entity("system_core_module_" + index);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    DrawComponent draw =
        new DrawComponent(
            new SimpleIPath(
                moduleName == null
                    ? "objects/tech/Screen_info_2.png"
                    : "objects/tech/Screen_info_1.png"));
    draw.tintColor(moduleName == null ? 0x66707AFF : 0x33CCFFFF);
    entity.add(draw);
    return entity;
  }

  /**
   * Creates one visible map cell for the central search exercise.
   *
   * @param point cell position
   * @param row map row
   * @param column map column
   * @return configured central map cell
   */
  public static Entity mapCell(Point point, int row, int column) {
    Entity entity = new Entity("system_core_map_" + row + "_" + column);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/tech/Screen_info_3.png"));
    draw.tintColor(0x56616BFF);
    entity.add(draw);
    return entity;
  }
}
