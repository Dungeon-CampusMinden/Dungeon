package level;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.entities.HeroController;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.DialogUtils;
import contrib.hud.UIUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.modules.keypad.KeypadFactory;
import contrib.systems.EventScheduler;
import contrib.utils.components.skill.SkillTools;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.systems.DrawSystem;
import core.utils.Point;
import java.util.*;

import core.utils.Rectangle;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.shader.OutlineShader;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import modules.computer.*;
import modules.trash.TrashMinigameUI;
import util.LastHourSounds;
import util.ui.BlackFadeCutscene;

/** The MushRoom. */
public class LastHourLevel extends DungeonLevel {

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
  public LastHourLevel(
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
    setupDecoderShelfs();
    setupPapers();
    setupInteractables();

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

    Game.player().ifPresent(p -> {
      p.fetch(InputComponent.class).ifPresent(ic -> {
        p.fetch(DrawComponent.class).ifPresent(dc -> {
          ic.registerCallback(Input.Keys.SPACE, (e) -> {
            if(dc.shaders().get("outline") == null) {
              dc.shaders().add("outline", new OutlineShader(1, Color.RED));
            } else {
              dc.shaders().remove("outline");
            }
          }, false, false);
        });
      });
    });

    EventScheduler.scheduleAction(this::playAmbientSound, 10 * 1000);
  }

  private void setupInteractables() {
    Entity desk0 = DecoFactory.createDeco(getPoint("desk-nothing0"), Deco.StampingTable);
    desk0.remove(DecoComponent.class);
    desk0.add(new InteractionComponent(() -> new Interaction((e, who) -> {
      DialogFactory.showOkDialog("More papers documenting weird science experiments.\nYou try to understand any of it, but it just doesn't make sense to you.", "", () -> {}, who.id());
    })));

    Entity desk1 = DecoFactory.createDeco(getPoint("desk-nothing1"), Deco.WritingTable);
    desk1.remove(DecoComponent.class);
    desk1.add(new InteractionComponent(() -> new Interaction((e, who) -> {
      DialogFactory.showOkDialog("A bunch of papers laying all over the place on the desk.\nYou weed through them, until a weird looking note catches your eye", "", () -> {
        DialogUtils.showImagePopUp("images/note-password-1.png");
      }, who.id());
    })));

    Entity printer = DecoFactory.createDeco(getPoint("printer"), Deco.Printer2);
    printer.remove(DecoComponent.class);
    printer.add(new InteractionComponent(() -> new Interaction((e, who) -> {
      DialogFactory.showOkDialog("Someone forgot to turn of the printer, so it's been spweing\nout more and more documents, until presumably the power outage\nstopped it.", "", () -> {}, who.id());
    })));


    listPointsIndexed("locker").forEach(t -> {
      Point p = t.a();
      int index = t.b();
      Entity locker = DecoFactory.createDeco(p, Deco.Cabinet);
      locker.remove(DecoComponent.class);
      locker.add(new InteractionComponent(() -> new Interaction((e, who) -> {
        DialogFactory.showOkDialog("You open the locker, but it's empty except for some white coats.\nSeems like someone already went through it...", "", () -> {}, who.id());
      })));
      Game.add(locker);
    });

    List.of(desk0, desk1, printer).forEach(Game::add);
  }

  private void setupPapers() {
    Game.levelEntities(Set.of(DecoComponent.class)).forEach(e -> {
      DecoComponent dc = e.fetch(DecoComponent.class).orElseThrow();
      if(dc.type() == Deco.SheetWritten1 || dc.type() == Deco.SheetWritten2) {
        e.fetch(CollideComponent.class).ifPresent(cc -> {
          cc.isSolid(false);
        });
        e.fetch(DrawComponent.class).ifPresent(drawComp -> {
          DrawSystem.getInstance().changeEntityDepth(e, DepthLayer.BackgroundDeco.depth());
        });
      }
    });
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
  private static final String trashNote = "images/note-password-2.png";
  private static final int trashIndex = 5;

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
                                    index == trashIndex ? trashNote : null);
                                DialogFactory.show(builder.build(), who.id());
                              })));
              Game.add(trashcan);
            });
  }

  private List<String> decoderTablePaths = List.of(
    "images/base64.png",
    "images/binary_hex.jpg",
    "images/hex_ascii.png",
    "images/braille.png",
    "images/decoder_74155.png",
    "images/morse.png"
  );
  private void setupDecoderShelfs(){
    listPointsIndexed("decode").forEach(t -> {
      Point p = t.a();
      int index = t.b();
      Entity decoderShelf = DecoFactory.createDeco(p, Deco.BookshelfLarge);
      decoderShelf.remove(DecoComponent.class);
      decoderShelf.add(new InteractionComponent(() -> new Interaction((e, who) -> {
        DialogContext.Builder builder = DialogContext.builder().type(DialogType.DefaultTypes.IMAGE);
        builder.put(DialogContextKeys.IMAGE, decoderTablePaths.get(index % decoderTablePaths.size()));
        DialogFactory.show(builder.build(), who.id());
      })));
      Game.add(decoderShelf);
    });
  }

  @Override
  protected void onTick() {
    checkPCStateUpdate();
    checkInteractFeedback();
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

    ComputerDialog.getInstance().ifPresent(cd -> {
      if (cd.sharedState() != csc){
        cd.updateState(csc);
      }
    });
  }

  private Entity interactableEntity = null;
  private void checkInteractFeedback() {
    Game.player().ifPresent(p -> {
      Optional<Entity> found = HeroController.findInteractable(p, SkillTools.cursorPositionAsPoint());
      if(found.isPresent() && found.get() != interactableEntity){
        // New interactable entity
        if(interactableEntity != null){
          // Remove old feedback
          removeInteractFeedback(interactableEntity);
        }
        interactableEntity = found.get();
        addInteractFeedback(interactableEntity);
      }
      else if(found.isEmpty() && interactableEntity != null){
        // No interactable entity anymore, remove old feedback
        removeInteractFeedback(interactableEntity);
        interactableEntity = null;
      }
    });
  }
  private void removeInteractFeedback(Entity entity){
    entity.fetch(DrawComponent.class).ifPresent(dc -> {
      dc.shaders().remove("outline");
    });
  }
  private void addInteractFeedback(Entity entity){
    entity.fetch(DrawComponent.class).ifPresent(dc -> {
      dc.shaders().add("outline", new OutlineShader(1, new Color(0.8f, 0, 0, 1f)));
    });
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
