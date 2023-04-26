package game.src.ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.skill.*;
import ecs.damage.Damage;
import graphic.Animation;
import ecs.components.OnDeathFunctions.EndGame;
import ecs.components.ai.idle.*;
import ecs.components.ai.transition.ITransition;
import ecs.components.ai.transition.RangeTransition;
import ecs.components.ai.transition.SelfDefendTransition;
import ecs.entities.Entity;
import game.src.ecs.components.ai.idle.CircleWalk;
import starter.Game;
import ecs.components.ai.idle.*;
import java.lang.Math;

public class Monster extends Entity {

    private final float xSpeed = 0.0f;
    private final float ySpeed = 0.0f;
    private final int maxHealth = 100;
    private int level = 0;

    private final String pathToIdleLeft = "";
    private final String pathToIdleRight = "";
    private final String pathToRunLeft = "";
    private final String pathToRunRight = "";
    private final String pathToGetHit = "";
    private final String pathToDie = "";

    public Monster(int level) {
        super();
    }

    protected IIdleAI setupIdleStrategy() {
        int random = (int) (Math.random() * 4);
        if (random == 0) {
            return new PatrouilleWalk(3.0f, 5, 1000, PatrouilleWalk.MODE.RANDOM);
        } else if (random == 1) {
            return new RadiusWalk(3, 2);
        } else if (random == 2) {
            return new StaticRadiusWalk(3, 2);
        } else {
            return new CircleWalk(2, 1);
        }
    }

    protected ITransition setupTransition() {
        int random = (int) (Math.random() * 2);
        if (random == 0) {
            return new SelfDefendTransition();
        } else {
            return new RangeTransition(2.0f);
        }
    }

    protected void getRandomSpawn() {
        Game.currentLevel.getRandomFloorTile();
    }

}
