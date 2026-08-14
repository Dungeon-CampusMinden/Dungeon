package feature.components;

import engine.Component;
import feature.entities.CharacterClass;

/**
 * A component that assigns a {@link CharacterClass} to an entity.
 *
 * <p>This component is used to define the character type of an entity, including its starting
 * skills, items, and base attributes.
 *
 * @param characterClass CharacterClass to store.
 */
public record CharacterClassComponent(CharacterClass characterClass) implements Component {}
