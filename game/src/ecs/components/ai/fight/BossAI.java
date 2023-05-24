package ecs.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.ai.AITools;
import ecs.components.skill.Skill;
import ecs.entities.DarkKnight;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import starter.Game;
import tools.Constants;
import tools.Point;

import java.util.logging.Logger;

/**
 * The BossAI is an AI for the Boss monsters. It's entity in the ECS. This
 * class helps to set up a Boss monsters with all its components and attributes.
 * It extends the IFightAI interface.
 */
public class BossAI implements IFightAI {
    private final int BREAK_TIME = 2 * Constants.FRAME_RATE;
    private int currentBreak = 0;
    private final Skill FIRST_SKILL;
    private final Skill SECOND_SKILL;
    private GraphPath<Tile> path;
    private boolean aggressive;
    private float range;
    private transient final Logger bossAILogger = Logger.getLogger(this.getClass().getName());
    /**
     * Constructor for the BossAI.
     * @param firstSkill the first skill of the boss
     * @param secondSkill the second skill of the boss
     * @param range the range of the boss
     */
    public BossAI(Skill firstSkill, Skill secondSkill, float range) {
        if (Game.getHero().isEmpty()) {
            throw new Error("There must be a Hero in the Game!");
        }
        this.FIRST_SKILL = firstSkill;
        this.SECOND_SKILL = secondSkill;
        this.range = range;
        bossAILogger.info("BossAI created" + "first skill: " + firstSkill + "second skill: " + secondSkill + "range: " + range);
    }

    /**
     * This methode is used to fight. If he Boss health >= 50%, the Boss attacks the Player with the FIRST_SKILL.
     * If the player is in range, the Boss spawns monsters with a random Position.
     * When the health drops unter 50%, the Boss attacks with the SECOND_SKILL, doubles his speed and runs to the player.
     * @param entity associated entity
     */
    @Override
    public void fight(Entity entity) {
        bossAILogger.info("fighting");
        if (aggressive) {
            bossAILogger.info("Boss AI is aggressive");
            path = AITools.calculatePathToHero(entity);
            AITools.move(entity, path);
        }
        currentBreak++;
        if (currentBreak < BREAK_TIME) {
            return; // END
        }
        // Is not on break
        bossAILogger.info("Boss AI is not on break");
        currentBreak = 0;
        if (getHealthRatio(entity) >= 0.5) {
            FIRST_SKILL.execute(entity);
            if (AITools.playerInRange(entity, range)) {
                new DarkKnight(Game.getLevel());
            }
            return; // END
        }
        // above half health
        if (!aggressive) {
            bossAILogger.info("Boss AI is not aggressive");
            setAggressive(entity);
            aggressive = true;
        }
        if (AITools.playerInRange(entity, range))
            bossAILogger.info("Boss AI is in range");
            new DarkKnight(Game.getLevel()).getComponent(PositionComponent.class)
                    .ifPresent(
                            (x) -> {
                                PositionComponent h = (PositionComponent) x;
                                h.setPosition(AITools.getRandomAccessibleTileCoordinateInRange(
                                        entityPosition(Game.getHero().get()), 2).toPoint());
                            });
        SECOND_SKILL.execute(entity);
        // END
    }

    private float getHealthRatio(Entity entity) {
        bossAILogger.info("getting health ratio");
        if (!entity.getComponent(HealthComponent.class).isPresent())
            return 0f;
        HealthComponent hc = (HealthComponent) entity.getComponent(HealthComponent.class).get();
        return (float) hc.getCurrentHealthpoints() / (float) hc.getMaximalHealthpoints();
    }

    /**
     * Makes the Boss aggressive by double his speed.
     * @param entity
     */
    private void setAggressive(Entity entity) {
        bossAILogger.info("setting BossAI to aggressive");
        entity.getComponent(VelocityComponent.class)
                .ifPresent(
                        (x) -> {
                            VelocityComponent v = (VelocityComponent) x;
                            v.setXVelocity(v.getXVelocity() * 2);
                            v.setYVelocity(v.getYVelocity() * 2);
                        });
    }

    /**
     * Gets the position of the Boss.
     * @param entity
     * @return
     */
    private Point entityPosition(Entity entity) {
        bossAILogger.info("getting entity position");
        return ((PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                        () -> new MissingComponentException(
                                "PositionComponent")))
                .getPosition();
    }
}
