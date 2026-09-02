package rooms.lasthour.level;

import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.DrawComponent;
import engine.components.InputComponent;
import engine.components.PositionComponent;
import engine.components.VelocityComponent;
import engine.level.DungeonLevel;
import engine.level.elements.tile.DoorTile;
import engine.level.utils.DesignLabel;
import engine.level.utils.LevelElement;
import engine.sound.CoreSounds;
import engine.sound.Sounds;
import engine.systems.DrawSystem;
import engine.utils.Point;
import engine.utils.Rectangle;
import engine.utils.Tuple;
import engine.utils.Vector2;
import engine.utils.components.draw.DepthLayer;
import engine.utils.components.draw.animation.Animation;
import engine.utils.components.draw.state.State;
import engine.utils.components.draw.state.StateMachine;
import engine.utils.components.path.SimpleIPath;
import engine.utils.logging.DungeonLogger;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.CharacterClassComponent;
import feature.components.CollideComponent;
import feature.components.Debugger;
import feature.components.DecoComponent;
import feature.components.InventoryComponent;
import feature.emote.Emote;
import feature.emote.EmoteFactory;
import feature.entities.CharacterClass;
import feature.entities.WorldItemBuilder;
import feature.entities.deco.Deco;
import feature.entities.deco.DecoFactory;
import feature.hud.DialogUtils;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogContextKeys;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.interaction.Interaction;
import feature.interaction.InteractionComponent;
import feature.interaction.keypad.KeypadComponent;
import feature.interaction.keypad.KeypadFactory;
import feature.inventory.Item;
import feature.inventory.items.HintItem;
import feature.puzzle.Puzzle;
import feature.puzzle.PuzzleMaker;
import feature.systems.EventScheduler;
import feature.systems.LevelEditorSystem;
import feature.timer.WorldTimerFactory;
import feature.utils.EntityUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import rooms.lasthour.modules.computer.ComputerDialog;
import rooms.lasthour.modules.computer.ComputerFactory;
import rooms.lasthour.modules.computer.ComputerProgress;
import rooms.lasthour.modules.computer.ComputerStateComponent;
import rooms.lasthour.modules.computer.content.BlogTab;
import rooms.lasthour.modules.trash.TrashMinigameFactory;
import rooms.lasthour.modules.usbstick.UsbStickColor;
import rooms.lasthour.modules.usbstick.UsbStickItem;
import rooms.lasthour.starter.LastHourClient;
import rooms.lasthour.util.LastHourAchievements;
import rooms.lasthour.util.LastHourPuzzle;
import rooms.lasthour.util.LastHourQuestLogUtil;
import rooms.lasthour.util.LastHourSounds;
import rooms.lasthour.util.LastHourTracking;
import rooms.lasthour.util.Lore;
import rooms.lasthour.util.shaders.LightingShader;
import rooms.lasthour.util.translation.TranslationKey;

/** The Last Hour Room. */
public class LastHourLevel extends DungeonLevel {
  private static final DungeonLogger LOGGER = DungeonLogger.getLogger(LastHourLevel.class);
  private static LastHourLevel Instance = null;

  private DoorTile storageDoor;
  private DoorTile exitDoor;
  private Entity pc;
  private Entity r2Phone;
  private Entity ringingPhoneEmote;
  private boolean isPhoneRinging = false;
  private String ringingPhoneDialog = "";
  private Runnable onCurrentPhoneCallResolved;
  private boolean firstPhoneCallTriggered = false;
  private boolean secondPhoneCallScheduled = false;
  private ComputerStateComponent cscLastTick;
  private Entity keypad;
  private int lastKnownVisibleCommentCount = 0;
  private final Set<Integer> usbCollectorWatchedPlayers = new HashSet<>();

  /** The state of the PC when it's off. */
  public static final String PC_STATE_OFF = "off";

