package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

import contrib.utils.components.health.DamageType;

import core.utils.Point;

import java.util.function.Supplier;

/**
 * FireballSkill is a subclass of {@link DamageProjectile}.
 *
 * <p>The FireballSkill class extends the functionality of {@link DamageProjectile} to implement the
 * specific behavior of the fireball skill.
 */
public class FireballSkill extends DamageProjectile {

    private static final String PROJECTILE_TEXTURES = "skills/fireball";
    private static final String PROJECTILE_SOUND = "sounds/fireball.wav";
    private static final float PROJECTILE_SPEED = 15.0f;
    private static final int DAMAGE_AMOUNT = 5;
    private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
    private static final Point HIT_BOX_SIZE = new Point(1, 1);
    private static final float PROJECTILE_RANGE = 7f;

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
                HIT_BOX_SIZE,
                targetSelection,
                PROJECTILE_RANGE);
    }

    @Override
    protected void playSound() {
        Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(PROJECTILE_SOUND));

        // Generate a random pitch between 1.5f and 2.0f
        float minPitch = 2f;
        float maxPitch = 3f;
        float randomPitch = MathUtils.random(minPitch, maxPitch);

        // Play the sound with the adjusted pitch
        long soundId = soundEffect.play();
        soundEffect.setPitch(soundId, randomPitch);

        // Set the volume
        soundEffect.setVolume(soundId, 0.05f);
    }
}
