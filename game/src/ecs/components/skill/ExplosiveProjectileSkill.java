package ecs.components.skill;

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

import ecs.damage.DamageType;

public abstract class ExplosiveProjectileSkill extends DamageProjectileSkill {

        public static final String DEFAULT_EXPLOSION_TEXTURES = "skills/explosion/default";
        public static final Damage DEFAULT_EXPLOSION_DAMAGE = new Damage(40, DamageType.FIRE, null);
        public static final Point DEFAULT_EXPLOSION_SIZE = new Point(15, 15);

        private String pathToTexturesOfExplosion;
        private Damage explosionDamage;
        private Point explosionSize;

        public ExplosiveProjectileSkill(
                        String pathToTexturesOfProjectile,
                        float projectileSpeed,
                        Damage projectileDamage,
                        Point projectileHitboxSize,
                        ITargetSelection selectionFunction,
                        float projectileRange) {
                super(pathToTexturesOfProjectile, projectileSpeed, projectileDamage, projectileHitboxSize,
                                selectionFunction, projectileRange);
                pathToTexturesOfExplosion = DEFAULT_EXPLOSION_TEXTURES;
                explosionDamage = DEFAULT_EXPLOSION_DAMAGE;
                explosionSize = DEFAULT_EXPLOSION_SIZE;
        }

        public ExplosiveProjectileSkill(
                        String pathToTexturesOfProjectile,
                        float projectileSpeed,
                        Damage projectileDamage,
                        Point projectileHitboxSize,
                        ITargetSelection selectionFunction,
                        float projectileRange,
                        String pathToTexturesOfExplosion,
                        Damage explosionDamage,
                        Point explosionSize) {
                super(pathToTexturesOfProjectile, projectileSpeed, projectileDamage, projectileHitboxSize,
                                selectionFunction, projectileRange);
                this.pathToTexturesOfExplosion = pathToTexturesOfExplosion;
                this.explosionDamage = explosionDamage;
                this.explosionSize = explosionSize;
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
                                                                        newExplosion(projectile);
                                                                        Game.removeEntity(projectile);
                                                                });
                                SkillTools.knockBack(a, b);
                        }
                };

                new HitboxComponent(
                                projectile, new Point(0.25f, 0.25f), projectileHitboxSize, collide, null);
        }

        private void newExplosion(Entity entity) {
                Entity explosion = new Entity();
                PositionComponent epc = (PositionComponent) entity.getComponent(PositionComponent.class)
                                .orElseThrow(
                                                () -> new MissingComponentException("PositionComponent"));
                new PositionComponent(explosion, epc.getPosition());

                Animation animation = AnimationBuilder.buildAnimation(pathToTexturesOfExplosion);
                new AnimationComponent(explosion, animation);

                Point aimedOn = selectionFunction.selectTargetPoint();
                Point targetPoint = new Point(epc.getPosition().x + 0.003f, epc.getPosition().y + 0.003f);
                Point velocity = SkillTools.calculateVelocity(epc.getPosition(), targetPoint, 0.001f);
                VelocityComponent vc = new VelocityComponent(explosion, velocity.x, velocity.y, animation, animation);
                new ProjectileComponent(explosion, epc.getPosition(), targetPoint);
                ICollide collide = (a, b, from) -> {
                        if (b != entity) {
                                b.getComponent(HealthComponent.class)
                                                .ifPresent(
                                                                hc -> {
                                                                        ((HealthComponent) hc)
                                                                                        .receiveHit(explosionDamage);
                                                                });
                                SkillTools.knockBack(a, b);
                        }
                };

                new HitboxComponent(
                                explosion, new Point(0.25f, 0.25f), explosionSize, collide, null);
        }

}
