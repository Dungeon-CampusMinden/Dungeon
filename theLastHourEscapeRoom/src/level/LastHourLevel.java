package level;

import com.badlogic.gdx.graphics.Color;
import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.entities.HeroController;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogFactory;
import contrib.hud.dialogs.DialogType;
import contrib.modules.emote.Emote;
import contrib.modules.emote.EmoteFactory;
import contrib.modules.interaction.Interaction;
import contrib.modules.interaction.InteractionComponent;
import contrib.modules.keypad.KeypadComponent;
import contrib.modules.keypad.KeypadFactory;
import contrib.modules.worldTimer.WorldTimerFactory;
import contrib.modules.worldTimer.WorldTimerSystem;
import contrib.systems.EventScheduler;
import contrib.utils.EntityUtils;
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
import core.utils.Rectangle;
import core.utils.Vector2;
import core.utils.components.draw.DepthLayer;
import core.utils.components.draw.animation.Animation;
import core.utils.components.draw.shader.OutlineShader;
import core.utils.components.draw.state.State;
import core.utils.components.draw.state.StateMachine;
import core.utils.components.path.SimpleIPath;
import core.utils.logging.DungeonLogger;
import java.util.*;
import modules.computer.*;
import modules.computer.content.BlogTab;
import modules.trash.TrashMinigameUI;
import util.LastHourSounds;
import util.Lore;
import util.shaders.LightingShader;
import util.ui.BlackFadeCutscene;

