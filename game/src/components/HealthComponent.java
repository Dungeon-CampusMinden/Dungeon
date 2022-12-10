package components;

/** Component to manage hit points */
public class HealthComponent extends Component {

    private int maxHealth;
    private int currentHealth;

    /**
     * @param maxHealth The maximum ammount of hitPoints this entity can have
     * @param startHealth The start ammount of hitPoints this entity has
     */
    public HealthComponent(int maxHealth, int startHealth) {
        this.maxHealth = maxHealth;
        currentHealth = startHealth;
    }

    /**
     * Heal this entity, limit to the maximum hit points
     *
     * @param ammount heal ammount
     */
    public void heal(int ammount) {
        currentHealth = Math.min(maxHealth, currentHealth + ammount);
    }

    /**
     * Damage this entity, limit is 0
     *
     * @param ammount damage ammount
     */
    public void dmg(int ammount) {
        currentHealth = Math.max(0, currentHealth - ammount);
    }

    /**
     * Set the maximum healt of this entity
     *
     * @param ammount
     */
    public void setMaxHealt(int ammount) {
        this.maxHealth = ammount;
    }

    /**
     * @return current health of this entity
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * @return max health of this entity
     */
    public int getMaxHealth() {
        return currentHealth;
    }
}
