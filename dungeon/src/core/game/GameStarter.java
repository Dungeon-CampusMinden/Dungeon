package core.game;

import com.badlogic.gdx.graphics.Color;
import core.language.Language;
import core.language.Localization;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

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

  private GameStarter(Builder builder) {
    this.title = builder.title;
    this.serverMainClass = builder.serverMainClass;
    this.backgroundImage = builder.backgroundImage;
    this.accentColor = builder.accentColor.cpy();
    this.serverArguments = builder.serverArguments.clone();
    this.localServerPort = builder.localServerPort;
    this.language = builder.language;
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

  /** Builder for {@link GameStarter}. */
  public static final class Builder {
    private final String title;
    private final Class<?> serverMainClass;

    private String backgroundImage;
    private Color accentColor = Color.WHITE;
    private String[] serverArguments = new String[] {ServerProcess.SERVER_ARGUMENT};
    private int localServerPort = PreRunConfiguration.networkPort();
    private Language language = Localization.getInstance().currentLanguage();

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
