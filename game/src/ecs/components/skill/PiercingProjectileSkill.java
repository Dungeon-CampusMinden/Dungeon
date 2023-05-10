package game.src.ecs.components.skill;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.collision.ICollide;
import ecs.components.skill.*;
import ecs.damage.Damage;
import ecs.entities.Entity;
import graphic.Animation;
import starter.Game;
import tools.Point;

import ecs.components.skill.DamageProjectileSkill;
import ecs.components.skill.ITargetSelection;
import ecs.components.skill.SkillTools;

public abstract class PiercingProjectileSkill extends DamageProjectileSkill {

    public PiercingProjectileSkill(
        String pathToTexturesOfProjectile,
        float projectileSpeed,
        Damage projectileDamage,
        Point projectileHitboxSize,
        ITargetSelection selectionFunction,
        float projectileRange) {
            super(pathToTexturesOfProjectile, projectileSpeed, projectileDamage, projectileHitboxSize, selectionFunction, projectileRange);
        }

    @Override
    public void execute(Entity entity) {
        Entity projectile = new Entity();
        PositionComponent epc =
                (PositionComponent)
                        entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                        () -> new MissingComponentException("PositionComponent"));
        new PositionComponent(projectile, epc.getPosition());

        Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfProjectile);
        new AnimationComponent(projectile, animation);

        Point aimedOn = selectionFunction.selectTargetPoint();
        Point targetPoint =
                SkillTools.calculateLastPositionInRange(
                        epc.getPosition(), aimedOn, projectileRange);
        Point velocity =
                SkillTools.calculateVelocity(epc.getPosition(), targetPoint, projectileSpeed);
        VelocityComponent vc =
                new VelocityComponent(projectile, velocity.x, velocity.y, animation, animation);
        new ProjectileComponent(projectile, epc.getPosition(), targetPoint);
        ICollide collide =
                (a, b, from) -> {
                    if (b != entity) {
                        b.getComponent(HealthComponent.class)
                                .ifPresent(
                                        hc -> {
                                            ((HealthComponent) hc).receiveHit(projectileDamage);
                                        });
                        b.getComponent(VelocityComponent.class)
                                        .ifPresent(vlc -> {
                                                ((VelocityComponent) vlc).setCurrentXVelocity(
                                                                Point.getUnitDirectionalVector(
                                                                                ((PositionComponent) b
                                                                                                .getComponent(PositionComponent.class)
                                                                                                .get())
                                                                                                .getPosition(),
                                                                                ((ProjectileComponent) projectile
                                                                                                .getComponent(ProjectileComponent.class)
                                                                                                .get())
                                                                                                .getStartPosition()).x
                                                                                * 2f);
                                                ((VelocityComponent) vlc).setCurrentYVelocity(
                                                                Point.getUnitDirectionalVector(
                                                                                ((PositionComponent) b
                                                                                                .getComponent(PositionComponent.class)
                                                                                                .get())
                                                                                                .getPosition(),
                                                                                ((ProjectileComponent) projectile
                                                                                                .getComponent(ProjectileComponent.class)
                                                                                                .get())
                                                                                                .getStartPosition()).y
                                                                                * 2f);
                                        });
                    }
                };

        new HitboxComponent(
                projectile, new Point(0.25f, 0.25f), projectileHitboxSize, collide, null);
    }

}
