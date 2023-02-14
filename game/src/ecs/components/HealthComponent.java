package ecs.components;

import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.Entity;
import graphic.Animation;
import java.util.ArrayList;
import java.util.List;
import semanticAnalysis.types.DSLContextMember;
import semanticAnalysis.types.DSLType;
import semanticAnalysis.types.DSLTypeMember;

/** The HealthComponent makes an entity vulnerable and killable */
@DSLType(name = "health_component")
public class HealthComponent extends Component {
    private static final List<String> missingTexture = List.of("animation/missingTexture.png");

    private final List<Damage> damageToGet;
    private @DSLTypeMember(name = "maximal_hit_points") int maximalHitPoints;
    private int currentHitPoints;
    private @DSLTypeMember(name = "on_death_function") IOnDeathFunction onDeath;
    private @DSLTypeMember(name = "get_hit_animation") Animation getHitAnimation;
    private @DSLTypeMember(name = "die_animation") Animation dieAnimation;

    /**
     * Creates a new HealthComponent
     *
     * @param entity associated entity
     * @param maximalHitPoints maximum amount of hit-points, currentHitPoints cant be bigger than
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
        this.maximalHitPoints = maximalHitPoints;
        this.currentHitPoints = maximalHitPoints;
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
        super(entity);
        this.maximalHitPoints = 1;
        this.currentHitPoints = 1;
        this.onDeath = entity2 -> {};
        this.getHitAnimation = new Animation(missingTexture, 100);
        this.dieAnimation = new Animation(missingTexture, 100);
        damageToGet = new ArrayList<>();
    }

    /**
     * Adds damage, which is accounted for by the system
     *
     * @param damage Damage that should be inflicted
     */
    public void getHit(Damage damage) {
        damageToGet.add(damage);
    }

    /** Triggers the onDeath Function */
    public void triggerOnDeath() {
        onDeath.onDeath(entity);
    }

    /**
     * @return List with all damage objects that still need to be accounted for
     */
    public List<Damage> getDamageList() {
        return damageToGet;
    }

    /**
     * Calculate the amount of damage of a certain type
     *
     * @param dt Type of damage object that still need to be accounted for
     * @return Sum of all damage objects of type dt (default: 0)
     */
    public int getDamage(DamageType dt) {
        return damageToGet.stream()
                .filter(d -> d.damageType() == dt)
                .mapToInt(Damage::damageAmount)
                .sum();
    }

    /** Clear the damage list */
    public void clearDamageList() {
        damageToGet.clear();
    }

    /**
     * Sets the current life points, capped at the value of the maximum hit-points
     *
     * @param amount new amount of current hit-points
     */
    public void setCurrentHitPoints(int amount) {
        this.currentHitPoints = Math.min(maximalHitPoints, amount);
    }

    /**
     * Sets the value of the Maximum Hit-points. If the new maximum hit-points are less than the
     * current hit-points, the current points are set to the new maximum hit-points.
     *
     * @param amount new amount of maximal hit-points
     */
    public void setMaximalHitPoints(int amount) {
        this.maximalHitPoints = amount;
        currentHitPoints = Math.min(currentHitPoints, maximalHitPoints);
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
     * @return The current hit-points the entity has
     */
    public int getCurrentHitPoints() {
        return currentHitPoints;
    }

    /**
     * @return The maximal hit-points the entity can have
     */
    public int getMaximalHitPoints() {
        return maximalHitPoints;
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
}
