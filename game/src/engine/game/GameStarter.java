package engine.game;

import com.badlogic.gdx.graphics.Color;
import engine.language.Language;
import engine.language.Localization;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Immutable menu/hosting integration config for wiring an explicit game into the reusable {@link
 * MainMenu}.
 *
 * <p>It describes how the game presents itself in the menu (title, background, accent color,
 * language) and how the "Host Game" option launches a dedicated server child process (server main
 * class, arguments, port). The actual client and server configuration lives in {@link
 * ClientStarter} and {@link ServerStarter}.
 *
 * <p>The configured {@link #language() language} is applied by the {@link MainMenu} on startup, so
 * the menu (and the game it launches) is already shown in the desired language.
 *
 * <p>Create instances via {@link #builder(String, Class)}. Required parameters are the game title
 * and the dedicated server main class; the remaining properties are optional.
 */
public final class GameStarter {

  private final String title;
  private final Class<?> serverMainClass;
  private final String backgroundImage;
  private final Color accentColor;
  private final String[] serverArguments;
  private final int localServerPort;
  private final Language language;
  private final String levelEditorLevelPath;
  private final BooleanSupplier continueAvailable;
  private final String[] continueServerArguments;
  private final Supplier<StartupConsent> startupConsent;
  private final Supplier<TrackingSettings> trackingSettings;

  private GameStarter(Builder builder) {
    this.title = builder.title;
    this.serverMainClass = builder.serverMainClass;
    this.backgroundImage = builder.backgroundImage;
    this.accentColor = builder.accentColor.cpy();
    this.serverArguments = builder.serverArguments.clone();
    this.localServerPort = builder.localServerPort;
    this.language = builder.language;
    this.levelEditorLevelPath = builder.levelEditorLevelPath;
    this.continueAvailable = builder.continueAvailable;
    this.continueServerArguments =
        builder.continueServerArguments == null ? null : builder.continueServerArguments.clone();
    this.startupConsent = builder.startupConsent;
    this.trackingSettings = builder.trackingSettings;
  }

  /**
   * Creates a builder with the required menu/hosting integration fields.
   *
   * @param title game title used in window and menu
   * @param serverMainClass dedicated server main class launched by "Host Game"
   * @return builder initialized with required values
   */
  public static Builder builder(String title, Class<?> serverMainClass) {
    return new Builder(title, serverMainClass);
  }

  /**
   * @return human-readable game title
   */
  public String title() {
    return title;
  }

  /**
   * @return dedicated server main class
   */
  public Class<?> serverMainClass() {
    return serverMainClass;
  }

  /**
   * @return optional internal background-image path for the main menu
   */
  public Optional<String> backgroundImage() {
    return Optional.ofNullable(backgroundImage);
  }

  /**
   * @return accent color used for the main-menu title
   */
  public Color accentColor() {
    return accentColor;
  }

  /**
   * @return server-process arguments used when hosting
   */
  public String[] serverArguments() {
    return serverArguments.clone();
  }

  /**
   * @return local server port used for hosting and localhost connection
   */
  public int localServerPort() {
    return localServerPort;
  }

  /**
   * @return language applied on startup for the menu and the launched game
   */
  public Language language() {
    return language;
  }

  /**
   * Returns the path the level editor saves levels to.
   *
   * <p>The level-editor entry (hidden main-menu entry and {@code --leveleditor} launch flag) is
   * only available if this path is configured; everything else the level editor needs is taken from
   * the {@link ServerStarter} and {@link ClientStarter} of the project.
   *
   * @return the optional level output path used by the level editor
   */
  public Optional<String> levelEditorLevelPath() {
    return Optional.ofNullable(levelEditorLevelPath);
  }

  /**
   * Returns whether this game currently exposes a resumable local save in its main menu.
   *
   * @return whether the optional continue action is configured and available
   */
  public boolean continueAvailable() {
    return continueAvailable != null && continueAvailable.getAsBoolean();
  }

  /**
   * @return server arguments used by the optional continue action
   * @throws IllegalStateException if no continue action was configured
   */
  public String[] continueServerArguments() {
    if (continueServerArguments == null) {
      throw new IllegalStateException("No continue game configuration was provided.");
    }
    return continueServerArguments.clone();
  }

  /**
   * Resolves the optional consent prompt that should be shown before the main menu can be used.
   *
   * @return a consent prompt, or empty when consent has already been decided
   */
  public Optional<StartupConsent> startupConsent() {
    return Optional.ofNullable(startupConsent.get());
  }

  /**
   * Resolves the optional tracking-management view for the main-menu settings.
   *
   * @return localized tracking settings, or empty for games without a consent flow
   */
  public Optional<TrackingSettings> trackingSettings() {
    return Optional.ofNullable(trackingSettings.get());
  }

  /**
   * Text and callback for one mandatory startup consent decision.
   *
   * @param title dialog title
   * @param summary short explanation shown above the detailed information
   * @param message information shown before the decision
   * @param acceptLabel label for accepting the consent
   * @param declineLabel label for declining the consent
   * @param decision callback receiving the selected decision
   */
  public record StartupConsent(
      String title,
      String summary,
      String message,
      String acceptLabel,
      String declineLabel,
      Consumer<Boolean> decision) {
    /**
     * Validates the consent prompt fields.
     *
     * @param title dialog title
     * @param summary short explanation shown above the detailed information
     * @param message information shown before the decision
     * @param acceptLabel label for accepting the consent
     * @param declineLabel label for declining the consent
     * @param decision callback receiving the selected decision
     */
    public StartupConsent {
      Objects.requireNonNull(title, "title");
      Objects.requireNonNull(summary, "summary");
      Objects.requireNonNull(message, "message");
      Objects.requireNonNull(acceptLabel, "acceptLabel");
      Objects.requireNonNull(declineLabel, "declineLabel");
      Objects.requireNonNull(decision, "decision");
    }
  }

  /**
   * Localized controls for reviewing a tracking decision and deleting local tracking data.
   *
   * @param title dialog title
   * @param summary short explanation shown above the detailed information
   * @param message detailed information shown in a scrollable area
   * @param status current decision text
   * @param enableLabel label for enabling tracking for the next run
   * @param disableLabel label for withdrawing consent for the next run
   * @param deleteLabel label for deleting local tracking files
   * @param deleteConfirmationTitle confirmation dialog title
   * @param deleteConfirmationMessage confirmation dialog message
   * @param deletedMessage feedback shown after successful deletion
   * @param deleteFailedMessage feedback shown when deletion fails
   * @param currentDecision current decision, or {@code null} when undecided
   * @param decision callback receiving the new decision
   * @param deleteLocalData deletes local tracking data and returns whether it succeeded
   */
  public record TrackingSettings(
      String title,
      String summary,
      String message,
      String status,
      String enableLabel,
      String disableLabel,
      String deleteLabel,
      String deleteConfirmationTitle,
      String deleteConfirmationMessage,
      String deletedMessage,
      String deleteFailedMessage,
      Boolean currentDecision,
      Consumer<Boolean> decision,
      BooleanSupplier deleteLocalData) {
    /**
     * Validates one tracking-management configuration.
     *
     * @param title dialog title
     * @param summary short explanation shown above the detailed information
     * @param message detailed information shown in a scrollable area
     * @param status current decision text
     * @param enableLabel label for enabling tracking
     * @param disableLabel label for withdrawing consent
     * @param deleteLabel label for deleting local tracking files
     * @param deleteConfirmationTitle confirmation dialog title
     * @param deleteConfirmationMessage confirmation dialog message
     * @param deletedMessage success feedback
     * @param deleteFailedMessage failure feedback
     * @param currentDecision current decision, or {@code null}
     * @param decision callback receiving the new decision
     * @param deleteLocalData callback deleting local tracking data
     */
    public TrackingSettings {
      Objects.requireNonNull(title, "title");
      Objects.requireNonNull(summary, "summary");
      Objects.requireNonNull(message, "message");
      Objects.requireNonNull(status, "status");
      Objects.requireNonNull(enableLabel, "enableLabel");
      Objects.requireNonNull(disableLabel, "disableLabel");
      Objects.requireNonNull(deleteLabel, "deleteLabel");
      Objects.requireNonNull(deleteConfirmationTitle, "deleteConfirmationTitle");
      Objects.requireNonNull(deleteConfirmationMessage, "deleteConfirmationMessage");
      Objects.requireNonNull(deletedMessage, "deletedMessage");
      Objects.requireNonNull(deleteFailedMessage, "deleteFailedMessage");
      Objects.requireNonNull(decision, "decision");
      Objects.requireNonNull(deleteLocalData, "deleteLocalData");
    }
  }

  /** Builder for {@link GameStarter}. */
  public static final class Builder {
    private final String title;
    private final Class<?> serverMainClass;

    private String backgroundImage;
    private Color accentColor = Color.WHITE;
    private String[] serverArguments = new String[] {ServerProcess.SERVER_ARGUMENT};
    private int localServerPort = PreRunConfiguration.networkPort();
    private Language language = Localization.getInstance().currentLanguage();
    private String levelEditorLevelPath;
    private BooleanSupplier continueAvailable;
    private String[] continueServerArguments;
    private Supplier<StartupConsent> startupConsent = () -> null;
    private Supplier<TrackingSettings> trackingSettings = () -> null;

    private Builder(String title, Class<?> serverMainClass) {
      this.title = validateTitle(title);
      this.serverMainClass = Objects.requireNonNull(serverMainClass, "serverMainClass");
    }

    /**
     * Sets an optional background image path for the main menu.
     *
     * @param backgroundImage internal asset path, or {@code null}/blank to disable the image
     * @return this builder instance
     */
    public Builder backgroundImage(String backgroundImage) {
      this.backgroundImage = normalizeBackgroundImage(backgroundImage);
      return this;
    }

    /**
     * Sets the main-menu title accent color.
     *
     * @param accentColor color used when rendering the game title in the menu
     * @return this builder instance
     */
    public Builder accentColor(Color accentColor) {
      this.accentColor = Objects.requireNonNull(accentColor, "accentColor");
      return this;
    }

    /**
     * Overrides server-process arguments for hosting.
     *
     * @param serverArguments command-line arguments for the dedicated server process
     * @return this builder instance
     */
    public Builder serverArguments(String... serverArguments) {
      Objects.requireNonNull(serverArguments, "serverArguments");
      if (serverArguments.length == 0) {
        throw new IllegalArgumentException("serverArguments must not be empty");
      }
      this.serverArguments =
          Arrays.stream(serverArguments).map(String::trim).toArray(String[]::new);
      return this;
    }

    /**
     * Overrides the local hosted server port.
     *
     * @param localServerPort local TCP port used for hosting and localhost connection
     * @return this builder instance
     */
    public Builder localServerPort(int localServerPort) {
      if (localServerPort <= 0 || localServerPort > 65535) {
        throw new IllegalArgumentException("localServerPort must be in range 1..65535");
      }
      this.localServerPort = localServerPort;
      return this;
    }

    /**
     * Sets the language used for the main menu and the launched game (default: the engine's current
     * language). It is applied by the {@link MainMenu} on startup.
     *
     * @param language language to apply before the menu is shown
     * @return this builder instance
     */
    public Builder language(Language language) {
      this.language = Objects.requireNonNull(language, "language");
      return this;
    }

    /**
     * Enables the level editor (hidden main-menu entry and {@code --leveleditor} launch flag) and
     * configures the path used when saving a level.
     *
     * <p>The level editor runs the server and client role in a single process; all remaining
     * configuration is taken from the project's {@link ServerStarter} and {@link ClientStarter}.
     *
     * @param pathToLevels default folder used to construct the initial level save file path
     * @return this builder
     */
    public Builder levelEditor(String pathToLevels) {
      this.levelEditorLevelPath = Objects.requireNonNull(pathToLevels, "pathToLevels");
      return this;
    }

    /**
     * Adds an optional local continue action to the main menu.
     *
     * <p>The availability supplier is evaluated when the menu is built. The alternate arguments are
     * passed to the dedicated server child instead of the normal new-game arguments; this keeps
     * save ownership on the authoritative server.
     *
     * @param available checks whether a resumable save exists
     * @param serverArguments arguments for the server process when continuing
     * @return this builder
     */
    public Builder continueGame(BooleanSupplier available, String... serverArguments) {
      this.continueAvailable = Objects.requireNonNull(available, "available");
      Objects.requireNonNull(serverArguments, "serverArguments");
      if (serverArguments.length == 0) {
        throw new IllegalArgumentException("continue serverArguments must not be empty");
      }
      this.continueServerArguments =
          Arrays.stream(serverArguments).map(String::trim).toArray(String[]::new);
      return this;
    }

    /**
     * Adds a startup consent prompt. The supplier is evaluated after localization has been
     * initialized and may return {@code null} once the decision is already known.
     *
     * @param startupConsent supplier for the optional consent prompt
     * @return this builder
     */
    public Builder startupConsent(Supplier<StartupConsent> startupConsent) {
      this.startupConsent = Objects.requireNonNull(startupConsent, "startupConsent");
      return this;
    }

    /**
     * Adds an optional tracking-management view to the main-menu settings.
     *
     * @param trackingSettings supplier for localized tracking controls
     * @return this builder instance
     */
    public Builder trackingSettings(Supplier<TrackingSettings> trackingSettings) {
      this.trackingSettings = Objects.requireNonNull(trackingSettings, "trackingSettings");
      return this;
    }

    /**
     * Builds an immutable {@link GameStarter} config.
     *
     * @return immutable starter configuration
     */
    public GameStarter build() {
      return new GameStarter(this);
    }

    private static String validateTitle(String title) {
      if (title == null || title.isBlank()) {
        throw new IllegalArgumentException("title must not be blank");
      }
      return title;
    }

    private static String normalizeBackgroundImage(String backgroundImage) {
      if (backgroundImage == null) {
        return null;
      }
      String trimmed = backgroundImage.trim();
      return trimmed.isEmpty() ? null : trimmed;
    }
  }
}
