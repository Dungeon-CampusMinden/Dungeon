package ecs.components.skill;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.components.collision.ICollide;
import ecs.damage.Damage;
import ecs.entities.Entity;
import graphic.Animation;
import starter.Game;
import tools.Point;

public class DamageMeleeSkill implements ISkillFunction {

        private String pathToTexturesOfProjectile;
        private float projectileSpeed;

        private static final float projectileRange = 0.5f;
        private Damage projectileDamage;
        private Point projectileHitboxSize;

        private ITargetSelection selectionFunction;

        public DamageMeleeSkill(
                        String pathToTexturesOfProjectile,
                        float projectileSpeed,
                        Damage projectileDamage,
                        Point projectileHitboxSize,
                        ITargetSelection selectionFunction) {
                this.pathToTexturesOfProjectile = pathToTexturesOfProjectile;
                this.projectileDamage = projectileDamage;
                this.projectileSpeed = projectileSpeed;
                this.projectileHitboxSize = projectileHitboxSize;
                this.selectionFunction = selectionFunction;
        }

        @Override
        public void execute(Entity entity) {
                Entity projectile = new Entity();
                PositionComponent epc = (PositionComponent) entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                                () -> new MissingComponentException("PositionComponent"));
                new PositionComponent(projectile, epc.getPosition());

                Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfProjectile);
                new AnimationComponent(projectile, animation);

                Point aimedOn = selectionFunction.selectTargetPoint();
                Point targetPoint = SkillTools.calculateLastPositionInRange(
                                epc.getPosition(), aimedOn, projectileRange);
                Point velocity = SkillTools.calculateVelocity(epc.getPosition(), targetPoint, projectileSpeed);
                VelocityComponent vc = new VelocityComponent(projectile, velocity.x, velocity.y, animation, animation);
                new ProjectileComponent(projectile, epc.getPosition(), targetPoint);
                ICollide collide = (a, b, from) -> {
                        if (b != entity) {
                                b.getComponent(HealthComponent.class)
                                                .ifPresent(
                                                                hc -> {
                                                                        ((HealthComponent) hc)
                                                                                        .receiveHit(projectileDamage);
                                                                });
                                b.getComponent(VelocityComponent.class)
                                                .ifPresent(
                                                                vlc -> {
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
