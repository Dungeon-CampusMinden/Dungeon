package contrib.hud.newhud;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;

/**
 * A StatusEffectSlot represents a single status effect within the {@link StatusEffectBar}.
 *
 * <p>It is responsible for displaying an effects icon and a counter, representing the stacks or
 * remaining time of the effect.
 */
public class StatusEffectSlot extends Table {

  private final Label counterLabel;
  private final Image effectIcon;

  /**
   * Creates a single StatusEffectSlot.
   *
   * @param skin The skin that defines the appearance of UI elements.
   */
  public StatusEffectSlot(Skin skin) {
    setSize(32, 32);
    setBackground(skin.getDrawable("gray"));

    Stack stack = new Stack();
    stack.setSize(32, 32);
    addActor(stack);

    effectIcon = new Image();

    Table iconTable = new Table();
    iconTable.top().left();
    iconTable.add(effectIcon).size(24, 24);

    counterLabel = new Label("", skin, "staminalabel");
    counterLabel.setFontScale(0.4f);

    Table counterTable = new Table();
    counterTable.bottom().right();
    counterTable.add(counterLabel).size(14, 14);

    stack.add(iconTable);
    stack.add(counterTable);
  }

  /**
   * Sets the texture used to represent the status effect.
   *
   * @param texture The texture of the effect.
   */
  public void setEffectIcon(Texture texture) {
    effectIcon.setDrawable(new TextureRegionDrawable(texture));
  }

  /**
   * Sets the value of the counter.
   *
   * @param count the value to be displayed.
   */
  public void setCounterLabel(int count) {
    counterLabel.setText(String.valueOf(count));
  }

  /**
   * Adds a tooltip with the given text to this status effect slot.
   *
   * @param text The Text to be displayed as the tooltip.
   * @param skin The skin that defines the appearance of UI elements.
   */
  public void addTooltip(String text, Skin skin) {
    Label tooltipLabel = new Label(text, skin);
    tooltipLabel.setVisible(true);
    Tooltip<Label> tooltip = new Tooltip<>(tooltipLabel);
    tooltip.setInstant(true);
    this.addListener(tooltip);
  }
}
