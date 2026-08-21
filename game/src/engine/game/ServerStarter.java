package engine.game;

import engine.Game;
import engine.utils.IVoidFunction;
import feature.entities.CharacterClass;
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
  private final int maximumPlayers;
  private final IVoidFunction onFrame;

  private ServerStarter(Builder builder) {
    super(builder);
    this.characterClasses = builder.characterClasses.clone();
    this.maximumPlayers = builder.maximumPlayers;
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

  /**
   * Returns the character class used for a local player in a single-process run (level editor).
   *
   * @return the first configured fallback character class, or {@link CharacterClass#WIZARD} if none
   *     is configured
   */
  CharacterClass primaryCharacterClass() {
    return characterClasses.length > 0 ? characterClasses[0] : CharacterClass.WIZARD;
  }

  /**
   * @return the per-frame callback of this server
   */
  IVoidFunction onFrame() {
    return onFrame;
  }

  @Override
  public void apply() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(true);
    PreRunConfiguration.networkPort(Integer.getInteger(ServerProcess.PORT_PROPERTY, port));
    PreRunConfiguration.networkServerMaximumPlayers(maximumPlayers);
    if (characterClasses.length > 0) {
      PreRunConfiguration.multiplayerCharacterClasses(characterClasses);
    }
    applyShared();
    Game.userOnFrame(onFrame);
  }

  /** Builder for {@link ServerStarter}. */
  public static final class Builder extends AbstractStarter.Builder<Builder> {

    private CharacterClass[] characterClasses = new CharacterClass[0];
    private int maximumPlayers = Integer.MAX_VALUE;
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
     * Sets the maximum number of multiplayer player identities accepted by the server.
     *
     * @param maximumPlayers the positive maximum number of players
     * @return this builder
     * @throws IllegalArgumentException if {@code maximumPlayers} is not positive
     */
    public Builder maximumPlayers(int maximumPlayers) {
      if (maximumPlayers < 1) {
        throw new IllegalArgumentException("maximumPlayers must be positive");
      }
      this.maximumPlayers = maximumPlayers;
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
