package ecs.components.skill;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.collision.ICollide;
import ecs.damage.Damage;
import ecs.entities.Entity;
import graphic.Animation;
import level.elements.tile.Tile;
import starter.Game;
import tools.Point;

import static starter.Game.currentLevel;

public class MeleeSkill implements ISkillFunction {

    private String pathToTexturesOfProjectile;
    private float projectileSpeed;

    private float projectileRange;
    private Damage projectileDamage;
    private Point projectileHitboxSize;

    private ITargetSelection selectionFunction;
    private float knockbackDistance;


    public MeleeSkill(
        String pathToTexturesOfProjectile,
        float projectileSpeed,
        Damage projectileDamage,
        Point projectileHitboxSize,
        float knockbackDistance,
        ITargetSelection selectionFunction,
        float projectileRange) {
        this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
        this.projectileDamage = projectileDamage;
        this.projectileSpeed = projectileSpeed;
        this.projectileRange = projectileRange;
        this.projectileHitboxSize = projectileHitboxSize;
        this.selectionFunction = selectionFunction;
        this.knockbackDistance = knockbackDistance;
    }

    @Override
    public void execute(Entity entity) {
        Entity meleeAttack = new Entity();
        PositionComponent epc =
            (PositionComponent)
                entity.getComponent(PositionComponent.class)
                    .orElseThrow(
                        () -> new MissingComponentException("PositionComponent"));
        new PositionComponent(meleeAttack, epc.getPosition());

        Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfProjectile);
        new AnimationComponent(meleeAttack, animation);

        Point aimedOn = selectionFunction.selectTargetPoint();
        Point targetPoint =
            SkillTools.calculateLastPositionInRange(
                epc.getPosition(), aimedOn, projectileRange);
        Point velocity =
            SkillTools.calculateVelocity(epc.getPosition(), targetPoint, projectileSpeed);
        VelocityComponent vc =
            new VelocityComponent(meleeAttack, velocity.x, velocity.y, animation, animation);
        new ProjectileComponent(meleeAttack, epc.getPosition(), targetPoint);

        ICollide collide =
            (a, b, from) -> {
                if (b != entity) {
                    b.getComponent(HealthComponent.class)
                        .ifPresent(
                            hc -> {
                                ((HealthComponent) hc).receiveHit(projectileDamage);
                                applyKnockback(b, entity, knockbackDistance);
                                Game.removeEntity(meleeAttack);
                            });
                }
            };

        new HitboxComponent(
            meleeAttack, new Point(0.25f, 0.25f), projectileHitboxSize, collide, null);
    }

    public void applyKnockback(Entity target, Entity entity, float knockbackDistance) {
        PositionComponent targetPositionComponent =
            (PositionComponent) target.getComponent(PositionComponent.class)
                .orElseThrow(
                    () -> new MissingComponentException("PositionComponent for target"));
        PositionComponent entityPositionComponent =
            (PositionComponent) entity.getComponent(PositionComponent.class)
                .orElseThrow(
                    () -> new MissingComponentException("PositionComponent for entity"));

        Point direction = Point.getUnitDirectionalVector(targetPositionComponent.getPosition(), entityPositionComponent.getPosition());

        Point newPosition = new Point(


            targetPositionComponent.getPosition().x + direction.x * knockbackDistance,
            targetPositionComponent.getPosition().y + direction.y * knockbackDistance
        );

        Tile newTile = currentLevel.getTileAt(newPosition.toCoordinate());
       if(newTile.isAccessible()){
              targetPositionComponent.setPosition(newPosition);
       }

    }
}
