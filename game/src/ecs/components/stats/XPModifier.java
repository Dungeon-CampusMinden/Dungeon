package ecs.components.stats;

import java.io.Serializable;

/** changes the amount of xp an entity gets */
public record XPModifier(float xpMultiplier) implements Serializable {
}
