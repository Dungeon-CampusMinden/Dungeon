package rooms.systemRecovery.level;

import engine.Entity;
import engine.Game;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.utils.Point;
import engine.utils.Tuple;
import feature.components.DecoComponent;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.dialogs.DialogFactory;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import java.util.List;
import java.util.Map;

/** Minimal server-side level for System Recovery. */
public class SystemRecoveryLevel extends DungeonLevel {

  private static final String LEVEL_NAME = "system-recovery-1";
  private static final String EXIT_DOOR_POINT = "exit-door";
  private static final String EXIT_KEYPAD_POINT = "exit-keypad";
  private static final String CLUE_BOARD_POINT = "clue-board";
  private static final List<Integer> EXIT_CODE = List.of(1, 2, 3, 4);

  /**
   * Creates the System Recovery level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   * @param decorations static decorations loaded from the level asset
   */
  public SystemRecoveryLevel(
      LevelElement[][] layout,
      DesignLabel designLabel,
      Map<String, Point> namedPoints,
      List<Tuple<Deco, Point>> decorations) {
    super(layout, designLabel, namedPoints, decorations, LEVEL_NAME);
  }

  /**
   * Creates the System Recovery level.
   *
   * @param layout the tile layout loaded from the level asset
   * @param designLabel the visual design for the tiles
   * @param namedPoints named points loaded from the level asset
   */
  public SystemRecoveryLevel(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, LEVEL_NAME);
  }

  @Override
  protected void onFirstTick() {
    DoorTile exitDoor = (DoorTile) tileAt(getPoint(EXIT_DOOR_POINT)).orElseThrow();
    exitDoor.close();

    setupClueBoard();
    setupExitKeypad(exitDoor);
  }

  private void setupClueBoard() {
    Entity clueBoard = DecoFactory.createDeco(getPoint(CLUE_BOARD_POINT), Deco.BookShelf);
    clueBoard.remove(DecoComponent.class);
    clueBoard.add(
        new InteractionComponent(
            new Interaction(
                (interacted, who) ->
                    DialogFactory.showDialogDialog(
                        "A maintenance note is pinned between old folders:[p]"
                            + "\"Exit lock reset: 1234.\"",
                        () -> {},
                        who.id()))));
    Game.add(clueBoard);
  }

  private void setupExitKeypad(DoorTile exitDoor) {
    Entity keypad =
        KeypadFactory.createKeypad(
            getPoint(EXIT_KEYPAD_POINT),
            EXIT_CODE,
            () -> {
              Sounds.play(CoreSounds.DOOR_OPEN);
              exitDoor.open();
            },
            true);
    keypad
        .fetch(KeypadComponent.class)
        .ifPresent(
            component -> {
              component.onCorrectCode(
                  player ->
                      DialogFactory.showDialogDialog(
                          "The lock clicks open. Time to leave.", () -> {}, player.id()));
              component.onWrongCode(
                  player ->
                      DialogFactory.showDialogDialog(
                          "The keypad rejects the code.", () -> {}, player.id()));
            });
    Game.add(keypad);
  }
}
