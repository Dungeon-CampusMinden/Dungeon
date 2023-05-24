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

public class BossAI implements IFightAI {
    private final int BREAK_TIME = 2 * Constants.FRAME_RATE;
    private int currentBreak = 0;
    private final Skill FIRST_SKILL;
    private final Skill SECOND_SKILL;
    private GraphPath<Tile> path;
    private boolean aggressive;
    private float range;

    public BossAI(Skill fistSkill, Skill secondSkill, float range) {
        if (Game.getHero().isEmpty()) {
            throw new Error("There must be a Hero in the Game!");
        }
        this.FIRST_SKILL = fistSkill;
        this.SECOND_SKILL = secondSkill;
        this.range = range;
    }

    @Override
    public void fight(Entity entity) {
        if (aggressive) {
            path = AITools.calculatePathToHero(entity);
            AITools.move(entity, path);
        }
        currentBreak++;
        if (currentBreak < BREAK_TIME) {
            return; // END
        }
        // Is not on break
        currentBreak = 0;
        if (getHealthRatio(entity) > 0.5) {
            FIRST_SKILL.execute(entity);
            if (AITools.playerInRange(entity, range)) {
                new DarkKnight(Game.getLevel());
            }
            return; // END
        }
        // above half health
        if (!aggressive) {
            setAggressive(entity);
            aggressive = true;
        }
        if (AITools.playerInRange(entity, range))
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
        if (!entity.getComponent(HealthComponent.class).isPresent())
            return 0f;
        HealthComponent hc = (HealthComponent) entity.getComponent(HealthComponent.class).get();
        return (float) hc.getCurrentHealthpoints() / (float) hc.getMaximalHealthpoints();
    }

    private void setAggressive(Entity entity) {
        entity.getComponent(VelocityComponent.class)
                .ifPresent(
                        (x) -> {
                            VelocityComponent v = (VelocityComponent) x;
                            v.setXVelocity(v.getXVelocity() * 2);
                            v.setYVelocity(v.getYVelocity() * 2);
                        });
    }

    private Point entityPosition(Entity entity) {
        return ((PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                        () -> new MissingComponentException(
                                "PositionComponent")))
                .getPosition();
    }
}
