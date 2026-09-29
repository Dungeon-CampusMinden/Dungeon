package rooms.gameofgames.level;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.level.DungeonLevel;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.utils.Point;
import engine.utils.Tuple;
import engine.utils.Vector2;
import engine.utils.components.draw.shader.EnergyFillShader;
import engine.utils.components.draw.shader.HueRemapShader;
import feature.components.CollideComponent;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.prefabs.Prefab;
import feature.prefabs.Region;
import feature.prefabs.types.DoorKeypadPrefab;
import feature.prefabs.types.DoorLeverPrefab;
import feature.prefabs.types.InvisibleWallPrefab;
import feature.prefabs.types.PressurePlatePrefab;
import feature.prefabs.types.PushableStonePrefab;
import feature.prefabs.types.WaterPrefab;
import feature.shader.ShaderComponent;
import feature.shader.ShaderSystem;
import java.util.List;
import java.util.Map;
import rooms.gameofgames.canvas.GameOfGamesCanvas;

/** Server-side level setup for the Game of Games escape room. */
public class GameOfGamesLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "game-of-games-1";
  private static final String BOOKSHELF_POINT = "bookshelf";
  private static final String BOOKSHELF_DIALOG =
      "Between unfinished board-game manuals and dusty rulebooks, one note stands out:[p]"
          + "Every game has rules. The interesting part is finding out who wrote them.";
  private static final Point FLOOD_TRIGGER_TILE = new Point(5, 7);
  private static final String FLOOD_LEVER = "r1/door-lever-2";
  private static final String FLOOD_KEYPAD = "r1/keypad-2";
  private static final Region FLOOD_REGION = new Region(new Point(2, 1), new Point(5, 6));

  private Entity floodTrigger;
  private boolean floodTriggered;
  private boolean floodDone;

  /**
   * Creates the first Game of Games level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public GameOfGamesLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the first Game of Games level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   */
  public GameOfGamesLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }

  @Override
  protected void onFirstTick() {
    setupBookshelf();
    setupCanvasTerminal();
    setupCanvasUnlock();
    setupFloodTrigger();
  }

  @Override
  protected void onTick() {
    if (floodTriggered && !floodDone) flood();
    Game.allPlayers()
        .filter(player -> !player.isPresent(ShaderComponent.class))
        .forEach(
            player ->
                player.add(
                    new ShaderComponent(
                        "gameofgames-player-hue", 0, new HueRemapShader(0.66f, 0.0f))));
  }

  private void setupCanvasTerminal() {
    Entity terminal = DecoFactory.createDeco(new Point(8.0f, 8.0f), Deco.PCFlat);
    terminal.remove(DecoComponent.class);
    terminal.add(
        new InteractionComponent(
            new Interaction((interacted, who) -> GameOfGamesCanvas.show(who.id(), who.id()))));
    Game.add(terminal);
  }

  private void setupCanvasUnlock() {
    Entity folder = DecoFactory.createDeco(new Point(13.0f, 10.0f), Deco.FolderRed);
    folder.remove(DecoComponent.class);
    ShaderComponent shaderComp =
        new ShaderComponent("hue", 0, new HueRemapShader(0.0f, 0.66f, 0.2f));
    folder.add(shaderComp);
    folder.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) -> {
                  boolean unlocked = !GameOfGamesCanvas.extraNodesUnlocked();

                  if (unlocked) {
                    ShaderSystem.getInstance()
                        .addLevelShader(
                            "level",
                            0,
                            new EnergyFillShader(0.9f, Color.RED, "items/rpg/food_bananas.png"),
                            who.id());
                  } else {
                    ShaderSystem.getInstance().removeLevelShader("level", who.id());
                  }

                  GameOfGamesCanvas.unlockExtraNodes(unlocked);
                  DialogFactory.showDialogDialog(
                      unlocked
                          ? "You memorize two more rules. Reopen the rule board to see them."
                          : "You forget the two extra rules again.",
                      () -> {},
                      who.id());
                })));
    Game.add(folder);
  }

  private void setupBookshelf() {
    Entity bookshelf = DecoFactory.createDeco(getPoint(BOOKSHELF_POINT), Deco.BookshelfLarge);
    bookshelf.remove(DecoComponent.class);
    bookshelf.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) ->
                    DialogFactory.showDialogDialog(BOOKSHELF_DIALOG, () -> {}, who.id()))));
    Game.add(bookshelf);
  }

  private void setupFloodTrigger() {
    floodTrigger = new Entity("gameofgames-flood-trigger");
    floodTrigger.add(new PositionComponent(FLOOD_TRIGGER_TILE));
    floodTrigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.ONE,
                (self, who, direction) -> {
                  if (who.isPresent(PlayerComponent.class)) floodTriggered = true;
                },
                CollideComponent.DEFAULT_COLLIDER)
            .isSolid(false));
    Game.add(floodTrigger);
  }

  // Runs from onTick instead of the collision callback, because spawning and removing prefab
  // entities while the collision system iterates the entities is not safe.
  private void flood() {
    floodDone = true;
    Game.remove(floodTrigger);

    Point doorPosition =
        prefab(DoorLeverPrefab.class, FLOOD_LEVER)
            .flatMap(Prefab::currentInstance)
            .map(lever -> lever.get(DoorLeverPrefab.DOOR_POSITION))
            .orElse(null);
    prefab(DoorLeverPrefab.class, FLOOD_LEVER).ifPresent(Prefab::remove);
    prefab(DoorKeypadPrefab.class, FLOOD_KEYPAD).ifPresent(Prefab::remove);

    if (doorPosition != null) {
      spawnPrefab(
          PressurePlatePrefab.class,
          "r1/flood-plate",
          plate ->
              plate
                  .with(PressurePlatePrefab.POSITION, new Point(4, 11))
                  .with(PressurePlatePrefab.DOOR_POSITION, doorPosition));
    }
    spawnPrefab(
        PushableStonePrefab.class,
        "r1/flood-stone",
        stone -> stone.with(PushableStonePrefab.POSITION, new Point(4, 9)));
    spawnPrefab(
        WaterPrefab.class,
        "r1/flood-water",
        water ->
            water
                .with(WaterPrefab.REGION, FLOOD_REGION)
                .with(WaterPrefab.COLOR, Color.valueOf("FF4001FF"))
                .with(WaterPrefab.SPEED, 0.18f)
                .with(WaterPrefab.LAYER, -120)
                .with(WaterPrefab.REPEAT, 5f)
                .with(WaterPrefab.FOAM_MIN_WIDTH, 0)
                .with(WaterPrefab.FOAM_MAX_WIDTH, 0)
                .with(WaterPrefab.LINE_INTERVAL, 5f));
    spawnPrefab(
        InvisibleWallPrefab.class,
        "r1/flood-wall",
        wall -> wall.with(InvisibleWallPrefab.REGION, FLOOD_REGION));
  }
}
