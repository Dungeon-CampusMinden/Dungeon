package ecs.components;

import com.badlogic.gdx.utils.Null;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import logging.CustomLogLevel;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/** The HealthComponent makes an entity vulnerable and killable */
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
    private transient final Logger healthLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates a new HealthComponent
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
     * Creates a HealthComponent with default values
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

    /** Clear the damage list */
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
    public void setDieAnimation(Animation dieAnimation) {
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
    public Animation getDieAnimation() {
        return dieAnimation;
    }

    /**
     * @return The last entity that caused damage to this entity.
     */
    public Optional<Entity> getLastDamageCause() {
        return Optional.ofNullable(this.lastCause);
    }
}
