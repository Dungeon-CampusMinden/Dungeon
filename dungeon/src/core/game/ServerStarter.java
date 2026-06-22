package core.game;

import contrib.entities.CharacterClass;
import core.Game;
import core.utils.IVoidFunction;
import java.util.Objects;

/**
 * Dedicated multiplayer server configuration for an explicit project, created via {@link #builder}.
 *
 * <p>{@link #apply()} sets the shared server role flags (multiplayer enabled, network server) and
 * the bind port (honoring the {@link ServerProcess#PORT_PROPERTY} forwarded by a hosting client),
 * applies the {@link AbstractStarter shared configuration}, and registers the optional per-frame
 * callback. The {@link MainMenu} invokes it when the process is started as a server (see {@link
 * MainMenu#shouldRunMpServer(String[])}).
 */
public final class ServerStarter extends AbstractStarter {

  private final CharacterClass[] characterClasses;
  private final IVoidFunction onFrame;

  private ServerStarter(Builder builder) {
    super(builder);
    this.characterClasses = builder.characterClasses.clone();
    this.onFrame = builder.onFrame;
  }

  /**
   * Creates a server starter builder with the required in-loop setup callback.
   *
   * @param onSetup the server setup callback (registered via {@link Game#userOnSetup})
   * @return a new builder
   */
  public static Builder builder(IVoidFunction onSetup) {
    return new Builder(onSetup);
  }

  @Override
  public void apply() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    PreRunConfiguration.networkPort(Integer.getInteger(ServerProcess.PORT_PROPERTY, port));
    if (characterClasses.length > 0) {
      PreRunConfiguration.multiplayerCharacterClasses(characterClasses);
    }
    applyShared();
    Game.userOnFrame(onFrame);
  }

  /** Builder for {@link ServerStarter}. */
  public static final class Builder extends AbstractStarter.Builder<Builder> {

    private CharacterClass[] characterClasses = new CharacterClass[0];
    private IVoidFunction onFrame = () -> {};

    private Builder(IVoidFunction onSetup) {
      super(onSetup);
    }

    @Override
    protected Builder self() {
      return this;
    }

    /**
     * Sets the fallback character classes assigned to connecting clients (round-robin).
     *
     * @param characterClasses the fallback character classes
     * @return this builder
     */
    public Builder characterClasses(CharacterClass... characterClasses) {
      this.characterClasses = Objects.requireNonNull(characterClasses, "characterClasses").clone();
      return this;
    }

    /**
     * Sets the per-frame callback (registered via {@link Game#userOnFrame}).
     *
     * @param onFrame the per-frame callback
     * @return this builder
     */
    public Builder onFrame(IVoidFunction onFrame) {
      this.onFrame = Objects.requireNonNull(onFrame, "onFrame");
      return this;
    }

    /**
     * Builds the immutable server starter.
     *
     * @return the configured {@link ServerStarter}
     */
    public ServerStarter build() {
      return new ServerStarter(this);
    }
  }
}
