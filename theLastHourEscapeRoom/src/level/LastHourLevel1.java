package level;

import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.modules.keypad.KeypadFactory;
import contrib.systems.EventScheduler;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.utils.Point;
import java.util.*;

import core.utils.Rectangle;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import modules.computer.ComputerFactory;
import modules.computer.ComputerProgress;
import modules.computer.ComputerStateComponent;
import modules.computer.LastHourDialogTypes;
import modules.trash.TrashMinigameUI;
import util.LastHourSounds;
import util.ui.BlackFadeCutscene;

/** The MushRoom. */
public class LastHourLevel1 extends DungeonLevel {

  private DoorTile storageDoor;
  public static Entity pc;
  public static final String PC_STATE_OFF = "off";
  public static final String PC_STATE_ON = "on";
  public static final String PC_STATE_VIRUS = "virus";
  public static final String PC_SIGNAL_ON = "on";
  public static final String PC_SIGNAL_INFECT = "infect";
  public static final String PC_SIGNAL_CLEAR = "clear";

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

    setupPC();
    setupTrashcans();

    String lorem =
        """
      Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.

      Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi. Lorem ipsum dolor sit amet, consectetuer adipiscing elit, sed diam nonummy nibh euismod tincidunt ut laoreet dolore magna aliquam erat volutpat.

      Ut wisi enim ad minim veniam, quis nostrud exerci tation ullamcorper suscipit lobortis nisl ut aliquip ex ea commodo consequat. Duis autem vel eum iriure dolor in hendrerit in vulputate velit esse molestie consequat, vel illum dolore eu feugiat nulla facilisis at vero eros et accumsan et iusto odio dignissim qui blandit praesent luptatum zzril delenit augue duis dolore te feugait nulla facilisi.
      """;

    //    DialogFactory.showOkDialog("Welcome to this escape room adventure!\nYou have 20 minutes to
    // find a way out.\n\nGood Luck!", "", () -> {});
    //    DialogFactory.showTextDialog("Welcome to this escape room adventure!\nYou have 20 minutes
    // to find a way out.\n\nGood Luck!", "", () -> {}, "OK", null, null);
    //    DialogFactory.showTextDialog(lorem, "Some Title", () -> {}, "Dann mal weiter...");

    BlackFadeCutscene.show(List.of("Test message", "And page 2"), false, true, () -> {
      DialogFactory.showYesNoDialog("Welcome to this escape room adventure!\nYou have 20 minutes to find a way out.\n\nGood Luck\n\nDo you want another popup?", "", () -> {
        DialogFactory.showTextDialog(lorem, "Some Title", () -> {}, "Dann mal weiter...");
      }, () -> {});
    });


    EventScheduler.scheduleAction(this::playAmbientSound, 10 * 1000);
  }

  private void setupPC(){
    pc = new Entity("pc-main");
    PositionComponent positionComp = new PositionComponent(getPoint("pc-main").translate(-0.7f, -0.9f));
    pc.add(positionComp);
    pc.add(new CollideComponent(new Rectangle(2.0f, 1.3f, 0.8f, 1f)));

    Map<String, Animation> animationMap = Animation.loadAnimationSpritesheet(new SimpleIPath("objects/desk_with_pc"));
    State stOff = State.fromMap(animationMap, PC_STATE_OFF);
    State stOn = State.fromMap(animationMap, PC_STATE_ON);
    State stVirus = State.fromMap(animationMap, PC_STATE_VIRUS);
    List<State> states = List.of(stOff, stOn, stVirus);
    StateMachine sm = new StateMachine(states, stOff);
    sm.addTransition(stOff, PC_SIGNAL_ON, stOn);
    sm.addTransition(stOn, PC_SIGNAL_INFECT, stVirus);
    sm.addTransition(stVirus, PC_SIGNAL_CLEAR, stOn);
    DrawComponent dc = new DrawComponent(sm);
    dc.depth(DepthLayer.Player.depth());
    pc.add(dc);
    ComputerFactory.attachComputerDialog(pc);
    Game.add(pc);

    Entity computerState = new Entity("computer-state");
    computerState.add(new ComputerStateComponent(ComputerProgress.OFF, false));
    Game.add(computerState);

    // Power switch (hidden under papers)
    Entity paper = DecoFactory.createDeco(getPoint("paper-switch"), Deco.SheetWritten2);
    paper.remove(DecoComponent.class);
    paper.add(new InteractionComponent(() -> new Interaction((e, who) -> {
      if(!dc.currentStateName().equals(PC_STATE_OFF)) return;
      DialogFactory.showYesNoDialog("There is a switch hidden below these stacks of paper.\n\nDo you want to flip it?", "", () -> {
        Sounds.playLocal(CoreSounds.SETTINGS_TOGGLE_CLICK, 1, 1.5f);
        DialogFactory.showOkDialog("You flipped the switch.\n\nYou can hear electricity buzzing throughout the room.", "", () -> {
          ComputerStateComponent.setState(ComputerProgress.ON);
          Sounds.play(LastHourSounds.ELECTRICITY_TURNED_ON, 1, 1.0f);
        });
      }, () -> {}, who.id());
    })));
    Game.add(paper);
  }

  private static final Deco[] trashcans = {Deco.TrashCanBlue, Deco.TrashCanGreen, Deco.TrashCanRed};
  private static final String[] trashcanNotes = {
    "images/note-password-1.png", null, "images/note-password-2.png"
  };

  private void setupTrashcans() {
    DialogFactory.register(LastHourDialogTypes.TRASHCAN, TrashMinigameUI::build);
    listPointsIndexed("trash")
        .forEach(
            t -> {
              Point p = t.a();
              int index = t.b();
              Deco deco = trashcans[index % trashcans.length];
              Entity trashcan = DecoFactory.createDeco(p, deco);
              trashcan.remove(DecoComponent.class);
              trashcan.add(
                  new InteractionComponent(
                      () ->
                          new Interaction(
                              (eInteract, who) -> {
                                DialogContext.Builder builder = DialogContext.builder();
                                builder.type(LastHourDialogTypes.TRASHCAN);
                                builder.put(
                                    TrashMinigameUI.KEY_NOTE_PATH,
                                    trashcanNotes[index % trashcanNotes.length]);
                                DialogFactory.show(builder.build(), who.id());
                              })));
              Game.add(trashcan);
            });
  }

  @Override
  protected void onTick() {
    checkPCStateUpdate();
  }

  private void checkPCStateUpdate() {
    ComputerStateComponent csc = ComputerStateComponent.getState();
    DrawComponent dc = pc.fetch(DrawComponent.class).orElseThrow();
    if(!dc.currentStateName().equals(pcStateToDCState(csc))){
      // Update local state to match shared state
      if(csc.isInfected()) {
        dc.sendSignal(PC_SIGNAL_INFECT);
        Sounds.play(LastHourSounds.COMPUTER_VIRUS_CAUGHT, 1, 1.0f);
      }
      else {
        if(csc.state() == ComputerProgress.ON) {
          dc.sendSignal(PC_SIGNAL_ON);
        }
        else {
          dc.sendSignal(PC_SIGNAL_CLEAR);
          Sounds.playLocal(CoreSounds.INTERFACE_BUTTON_BACKWARD);
        }
      }
    }
  }

  private String pcStateToDCState(ComputerStateComponent csc) {
    if(csc.isInfected()) return PC_STATE_VIRUS;
    if(csc.state() == ComputerProgress.OFF) return PC_STATE_OFF;
    return PC_STATE_ON;
  }

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
