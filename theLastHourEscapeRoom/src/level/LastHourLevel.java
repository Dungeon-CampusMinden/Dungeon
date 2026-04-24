package level;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import contrib.components.CollideComponent;
import contrib.components.DecoComponent;
import contrib.entities.WorldItemBuilder;
import contrib.entities.deco.Deco;
import contrib.entities.deco.DecoFactory;
import contrib.hud.DialogUtils;
import contrib.hud.dialogs.ChoiceOption;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.DialogEntry;
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
import contrib.systems.LevelEditorSystem;
import contrib.utils.EntityUtils;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.InputComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.DungeonLevel;
import core.level.elements.tile.DoorTile;
import core.level.utils.DesignLabel;
import core.level.utils.LevelElement;
import core.sound.CoreSounds;
import core.sound.Sounds;
import core.systems.DrawSystem;
import core.utils.CursorUtil;
import core.utils.Cursors;
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
import modules.usbstick.UsbStickColor;
import modules.usbstick.UsbStickItem;
import util.InteractionHelper;
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
    setupR2PaperTrigger();
    setupUsbSticks();

    setupTestMcd();

    EventScheduler.scheduleAction(this::playAmbientSound, 10 * 1000);
  }

  private void setupTestMcd() {
    Entity trigger = new Entity("test-mcd-trigger");

    List<String> options =
        List.of(
            "[img=items/rpg/item_scroll.png] Enchanted Scroll [key code=" + Input.Keys.B + "]",
            "[shake strength=0.6] [img=items/rpg/potion_purple.png] [color=#aa00aa]Mysterious Potion [img=items/rpg/potion_purple.png]",
            "[size=30][img=items/rpg/item_compass.png] Ancient Compass",
            "[img=items/rpg/key1.png] Golden Key",
            "[img=items/rpg/item_magnifying_glass.png] Magnifying"
                + " Glass [img=items/rpg/item_magnifying_glass.png] [img=items/rpg/item_magnifying_glass.png] [img=items/rpg/item_magnifying_glass.png] [img=items/rpg/item_magnifying_glass.png] [img=items/rpg/item_magnifying_glass.png]");
    String question =
        "[tr speed=1.0][word-space=2.0]You found a [n][shake][size=30][color=#aa00aa]mysterious chest[/color][/size][/shake][n][word-space=1.0][pause=0.2][tr speed=1.5] Which [shake strength=0.6 speed=0.2]item[/shake] do you take?[pause=0.5]";
    //    String description =
    //        "[tr speed=1.0]Look at this scroll: [img-block path=items/rpg/item_scroll.png
    // width=80]";
    String description = null;

    trigger.add(new PositionComponent(getPoint("r1-mcd")));
    trigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.ONE,
                (e, other, dir) -> {
                  List<ChoiceOption> opts = ChoiceOption.ofList(options);
                  DialogFactory.showMultipleChoiceDialog(
                      question,
                      null,
                      description,
                      new ArrayList<>(opts),
                      false,
                      payload -> {
                        LOGGER.warn("Option selected: " + payload);
                      },
                      () -> {},
                      other.id());
                },
                null)
            .isSolid(false));
    Game.add(trigger);
  }

  private void showIntro(int targetId) {
    BlackFadeCutscene.show(
        Lore.IntroTexts,
        false,
        true,
        () ->
            DialogFactory.showDialogDialog(
                List.of(
                    DialogEntry.of(
                        "[color=#aaaaaa][size=25]...",
                        "logo/cat_logo_64x64.png",
                        Lore.PostIntroDialogText1),
                    DialogEntry.of(
                        "[color=#aaaaaa][size=25]...",
                        "logo/cat_logo_64x64.png",
                        Lore.PostIntroDialogText2)),
                () -> {},
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

  private static final String R2_PAPER_IMAGE = "images/random_nothing.png";
  private static final float R2_PAPER_SPEED = 45.0f;
  private static final float R2_PAPER_MAX_SPEED = 45.0f;

  /**
   * Sets up a walk-over trigger at {@code r2-paper-trigger} that fires {@link #r2SpawnPapers()}.
   */
  private void setupR2PaperTrigger() {
    Entity trigger = new Entity("r2-paper-trigger");
    trigger.add(new PositionComponent(getPoint("r2-paper-trigger")));
    trigger.add(
        new CollideComponent(
                Vector2.ZERO,
                Vector2.ONE,
                (e, other, dir) -> {
                  other
                      .fetch(InputComponent.class)
                      .ifPresent(
                          ic -> {
                            r2SpawnPapers();
                            Game.remove(trigger);
                          });
                },
                null)
            .isSolid(false));
    Game.add(trigger);

    Debugger.addAction(this::r2SpawnPapers);
  }

  /**
   * Spawns four paper entities at the {@code r2-vent} named point and gives each an initial impulse
   * so they spread out.
   */
  public void r2SpawnPapers() {
    Point ventPos = getPoint("r2-vent");
    float s = R2_PAPER_SPEED;

    Vector2[] impulses = {
      Vector2.of(-s, 1.5f * -s),
      Vector2.of(s, 1.5f * -s),
      Vector2.of(0, -2.0f * s),
      Vector2.of(0, -1.0f * s)
    };

    for (int i = 0; i < impulses.length; i++) {
      Entity paper = new Entity("r2-note");
      paper.add(new PositionComponent(ventPos.translate(0, -0.25f)));
      paper.add(new DrawComponent(new SimpleIPath("items/rpg/item_paper.png")));
      paper.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(false));
      paper.fetch(DrawComponent.class).ifPresent(dc -> dc.depth(DepthLayer.BackgroundDeco.depth()));

      VelocityComponent vc = new VelocityComponent(R2_PAPER_MAX_SPEED, 25f);
      vc.applyForce("vent-impulse", impulses[i]);
      paper.add(vc);

      paper.add(
          new InteractionComponent(
              () ->
                  new Interaction(
                      (e, who) -> {
                        DialogUtils.showImagePopUp(R2_PAPER_IMAGE, who.id());
                      })));

      Game.add(paper);
    }
  }

  /** Places all four colored USB stick items at {@code r2-folders}, spaced 1 tile apart in +x. */
  private void setupUsbSticks() {
    Point origin = getPoint("r2-folders");
    UsbStickColor[] colors = UsbStickColor.values();
    for (int i = 0; i < colors.length; i++) {
      Point pos = origin.translate(i, 0);
      Entity usbEntity =
          WorldItemBuilder.buildWorldItemSimpleInteraction(
              UsbStickItem.createUsbStickItem(colors[i]), pos);
      Game.add(usbEntity);
    }
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

    if (LevelEditorSystem.active()) {
      ls.ambientLight(1.0f);
      return;
    } else {
      ls.ambientLight(0.0f);
    }

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

  /** The entity that currently has the solid (in-range) interaction outline, or {@code null}. */
  private static Entity currentHighlightedEntity = null;

  /** The entity that currently has the semi-transparent (discovery) outline, or {@code null}. */
  private static Entity currentSemiHighlightedEntity = null;

  /** Solid highlight color for the entity that will actually be interacted with. */
  private static final Color HIGHLIGHT_SOLID = new Color(0.8f, 0, 0, 1f);

  /** Semi-transparent highlight color for discoverable but out-of-range entities. */
  private static final Color HIGHLIGHT_SEMI = new Color(0.8f, 0.7f, 0, 0.4f);

  private static final String SHADER_NAME = "highlight_outline";

  /**
   * Cursor-first interaction feedback. The entity nearest to the cursor gets a semi-transparent
   * discovery outline so players can scan the room. If that entity is also within interaction range
   * of the hero, it gets a solid red outline instead, and the world cursor switches to {@link
   * Cursors#INTERACT}.
   */
  static void checkInteractFeedback() {
    Game.player()
        .ifPresent(
            p -> {
              Optional<Entity> nearCursor = InteractionHelper.findCursorNearEntity();
              Optional<Entity> inRange = InteractionHelper.findInteractTarget(p);

              // Clear previous highlights
              clearHighlight(currentHighlightedEntity);
              clearHighlight(currentSemiHighlightedEntity);
              currentHighlightedEntity = null;
              currentSemiHighlightedEntity = null;

              // Apply highlights
              nearCursor.ifPresent(
                  e -> {
                    if (inRange.isPresent() && e.id() == inRange.get().id()) {
                      // Entity is near cursor AND in hero range → solid highlight
                      applyOutline(e, HIGHLIGHT_SOLID);
                      currentHighlightedEntity = e;
                    } else {
                      // Entity is near cursor but out of hero range → semi highlight
                      applyOutline(e, HIGHLIGHT_SEMI);
                      currentSemiHighlightedEntity = e;
                    }
                  });

              // Update world cursor (only when entity is actually interactable)
              updateWorldCursor(inRange.isPresent());
            });
  }

  /**
   * Sets the world cursor to the interact cursor when an interactable is targeted, or clears it
   * otherwise. Uses {@link CursorUtil#setWorldCursor} / {@link CursorUtil#clearWorldCursor} so the
   * Stage input listener respects the override and does not flicker back to DEFAULT every frame.
   *
   * @param hasTarget true if an interactable entity is currently targeted by the cursor
   */
  private static void updateWorldCursor(boolean hasTarget) {
    if (hasTarget) {
      CursorUtil.setWorldCursor(Cursors.INTERACT);
    } else {
      CursorUtil.clearWorldCursor();
    }
  }

  static void clearHighlight(Entity entity) {
    if (entity == null) return;
    entity.fetch(DrawComponent.class).ifPresent(dc -> dc.shaders().remove(SHADER_NAME));
  }

  static void applyOutline(Entity entity, Color color) {
    entity
        .fetch(DrawComponent.class)
        .ifPresent(dc -> dc.shaders().add(SHADER_NAME, new OutlineShader(1, color)));
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
