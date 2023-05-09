package ecs.components.skill;

import ecs.entities.Entity;
import tools.Constants;

import java.util.logging.Logger;

public class Skill {

    private ISkillFunction skillFunction;
    private int coolDownInFrames;
    private int currentCoolDownInFrames;
    private float manaCost;

    /**
     * @param skillFunction Function of this skill
     */
    public Skill(ISkillFunction skillFunction, float coolDownInSeconds) {
        this.skillFunction = skillFunction;
        this.coolDownInFrames = (int) (coolDownInSeconds * Constants.FRAME_RATE);
        this.currentCoolDownInFrames = 0;
    }

    /**
     * @param skillFunction Function of this skill
     * @param manaCost Mana required to use this Skill
     */
    public Skill(ISkillFunction skillFunction, float coolDownInSeconds, float manaCost) {
        this.skillFunction = skillFunction;
        this.coolDownInFrames = (int) (coolDownInSeconds * Constants.FRAME_RATE);
        this.currentCoolDownInFrames = 0;
        this.manaCost = manaCost;
    }

    /**
     * Execute the method of this skill
     *
     * @param entity entity which uses the skill
     */
    public void execute(Entity entity) {
        Logger loggertest = Logger.getLogger(this.getClass().getName());
        loggertest.info(currentCoolDownInFrames + " " + coolDownInFrames);
        if (!isOnCoolDown() && canAfford(entity)) {
            entity.getComponent(ManaComponent.class)
                .map(mc -> (ManaComponent) mc).ifPresent(mc -> mc.adjustCurrentMana(-this.manaCost));
            skillFunction.execute(entity);
            activateCoolDown();
        }
    }

    /**
     * @return true if cool down is not 0, else false
     */
    public boolean isOnCoolDown() {
        return currentCoolDownInFrames > 0;
    }

    /**
     * check whether an Entity has enough mana tu use a Skill
     *
     * @param entity Entity that is supposed to afford the Skill
     * @return true if Entitys mana is greater or equal to the Skills mana cost, false otherwise
     */
    public boolean canAfford(Entity entity) {
        boolean[] value = {false};
        entity.getComponent(ManaComponent.class)
            .map(mc -> (ManaComponent) mc)
            .ifPresent(mc -> {
                if (mc.getCurrentMana() < this.manaCost) {
                    System.out.println("Can't afford " + mc.getCurrentMana());
                } else value[0] = true;
            });
        return value[0];
    }

    /** activate cool down */
    public void activateCoolDown() {
        currentCoolDownInFrames = coolDownInFrames;
    }

    /** reduces the current cool down by frame */
    public void reduceCoolDown() {
        currentCoolDownInFrames = Math.max(0, --currentCoolDownInFrames);
    }
}
