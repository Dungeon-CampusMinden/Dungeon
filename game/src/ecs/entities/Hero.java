package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.AnimationComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import ecs.components.skill.ISkillFunction;
import ecs.components.skill.Skill;
import ecs.components.skill.SkillComponent;
import graphic.Animation;
import level.elements.tile.Tile;
import tools.Point;

public class Hero extends Entity {

    private Skill firstSkill;
    private Skill secondSkill;
    /**
     * Entity with Components
     *
     * @param startPosition position at start
     */
    public Hero(Point startPosition) {
        super();
        new PositionComponent(this, startPosition);
        new PlayableComponent(this);
        SkillComponent sc = new SkillComponent(this);
        new HitboxComponent(this, (a, b, c) -> System.out.println("heroCollision"));

        ISkillFunction function = (e -> System.out.println("HERO SKILL"));
        firstSkill = new Skill(function, 3);
        secondSkill = new Skill(function, 3);
        sc.addSkill(firstSkill);
        sc.addSkill(secondSkill);
        setupAnimationComponent();
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation("knight/idleRight");
        Animation idleLeft = AnimationBuilder.buildAnimation("knight/idleLeft");
        Animation moveRight = AnimationBuilder.buildAnimation("knight/runRight");
        Animation moveLeft = AnimationBuilder.buildAnimation("knight/runLeft");

        new AnimationComponent(this, idleLeft, idleRight);

        new VelocityComponent(this, 0.3f, 0.3f, moveLeft, moveRight);
    }

    public static void heroCollision(HitboxComponent other, Tile.Direction from) {
        System.out.println("HERO COLLISION");
    }

    /**
     * @return the first skill
     */
    public Skill getFirstSkill() {
        return firstSkill;
    }

    /**
     * @return the second skill
     */
    public Skill getSecondSkill() {
        return secondSkill;
    }
}
