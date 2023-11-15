package contrib.utils.components.skill;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

import contrib.utils.components.health.DamageType;

import core.utils.Point;
import core.utils.components.draw.Animation;
import core.utils.components.draw.IPath;

import java.util.function.Supplier;

/**
 * FireballSkill is a subclass of DamageProjectile.
 *
 * <p>The FireballSkill class extends the functionality of DamageProjectile to implement the
 * specific behavior of the fireball skill.
 */
public class FireballSkill extends DamageProjectile {

    private static final Animation PROJECTILE_TEXTURES = Animation.of("skills/fireball");
    private static final IPath PROJECTILE_SOUND = new IPath(){
        @Override
        public String pathString() {
            return "sounds/fireball.wav";
        }

        @Override
        public int priority() {
            return 0;
        }
    };
    private static final float PROJECTILE_SPEED = 15.0f;
    private static final int DAMAGE_AMOUNT = 5;
    private static final DamageType DAMAGE_TYPE = DamageType.FIRE;
    private static final Point HITBOX_SIZE = new Point(1, 1);
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
                HITBOX_SIZE,
                targetSelection,
                PROJECTILE_RANGE);
    }

    @Override
    protected void playSound() {
        Sound soundEffect = Gdx.audio.newSound(Gdx.files.internal(PROJECTILE_SOUND.pathString()));

        // Generate a random pitch between min and max
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
