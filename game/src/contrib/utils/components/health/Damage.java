package contrib.utils.components.health;

import com.badlogic.gdx.utils.Null;
import core.Entity;

/**
 * Damage that can reduce the life points of an entity.
 *
 * @param damageAmount Number of life points to be deducted. Value before taking into account
 *     resistances and vulnerabilities.
 * @param damageType Type of damage, this is important for accounting the actual damage taking into
 *     account resistances or vulnerabilities.
 * @param cause Entity that caused the damage (e.g. the player). Can be null.
 */
public record Damage(int damageAmount, DamageType damageType, @Null Entity cause) {}
