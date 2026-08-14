package engine.game;

import com.badlogic.gdx.Input;
import engine.Game;
import engine.configuration.KeyboardConfig;
import engine.level.loader.DungeonLoader;
import engine.utils.IVoidFunction;
import feature.components.Debugger;
import feature.entities.CharacterClass;
import feature.entities.HeroBuilder;
import feature.systems.DebugDrawSystem;
import feature.systems.LevelEditorSystem;
import java.util.Objects;

/**
 * True-singleplayer configuration for an explicit project, created via {@link #builder}.
 *
 * <p>Singleplayer keeps the authoritative game systems and the client systems in one process and
 * uses the local network handler. This is useful for tools such as the level editor that need to
 * modify the authoritative level directly.
 */
public final class SingleplayerStarter extends AbstractStarter {

  private final IVoidFunction serverSetup;
  private final IVoidFunction clientSetup;
  private final IVoidFunction onFrame;
  private final CharacterClass characterClass;
  private final boolean levelEditor;
  private final String pathToLevels;

  private SingleplayerStarter(Builder builder) {
    super(builder);
    this.serverSetup = builder.serverSetup;
    this.clientSetup = builder.clientSetup;
    this.onFrame = builder.onFrame;
    this.characterClass = builder.characterClass;
    this.levelEditor = builder.levelEditor;
    this.pathToLevels = builder.pathToLevels;
  }

  /**
   * Creates a builder with the setup callbacks required to combine the server and client roles.
   *
   * @param serverSetup authoritative systems and registrations
   * @param clientSetup client systems and registrations
   * @return a new builder
   */
  public static Builder builder(IVoidFunction serverSetup, IVoidFunction clientSetup) {
    return new Builder(serverSetup, clientSetup);
  }

  @Override
  public void apply() {
    apply(false);
  }

  /** Applies this starter and enables the level editor when it was configured. */
  public void applyLevelEditor() {
    if (!levelEditor) {
      throw new IllegalStateException("Level editor support is not configured.");
    }
    apply(true);
  }

  private void apply(boolean enableLevelEditor) {
    PreRunConfiguration.multiplayerEnabled(false);
    PreRunConfiguration.isNetworkServer(true);
    PreRunConfiguration.networkPort(port);
    PreRunConfiguration.multiplayerCharacterClass(characterClass);

    // The menu has already registered client-side handlers. Replace them with authoritative ones.
    DungeonLoader.clearLevelOrder();
    applyShared();
    Game.reconfigureNetworkHandler();
    Game.userOnFrame(onFrame);
    Game.userOnSetup(
        () -> {
          serverSetup.execute();
          clientSetup.execute();
          Game.add(
              HeroBuilder.builder()
                  .characterClass(characterClass)
                  .isLocalPlayer(true)
                  .username(PreRunConfiguration.username())
                  .build());
          if (enableLevelEditor) {
            Game.add(new Debugger());
            KeyboardConfig.PAUSE.value(Input.Keys.UNKNOWN);
            Game.add(new DebugDrawSystem());
            Game.add(new LevelEditorSystem(pathToLevels));
            LevelEditorSystem.activateOnStart();
          }
        });
  }

  /** Builder for {@link SingleplayerStarter}. */
  public static final class Builder extends AbstractStarter.Builder<Builder> {

    private final IVoidFunction serverSetup;
    private final IVoidFunction clientSetup;
    private IVoidFunction onFrame = () -> {};
    private CharacterClass characterClass = CharacterClass.WIZARD;
    private boolean levelEditor;
    private String pathToLevels = "";

    private Builder(IVoidFunction serverSetup, IVoidFunction clientSetup) {
      super(() -> {});
      this.serverSetup = Objects.requireNonNull(serverSetup, "serverSetup");
      this.clientSetup = Objects.requireNonNull(clientSetup, "clientSetup");
    }

    @Override
    protected Builder self() {
      return this;
    }

    /**
     * Sets the character class for the local player.
     *
     * @param characterClass local player class
     * @return this builder
     */
    public Builder characterClass(CharacterClass characterClass) {
      this.characterClass = Objects.requireNonNull(characterClass, "characterClass");
      return this;
    }

    /**
     * Sets the callback invoked once per frame.
     *
     * @param onFrame per-frame callback
     * @return this builder
     */
    public Builder onFrame(IVoidFunction onFrame) {
      this.onFrame = Objects.requireNonNull(onFrame, "onFrame");
      return this;
    }

    /**
     * Enables the level editor and configures the path used when saving a level.
     *
     * @param pathToLevels level output path
     * @return this builder
     */
    public Builder levelEditor(String pathToLevels) {
      this.levelEditor = true;
      this.pathToLevels = Objects.requireNonNull(pathToLevels, "pathToLevels");
      return this;
    }

    /**
     * Builds the immutable singleplayer starter.
     *
     * @return configured singleplayer starter
     */
    public SingleplayerStarter build() {
      return new SingleplayerStarter(this);
    }
  }
}
