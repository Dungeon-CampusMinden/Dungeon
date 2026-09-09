package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.AnimationConfig;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.ArrayList;
import java.util.List;

/** Existing art placed by prop markers, with floor footprints for solid furniture. */
final class ProgrammingProps {
  private ProgrammingProps() {}

  static CollideComponent chestCollider() {
    return new CollideComponent(Vector2.of(0.1f, 0.05f), Vector2.of(0.8f, 0.55f));
  }

  static CollideComponent vaseCollider() {
    return new CollideComponent(Vector2.of(0.2f, 0.05f), Vector2.of(0.6f, 0.4f));
  }

  /**
   * Creates wall debris using ordinary synchronized draw states.
   *
   * @param level the level containing the masonry gate markers
   * @return initially hidden stone draw components to reveal when the gate breaks
   */
  static List<DrawComponent> wall(DungeonLevel level) {
    List<DrawComponent> stones = new ArrayList<>();
    var start = level.getPoint("act1-gate-start");
    var end = level.getPoint("act1-gate-end");
    // Sparse debris appears only when the native masonry gate breaks.
    for (int y = (int) start.y(); y <= end.y(); y += 3) {
      int x = (int) start.x();
      DrawComponent draw = new DrawComponent(new SimpleIPath("objects/stone"), "idle");
      draw.depth(DepthLayer.Ground.depth());
      draw.tintColor(0xFFFFFF00);
      Entity stone = new Entity("programming-breakable-wall-" + x + "-" + y);
      stone.add(new PositionComponent(new engine.utils.Point(x, y)));
      stone.add(draw);
      Game.add(stone);
      stones.add(draw);
    }
    return stones;
  }

  static void spawn(DungeonLevel level) {
    level
        .namedPoints()
        .forEach(
            (name, point) -> {
              if (!name.startsWith("prop-")) return;
              DrawComponent draw;
              if (name.startsWith("prop-workbench"))
                draw =
                    new DrawComponent(
                        new SimpleIPath("spritesheets/FD_Dungeon_Free.png"),
                        new AnimationConfig(new SpritesheetConfig(192, 352, 1, 1, 32, 16)));
              else if (name.startsWith("prop-torch"))
                draw = new DrawComponent(new SimpleIPath("objects/torch"), "on");
              else if (name.startsWith("prop-forge-kettle"))
                draw = new DrawComponent(new SimpleIPath("objects/magic_kettle"));
              else if (name.startsWith("prop-forge-vase"))
                draw = new DrawComponent(new SimpleIPath("objects/vase"));
              else if (name.startsWith("prop-forge-crate"))
                draw = new DrawComponent(new SimpleIPath("objects/crate/basic.png"));
              else return;
              draw.depth(DepthLayer.Player.depth());
              Entity prop = new Entity("programming-" + name);
              prop.add(new PositionComponent(point));
              prop.add(draw);
              if (name.startsWith("prop-torch"))
                prop.add(
                    new InteractionComponent(
                        new Interaction(
                            (interacted, who) -> {
                              if (Game.isMultiplayerClient()) return;
                              var state =
                                  interacted
                                      .fetch(DrawComponent.class)
                                      .orElseThrow()
                                      .stateMachine();
                              state.setState(
                                  state.getCurrentStateName().equals("on") ? "off" : "on", null);
                            })));
              if (name.startsWith("prop-workbench"))
                prop.add(new CollideComponent(Vector2.of(0.05f, 0.05f), Vector2.of(1.9f, 0.65f)));
              else if (name.startsWith("prop-forge-crate") || name.startsWith("prop-forge-kettle"))
                prop.add(chestCollider());
              else if (name.startsWith("prop-forge-vase")) prop.add(vaseCollider());
              Game.add(prop);
            });
  }
}
