package ecs.damage;

/**
 * Damage that can reduce the life points of an entity
 *
 * @param damageAmount Number of life points to be deducted. Value before taking into account
 *     resistances and vulnerabilities.
 * @param damageType Type of damage, this is important for accounting the actual damage taking into
 *     account resistances or vulnerabilities
 */
public record Damage(int damageAmount, DamageType damageType) {}
