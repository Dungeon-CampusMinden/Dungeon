package core.game;

import core.utils.IVoidFunction;
import java.util.Objects;

/**
 * Multiplayer client configuration for an explicit project, created via {@link #builder}.
 *
 * <p>{@link #apply()} sets the shared client role flags (multiplayer enabled, not a server, no
 * forced character class so the server decides) and then applies the {@link AbstractStarter shared
 * configuration}. The {@link MainMenu} applies it before showing the menu; a standalone dev client
 * can apply it directly and call {@link core.Game#run()}.
 */
public final class ClientStarter extends AbstractStarter {

  private final IVoidFunction initLocalization;
  private final IVoidFunction registerSettings;

  private ClientStarter(Builder builder) {
    super(builder);
    this.initLocalization = builder.initLocalization;
    this.registerSettings = builder.registerSettings;
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
    // Initialize localization and register the client settings before the main menu is shown, so
    // the main-menu settings (which use localized labels) and the in-game settings stay in sync.
    initLocalization.execute();
    registerSettings.execute();
    applyShared();
  }

  /** Builder for {@link ClientStarter}. */
  public static final class Builder extends AbstractStarter.Builder<Builder> {

    private IVoidFunction initLocalization = () -> {};
    private IVoidFunction registerSettings = () -> {};

    private Builder(IVoidFunction onSetup) {
      super(onSetup);
    }

    @Override
    protected Builder self() {
      return this;
    }

    /**
     * Sets the callback that initializes localization (registering translation files).
     *
     * <p>It is invoked by {@link #apply()} (before the {@link MainMenu} shows the menu and before
     * {@link #registerSettings(IVoidFunction)}), so localized labels are available for the
     * main-menu settings and stay in sync with the in-game settings.
     *
     * @param initLocalization the localization-initialization callback
     * @return this builder
     */
    public Builder initLocalization(IVoidFunction initLocalization) {
      this.initLocalization = Objects.requireNonNull(initLocalization, "initLocalization");
      return this;
    }

    /**
     * Sets the callback that registers additional client settings.
     *
     * <p>It is invoked by {@link #apply()} (before the {@link MainMenu} shows the menu), so the
     * registered settings are already available in the main-menu settings and stay in sync with the
     * in-game settings.
     *
     * @param registerSettings the settings-registration callback
     * @return this builder
     */
    public Builder registerSettings(IVoidFunction registerSettings) {
      this.registerSettings = Objects.requireNonNull(registerSettings, "registerSettings");
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
