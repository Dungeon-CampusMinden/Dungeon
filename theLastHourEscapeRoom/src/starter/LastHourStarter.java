package starter;

import com.badlogic.gdx.graphics.Color;
import core.game.GameStarter;
import java.util.Optional;

/**
 * {@link GameStarter} integration for "The Last Hour" used by the main menu.
 *
 * <p>The client is configured exactly like the standalone {@link LastHourClient} dev client, and
 * the dedicated server is launched through the existing {@link TheLastHour} {@code --server} entry
 * point.
 */
public class LastHourStarter implements GameStarter {

  private static final String BACKGROUND_IMAGE = "images/lasthour.png";

  @Override
  public String title() {
    return "The Last Hour";
  }

  @Override
  public Optional<String> backgroundImage() {
    return Optional.of(BACKGROUND_IMAGE);
  }

  @Override
  public Color accentColor() {
    return new Color(0.56f, 0.87f, 1f, 1f);
  }

  @Override
  public void configureClient() {
    LastHourClient.configureClient();
  }

  @Override
  public Class<?> serverMainClass() {
    return TheLastHour.class;
  }
}
