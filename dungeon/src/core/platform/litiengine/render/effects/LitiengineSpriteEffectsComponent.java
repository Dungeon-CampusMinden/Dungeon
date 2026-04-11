package core.platform.litiengine.render.effects;

import core.Component;
import java.util.Objects;

/** Component that stores ordered LITIENGINE-only sprite effects for an entity. */
public final class LitiengineSpriteEffectsComponent implements Component {

  private final LitiengineSpriteEffects effects;

  public LitiengineSpriteEffectsComponent() {
    this(new LitiengineSpriteEffects());
  }

  public LitiengineSpriteEffectsComponent(LitiengineSpriteEffects effects) {
    this.effects = Objects.requireNonNull(effects, "effects");
  }

  public LitiengineSpriteEffects effects() {
    return effects;
  }
}
