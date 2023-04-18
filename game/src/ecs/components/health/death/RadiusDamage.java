package ecs.components.health.death;

import ecs.components.PositionComponent;
import ecs.components.ai.AITools;
import ecs.components.health.HealthComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.entities.AnimationEntity;
import ecs.entities.Entity;
import graphic.Animation;
import graphic.textures.TextureHandler;
import java.util.List;
import starter.Game;

public class RadiusDamage implements IOnDeathFunction {

    private static List<String> frames_explosion =
            TextureHandler.getInstance().getTexturePaths("animation/explosion/white");

    private final float range;
    private final int damage;

    public RadiusDamage() {
        this(3.0f, 2);
    }

    public RadiusDamage(float range, int damage) {
        this.range = range;
        this.damage = damage;
    }

    @Override
    public void onDeath(Entity entity) {

        entity.getComponent(PositionComponent.class)
                .ifPresent(
                        component -> {
                            AnimationEntity explosion =
                                    new AnimationEntity(
                                            new Animation(frames_explosion, 30 / 12, false),
                                            ((PositionComponent) component).getPosition());
                        });

        Game.getEntities().stream()
                .filter(e -> AITools.entityInRange(entity, e, range))
                .forEach(
                        e -> {
                            e.getComponent(HealthComponent.class)
                                    .ifPresent(
                                            component -> {
                                                ((HealthComponent) component)
                                                        .receiveHit(
                                                                new Damage(
                                                                        damage,
                                                                        DamageType.EXPLOSION,
                                                                        entity));
                                            });
                        });
    }
}
