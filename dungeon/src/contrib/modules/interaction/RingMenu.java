package contrib.modules.interaction;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import contrib.hud.UIUtils;
import core.Game;
import java.util.List;
import java.util.function.Consumer;

/**
 * A non-blocking radial menu for interaction selection.
 *
 * <p>When the user selects an interaction or cancels, the menu calls the provided callback and
 * removes itself from the stage.
 */
public class RingMenu extends Group {

  private static final float RADIUS = 120f;

  private final Skin skin = UIUtils.defaultSkin();
  private final Consumer<Interaction> onSelected;

  private RingMenu(Consumer<Interaction> onSelected) {
    this.onSelected = onSelected;
  }

  /**
   * Creates and shows a non-blocking ring menu.
   *
   * @param interactable The source of interactions.
   * @param onSelected Callback when the user chooses an interaction (or null if cancelled).
   */
  public static void show(IInteractable interactable, Consumer<Interaction> onSelected) {
    Game.stage()
        .ifPresentOrElse(
            stage -> {
              RingMenu menu = new RingMenu(onSelected);

              // Must be done BEFORE build
              menu.setSize(stage.getWidth(), stage.getHeight());
              menu.setPosition(0, 0);

              // Must be done BEFORE build so getStage() is not null
              stage.addActor(menu);

              // Now getStage() is valid
              menu.build(interactable);
            },
            () -> {
              throw new IllegalStateException("No stage available to show RingMenu");
            });
  }

  private void build(IInteractable interactable) {
    List<Interaction> interactions =
        List.of(
            interactable.look(),
            interactable.interact(),
            interactable.take(),
            interactable.talk(),
            interactable.usewithitem(),
            interactable.attack());

    float centerX = getStage().getWidth() / 2f;
    float centerY = getStage().getHeight() / 2f;

    setSize(getStage().getWidth(), getStage().getHeight());

    // --- Center Cancel Button ---
    TextButton cancel = new TextButton("Cancel", skin);
    cancel.setPosition(centerX - cancel.getWidth() / 2f, centerY - cancel.getHeight() / 2f);

    cancel.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            close(null); // null = cancelled
          }
        });
    addActor(cancel);

    // --- Buttons in a ring ---
    int count = interactions.size();
    for (int i = 0; i < count; i++) {
      Interaction interaction = interactions.get(i);

      float angleDeg = (360f / count) * i;
      float angleRad = angleDeg * MathUtils.degreesToRadians;

      float x = centerX + MathUtils.cos(angleRad) * RADIUS;
      float y = centerY + MathUtils.sin(angleRad) * RADIUS;

      TextButton btn = new TextButton(interaction.displayName(), skin);
      btn.setPosition(x - btn.getWidth() / 2f, y - btn.getHeight() / 2f);

      btn.addListener(
          new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
              close(interaction);
            }
          });

      addActor(btn);
    }
  }

  private void close(Interaction interaction) {
    onSelected.accept(interaction);
    remove(); // remove this menu from stage
  }
}
