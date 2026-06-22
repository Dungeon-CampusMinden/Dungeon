package core.game;

import core.utils.IVoidFunction;

/**
 * Multiplayer client configuration for an explicit project, created via {@link #builder}.
 *
 * <p>{@link #apply()} sets the shared client role flags (multiplayer enabled, not a server, no
 * forced character class so the server decides) and then applies the {@link AbstractStarter shared
 * configuration}. The {@link MainMenu} applies it before showing the menu; a standalone dev client
 * can apply it directly and call {@link core.Game#run()}.
 */
public final class ClientStarter extends AbstractStarter {

  private ClientStarter(Builder builder) {
    super(builder);
  }

  /**
   * Creates a client starter builder with the required in-loop setup callback.
   *
   * @param onSetup the client setup callback (registered via {@link core.Game#userOnSetup})
   * @return a new builder
   */
  public static Builder builder(IVoidFunction onSetup) {
    return new Builder(onSetup);
  }

  @Override
  public void apply() {
    PreRunConfiguration.multiplayerEnabled(true);
    PreRunConfiguration.isNetworkServer(false);
    PreRunConfiguration.networkPort(port);
    PreRunConfiguration.multiplayerCharacterClass(null); // server decides
    applyShared();
  }

  /** Builder for {@link ClientStarter}. */
  public static final class Builder extends AbstractStarter.Builder<Builder> {

    private Builder(IVoidFunction onSetup) {
      super(onSetup);
    }

    @Override
    protected Builder self() {
      return this;
    }

    /**
     * Builds the immutable client starter.
     *
     * @return the configured {@link ClientStarter}
     */
    public ClientStarter build() {
      return new ClientStarter(this);
    }
  }
}
