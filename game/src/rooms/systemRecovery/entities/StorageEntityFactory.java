package rooms.systemRecovery.entities;

import engine.Entity;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.utils.Point;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.hud.DialogUtils;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import rooms.systemRecovery.util.StorageCellColors;
import rooms.systemRecovery.util.SystemRecoveryText;

/** Builds the cell markers and materialized values used by riddle 8. */
public final class StorageEntityFactory {

  private StorageEntityFactory() {}

  /**
   * Creates an interactable marker for one cell of the storage matrix.
   *
   * @param point cell position
   * @param row matrix row
   * @param column matrix column
   * @return configured storage cell entity
   */
  public static Entity matrixCell(Point point, int row, int column) {
    Entity entity = new Entity("storage_matrix_cell_" + row + "_" + column);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    entity.add(
        new InteractionComponent(
            new Interaction(
                (cell, who) -> {
                  String state =
                      cell.name().endsWith("_filled")
                          ? "filled"
                          : cell.name().endsWith("_target")
                              ? "target"
                              : cell.name().endsWith("_active") ? "active" : "empty";
                  DialogUtils.showTextPopup(
                      SystemRecoveryText.key("world.matrix." + state, row, column),
                      SystemRecoveryText.key("world.matrix.title"),
                      who.id());
                })));
    return entity;
  }

  /**
   * Creates one visible value materialized by the two-dimensional storage.
   *
   * @param point item position
   * @param value value represented by the item
   * @return configured storage value entity
   */
  public static Entity valueItem(Point point, int value) {
    Entity entity = new Entity("storage_value_item_" + value);
    entity.add(new PositionComponent(point));
    entity.add(new CollideComponent());
    DrawComponent draw = new DrawComponent(new SimpleIPath("objects/tech/Screen_info_3.png"));
    draw.depth(DepthLayer.AbovePlayer.depth());
    draw.tintColor(StorageCellColors.forValue(value));
    entity.add(draw);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (_, who) ->
                    DialogUtils.showTextPopup(
                        SystemRecoveryText.key("world.matrix.item", value),
                        SystemRecoveryText.key("world.matrix.title"),
                        who.id()))));
    return entity;
  }
}