  private static final String PC_STATE_ON = "on";
  private static final String PC_STATE_VIRUS = "virus";
  private static final String PC_SIGNAL_ON = "on";
  private static final String PC_SIGNAL_INFECT = "infect";
  private static final String PC_SIGNAL_CLEAR = "clear";
  private static final int PHONE_RINGING_EMOTE_DURATION_MS = 60 * 60 * 1000;
  private static final long FIRST_PHONE_RING_DELAY_MS = 30_000L;
  private static final long SECOND_PHONE_RING_DELAY_MS = 45_000L;

  private static Puzzle puzzle;

  private static final Set<Integer> INTRO_SHOWN_TO = new HashSet<>();
  private static boolean timerExpired = false;

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
    timerExpired = false;
    LastHourQuestLogUtil.initializeQuestLog();

    storageDoor = (DoorTile) tileAt(getPoint("door-storage")).orElseThrow();
    storageDoor.close();

    DoorTile entryDoor = (DoorTile) tileAt(getPoint("door-entry")).orElseThrow();
    entryDoor.close();
    this.exitDoor = entryDoor;

    keypad =
        KeypadFactory.createKeypad(
            getPoint("keypad-storage"),
            Lore.DoorCode,
            () -> {
              storageDoor.open();
              LastHourQuestLogUtil.addStorageRoomQuestLogEntry();
              LastHourQuestLogUtil.addDoorCodeQuestLogEntry();
              EventScheduler.scheduleAction(this::triggerFirstPhoneCall, FIRST_PHONE_RING_DELAY_MS);
            },
            true);
    keypad
        .fetch(KeypadComponent.class)
        .ifPresent(
            component -> {
              component.onCorrectCode(
                  player -> {
                    LastHourTracking.attempt(
                        LastHourPuzzle.STORAGE_ACCESS,
                        "storage-keypad",
                        "numeric-code",
                        component.enteredDigits().stream()
                            .map(String::valueOf)
                            .collect(java.util.stream.Collectors.joining()),
                        true,
                        player);
                    LastHourTracking.solved(LastHourPuzzle.STORAGE_ACCESS);
                    LastHourTracking.started(LastHourPuzzle.BLUE_USB);
                    LastHourAchievements.trigger(player, LastHourAchievements.KEYPAD_CODE);
                  });
              component.onWrongCode(
                  player -> {
                    LastHourTracking.attempt(
                        LastHourPuzzle.STORAGE_ACCESS,
                        "storage-keypad",
                        "numeric-code",
                        component.enteredDigits().stream()
                            .map(String::valueOf)
                            .collect(java.util.stream.Collectors.joining()),
                        false,
                        player);
                    LastHourAchievements.checkBruteforce(player, component.wrongCodeAttempts());
                  });
            });
    Game.add(keypad);

    setupPC();
    setupTrashcans();
    setupDecoderShelfs();
    setupPapers();
    setupInteractables();
    if (!Game.isHeadless()) setupLightingShader();
    setupTimer();
    setupEndTrigger();
    setupR2Decorations();
    setupR1Vents();
    setupR2DecoyContainers();
    setupUsbSticks();

