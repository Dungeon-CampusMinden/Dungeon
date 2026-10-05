package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.components.LeverComponent;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.prefabs.types.TorchPrefab;
import java.util.ArrayList;
import java.util.List;
import rooms.programming.ProgrammingAchievements;

/** Room torch interactions, chest footprints, and breakable wall debris. */
final class ProgrammingProps {
  static final String TORCH_PREFIX = "torch-";
  static final String HEART_TORCH = TORCH_PREFIX + "decisions-heart";

  private ProgrammingProps() {}

  static boolean torch(Entity entity) {
    return entity.name().startsWith(TORCH_PREFIX);
  }

  static void installTorches(DungeonLevel level) {
    level
        .prefabs(TorchPrefab.class)
        .forEach(prefab -> switchableTorch(prefab.torchEntity().orElseThrow()));
  }

  static CollideComponent chestCollider() {
    return new CollideComponent(Vector2.of(0.1f, 0.05f), Vector2.of(0.8f, 0.55f));
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

  /**
   * All room torches use the same server-owned interaction and synchronized flame state.
   *
   * @param torch torch to equip with the shared toggle interaction
   */
  static void switchableTorch(Entity torch) {
    torch.add(new InteractionComponent(new Interaction(ProgrammingProps::toggleTorch)));
  }

  static void toggleTorch(Entity torch, Entity who) {
    if (Game.isMultiplayerClient()) return;
    var lever = torch.fetch(LeverComponent.class).orElseThrow();
    lever.toggle();
    String signal = lever.isOn() ? "on" : "off";
    torch.fetch(DrawComponent.class).orElseThrow().sendSignal(signal);
    ProgrammingProgress.interaction(torch.name(), "turn-" + signal, who);
    if (!lever.isOn()) {
      ProgrammingAchievements.LIGHTS_OUT.unlock(who);
      if (torch.name().equals(HEART_TORCH)) ProgrammingAchievements.HEARTFIRE_OUT.unlock(who);
    }
    if (blackout()) ProgrammingAchievements.BLACKOUT.unlock(who);
  }

  /**
   * Every room torch must explicitly be off; an empty world is not a blackout.
   *
   * @return whether at least one torch exists and all room torches are off
   */
  static boolean blackout() {
    var torches = Game.levelEntities().filter(ProgrammingProps::torch).toList();
    return !torches.isEmpty()
        && torches.stream()
            .allMatch(
                entity ->
                    entity
                        .fetch(DrawComponent.class)
                        .map(draw -> draw.stateMachine().getCurrentStateName().equals("off"))
                        .orElse(false));
  }
}
