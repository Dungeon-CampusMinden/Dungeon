package feature.level.visibility;

import engine.Component;
import engine.utils.Rectangle;

/**
 * Component to hide a certain region in the world unless stepped into by the player.
 *
 * @param region The region that is hidden
 * @param transitionSize The transitionSize for a smooth transition
 */
public record LevelHideComponent(Rectangle region, float transitionSize) implements Component {}
