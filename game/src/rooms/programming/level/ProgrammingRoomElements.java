package rooms.programming.level;

import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.SpritesheetConfig;
import engine.utils.components.draw.state.CharacterStateFactory;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import feature.components.CollideComponent;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import java.util.List;
import rooms.programming.modules.loops.LoopPuzzle;
import rooms.programming.modules.loops.LoopRune;

/** Places the shared golem, assignment chests, collectible runes, and workshop notes. */
final class ProgrammingRoomElements {

  // The 64x72 frame has 63 visible pixels in height; one unscaled tile is 64 pixels.
  private static final float GOLEM_SCALE = 5f * 64f / 63f;
  private static final float GOLEM_INTERACTION_RANGE = 3.5f;
  // The floor footprint spans the feet, not the full height of the standing sprite.
  private static final Vector2 GOLEM_HITBOX_OFFSET = Vector2.of(3f / 64f, 3f / 64f);
  private static final Vector2 GOLEM_HITBOX_SIZE = Vector2.of(58f / 64f, 2.5f / GOLEM_SCALE);

  private ProgrammingRoomElements() {}

  static ProgrammingGolemRuntime spawn(DungeonLevel level) {
    ProgrammingProps.spawn(level);
    ProgrammingMazeWorld.spawn(level);
    Entity golem = createEntity(level, "variables-golem", Visual.GOLEM, 0);
    ProgrammingGolemRuntime runtime = new ProgrammingGolemRuntime(level, golem);
    golem.add(
        new InteractionComponent(
            new Interaction((interacted, who) -> runtime.show(who), GOLEM_INTERACTION_RANGE)));
    Game.add(golem);
    spawnBindingChest(level, runtime, true);
    spawnBindingChest(level, runtime, false);
    LoopPuzzle.runes().forEach(rune -> spawnLoopRune(level, rune, runtime));
    spawnControl(level, "loop-terminal", Visual.BOOK, runtime, false);
    spawnControl(level, "loop-monitor", Visual.SEHSTEIN, runtime, true);
    spawnText(level, "archive-instructions", Visual.BOOK, ProgrammingStory.archive());
    spawnText(level, "intro-tablet", Visual.BOOK, ProgrammingStory.letter());
    spawnText(level, "forge-maintenance-note", Visual.SCROLL, ProgrammingStory.maintenance());
    if (level.namedPoints().containsKey("variables-translation")) {
      Entity tablet = createEntity(level, "variables-translation", Visual.BOOK, 0);
      tablet.add(
          new InteractionComponent(new Interaction((entity, who) -> runtime.showBindingBook(who))));
      Game.add(tablet);
    }
    return runtime;
  }

