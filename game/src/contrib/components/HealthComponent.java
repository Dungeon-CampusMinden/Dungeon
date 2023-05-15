package contrib.components;

import com.badlogic.gdx.utils.Null;

import contrib.systems.HealthSystem;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import contrib.utils.components.health.IOnDeathFunction;

import core.Component;
import core.Entity;
import core.systems.DrawSystem;
import core.utils.components.draw.Animation;
import core.utils.logging.CustomLogLevel;

import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * The HealthComponent adds health points and the ability to take damage and die to an entity.
 *
 * <p>It keeps track of the current health points and its maximum.
 *
 * <p>It also keeps track of the damage received via the {@link #receiveHit(Damage) receiveHit}
 * method. The damage is stored in a list and can be retrieved via the {@link #getDamage(DamageType)
 * getDamage} method. The damage is applied and cleared by the {@link HealthSystem HealthSystem}
 * every tick. To determine what the last cause of damage was, the {@link #getLastDamageCause()}
 * method can be used.
 *
 * <p>The HealthComponent also provides the ability to set an onDeath function, which is called when
 * the health points reach 0 or less. The onDeath function can be set via the {@link
 * #setOnDeath(IOnDeathFunction) setOnDeath} method.
 *
 * <p>Finally, the HealthComponent provides the ability to set animations for the entity to be
 * played when it is hit or dies. These animations can be set via the {@link
 * #setGetHitAnimation(Animation) setGetHitAnimation} and {@link #setDieAnimation(Animation)
 * setDieAnimation} methods and are played by the {@link DrawSystem DrawSystem} automatically.
 */
@DSLType(name = "health_component")
public class HealthComponent extends Component {
    private static final List<String> missingTexture = List.of("animation/missingTexture.png");
    private final List<Damage> damageToGet;
    private @DSLTypeMember(name = "maximal_health_points") int maximalHealthpoints;
    private int currentHealthpoints;
    private @Null Entity lastCause = null;
    private @DSLTypeMember(name = "on_death_function") IOnDeathFunction onDeath;
    private @DSLTypeMember(name = "get_hit_animation") Animation getHitAnimation;
    private @DSLTypeMember(name = "die_animation") Animation dieAnimation;
    private final Logger healthLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates a new HealthComponent
     *
     * <p>Create a new HealthComponent by explicitly setting maximal health points, onDeath
     * function, a getHitAnimation and a dieAnimation.
     *
     * @param entity associated entity
     * @param maximalHitPoints maximum amount of hit-points, currentHitPoints can't be bigger than
     *     that
     * @param onDeath Function that gets called, when this entity dies
     * @param getHitAnimation Animation to be played as the entity was hit
     * @param dieAnimation Animation to be played as the entity dies
     */
    public HealthComponent(
            Entity entity,
            int maximalHitPoints,
            IOnDeathFunction onDeath,
            Animation getHitAnimation,
            Animation dieAnimation) {
        super(entity);
        this.maximalHealthpoints = maximalHitPoints;
        this.currentHealthpoints = maximalHitPoints;
        this.onDeath = onDeath;
        this.getHitAnimation = getHitAnimation;
        this.dieAnimation = dieAnimation;
        damageToGet = new ArrayList<>();
    }

    /**
     * Creates a HealthComponent with default values.
     *
     * <p>The maximal health points are set to 1, the onDeath function is empty and the animations
     * are set to an animation composed of the "missingTexture" texture.
     *
     * @param entity associated entity
     */
    public HealthComponent(@DSLContextMember(name = "entity") Entity entity) {
        this(
                entity,
                1,
                entity2 -> {},
                new Animation(missingTexture, 100),
                new Animation(missingTexture, 100));
    }

    /**
     * Adds damage, which is accounted for by the system
     *
     * @param damage Damage that should be inflicted
     */
    public void receiveHit(Damage damage) {
        damageToGet.add(damage);
        this.lastCause = damage.cause() != null ? damage.cause() : this.lastCause;
    }

    /** Triggers the onDeath Function */
    public void triggerOnDeath() {
        onDeath.onDeath(entity);
    }

    /**
     * Calculate the amount of damage of a certain type
     *
     * @param dt Type of damage object that still need to be accounted for
     * @return Sum of all damage objects of type dt (default: 0)
     */
    public int getDamage(DamageType dt) {
        int damageSum =
                damageToGet.stream()
                        .filter(d -> d.damageType() == dt)
                        .mapToInt(Damage::damageAmount)
                        .sum();

        healthLogger.log(
                CustomLogLevel.DEBUG,
                this.getClass().getSimpleName()
                        + " processed damage for entity '"
                        + entity.getClass().getSimpleName()
                        + "': "
                        + damageSum);

        return damageSum;
    }

    /**
     * Clear the damage list. The damage list is used to determine the damage the entity should
     * receive on next tick.
     */
    public void clearDamage() {
        damageToGet.clear();
    }

    /**
     * Sets the current life points, capped at the value of the maximum hit-points
     *
     * @param amount new amount of current health-points
     */
    public void setCurrentHealthpoints(int amount) {
        this.currentHealthpoints = Math.min(maximalHealthpoints, amount);
    }

    /**
     * Sets the value of the Maximum health-points. If the new maximum health-points are less than
     * the current health-points, the current points are set to the new maximum health-points.
     *
     * @param amount new amount of maximal health-points
     */
    public void setMaximalHealthpoints(int amount) {
        this.maximalHealthpoints = amount;
        currentHealthpoints = Math.min(currentHealthpoints, maximalHealthpoints);
    }

    /**
     * Set the animation to be played when the entity dies
     *
     * @param dieAnimation new dieAnimation
     */
    public void setDeathAnimation(Animation dieAnimation) {
        this.dieAnimation = dieAnimation;
    }

    /**
     * Set the animation to be played when the entity is hit
     *
     * @param isHitAnimation new isHitAnimation
     */
    public void setGetHitAnimation(Animation isHitAnimation) {
        this.getHitAnimation = isHitAnimation;
    }

    /**
     * Set a new function to be called when dying.
     *
     * @param onDeath new onDeath function
     */
    public void setOnDeath(IOnDeathFunction onDeath) {
        this.onDeath = onDeath;
    }

    /**
     * @return The current health-points the entity has
     */
    public int getCurrentHealthpoints() {
        return currentHealthpoints;
    }

    /**
     * @return The maximal health-points the entity can have
     */
    public int getMaximalHealthpoints() {
        return maximalHealthpoints;
    }

    /**
     * @return Animation to be played as the entity was hit
     */
    public Animation getGetHitAnimation() {
        return getHitAnimation;
    }

    /**
     * @return Animation to be played when dying
     */
    public Animation getDeathAnimation() {
        return dieAnimation;
    }

    /**
     * @return The last entity that caused damage to this entity.
     */
    public Optional<Entity> getLastDamageCause() {
        return Optional.ofNullable(this.lastCause);
    }

    public boolean isDead() {
        return currentHealthpoints <= 0;
    }
}
