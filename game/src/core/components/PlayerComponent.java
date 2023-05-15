package core.components;

import contrib.utils.components.skill.Skill;

import core.Component;
import core.Entity;
import core.utils.logging.CustomLogLevel;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * This component is for the player character entity only. It should only be implemented by one
 * entity and mark this entity as the player character. This component stores data that is only
 * relevant for the player character. The PlayerSystems acts on the PlayableComponent.
 */
public class PlayerComponent extends Component {

    private boolean playable;
    private final Logger playableCompLogger = Logger.getLogger(this.getClass().getName());

    private Skill skillSlot1;
    private Skill skillSlot2;

    /**
     * @param entity associated entity
     * @param skillSlot1 skill that will be on the first skillslot
     * @param skillSlot2 skill that will be on the second skillslot
     */
    public PlayerComponent(Entity entity, Skill skillSlot1, Skill skillSlot2) {
        super(entity);
        playable = true;
        this.skillSlot1 = skillSlot1;
        this.skillSlot2 = skillSlot2;
    }

    /** {@inheritDoc} */
    public PlayerComponent(Entity entity) {
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
     * @param skillSlot2 skill that will be on the first skillslot
     */
    public void setSkillSlot2(Skill skillSlot2) {
        this.skillSlot2 = skillSlot2;
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
}
