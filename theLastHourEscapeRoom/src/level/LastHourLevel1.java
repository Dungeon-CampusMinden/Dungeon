package level;

import contrib.components.DecoComponent;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.modules.keypad.KeypadFactory;
import contrib.systems.EventScheduler;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import core.Entity;
import core.Game;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.utils.Point;
import modules.computer.ComputerFactory;
import modules.computer.ComputerState;
import modules.computer.ComputerStateComponent;
import modules.computer.LastHourDialogTypes;
import modules.trash.TrashMinigameUI;

import java.util.*;

/** The MushRoom. */
public class LastHourLevel1 extends DungeonLevel {

  private DoorTile storageDoor;

  /**
   * Creates a new Demo Level.
   *
   * @param layout The layout of the level.
   * @param designLabel The design label of the level.
   * @param namedPoints The custom points of the level.
   */
  public LastHourLevel1(
      LevelElement[][] layout, DesignLabel designLabel, Map<String, Point> namedPoints) {
    super(layout, designLabel, namedPoints, "last-hour-1");
  }

  @Override
  protected void onFirstTick() {
    Game.levelEntities(Set.of(DecoComponent.class))
        .forEach(
            e -> {
              e.remove(InteractionComponent.class);
            });

    storageDoor = (DoorTile) tileAt(getPoint("door-storage")).orElseThrow();
    storageDoor.close();

    DoorTile entryDoor = (DoorTile) tileAt(getPoint("door-entry")).orElseThrow();
    entryDoor.close();

    Game.add(
        KeypadFactory.createKeypad(
            getPoint("keypad-storage"),
            List.of(1, 2, 3, 4),
            () -> {
              storageDoor.open();
            },
            true));

    // Main PC
    Entity pc = DecoFactory.createDeco(getPoint("pc-main"), Deco.DeskWithPC1);
    pc.remove(DecoComponent.class);
    ComputerFactory.attachComputerDialog(pc);
    Game.add(pc);

    Entity computerState = new Entity("computer-state");
    computerState.add(new ComputerStateComponent(ComputerState.PRE_LOGIN));
    Game.add(computerState);

    setupTrashcans();

    EventScheduler.scheduleAction(this::playAmbientSound, 10 * 1000);
  }

  private static final Deco[] trashcans = {
    Deco.TrashCanBlue, Deco.TrashCanGreen, Deco.TrashCanRed
  };
  private static final String[] trashcanNotes = {
    "images/note-password-1.png", null, "images/note-password-2.png"
  };
  private void setupTrashcans() {
    DialogFactory.register(LastHourDialogTypes.TRASHCAN, TrashMinigameUI::build);
    listPointsIndexed("trash").forEach(t -> {
      Point p = t.a();
      int index = t.b();
      Deco deco = trashcans[index % trashcans.length];
      Entity trashcan = DecoFactory.createDeco(p, deco);
      trashcan.remove(DecoComponent.class);
      trashcan.add(new InteractionComponent(() -> new Interaction((eInteract, who) -> {
        DialogContext.Builder builder = DialogContext.builder();
        builder.type(LastHourDialogTypes.TRASHCAN);
        builder.put(TrashMinigameUI.KEY_NOTE_PATH, trashcanNotes[index % trashcanNotes.length]);
        DialogFactory.show(builder.build(), who.id());
      })));
      Game.add(trashcan);
    });
  }

  @Override
  protected void onTick() {}

  /** Plays ambient sounds at random intervals. */
  private void playAmbientSound() {
    // TODO: Copied from MushRoom, use different sounds.
    double r = Math.random();
    if (r < 0.20) {
      //      Sounds.TREE_AMBIENT_CREAK.play();
    } else if (r < 0.40) {
      //      Sounds.ANIMAL_AMBIENT.play();
    } else if (r < 0.60) {
      //      Sounds.random(Sounds.WIND_AMBIENT_1, Sounds.WIND_AMBIENT_2, Sounds.WIND_AMBIENT_3);
    }

    EventScheduler.scheduleAction(this::playAmbientSound, (long) (Math.random() * 10000 + 10000));
  }
}
