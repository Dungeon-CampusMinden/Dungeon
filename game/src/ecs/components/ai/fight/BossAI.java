package ecs.components.ai.fight;

import com.badlogic.gdx.ai.pfa.GraphPath;
import ecs.components.HealthComponent;
import ecs.components.MissingComponentException;
import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.components.skill.Skill;
import ecs.entities.DarkKnight;
import ecs.entities.Entity;
import level.elements.tile.Tile;
import starter.Game;
import tools.Constants;
import tools.Point;

public class BossAI implements IFightAI{
    private final int breakTime = 2 * Constants.FRAME_RATE;
    private int currentBreak = 0;
    private Skill firstSkill;
    private Skill secondSkill;
    private GraphPath<Tile> path;

    public BossAI(Skill fistSkill, Skill secondSkill){
        if (Game.getHero().isEmpty()) {
            throw new Error("There must be a Hero in the Game!");
        }
        this.firstSkill = fistSkill;
        this.secondSkill = secondSkill;
    }

    @Override
    public void fight(Entity entity) {
        if(getHealth(entity) > 0.5) {
            if (AITools.playerInRange(entity, 4)) {
                if (currentBreak >= breakTime) {
                    currentBreak = 0;
                    new DarkKnight(Game.getLevel());
                }
            }
            firstSkill.execute(entity);
            currentBreak++;
        }
        else {
            if (AITools.playerInRange(entity, 4)) {
                if (currentBreak >= breakTime) {
                    currentBreak = 0;
                    new DarkKnight(Game.getLevel()).
                        getComponent(PositionComponent.class)
                        .ifPresent(
                            (x) -> {
                                PositionComponent h = (PositionComponent) x;
                                h.setPosition(AITools.getRandomAccessibleTileCoordinateInRange(entityPosition(Game.getHero().get()),2).toPoint());
                            }
                        );
                }
                path = AITools.calculatePathToHero(entity);
                AITools.move(entity, path);
                secondSkill.execute(entity);
            }
            currentBreak++;
        }
    }

    public float getHealth(Entity entity){
        float[] health = new float[1];
        entity.
                getComponent(HealthComponent.class)
                    .ifPresent(
                        (x) -> {
                            HealthComponent h = (HealthComponent) x;
                            health[0] = (float)h.getCurrentHealthpoints()/(float)h.getMaximalHealthpoints();
                        }
                    );
        return health[0];
    }

    private Point entityPosition(Entity entity) {
        return ((PositionComponent)
            entity.getComponent(PositionComponent.class)
                .orElseThrow(
                    () ->
                        new MissingComponentException(
                            "PositionComponent")))
            .getPosition();
    }
}