/** The Last Hour Room. */
public class LastHourLevel extends DungeonLevel {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LastHourLevel.class);
  private static LastHourLevel Instance = null;

  private DoorTile storageDoor;
  private Entity pc;
  private ComputerStateComponent cscLastTick;
  private Entity keypad;
  private int lastKnownVisibleCommentCount = 0;

  /** The state of the PC when it's off. */
  public static final String PC_STATE_OFF = "off";

  private static final String PC_STATE_ON = "on";
  private static final String PC_STATE_VIRUS = "virus";
  private static final String PC_SIGNAL_ON = "on";
  private static final String PC_SIGNAL_INFECT = "infect";
  private static final String PC_SIGNAL_CLEAR = "clear";

  private static final Set<Integer> INTRO_SHOWN_TO = new HashSet<>();

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
    Instance = this;
  }

  /**
   * Gets the instance of the LastHourLevel.
   *
   * @return The instance of the LastHourLevel.
   */
  public static LastHourLevel getInstance() {
    return Instance;
  }

  @Override
  protected void onFirstTick() {
    storageDoor = (DoorTile) tileAt(getPoint("door-storage")).orElseThrow();
    storageDoor.close();

    DoorTile entryDoor = (DoorTile) tileAt(getPoint("door-entry")).orElseThrow();
    entryDoor.close();

    keypad =
        KeypadFactory.createKeypad(
            getPoint("keypad-storage"), Lore.DoorCode, () -> storageDoor.open(), true);
    Game.add(keypad);

    setupPC();
    setupTrashcans();
    setupDecoderShelfs();
    setupPapers();
    setupInteractables();
    if (!Game.isHeadless()) setupLightingShader();
    setupTimer();
    setupEndTrigger();

    EventScheduler.scheduleAction(this::playAmbientSound, 10 * 1000);
  }

  private void showIntro(int targetId) {
    BlackFadeCutscene.show(
        Lore.IntroTexts,
        false,
        true,
        () ->
            DialogFactory.showTextDialog(
                Lore.PostIntroDialogText1,
                "",
                () -> DialogFactory.showOkDialog(Lore.PostIntroDialogText2, "", () -> {}, targetId),
                null,
                targetId),
        targetId);
    INTRO_SHOWN_TO.add(targetId);
  }

  private void setupEndTrigger() {
    Entity trigger = new Entity("end-trigger");
    trigger.add(new PositionComponent(getPoint("end-trigger")));
    trigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.ONE,
                (e, other, dir) -> {
                  other
                      .fetch(InputComponent.class)
                      .ifPresent(
                          pc -> {
                            BlackFadeCutscene.show(
                                Lore.OutroTexts, true, false, () -> Game.exit("Win"));
                          });
                },
                null)
            .isSolid(false));
    Game.add(trigger);

    Entity triggerLockMove = new Entity("end-trigger-lock-move");
    triggerLockMove.add(new PositionComponent(getPoint("end-trigger-lock-move")));
    triggerLockMove.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.ONE,
                (e, other, dir) -> {
                  other
                      .fetch(InputComponent.class)
                      .ifPresent(inputComponent -> inputComponent.deactivateControls(true));
                },
                null)
            .isSolid(false));
    Game.add(triggerLockMove);
  }

  private void setupTimer() {
    int unixTime = (int) (System.currentTimeMillis() / 1000L);
    Game.add(WorldTimerFactory.createWorldTimer(getPoint("timer"), unixTime, 60 * 20));
    if (!Game.isHeadless()) {
      Game.add(new WorldTimerSystem());
    }
  }

  static void setupLightingShader() {
    DrawSystem.getInstance().sceneShaders().add("lighting", new LightingShader().ambientLight(0));
  }

  private static final String cabinetImagePath = "images/virus-phrases.png";

  private void setupInteractables() {
    Entity desk0 = DecoFactory.createDeco(getPoint("desk-nothing0"), Deco.StampingTable);
    desk0.remove(DecoComponent.class);
    desk0.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (e, who) -> {
                      DialogFactory.showOkDialog(
                          "More papers documenting weird science experiments.\nYou try to understand any of it, but it just doesn't make sense to you.",
                          "",
                          () -> {},
                          who.id());
                    })));

    Entity desk1 = DecoFactory.createDeco(getPoint("desk-nothing1"), Deco.WritingTable);
    desk1.remove(DecoComponent.class);
    desk1.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (e, who) -> {
                      DialogFactory.showOkDialog(
                          "A bunch of papers laying all over the place on the desk.\nYou weed through them, until a weird looking note catches your eye",
                          "",
                          () -> {
                            DialogUtils.showImagePopUp("images/note-password-1.png", who.id());
                          },
                          who.id());
                    })));

    Entity printer = DecoFactory.createDeco(getPoint("printer"), Deco.Printer2);
    printer.remove(DecoComponent.class);
    printer.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (e, who) -> {
                      DialogFactory.showOkDialog(
                          "Someone forgot to turn of the printer, so it's been spewing\nout more and more documents, until presumably the power outage\nstopped it.",
                          "",
                          () -> {},
                          who.id());
                    })));

    listPointsIndexed("locker")
        .forEach(
            t -> {
              Point p = t.a();
              int index = t.b();
              Entity locker = DecoFactory.createDeco(p, Deco.Cabinet);
              locker.remove(DecoComponent.class);
              locker.add(
                  new InteractionComponent(
                      () ->
                          new Interaction(
                              (e, who) -> {
                                if (index == 2) {
                                  DialogFactory.showOkDialog(
                                      "You open the locker. Lots of white coats are hanging inside,\nbut one of them has a piece of paper in the pocket.\n\nYou unfold the paper to take a look at it.",
                                      "",
                                      () -> {
                                        DialogUtils.showImagePopUp(cabinetImagePath, who.id());
                                      },
                                      who.id());
                                  return;
                                }
                                DialogFactory.showOkDialog(
                                    "You open the locker, but it's empty except for some white coats.\nSeems like someone already went through it...",
                                    "",
                                    () -> {},
                                    who.id());
                              })));
              Game.add(locker);
            });

    List.of(desk0, desk1, printer).forEach(Game::add);
  }

  private void setupPapers() {
    Game.levelEntities(Set.of(DecoComponent.class))
        .forEach(
            e -> {
              DecoComponent dc = e.fetch(DecoComponent.class).orElseThrow();
              if (dc.type() == Deco.SheetWritten1 || dc.type() == Deco.SheetWritten2) {
                e.fetch(CollideComponent.class)
                    .ifPresent(
                        cc -> {
                          cc.isSolid(false);
                        });
                e.fetch(DrawComponent.class)
                    .ifPresent(
                        drawComp -> {
                          DrawSystem.getInstance()
                              .changeEntityDepth(e, DepthLayer.BackgroundDeco.depth());
                        });
              }
            });
  }

  private void setupPC() {
    pc = new Entity("pc-main");
    PositionComponent positionComp =
        new PositionComponent(getPoint("pc-main").translate(-0.7f, -0.9f));
    pc.add(positionComp);
    pc.add(new CollideComponent(new Rectangle(2.0f, 1.3f, 0.8f, 1f)));

    Map<String, Animation> animationMap =
        Animation.loadAnimationSpritesheet(new SimpleIPath("objects/desk_with_pc"));
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
    computerState.add(new ComputerStateComponent(ComputerProgress.OFF, false, null, 0));
    Game.add(computerState);

    // Power switch (hidden under papers)
    Entity paper = DecoFactory.createDeco(getPoint("paper-switch"), Deco.SheetWritten2);
    paper.remove(DecoComponent.class);
    paper.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (e, who) -> {
                      if (!dc.currentStateName().equals(PC_STATE_OFF)) return;
                      DialogFactory.showYesNoDialog(
                          "There is a switch hidden below these stacks of paper.\n\nDo you want to flip it?",
                          "",
                          () -> {
                            Sounds.play(CoreSounds.SETTINGS_TOGGLE_CLICK, 1, 1.5f);
                            DialogFactory.showOkDialog(
                                "You flipped the switch.\n\nYou can hear electricity buzzing throughout the room,\nas a few partly broken lights turn on.",
                                "",
                                () -> {
                                  ComputerStateComponent.setState(ComputerProgress.ON);
                                  Sounds.play(LastHourSounds.ELECTRICITY_TURNED_ON, 1, 1.0f);
                                },
                                who.id());
                          },
                          () -> {},
                          who.id());
                    })));
    Game.add(paper);

    Entity profilePaper = DecoFactory.createDeco(getPoint("profile-paper"), Deco.SheetWritten1);
    profilePaper.remove(DecoComponent.class);
    profilePaper.remove(CollideComponent.class);
    DrawSystem.getInstance().changeEntityDepth(profilePaper, DepthLayer.AbovePlayer.depth());
    profilePaper.add(
        new InteractionComponent(
            () ->
                new Interaction(
                    (e, who) -> {
                      DialogUtils.showImagePopUp("images/scientist_profile.png", who.id());
                    })));
    Game.add(profilePaper);
  }

  private static final Deco[] trashcans = {Deco.TrashCanBlue, Deco.TrashCanGreen, Deco.TrashCanRed};
  private static final String trashNote = "images/note-password-2.png";
  private static final List<Integer> PaperCounts = List.of(50, 10, 50, 50, 5, 15, 3);
  private static final int trashIndex = 3;

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
                                builder.put(
                                    TrashMinigameUI.KEY_PAPER_COUNT,
                                    PaperCounts.get(index % PaperCounts.size()));
                                DialogFactory.show(builder.build(), who.id());
                              })));
              Game.add(trashcan);
            });
  }

  private final List<String> decoderTablePaths =
      List.of(
          "images/random_nothing.png",
          "images/binary_hex.jpg",
          "images/hex_ascii.png",
          "images/braille.png",
          "images/decoder_74155.png",
          "images/morse.png");

  private void setupDecoderShelfs() {
    listPointsIndexed("decode")
        .forEach(
            t -> {
              Point p = t.a();
              int index = t.b();
              Entity decoderShelf = DecoFactory.createDeco(p, Deco.BookshelfLarge);
              decoderShelf.remove(DecoComponent.class);
              decoderShelf.add(
                  new InteractionComponent(
                      () ->
                          new Interaction(
                              (e, who) -> {
                                DialogContext.Builder builder =
                                    DialogContext.builder().type(DialogType.DefaultTypes.IMAGE);
                                builder.put(
                                    DialogContextKeys.IMAGE,
                                    decoderTablePaths.get(index % decoderTablePaths.size()));
                                DialogFactory.show(builder.build(), who.id());
                              })));
              Game.add(decoderShelf);
            });
  }

  @Override
  protected void onTick() {
    checkPCStateUpdate();
    Game.allPlayers().filter(p -> !INTRO_SHOWN_TO.contains(p.id())).forEach(p -> showIntro(p.id()));
    if (!Game.isHeadless()) {
      checkInteractFeedback();
      updateLightingShader(EntityUtils.getPosition(pc), getPoint("timer"), keypad);
    }
  }

  static void updateLightingShader(Point pcPos, Point timerPos, Entity keypad) {
    if (!(DrawSystem.getInstance().sceneShaders().get("lighting") instanceof LightingShader ls))
      return;

    ls.clearLightSources();

    if (ComputerStateComponent.getState().isPresent()) {
      var state = ComputerStateComponent.getState().get();
      if (state.state().hasReached(ComputerProgress.ON)) {
        ls.ambientLight(0.2f);
        Color color = state.isInfected() ? Color.RED : Color.BLUE;
        float intensity = state.isInfected() ? 0.8f : 0.5f;
        ls.addLightSource(pcPos, intensity, color);

        ls.addLightSource(timerPos.translate(0.75f, 0), 0.5f, Color.RED);

        var keyComp = keypad.fetch(KeypadComponent.class).orElseThrow();
        Color keypadColor = keyComp.isUnlocked() ? Color.GREEN : Color.RED;
        ls.addLightSource(
            Game.positionOf(keypad).orElse(new Point(0, 0)).translate(0.5f, 0.5f),
            0.3f,
            keypadColor);
      }
    }

    Game.allPlayers().forEach(e -> ls.addLightSource(EntityUtils.getPosition(e), 1f));
  }

  private void checkPCStateUpdate() {
    if (ComputerStateComponent.getState().isEmpty()) {
      LOGGER.warn("ComputerStateComponent is missing from level, cannot update PC state.");
      return;
    }

    ComputerStateComponent csc = ComputerStateComponent.getState().get();
    if (cscLastTick == null) {
      cscLastTick = csc;
      return; // Skip update check on first tick.
    }

    DrawComponent dc = pc.fetch(DrawComponent.class).orElseThrow();
    if (!pcStateToDCState(cscLastTick).equals(pcStateToDCState(csc))) {
      // Update local state to match shared state
      if (csc.isInfected()) {
        dc.sendSignal(PC_SIGNAL_INFECT);
        Sounds.play(LastHourSounds.COMPUTER_VIRUS_CAUGHT, 1, 1.0f);
      } else {
        if (csc.state() == ComputerProgress.ON) {
          dc.sendSignal(PC_SIGNAL_ON);
        } else {
          dc.sendSignal(PC_SIGNAL_CLEAR);
          Sounds.play(CoreSounds.INTERFACE_BUTTON_BACKWARD);
        }
      }
    }

    // Check if a new blog comment has become visible and play a notification sound
    if (csc.state().hasReached(ComputerProgress.LOGGED_IN)) {
      int currentVisibleComments = BlogTab.countVisibleComments();
      if (currentVisibleComments > lastKnownVisibleCommentCount) {
        Sounds.play(LastHourSounds.COMPUTER_COMMENT_RECEIVED);
        Game.add(
            EmoteFactory.createEmote(getPoint("pc-main").translate(0.5f, 2f), Emote.IDEA, 4000));
      }
      lastKnownVisibleCommentCount = currentVisibleComments;
    }

    if (cscLastTick != csc) {
      ComputerDialog.getInstance().ifPresent(cd -> cd.updateState(csc));
    }

    cscLastTick = csc;
  }

  static List<Entity> currentInteractablesInRange = new ArrayList<>();

  static void checkInteractFeedback() {
    Game.player()
        .ifPresent(
            p -> {
              List<Entity> interactables = HeroController.findInteractablesInRange(p);
              Optional<Entity> closest =
                  HeroController.findInteractable(p, SkillTools.cursorPositionAsPoint());

              currentInteractablesInRange.forEach(LastHourLevel::removeInteractFeedback);
              currentInteractablesInRange.clear();
              interactables.forEach(
                  e -> addInteractFeedback(e, closest.isPresent() && e.id() == closest.get().id()));
              currentInteractablesInRange.addAll(interactables);
            });
  }

  static void removeInteractFeedback(Entity entity) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.shaders().remove("outline");
            });
  }

  static void addInteractFeedback(Entity entity, boolean isImportant) {
    Color color = isImportant ? new Color(0.8f, 0, 0, 1f) : new Color(0.8f, 0.7f, 0, 0.4f);
    entity
        .fetch(DrawComponent.class)
        .ifPresent(
            dc -> {
              dc.shaders().add("outline", new OutlineShader(1, color));
            });
  }

  private String pcStateToDCState(ComputerStateComponent csc) {
    if (csc.isInfected()) return PC_STATE_VIRUS;
    if (csc.state() == ComputerProgress.OFF) return PC_STATE_OFF;
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
