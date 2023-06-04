package ecs.components;

import ecs.components.skill.Skill;
import ecs.entities.Entity;
import java.util.Optional;
import java.util.logging.Logger;
import logging.CustomLogLevel;

/**
 * This component is for the player character entity only. It should only be
 * implemented by one
 * entity and mark this entity as the player character. This component stores
 * data that is only
 * relevant for the player character. The PlayerSystems acts on the
 * PlayableComponent.
 */
public class PlayableComponent extends Component {

    private boolean playable;
    private transient final Logger playableCompLogger = Logger.getLogger(this.getClass().getName());

    private Skill skillSlot1;
    private Skill skillSlot2;
    private Skill skillSlot3;
    private Skill skillSlot4;
    private Skill skillSlot5;
    private Skill skillSlot6;

    /**
     * @param entity     associated entity
     * @param skillSlot1 skill that will be on the first skillslot
     * @param skillSlot2 skill that will be on the second skillslot
     * @param skillSlot3 skill that will be on the third skillslot
     * @param skillSlot4 skill that will be on the fourth skillslot
     * @param skillSlot5 skill that will be on the fifth skillslot
     * @param skillSlot6 skill that will be on the sixth skillslot
     */
    PlayableComponent(Entity entity, Skill skillSlot1, Skill skillSlot2, Skill skillSlot3, Skill skillSlot4,
            Skill skillSlot5, Skill skillSlot6) {
        this(entity, skillSlot1, skillSlot2);
        this.skillSlot3 = skillSlot3;
        this.skillSlot4 = skillSlot4;
        this.skillSlot5 = skillSlot5;
        this.skillSlot6 = skillSlot6;
    }

    /**
     * @param entity     associated entity
     * @param skillSlot1 skill that will be on the first skillslot
     * @param skillSlot2 skill that will be on the second skillslot
     */
    public PlayableComponent(Entity entity, Skill skillSlot1, Skill skillSlot2) {
        super(entity);
        playable = true;
        this.skillSlot1 = skillSlot1;
        this.skillSlot2 = skillSlot2;
    }

    /** {@inheritDoc} */
    public PlayableComponent(Entity entity) {
        super(entity);
        playable = true;
    }

    /**
     * @return the playable state
     */
    public boolean isPlayable() {
        playableCompLogger.log(
                CustomLogLevel.DEBUG,
                "Checking if entity '"
                        + entity.getClass().getSimpleName()
                        + "' is playable: "
                        + playable);
        return playable;
    }

    /**
     * @param playable set the playabale state
     */
    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    /**
     * @param skillSlot1 skill that will be on the first skillslot
     */
    public void setSkillSlot1(Skill skillSlot1) {
        this.skillSlot1 = skillSlot1;
    }

    /**
     * @param skillSlot2 skill that will be on the second skillslot
     */
    public void setSkillSlot2(Skill skillSlot2) {
        this.skillSlot2 = skillSlot2;
    }

    /**
     * @param skillSlot3 skill that will be on the third skillslot
     */
    public void setSkillSlot3(Skill skillSlot3) {
        this.skillSlot3 = skillSlot3;
    }

    /**
     * @param skillSlot4 skill that will be on the fourth skillslot
     */
    public void setSkillSlot4(Skill skillSlot4) {
        this.skillSlot4 = skillSlot4;
    }

    /**
     * @param skillSlot5 skill that will be on the fifth skillslot
     */
    public void setSkillSlot5(Skill skillSlot5) {
        this.skillSlot5 = skillSlot5;
    }

    /**
     * @param skillSlot6 skill that will be on the sixth skillslot
     */
    public void setSkillSlot6(Skill skillSlot6) {
        this.skillSlot6 = skillSlot6;
    }

    /**
     * @return skill on first skill slot
     */
    public Optional<Skill> getSkillSlot1() {
        return Optional.ofNullable(skillSlot1);
    }

    /**
     * @return skill on second skill slot
     */
    public Optional<Skill> getSkillSlot2() {
        return Optional.ofNullable(skillSlot2);
    }

    /**
     * @return skill on third skill slot
     */
    public Optional<Skill> getSkillSlot3() {
        return Optional.ofNullable(skillSlot3);
    }

    /**
     * @return skill on fourth skill slot
     */
    public Optional<Skill> getSkillSlot4() {
        return Optional.ofNullable(skillSlot4);
    }

    /**
     * @return skill on fifth skill slot
     */
    public Optional<Skill> getSkillSlot5() {
        return Optional.ofNullable(skillSlot5);
    }

    /**
     * @return skill on sixth skill slot
     */
    public Optional<Skill> getSkillSlot6() {
        return Optional.ofNullable(skillSlot6);
    }
}
