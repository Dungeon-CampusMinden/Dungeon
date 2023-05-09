package ecs.components.skill;

import ecs.components.Component;
import ecs.entities.Entity;

/** A ManaComponent stores the associated entity's mana */
public class ManaComponent extends Component {

    private float maxMana;
    private float currentMana;
    private float manaRegenRate;

    /**
     * @param entity associated entity
     */
    public ManaComponent(Entity entity) {
        super(entity);
        this.maxMana = 0f;
        this.currentMana = 0f;
        this.manaRegenRate = 0f;
    }

    /**
     * increases current mana by the specified rate each frame,
     * does not exceed maximum or fall below 0
     */
    public void regenerate() {
        currentMana = Math.max(0, Math.min(maxMana, currentMana + manaRegenRate));
    }

    /** @return maximum amount of mana */
    public float getMaxMana() {
        return maxMana;
    }

    /**
     * sets maximum amount of mana
     *
     * @param maxMana maximum amount of mana
     */
    public void setMaxMana(float maxMana) {
        this.maxMana = maxMana;
    }

    /** @return current amount of mana */
    public float getCurrentMana() {
        return currentMana;
    }

    /**
     * sets current amount of mana
     *
     * @param currentMana current amount of mana
     */
    public void setCurrentMana(float currentMana) {
        this.currentMana = currentMana;
    }

    /** @return current regeneration rate of mana per frame */
    public float getManaRegenRate() {
        return manaRegenRate;
    }

    /**
     * sets current regeneration rate of mana per frame,
     * can be negative to subtract mana over time
     *
     * @param manaRegenRate current regeneration rate of mana
     */
    public void setManaRegenRate(float manaRegenRate) {
        this.manaRegenRate = manaRegenRate;
    }

    /**
     * adds a specified amount of mana,
     * can be negative to subtract mana
     *
     * @param adjustment amount the current mana is going to be changed by
     */
    public void adjustCurrentMana(float adjustment){
        this.currentMana += adjustment;
    }
}
