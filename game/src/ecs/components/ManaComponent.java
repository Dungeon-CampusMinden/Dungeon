package ecs.components;

import java.util.logging.Logger;

import ecs.entities.Entity;

/** ManaComponent gives an entity the ability to spend mana */
public class ManaComponent extends Component {

    private int currentMana, maxMana, regenerationRatePerSecond = 1;
    private transient final Logger manaLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Creates a new ManaComponent
     * 
     * @implNote Use this one to create a fully new manaComponent
     * 
     * @param entity  the entity that owns this component
     * @param maxMana the maximum amount of mana that the entity can have (also the
     *                current amount of mana)
     * 
     * @see #ManaComponent(Entity, int, int)
     */
    public ManaComponent(Entity entity, int maxMana) {
        this(entity, maxMana, maxMana);
    }

    /**
     * Creates a new ManaComponent
     * 
     * @implNote Use this one to replace an old manaComponent
     * 
     * @param entity      the entity that owns this component
     * @param maxMana     the maximum amount of mana that the entity can have
     * @param currentMana the current amount of mana that the entity has
     * 
     * @see #ManaComponent(Entity, int)
     */
    public ManaComponent(Entity entity, int maxMana, int currentMana) {
        super(entity);
        this.maxMana = maxMana;
        this.currentMana = currentMana > maxMana ? maxMana : currentMana;
        manaLogger.info("New ManaComponent created:: maxMana = " + maxMana + ", currentMana = " + currentMana);
    }

    /**
     * Increments the current mana by {@code 1} every {@code} 1000 /
     * regenerationRatePerSecond {@code} milliseconds
     */
    public void regenerate() {
        currentMana += currentMana < maxMana && System.currentTimeMillis() % 1000 < 50 ? regenerationRatePerSecond : 0;
    }

    /**
     * Reduces the current mana if it is greater than the mana that shall be spent
     * 
     * @param amount mana that shall be spent
     * @return {@code true} if {@code amount} is equal or less than
     *         {@code currentMana} otherwise {@code false}
     * 
     * @see #hasMana(int)
     */
    public boolean spendMana(int amount) {
        int manaSpent = hasMana(amount) ? amount : 0;
        currentMana -= manaSpent;
        manaLogger.info(manaSpent + " mana used");
        return hasMana(amount);
    }

    /**
     * Returns wether or not there is sufficient mana
     * 
     * @param amount amount of mana that shall be tested for
     * @return {@code true} if {@code amount} is equal or less than
     *         {@code currentMana} otherwise {@code false}
     */
    public boolean hasMana(int amount) {
        return currentMana >= amount;
    }

    public void setCurrentMana(int currentMana) {
        this.currentMana = currentMana > maxMana ? maxMana : currentMana;
    }

    public void setMaxMana(int maxMana) {
        this.maxMana = maxMana;
        this.currentMana = maxMana > currentMana ? currentMana : maxMana;
    }

    public void setRegenerationRatePerSecond(int regenerationRatePerSecond) {
        this.regenerationRatePerSecond = regenerationRatePerSecond > 0 ? regenerationRatePerSecond : 1;
    }

    public int getCurrentMana() {
        return currentMana;
    }

    public int getMaxMana() {
        return maxMana;
    }

    public int getRegenerationRatePerSecond() {
        return regenerationRatePerSecond;
    }

}
