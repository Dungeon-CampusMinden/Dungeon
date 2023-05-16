package core.components;

import com.badlogic.gdx.utils.Null;

import contrib.utils.components.skill.Skill;

import core.Component;
import core.Entity;
import core.utils.logging.CustomLogLevel;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Component that marks an entity as playable.
 *
 * <p>This component is used to mark an entity as playable by the player. It also contains skills
 * that can be used by the player to interact with the game world. The skills are stored in two
 * slots. The skills can be null if the entity does not have any skills.
 *
 * <p>This component is used by the {@link core.systems.PlayerSystem PlayerSystem} to determine if
 * an entity is playable or not.
 */
public class PlayerComponent extends Component {

    private boolean playable;
    private final Logger playableCompLogger = Logger.getLogger(this.getClass().getName());

    private Skill skillSlot1;
    private Skill skillSlot2;

    /**
     * Creates a new PlayerComponent with specific skills.
     *
     * <p>This constructor can be used if the entity has skills. By default, the associated entity
     * is playable.
     *
     * @param entity - the entity this component belongs to
     * @param skillSlot1 - the first skill slot (can be null)
     * @param skillSlot2 - the second skill slot (can be null)
     */
    public PlayerComponent(Entity entity, @Null Skill skillSlot1, @Null Skill skillSlot2) {
        super(entity);
        playable = true;
        this.skillSlot1 = skillSlot1;
        this.skillSlot2 = skillSlot2;
    }

    /**
     * Creates a new PlayerComponent.
     *
     * <p>This constructor can be used if the entity does not have any skills. By default, the
     * associated entity is playable.
     *
     * @param entity - the entity this component belongs to
     */
    public PlayerComponent(Entity entity) {
        super(entity);
        playable = true;
    }

    /**
     * Checks if the entity is playable or not
     *
     * @return true if the entity
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
     * Sets the playable property.
     *
     * @param playable - true to play false to
     */
    public void setPlayable(boolean playable) {
        this.playable = playable;
    }

    /**
     * Sets the first skill slot
     *
     * @param skillSlot1 - the first skill slot
     */
    public void setSkillSlot1(Skill skillSlot1) {
        this.skillSlot1 = skillSlot1;
    }

    /**
     * Sets the second skill slot
     *
     * @param skillSlot2 - the second skill slot
     */
    public void setSkillSlot2(Skill skillSlot2) {
        this.skillSlot2 = skillSlot2;
    }

    /**
     * Returns the first skill this slot participates in.
     *
     * @return An Optional of the first skill this slot participates in
     */
    public Optional<Skill> getSkillSlot1() {
        return Optional.ofNullable(skillSlot1);
    }

    /**
     * Returns the second skill in this slot if there is one.
     *
     * @return An {@link Optional<Skill>} of the second skill
     */
    public Optional<Skill> getSkillSlot2() {
        return Optional.ofNullable(skillSlot2);
    }
}
