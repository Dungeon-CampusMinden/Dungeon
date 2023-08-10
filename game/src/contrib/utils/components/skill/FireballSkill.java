package contrib.utils.components.skill;

import contrib.utils.components.health.DamageType;

import core.utils.Point;

import java.util.function.Supplier;

/**
 * FireballSkill is a subclass of DamageProjectile.
 *
 * <p>The FireballSkill class extends the functionality of DamageProjectile to implement the
 * specific behavior of the fireball skill.
 */
public class FireballSkill extends DamageProjectile {

    private static final String PROJECTILE_TEXTURES = "skills/fireball";
    private static final float PROJECTILE_SPEED = 15.0f;
    private static final int DAMAGE_AMOUNT = 1;
    private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
    private static final Point HITBOX_SIZE = new Point(1, 1);
    private static final float PROJECTILE_RANGE = 5f;

    /**
     * Create a {@link DamageProjectile} that looks like a fireball and will cause fire damage.
     *
     * @param targetSelection A function used to select the point where the projectile should fly
     *     to.
     * @see DamageProjectile
     */
    public FireballSkill(Supplier<Point> targetSelection) {
        super(
                PROJECTILE_TEXTURES,
                PROJECTILE_SPEED,
                DAMAGE_AMOUNT,
                DAMAGE_TYPE,
                HITBOX_SIZE,
                targetSelection,
                PROJECTILE_RANGE);
    }
}
