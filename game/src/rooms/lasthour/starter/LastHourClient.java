package rooms.lasthour.starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import engine.Entity;
import engine.Game;
import engine.components.PlayerComponent;
import engine.components.PositionComponent;
import engine.game.PreRunConfiguration;
import engine.network.ConnectionListener;
import engine.network.messages.s2c.EntitySpawnEvent;
import engine.utils.CursorUtil;
import engine.utils.Tuple;
import engine.utils.components.draw.DrawComponentFactory;
import engine.utils.components.draw.TextureGenerator;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.draw.shader.ColorGradeShader;
import engine.utils.components.draw.shader.HueRemapShader;
import engine.utils.components.draw.shader.ShaderList;
import engine.utils.components.path.SimpleIPath;
import engine.utils.settings.ButtonBindingSetting;
import engine.utils.settings.ClientSettings;
import engine.utils.settings.DescriptionSetting;
import engine.utils.settings.SectionDividerSetting;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.Debugger;
import feature.entities.CharacterClass;
import feature.entities.HeroBuilder;
import feature.hud.dialogs.DialogFactory;
import feature.input.configuration.KeyboardConfig;
import feature.interaction.InteractionComponent;
import feature.puzzle.PuzzleMaker;
import feature.puzzle.PuzzlePieceItem;
import feature.puzzle.PuzzleTextureGenerator;
import feature.questlog.QuestLogUtil;
import feature.systems.AttributeBarSystem;
import feature.systems.PositionSync;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import rooms.lasthour.level.LastHourLevel;
import rooms.lasthour.modules.computer.ComputerFactory;
import rooms.lasthour.modules.computer.ComputerStateSyncSystem;
import rooms.lasthour.modules.computer.LastHourDialogTypes;
import rooms.lasthour.modules.trash.TrashMinigameUI;
import rooms.lasthour.modules.usbstick.UsbStickItem;
import rooms.lasthour.network.LastHourEntitySpawnStrategy;
import rooms.lasthour.network.LastHourSnapshotTranslator;
import rooms.lasthour.util.LastHourAchievements;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class LastHourClient {

  private static final String BACKGROUND_MUSIC = "sounds/forest_bgm.wav";
  private static Music backgroundMusic;

  /** Pre-run registrations for the client (custom dialogs and items). */
  public static void registerClientContent() {
    LastHourAchievements.register();
    registerCustomDialogs();
    UsbStickItem.ensureRegistration();
    PuzzlePieceItem.ensureRegistration();
  }

  /** In-loop client setup (entity spawn handler, systems, connection listener). */
  public static void clientSetup() {
    registerEntitySpawnHandler();
    LastHourLevel.ensureClientPuzzles();
    if (TheLastHour.DEBUG_MODE) {
      Game.add(new Debugger());
    }
    TheLastHour.registerLocalWorldTimerSystem();
    Game.stage().ifPresent(CursorUtil::initListener);
    Game.remove(AttributeBarSystem.class);
    Game.add(new ComputerStateSyncSystem());

    setupMusic();
    staticRenderTextures();

    Game.network()
        .addConnectionListener(
            new ConnectionListener() {
              @Override
              public void onConnected() {
                Game.windowTitle("TheLastHour Client - " + PreRunConfiguration.username());
              }

              @Override
              public void onDisconnected(String reason) {}
            });
  }

  private static void registerCustomDialogs() {
    ComputerFactory.ensureRegistration();
    DialogFactory.register(LastHourDialogTypes.TRASHCAN, TrashMinigameUI::build);
    BlackFadeCutscene.register();
  }

  /**
   * Registers a custom spawn handler that supports metadata-only Last Hour entities and collider
   * synchronization.
   */
  private static void registerEntitySpawnHandler() {
    Game.network()
        .messageDispatcher()
        .registerHandler(
            EntitySpawnEvent.class,
            (ctx, event) -> {
              if (Game.levelEntities().anyMatch(e -> e.id() == event.entityId())) {
                return;
              }

              if (event.playerComponent() != null) {
                if (spawnPlayer(event) && ctx != null) {
                  ctx.clientState().ifPresent(state -> state.trackNetworkEntity(event.entityId()));
                }
                return;
              }

              Entity newEntity = new Entity(event.entityId());
              if (event.positionComponent() != null) {
                newEntity.add(event.positionComponent());
              }
              ensurePuzzlePieceTextures(event.metadata());
              if (event.drawInfo() != null) {
                newEntity.add(DrawComponentFactory.fromDrawInfo(event.drawInfo()));
              }
              if (event.metadata().containsKey(LastHourEntitySpawnStrategy.METADATA_INTERACTABLE)) {
                newEntity.add(new InteractionComponent());
              }
              LastHourSnapshotTranslator.computerStateFromMetadata(event.metadata())
                  .ifPresent(newEntity::add);
              LastHourSnapshotTranslator.keypadStateFromMetadata(event.metadata())
                  .ifPresent(newEntity::add);
              LastHourSnapshotTranslator.worldTimerStateFromMetadata(event.metadata())
                  .ifPresent(newEntity::add);
              LastHourSnapshotTranslator.questLogFromMetadata(event.metadata())
                  .ifPresent(
                      questLog -> {
                        newEntity.add(questLog);
                        QuestLogUtil.setClientQuestLog(newEntity);
                      });
              LastHourSnapshotTranslator.applyCollideMetadata(newEntity, event.metadata());
              Game.add(newEntity);
              if (ctx != null) {
                ctx.clientState().ifPresent(state -> state.trackNetworkEntity(event.entityId()));
              }
            });
  }

  private static boolean spawnPlayer(EntitySpawnEvent event) {
    PlayerComponent playerComponent = event.playerComponent();
    if (playerComponent == null) {
      return false;
    }

    boolean alreadyGotAHero = Game.player().isPresent();
    boolean isLocal = Objects.equals(playerComponent.playerName(), PreRunConfiguration.username());
    if (alreadyGotAHero && isLocal) {
      return false;
    }

    Entity hero =
        HeroBuilder.builder()
            .id(event.entityId())
            .characterClass(CharacterClass.fromByteId(event.characterClassId()))
            .isLocalPlayer(isLocal)
            .username(playerComponent.playerName())
            .build();
    applySpawnPosition(hero, event.positionComponent());
    LastHourSnapshotTranslator.applyCollideMetadata(hero, event.metadata());
    Game.add(hero);
    return true;
  }

  private static void applySpawnPosition(Entity entity, PositionComponent positionComponent) {
    if (positionComponent == null) {
      return;
    }

    entity
        .fetch(PositionComponent.class)
        .ifPresent(
            existingPosition -> {
              existingPosition.position(positionComponent.position());
              existingPosition.viewDirection(positionComponent.viewDirection());
              existingPosition.rotation(positionComponent.rotation());
              existingPosition.scale(positionComponent.scale());
              PositionSync.syncPosition(entity);
            });
  }

  /**
   * If the spawn event represents a puzzle piece world item, materializes the parent puzzle locally
   * (so {@link feature.puzzle.PuzzleDialog} can look it up later) and generates the
   * {@code @gen/puzzle/<id>/<idx>.png} textures so the draw component built right after this call
   * resolves to the correct image fragment.
   *
   * @param metadata the spawn event metadata
   */
  private static void ensurePuzzlePieceTextures(Map<String, String> metadata) {
    if (metadata == null) return;
    String puzzleId = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_ID);
    if (puzzleId == null) return;
    String imagePath = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_IMAGE);
    String pieceCount = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_COUNT);
    String seed = metadata.get(LastHourEntitySpawnStrategy.METADATA_PUZZLE_PIECE_SEED);
    if (imagePath == null || pieceCount == null || seed == null) return;
    try {
      var puzzle =
          PuzzleMaker.ensurePuzzle(
              puzzleId,
              new SimpleIPath(imagePath),
              Integer.parseInt(pieceCount),
              Long.parseLong(seed));
      PuzzleTextureGenerator.ensureRegistered(puzzle);
    } catch (RuntimeException ignored) {
      // Best-effort: if anything goes wrong, fall through to the default texture loading
      // and let it fail loudly (the resulting visible error is clearer than a partial spawn).
    }
  }

  private static final String T_SETTINGS_CONTROLS_HEADER = "settings.controls_header";
  private static final String T_SETTINGS_CONTROLS_DESCRIPTION = "settings.controls_description";
  private static final String T_SETTINGS_PAUSE = "settings.pause";
  private static final String T_SETTINGS_INTERACT = "settings.interact";
  private static final String T_SETTINGS_INVENTORY = "settings.inventory";
  private static final String T_SETTINGS_QUESTLOG = "settings.questlog";
  private static final String T_SETTINGS_INVENTORY_DESCRIPTION = "settings.inventory_description";

  /** Registers additional client settings. */
  static void registerSettings() {
    ClientSettings.registerSetting(new SectionDividerSetting(T_SETTINGS_CONTROLS_HEADER));
    ClientSettings.registerSetting(
        new DescriptionSetting(T_SETTINGS_CONTROLS_DESCRIPTION, Input.Keys.E));
    ClientSettings.registerSetting(new ButtonBindingSetting(T_SETTINGS_PAUSE, Input.Keys.P, false));
    ClientSettings.registerSetting(
        new ButtonBindingSetting(T_SETTINGS_INTERACT, Input.Keys.E, false));
    ClientSettings.registerSetting(
        new ButtonBindingSetting(T_SETTINGS_INVENTORY, Input.Keys.I, false));
    ClientSettings.registerSetting(
        new ButtonBindingSetting(T_SETTINGS_QUESTLOG, KeyboardConfig.QUESTLOG_OPEN.value(), false));
    ClientSettings.registerSetting(
        new DescriptionSetting(T_SETTINGS_INVENTORY_DESCRIPTION, Input.Buttons.RIGHT));
  }

  private static final List<Tuple<String, Color>> USB_TEXTURES =
      List.of(
          Tuple.of("items/usb-side-green.png", Color.GREEN),
          Tuple.of("items/usb-side-blue.png", Color.BLUE),
          Tuple.of("items/usb-side-yellow.png", Color.YELLOW));

  /** Statically renders the needed textures. */
  private static void staticRenderTextures() {
    String basePath = "items/usb-side-red.png";
    float baseHue = 0.0f;

    for (Tuple<String, Color> usbTexture : USB_TEXTURES) {
      ShaderList shaderList = new ShaderList();

      String outTexturePath = usbTexture.a();
      Color color = usbTexture.b();
      float[] hsv = new float[3];
      shaderList.add("hueRemap", new HueRemapShader(baseHue, color.toHsv(hsv)[0] / 360f));

      TextureGenerator.registerRenderShaderTexture(basePath, outTexturePath, shaderList);
    }

    // Invert the keyboard / mouse input-prompt spritesheet so the white-on-transparent icons
    // read clearly against the dark HUD text used in this game.
    String keyboardPromptPath = "hud/input/keyboard_mouse.png";
    ShaderList invertShaders = new ShaderList();
    invertShaders.add("invert", new ColorGradeShader().invert(true));
    TextureGenerator.registerRenderShaderTexture(
        keyboardPromptPath, keyboardPromptPath, invertShaders);

    // Put the first frame of the idle animation of the rogue and the char03 characters into a
    // special texture in "@gen/char03.png" and "@gen/rogue.png", so they can be used for Dialogs
    // without needing to parse the spritesheet again.
    registerCharacterPortrait(CharacterClass.THE_LAST_HOUR_ROGUE, ROGUE_PORTRAIT_PATH);
    registerCharacterPortrait(CharacterClass.THE_LAST_HOUR_CHAR03, CHAR03_PORTRAIT_PATH);
  }

  /** Path of the generated portrait texture for the Rogue character. */
  public static final String ROGUE_PORTRAIT_PATH = "@gen/rogue.png";

  /** Path of the generated portrait texture for the Char03 character. */
  public static final String CHAR03_PORTRAIT_PATH = "@gen/char03.png";

  /** Width / height in pixels of a single frame in the character spritesheets. */
  private static final int CHARACTER_FRAME_SIZE = 32;

  private static final int CHARACTER_FRAME_PADDING = 8;

  /**
   * Extracts the first frame from the given character's spritesheet and registers it as a
   * standalone texture in the {@link TextureMap} under {@code outPath}.
   *
   * @param characterClass character whose sprite sheet should be sampled
   * @param outPath virtual texture-map output path for the generated portrait
   */
  private static void registerCharacterPortrait(CharacterClass characterClass, String outPath) {
    String sheetPath = characterClass.textures().pathString();
    if (!sheetPath.endsWith(".png")) {
      sheetPath = sheetPath + "/" + sheetPath.substring(sheetPath.lastIndexOf('/') + 1) + ".png";
    }
    TextureGenerator.registerSpritesheetRegionTexture(
        sheetPath,
        CHARACTER_FRAME_PADDING,
        CHARACTER_FRAME_PADDING,
        CHARACTER_FRAME_SIZE - CHARACTER_FRAME_PADDING * 2,
        CHARACTER_FRAME_SIZE - CHARACTER_FRAME_PADDING * 2,
        outPath);
  }

  /**
   * Initializes and starts the background music for the game, and sets up listeners to adjust the
   * volume based on client settings changes.
   */
  private static void setupMusic() {
    backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal(BACKGROUND_MUSIC));
    backgroundMusic.setLooping(true);
    backgroundMusic.play();
    backgroundMusic.setVolume(
        ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);

    ClientSettings.setOnVolumeChange(
        (key, value) -> {
          if (key.equals(ClientSettings.KEY_MUSIC_VOLUME)
              || key.equals(ClientSettings.KEY_MASTER_VOLUME)) {
            backgroundMusic.setVolume(
                ClientSettings.musicVolume() / 100f * ClientSettings.masterVolume() / 100f);
          }
        });
  }
}
