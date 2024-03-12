package entities;

import com.badlogic.gdx.utils.Align;
import contrib.components.InteractionComponent;
import contrib.hud.dialogs.TextDialog;
import core.Entity;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.utils.Point;
import core.utils.components.draw.Animation;
import core.utils.components.path.SimpleIPath;
import java.io.IOException;

public class SignFactory {

  private static final float DEFAULT_INTERACTION_RADIUS = 2.5f;
  private static final Animation SIGN_TEXTURE =
      Animation.fromSingleImage(
          new SimpleIPath("objects/mailbox/mailbox_2.png")); // TODO: Change to sign texture
  private static final String DEFAULT_TITLE = "Schild";
  public static final int DEFAULT_WIDTH = 600;
    public static final int DEFAULT_HEIGHT = 300;

  public static Entity createSign(String text, Point pos) throws IOException {
    return createSign(text, DEFAULT_TITLE, pos);
  }

  public static Entity createSign(String text, String title, Point pos) {
    Entity sign = new Entity();

    sign.add(new PositionComponent(pos));
    sign.add(new DrawComponent(SIGN_TEXTURE));
    sign.add(
        new InteractionComponent(
            DEFAULT_INTERACTION_RADIUS,
            true,
            (entity, who) -> {
              TextDialog.textDialog(text, "OK", title, DEFAULT_WIDTH, DEFAULT_HEIGHT, Align.topLeft);
            }));

    return sign;
  }
}
