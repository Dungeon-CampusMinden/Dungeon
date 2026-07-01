package starter;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import contrib.components.CollideComponent;
import contrib.entities.CharacterClass;
import contrib.entities.HeroBuilder;
import contrib.hud.dialogs.DialogFactory;
import contrib.modules.interaction.InteractionComponent;
import contrib.modules.puzzle.PuzzleMaker;
import contrib.modules.puzzle.PuzzlePieceItem;
import contrib.modules.puzzle.PuzzleTextureGenerator;
import contrib.systems.AttributeBarSystem;
import contrib.systems.PositionSync;
import contrib.utils.components.Debugger;
import core.Entity;
import core.Game;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.game.PreRunConfiguration;
import core.language.Language;
import core.language.Localization;
import core.network.ConnectionListener;
import core.network.messages.s2c.EntitySpawnEvent;
import core.utils.CursorUtil;
import core.utils.Tuple;
import core.utils.components.draw.DrawComponentFactory;
import core.utils.components.draw.TextureGenerator;
import core.utils.components.draw.TextureMap;
import core.utils.components.draw.shader.ColorGradeShader;
import core.utils.components.draw.shader.HueRemapShader;
import core.utils.components.draw.shader.ShaderList;
import core.utils.components.path.SimpleIPath;
import core.utils.settings.ButtonBindingSetting;
import core.utils.settings.ClientSettings;
import core.utils.settings.DescriptionSetting;
import core.utils.settings.SectionDividerSetting;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import level.LastHourLevel;
import modules.computer.ComputerFactory;
import modules.computer.ComputerStateSyncSystem;
import modules.computer.LastHourDialogTypes;
import modules.trash.TrashMinigameUI;
import modules.usbstick.UsbStickItem;
import network.LastHourEntitySpawnStrategy;
import network.LastHourSnapshotTranslator;
import util.ui.BlackFadeCutscene;

/** The main class for the Multiplayer Client for development and testing purposes. */
public final class LastHourClient {

  private static final String BACKGROUND_MUSIC = "sounds/forest_bgm.wav";
  private static Music backgroundMusic;

  /** Pre-run registrations for the client (custom dialogs and items). */
  public static void registerClientContent() {
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
    DialogFactory.register(LastHourDialogTypes.TEXT_CUTSCENE, BlackFadeCutscene::build);
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
              if (Game.allEntities().anyMatch(e -> e.id() == event.entityId())) {
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
              applyCollideMetadata(newEntity, event.metadata());
              newEntity.persistent(event.isPersistent());
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
            .persistent(event.isPersistent())
            .isLocalPlayer(isLocal)
            .username(playerComponent.playerName())
            .build();
    applySpawnPosition(hero, event.positionComponent());
    applyCollideMetadata(hero, event.metadata());
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

  private static void applyCollideMetadata(Entity entity, Map<String, String> metadata) {
    LastHourSnapshotTranslator.collideComponentFromMetadata(metadata)
        .ifPresent(
            collideComponent -> {
              CollideComponent component =
                  entity
                      .fetch(CollideComponent.class)
                      .orElseGet(
                          () -> {
                            CollideComponent newComponent = new CollideComponent();
                            entity.add(newComponent);
                            return newComponent;
                          });
              component.isSolid(collideComponent.isSolid());
              component.collider(collideComponent.collider());
              PositionSync.syncPosition(entity);
            });
  }

  /**
   * If the spawn event represents a puzzle piece world item, materializes the parent puzzle locally
   * (so {@link contrib.modules.puzzle.PuzzleDialog} can look it up later) and generates the
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

  /** Registers the translation files for the supported languages. */
  static void initLocalization() {
    Localization localization = Game.localization();
    localization.registerTranslationFile(Language.DE, "language/de.json");
    localization.registerTranslationFile(Language.EN, "language/en.json");
  }

  private static final String T_SETTINGS_CONTROLS_HEADER = "settings.controls_header";
  private static final String T_SETTINGS_CONTROLS_DESCRIPTION = "settings.controls_description";
  private static final String T_SETTINGS_PAUSE = "settings.pause";
  private static final String T_SETTINGS_INTERACT = "settings.interact";
  private static final String T_SETTINGS_INVENTORY = "settings.inventory";
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