  private static void spawnText(DungeonLevel level, String point, Visual visual, String text) {
    if (!level.namedPoints().containsKey(point)) return;
    Entity entity = createEntity(level, point, visual, 0);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) -> {
                  if (Game.isMultiplayerClient()) return;
                  ProgrammingGolemRuntime.showText(who, text);
                })));
    Game.add(entity);
  }

  private static void spawnBindingChest(
      DungeonLevel level, ProgrammingGolemRuntime runtime, boolean properties) {
    Entity chest =
        createEntity(
            level, properties ? "variables-properties" : "variables-vessels", Visual.CHEST, 0);
    chest.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) -> {
                  if (Game.isMultiplayerClient()) return;
                  runtime.collectBindingSupply(properties, who);
                  interacted
                      .fetch(DrawComponent.class)
                      .orElseThrow()
                      .stateMachine()
                      .setState("open_empty", null);
                })));
    Game.add(chest);
  }

  private static void spawnControl(
      DungeonLevel level,
      String point,
      Visual visual,
      ProgrammingGolemRuntime runtime,
      boolean observation) {
    Entity entity = createEntity(level, point, visual, 0);
    if (!observation) entity.fetch(DrawComponent.class).orElseThrow().tintColor(0xDD99FFFF);
    entity.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) -> {
                  if (observation) runtime.showObservation(who);
                  else runtime.showTerminal(who);
                },
                2f)));
    Game.add(entity);
  }

  private static void spawnLoopRune(
      DungeonLevel level, LoopRune rune, ProgrammingGolemRuntime runtime) {
    Entity entity =
        createEntity(level, "rune-" + rune.id(), Visual.RUNE, LoopPuzzle.runes().indexOf(rune));
    entity.fetch(PositionComponent.class).orElseThrow().scale(0.65f);
    entity.fetch(DrawComponent.class).orElseThrow().depth(DepthLayer.Player.depth() + 1);
    entity
        .fetch(DrawComponent.class)
        .orElseThrow()
        .tintColor(
            switch (rune.program().type()) {
              case WHILE -> 0x99CCFFFF;
              case DO_WHILE -> 0xDD99FFFF;
              case FOR -> 0xFFCC88FF;
            });
    entity.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) -> {
                  if (!runtime.collectRune(rune.id(), who)) return;
                  Game.remove(interacted);
                })));
    Game.add(entity);
  }

  private static Entity createEntity(
      DungeonLevel level, String pointName, Visual visual, int runeIndex) {
    Entity entity = new Entity("programming-" + pointName);
    PositionComponent position = new PositionComponent(level.getPoint(pointName));
    if (visual == Visual.GOLEM) {
      position.scale(GOLEM_SCALE);
      entity.add(new VelocityComponent(2.5f, 8f));
      entity.add(new CollideComponent(GOLEM_HITBOX_OFFSET, GOLEM_HITBOX_SIZE));
    } else if (visual == Visual.CHEST) {
      entity.add(ProgrammingProps.chestCollider());
    } else if (visual == Visual.SEHSTEIN) {
      // The crystal rests on the workbench; its table already supplies the floor collision.
      position.scale(.8f);
    } else if (visual == Visual.BOOK || visual == Visual.SCROLL) {
      position.scale(0.6f);
    }
    entity.add(position);
    entity.add(visual.drawComponent(runeIndex));
    return entity;
  }

  private enum Visual {
    RUNE,
    CHEST,
    BOOK,
    SCROLL,
    SEHSTEIN,
    GOLEM;

    private DrawComponent drawComponent(int runeIndex) {
      DrawComponent drawComponent =
          switch (this) {
            case RUNE ->
                new DrawComponent(
                    new SimpleIPath("spritesheets/runes.png"),
                    new SpritesheetConfig(runeIndex % 8 * 16, runeIndex % 24 / 8 * 16, 1, 1));
            case CHEST -> new DrawComponent(new SimpleIPath("objects/treasurechest"), "closed");
            case BOOK -> new DrawComponent(new SimpleIPath("items/rpg/item_book_brown.png"));
            case SCROLL -> new DrawComponent(new SimpleIPath("items/rpg/item_scroll.png"));
            case SEHSTEIN ->
                new DrawComponent(
                    new StateMachine(
                        List.of(
                            new State(
                                "idle",
                                new SimpleIPath("rooms/programming/art/sehstein.png"),
                                new SpritesheetConfig(0, 0, 1, 1, 16, 24)),
                            new State(
                                "active",
                                new SimpleIPath("rooms/programming/art/sehstein.png"),
                                new SpritesheetConfig(16, 0, 1, 1, 16, 24)))));
            case GOLEM ->
                new DrawComponent(
                    CharacterStateFactory.createStateMachine(
                        new SimpleIPath("character/monster/programming_golem")));
          };
      drawComponent.depth(
          switch (this) {
            case RUNE -> DepthLayer.Ground.depth();
            // Tabletop items must render above the furniture supporting them.
            case BOOK, SCROLL, SEHSTEIN -> DepthLayer.Player.depth() + 1;
            // Share the player's layer so Y sorting places characters behind standing objects.
            case GOLEM, CHEST -> DepthLayer.Player.depth();
          });
      return drawComponent;
    }
  }
}
