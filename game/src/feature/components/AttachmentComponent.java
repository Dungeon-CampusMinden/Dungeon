package feature.components;

import engine.Component;
import engine.Entity;
import engine.utils.Vector2;
import feature.systems.AttachmentSystem;

/**
 * Attaches an entity to a target entity. The {@link AttachmentSystem} keeps the entity at the
 * target's position plus the offset and removes it once the target has left the game.
 *
 * @param target the entity this entity follows
 * @param offset offset from the target's center
 */
public record AttachmentComponent(Entity target, Vector2 offset) implements Component {}