    EventScheduler.scheduleAction(this::playAmbientSound, 10 * 1000);
  }

  private void showIntro(int targetId) {
    LastHourQuestLogUtil.addIntroInvestigationQuestLogEntries();
    BlackFadeCutscene.show(
        Lore.IntroTexts,
        false,
        true,
        true,
        () ->
            DialogFactory.showDialogDialog(
                "[speaker img=logo/cat_logo_64x64.png name=\"[color=#aaaaaa][size=25]...\"]"
                    + TranslationKey.PostIntroDialogText1
                    + "[p]"
                    + TranslationKey.PostIntroDialogText2,
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
                            LastHourQuestLogUtil.addEscapeQuestLogEntries();
                            LastHourAchievements.trigger(
                                other,
                                timerExpired
                                    ? LastHourAchievements.ESCAPED_TOO_LATE
                                    : LastHourAchievements.ESCAPED_IN_TIME);
                            BlackFadeCutscene.show(
                                endingLoreTexts(), true, false, true, Game::complete);
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
    Game.add(WorldTimerFactory.createWorldTimer(getPoint("timer"), unixTime, 60 * 60));
  }

  /**
   * Called once, locally, when the world timer reaches zero. Plays a recording of Dr. Mertens
   * through his office security system speakers, informing the local player that all of his data is
   * now being automatically destroyed.
   *
   * <p>This is triggered locally on each client, so it only ever targets the local player.
   */
  public static void onTimerExpired() {
    timerExpired = true;
    Game.player()
        .ifPresent(
            player ->
                DialogFactory.showDialogDialog(
                    TranslationKey.TimerExpiredRecording, () -> {}, player.id()));
  }

  private static List<Tuple<String, Integer>> endingLoreTexts() {
    return timerExpired ? Lore.BadOutroTexts : Lore.OutroTexts;
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
            new Interaction(
                (e, who) -> {
                  DialogFactory.showOkDialog(TranslationKey.DeskNothing_1, "", () -> {}, who.id());
                })));

    Entity desk1 = DecoFactory.createDeco(getPoint("desk-nothing1"), Deco.WritingTable);
    desk1.remove(DecoComponent.class);
    Item passwordNote1 = new HintItem(new SimpleIPath("images/note-password-1.png"));
    InventoryComponent desk1Inv = new InventoryComponent();
    desk1Inv.add(passwordNote1);
    desk1.add(desk1Inv);
    desk1.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogFactory.showOkDialog(
                      TranslationKey.DeskNothing_2, "", () -> openDualInventory(e, who), who.id());
                })));

    Entity printer = DecoFactory.createDeco(getPoint("printer"), Deco.Printer2);
    printer.remove(DecoComponent.class);
    printer.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogFactory.showOkDialog(TranslationKey.Printer, "", () -> {}, who.id());
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
                      new Interaction(
                          (e, who) -> {
                            if (index == 2) {
                              LastHourQuestLogUtil.addTrustCarefullyQuestLogEntry();
                              DialogFactory.showOkDialog(
                                  TranslationKey.LockerFind,
                                  "",
                                  () -> {
                                    DialogUtils.showImagePopUp(cabinetImagePath, who.id());
                                  },
                                  who.id());
                              return;
                            }
                            DialogFactory.showOkDialog(
                                TranslationKey.LockerEmpty, "", () -> {}, who.id());
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
    computerState.add(ComputerStateComponent.of(ComputerProgress.OFF, false, null, 0));
    Game.add(computerState);

    // Power switch (hidden under papers)
    Entity paper = DecoFactory.createDeco(getPoint("paper-switch"), Deco.SheetWritten2);
    paper.remove(DecoComponent.class);
    paper.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  LastHourTracking.started(LastHourPuzzle.POWER);
                  if (!dc.currentStateName().equals(PC_STATE_OFF)) return;
                  DialogFactory.showYesNoDialog(
                      TranslationKey.LightSwitch_1,
                      "",
                      () -> {
                        Sounds.play(CoreSounds.SETTINGS_TOGGLE_CLICK, 1, 1.5f);
                        DialogFactory.showOkDialog(
                            TranslationKey.LightSwitch_2,
                            "",
                            () -> {
                              ComputerStateComponent.setState(ComputerProgress.ON);
                              LastHourTracking.solved(LastHourPuzzle.POWER);
                              LastHourTracking.started(LastHourPuzzle.LOGIN);
                              LastHourAchievements.trigger(who, LastHourAchievements.LIGHTS_ON);
                              LastHourQuestLogUtil.addPowerSwitchQuestLogEntry();
                              LastHourQuestLogUtil.addInvestigatePcQuestLogEntry();
                              Sounds.play(LastHourSounds.ELECTRICITY_TURNED_ON, 1, 1.0f);
                            },
                            who.id());
                      },
                      () -> {},
                      who.id());
                })));
    Game.add(paper);

    Debugger.addAction(
        () -> {
          ComputerStateComponent.setState(ComputerProgress.ON);
          Sounds.play(LastHourSounds.ELECTRICITY_TURNED_ON, 1, 1.0f);
          r2SpawnPapers();
        });

    Entity profilePaper = DecoFactory.createDeco(getPoint("profile-paper"), Deco.SheetWritten1);
    profilePaper.remove(DecoComponent.class);
    profilePaper.remove(CollideComponent.class);
    DrawSystem.getInstance().changeEntityDepth(profilePaper, DepthLayer.AbovePlayer.depth());
    profilePaper.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  LastHourQuestLogUtil.addProfilePaperQuestLogEntry();
                  DialogUtils.showImagePopUp("images/scientist_profile.png", who.id());
                })));
    Game.add(profilePaper);
  }

  private static final Deco[] trashcans = {Deco.TrashCanBlue, Deco.TrashCanGreen, Deco.TrashCanRed};
  private static final String trashNote = "images/note-password-2.png";
  private static final List<Integer> PaperCounts = List.of(50, 10, 50, 50, 5, 15, 3);
  private static final int trashIndex = 3;

  private void setupTrashcans() {
    listPointsIndexed("trash")
        .forEach(
            t -> {
              Point p = t.a();
              int index = t.b();
              Deco deco = trashcans[index % trashcans.length];
              Entity trashcan = DecoFactory.createDeco(p, deco);
              trashcan.remove(DecoComponent.class);
              int paperCount = PaperCounts.get(index % PaperCounts.size());
              boolean hasReward = index == trashIndex;

              final boolean[] awarded = {false};
              trashcan.add(
                  new InteractionComponent(
                      new Interaction(
                          (eInteract, who) -> {
                            if (!hasReward || awarded[0]) {
                              showTrashMinigame(who, null, paperCount, null);
                              return;
                            }
                            Item reward = new HintItem(new SimpleIPath(trashNote));
                            showTrashMinigame(
                                who,
                                reward,
                                paperCount,
                                () -> {
                                  if (awarded[0]) return;
                                  awarded[0] = true;
                                  LastHourQuestLogUtil.addTrashNoteQuestLogEntry();
                                });
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
                      new Interaction(
                          (e, who) -> {
                            DialogContext.Builder builder =
                                DialogContext.builder().type(DialogType.DefaultTypes.IMAGE);
                            LastHourQuestLogUtil.addDecoderShelvesQuestLogEntry();
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

  // Puzzle definition for the r2-papers puzzle. Shared between the server (which spawns the
  // world items in r2SpawnPapers) and the client (which pre-generates the matching textures
  // in ensureClientPuzzles) so both derive the same deterministic puzzle id.
  private static final SimpleIPath R2_PUZZLE_IMAGE = new SimpleIPath("images/final-code.png");
  private static final int R2_PUZZLE_PIECE_COUNT = 4;
  private static final long R2_PUZZLE_SEED = 1586791695537379744L;

  /**
   * Pre-creates every {@link Puzzle} this level uses so the {@code @gen/puzzle/<id>/<i>.png}
   * textures are materialized in the {@link engine.utils.components.draw.TextureMap} before any
   * network message references them. Must be called on the libGDX render thread.
   */
  public static void ensureClientPuzzles() {
    PuzzleMaker.makePuzzle(R2_PUZZLE_IMAGE, R2_PUZZLE_PIECE_COUNT, null, R2_PUZZLE_SEED, false);
  }

  /**
   * Spawns four paper entities at the {@code r2-vent} named point and gives each an initial impulse
   * so they spread out.
   */
  public void r2SpawnPapers() {
    LastHourTracking.started(LastHourPuzzle.EXIT_CODE_ASSEMBLY);
    puzzle =
        PuzzleMaker.makePuzzle(
            R2_PUZZLE_IMAGE,
            R2_PUZZLE_PIECE_COUNT,
            (solvedPuzzle, solver) -> {
              LastHourTracking.solved(LastHourPuzzle.EXIT_CODE_ASSEMBLY);
              LastHourTracking.started(LastHourPuzzle.EXIT);
              LastHourQuestLogUtil.addFinalCodeQuestLogEntry();
              solvedPuzzle.removeItems(solver);
              if (solver != null) {
                solver
                    .fetch(InventoryComponent.class)
                    .ifPresent(inv -> inv.add(new HintItem(solvedPuzzle.imagePath())));
              }
            },
            R2_PUZZLE_SEED,
            false);

    Point ventPos = getPoint("r2-vent");
    float s = R2_PAPER_SPEED;

    Vector2[] impulses = {
      Vector2.of(-s, 1.5f * -s),
      Vector2.of(s, 1.5f * -s),
      Vector2.of(0, -2.0f * s),
      Vector2.of(0, -1.0f * s)
    };

    for (int i = 0; i < impulses.length; i++) {
      Item item = puzzle.items().get(i);
      Entity paper = WorldItemBuilder.buildWorldItem(item, ventPos.translate(0, -0.25f));
      paper.fetch(CollideComponent.class).ifPresent(cc -> cc.isSolid(false));
      paper.fetch(DrawComponent.class).ifPresent(dc -> dc.depth(DepthLayer.BackgroundDeco.depth()));

      VelocityComponent vc = new VelocityComponent(R2_PAPER_MAX_SPEED, 25f);
      vc.applyForce("vent-impulse", impulses[i]);
      paper.add(vc);

      Game.add(paper);
    }
  }

  /**
   * Sets up decorative interactables in room 2: an air-conditioner-style floor vent on top of the
   * {@code r2-vent} point and a writing table on {@code r2-desk}, both with interaction dialogs.
   */
  private void setupR2Decorations() {
    Entity vent = DecoFactory.createDeco(getPoint("r2-vent"), Deco.FloorBarsSmall);
    vent.remove(DecoComponent.class);
    vent.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogFactory.showOkDialog(TranslationKey.VentDialog, "", () -> {}, who.id());
                })));
    Game.add(vent);

    Entity desk = DecoFactory.createDeco(getPoint("r2-desk"), Deco.WritingTable);
    desk.remove(DecoComponent.class);
    desk.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogFactory.showOkDialog(TranslationKey.R2DeskNoteText, "", () -> {}, who.id());
                  LastHourQuestLogUtil.addUsbColorHintQuestLogEntry();
                })));
    Game.add(desk);

    setupPhone();
  }

  /**
   * Sets up the two decoy vents in room 1 at the {@code r1-vent0} and {@code r1-vent1} points. They
   * look identical to the real vent and are interactable, but their dialog only reveals a partial,
   * scratched-off serial number ending in three dashes.
   */
  private void setupR1Vents() {
    String[] points = {"r1-vent0", "r1-vent1"};
    Entity decoyVent1 = DecoFactory.createDeco(getPoint(points[0]), Deco.FloorBarsSmall);
    decoyVent1.remove(DecoComponent.class);
    decoyVent1.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogFactory.showOkDialog(
                      TranslationKey.DecoyVentDialog1, "", () -> {}, who.id());
                })));
    Game.add(decoyVent1);

    Entity decoyVent2 = DecoFactory.createDeco(getPoint(points[1]), Deco.FloorBarsSmall);
    decoyVent2.remove(DecoComponent.class);
    decoyVent2.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  DialogFactory.showOkDialog(
                      TranslationKey.DecoyVentDialog2, "", () -> {}, who.id());
                })));
    Game.add(decoyVent2);
  }

  /**
   * Sets up two decoy containers in room 2: a shelf at {@code r2-decoy-shelf} and a cabinet at
   * {@code r2-decoy-cabinet}. Both are interactable and open an (empty) inventory on interaction.
   */
  private void setupR2DecoyContainers() {
    Entity decoyShelf = DecoFactory.createDeco(getPoint("r2-decoy-shelf"), Deco.BookshelfLarge);
    decoyShelf.remove(DecoComponent.class);
    addInventory(decoyShelf, List.of());
    Game.add(decoyShelf);

    Entity decoyCabinet = DecoFactory.createDeco(getPoint("r2-decoy-cabinet"), Deco.Cabinet);
    decoyCabinet.remove(DecoComponent.class);
    addInventory(decoyCabinet, List.of());
    Game.add(decoyCabinet);
  }

  private void setupPhone() {
    r2Phone = DecoFactory.createDeco(getPoint("r2-phone"), Deco.Phone);
    r2Phone.remove(DecoComponent.class);
    Game.add(r2Phone);
    DrawSystem.getInstance().changeEntityDepth(r2Phone, DepthLayer.AbovePlayer.depth());
    updatePhoneInteraction();
  }

  private void triggerFirstPhoneCall() {
    if (firstPhoneCallTriggered) return;
    firstPhoneCallTriggered = true;
    ringPhone(TranslationKey.Ringing1, this::scheduleSecondPhoneCall);
  }

  private void scheduleSecondPhoneCall() {
    if (secondPhoneCallScheduled) return;
    secondPhoneCallScheduled = true;
    EventScheduler.scheduleAction(
        () -> ringPhone(TranslationKey.Ringing2), SECOND_PHONE_RING_DELAY_MS);
  }

  /**
   * Triggers phone ringing and sets the dialog shown when answering the call.
   *
   * @param dialogText dialog script to display when the phone is answered
   */
  public void ringPhone(String dialogText) {
    ringPhone(dialogText, null);
  }

  private void ringPhone(String dialogText, Runnable onResolved) {
    isPhoneRinging = true;
    ringingPhoneDialog = dialogText;
    onCurrentPhoneCallResolved = onResolved;
    Sounds.play(LastHourSounds.PHONE_RINGING);
    updatePhoneInteraction();

    if (ringingPhoneEmote == null) {
      ringingPhoneEmote =
          EmoteFactory.createEmote(
              EntityUtils.getPosition(r2Phone), Emote.EXCLAMATION, PHONE_RINGING_EMOTE_DURATION_MS);
      Game.add(ringingPhoneEmote);
    }
  }

  private void stopPhoneRinging() {
    isPhoneRinging = false;
    ringingPhoneDialog = "";
    Runnable resolvedCallback = onCurrentPhoneCallResolved;
    onCurrentPhoneCallResolved = null;
    updatePhoneInteraction();
    if (ringingPhoneEmote != null) {
      Game.remove(ringingPhoneEmote);
      ringingPhoneEmote = null;
    }
    if (resolvedCallback != null) {
      resolvedCallback.run();
    }
  }

  private void updatePhoneInteraction() {
    if (r2Phone == null) return;

    r2Phone.remove(InteractionComponent.class);
    r2Phone.add(
        new InteractionComponent(
            new Interaction(
                (e, who) -> {
                  if (isPhoneRinging) {
                    DialogFactory.showDialogDialog(
                        ringingPhoneDialog, portraitPathFor(who), this::stopPhoneRinging, who.id());
                    return;
                  }

                  DialogFactory.showOkDialog(
                      TranslationKey.PhoneInteraction, "", () -> {}, who.id());
                })));
  }

  /**
   * Returns the path of the pre-rendered character portrait texture for the given hero entity.
   *
   * @param who The hero entity that interacted with the phone.
   * @return The (virtual) texture path of the portrait, or the default speaker image if the
   *     character class is unknown / has no portrait.
   */
  private static String portraitPathFor(Entity who) {
    CharacterClass cc =
        who.fetch(CharacterClassComponent.class)
            .map(CharacterClassComponent::characterClass)
            .orElse(null);
    if (cc == null) return "other/unknown.png";
    return switch (cc) {
      case THE_LAST_HOUR_ROGUE -> LastHourClient.ROGUE_PORTRAIT_PATH;
      case THE_LAST_HOUR_CHAR03 -> LastHourClient.CHAR03_PORTRAIT_PATH;
      default -> "other/unknown.png";
    };
  }

  /** Places the 4 colored USB sticks. */
  private void setupUsbSticks() {
    Item redUsb = UsbStickItem.createUsbStickItem(UsbStickColor.Red);
    Item yellowUsb = UsbStickItem.createUsbStickItem(UsbStickColor.Yellow);
    Item greenUsb = UsbStickItem.createUsbStickItem(UsbStickColor.Green);
    Item blueUsb = UsbStickItem.createUsbStickItem(UsbStickColor.Blue);

    Entity folder = DecoFactory.createDeco(getPoint("r2-folder"), Deco.FolderRed);
    folder.remove(DecoComponent.class);
    addInventory(folder, List.of(redUsb));
    Game.add(folder);

    Entity shelf = DecoFactory.createDeco(getPoint("r2-shelf"), Deco.BookshelfLarge);
    shelf.remove(DecoComponent.class);
    addInventory(shelf, List.of(greenUsb));
    Game.add(shelf);

    Entity usbOnTable = WorldItemBuilder.buildWorldItem(yellowUsb, getPoint("r2-usb-on-table"));
    DrawSystem.getInstance().changeEntityDepth(usbOnTable, DepthLayer.AbovePlayer.depth());
    Game.add(usbOnTable);

    Entity blueTrash = DecoFactory.createDeco(getPoint("r2-trash"), Deco.TrashCanBlue);
    blueTrash.remove(DecoComponent.class);
    final boolean[] awarded = {false};
    blueTrash.add(
        new InteractionComponent(
            new Interaction(
                (eInteract, who) -> {
                  if (awarded[0]) {
                    showTrashMinigame(who, null, 30, null);
                    return;
                  }
                  Item reward = UsbStickItem.createUsbStickItem(UsbStickColor.Blue);
                  showTrashMinigame(
                      who,
                      reward,
                      30,
                      () -> {
                        if (awarded[0]) return;
                        awarded[0] = true;
                        LastHourQuestLogUtil.addUsbSearchQuestLogEntry();
                      });
                })));
    Game.add(blueTrash);
  }

  private void showTrashMinigame(Entity who, Item reward, int paperCount, Runnable afterAward) {
    LastHourAchievements.trigger(who, LastHourAchievements.TRASH_DIVER);
    TrashMinigameFactory.show(who, reward, paperCount, afterAward);
  }

  @Override
  protected void onTick() {
    checkPCStateUpdate();
    Game.allPlayers().filter(p -> !INTRO_SHOWN_TO.contains(p.id())).forEach(p -> showIntro(p.id()));
    registerUsbCollectorHooks();
    if (!Game.isHeadless()) {
      updateLightingShader(EntityUtils.getPosition(pc), getPoint("timer"), keypad);
    }
  }

  private void registerUsbCollectorHooks() {
    Game.allPlayers()
        .filter(player -> !usbCollectorWatchedPlayers.contains(player.id()))
        .forEach(this::registerUsbCollectorHook);
  }

  private void registerUsbCollectorHook(Entity player) {
    player
        .fetch(InventoryComponent.class)
        .ifPresent(
            inventory -> {
              Consumer<Item> previousOnItemAdded = inventory.onItemAdded();
              inventory.onItemAdded(
                  item -> {
                    previousOnItemAdded.accept(item);
                    if (item instanceof UsbStickItem.BaseUsbStick) {
                      LastHourAchievements.checkAllUsbSticks();
                    }
                  });
              usbCollectorWatchedPlayers.add(player.id());
              LastHourAchievements.checkAllUsbSticks();
            });
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
        if (state.lightsOn()) {
          ls.ambientLight(0.2f);
        }
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
    String prevDcState = pcStateToDCState(cscLastTick);
    String newDcState = pcStateToDCState(csc);
    if (!prevDcState.equals(newDcState)) {
      // Drive the DrawComponent state machine based on the actual transition, not just the
      // resulting state. The state machine only knows:
      //   OFF -PC_SIGNAL_ON-> ON, ON -PC_SIGNAL_INFECT-> VIRUS, VIRUS -PC_SIGNAL_CLEAR-> ON.
      if (newDcState.equals(PC_STATE_VIRUS)) {
        dc.sendSignal(PC_SIGNAL_INFECT);
        Sounds.play(LastHourSounds.COMPUTER_VIRUS_CAUGHT, 1, 1.0f);
      } else if (prevDcState.equals(PC_STATE_VIRUS) && newDcState.equals(PC_STATE_ON)) {
        dc.sendSignal(PC_SIGNAL_CLEAR);
        Sounds.play(CoreSounds.INTERFACE_BUTTON_BACKWARD);
      } else if (prevDcState.equals(PC_STATE_OFF) && newDcState.equals(PC_STATE_ON)) {
        dc.sendSignal(PC_SIGNAL_ON);
      }
    }

    // Check if a new blog comment has become visible and play a notification sound
    if (csc.state().hasReached(ComputerProgress.LOGGED_IN)) {
      int currentVisibleComments = BlogTab.countVisibleComments();
      if (currentVisibleComments > lastKnownVisibleCommentCount) {
        Sounds.play(LastHourSounds.COMPUTER_COMMENT_RECEIVED);
        Game.add(
            EmoteFactory.createEmote(getPoint("pc-main").translate(1, 1.5f), Emote.IDEA, 4000));
      }
      lastKnownVisibleCommentCount = currentVisibleComments;
    }

    if (cscLastTick != csc) {
      ComputerDialog.getInstance().ifPresent(cd -> cd.updateState(csc));
    }

    applyControlPanelChanges(cscLastTick, csc);

    cscLastTick = csc;
  }

  /**
   * Applies any world-affecting control-panel state diffs between the previous and current
   * ComputerStateComponent snapshots.
   *
   * @param prev the previous computer state snapshot
   * @param now the current computer state snapshot
   */
  private void applyControlPanelChanges(ComputerStateComponent prev, ComputerStateComponent now) {
    // Door 1 (storage)
    if (prev.door1Open() != now.door1Open() && storageDoor != null) {
      if (now.door1Open()) storageDoor.open();
      else storageDoor.close();
    }
    // Door 2 (exit)
    if (!prev.door2Open() && now.door2Open() && exitDoor != null) {
      exitDoor.open();
    }
    // AC (one-shot paper spawn on rising edge)
    if (!prev.acOn() && now.acOn() && puzzle == null) {
      r2SpawnPapers();
    }
  }

  private static void addInventory(Entity entity, List<Item> items) {
    InventoryComponent ic = new InventoryComponent();
    items.forEach(ic::add);
    entity.add(ic);
    entity.add(new InteractionComponent(new Interaction(LastHourLevel::openDualInventory)));
  }

  /**
   * Opens the dual-inventory dialog between {@code who} (the hero) and {@code container} (the
   * inventory-bearing entity). The container must already have an {@link InventoryComponent}.
   *
   * @param container the entity whose inventory should be shown on one side
   * @param who the hero entity interacting with the container
   */
  private static void openDualInventory(Entity container, Entity who) {
    DialogContext ctx =
        new DialogContext(
            DialogType.DefaultTypes.DUAL_INVENTORY,
            true,
            Map.of(
                DialogContextKeys.ENTITY, who.id(),
                DialogContextKeys.SECONDARY_ENTITY, container.id()));
    ctx.owner(who.id());
    DialogFactory.show(ctx, who.id());
  }

  private String pcStateToDCState(ComputerStateComponent csc) {
    if (csc.isInfected()) return PC_STATE_VIRUS;
    if (csc.state() == ComputerProgress.OFF) return PC_STATE_OFF;
    return PC_STATE_ON;
  }

  /** Plays ambient sounds at random intervals. */
  private void playAmbientSound() {
    // TODO: Copied from MushRoom, use different sounds.
    // double r = Math.random();
    // if (r < 0.20) {
    //   Sounds.TREE_AMBIENT_CREAK.play();
    // } else if (r < 0.40) {
    //   Sounds.ANIMAL_AMBIENT.play();
    // } else if (r < 0.60) {
    //   Sounds.random(Sounds.WIND_AMBIENT_1, Sounds.WIND_AMBIENT_2, Sounds.WIND_AMBIENT_3);
    // }
    EventScheduler.scheduleAction(this::playAmbientSound, (long) (Math.random() * 10000 + 10000));
  }
}
