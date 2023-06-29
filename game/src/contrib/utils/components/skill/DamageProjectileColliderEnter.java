package contrib.utils.components.skill;

import contrib.components.HealthComponent;
import contrib.utils.components.collision.ICollide;
import contrib.utils.components.health.Damage;
import core.Entity;
import core.Game;
import core.level.Tile;

public class DamageProjectileColliderEnter implements ICollide {

    private Entity entity;
    private Damage projectileDamage;
    private Entity projectile;

    public DamageProjectileColliderEnter(Entity entity, Damage projectileDamage, Entity projectile){
        this.entity = entity;
        this.projectileDamage = projectileDamage;
        this.projectile = projectile;
    }
    @Override
    public void onCollision(Entity a, Entity b, Tile.Direction from) {
        if (b != entity) {
            b.getComponent(HealthComponent.class)
                .ifPresent(
                    hc -> {
                        ((HealthComponent) hc).receiveHit(projectileDamage);
                        Game.removeEntity(projectile);
                    });
        }
    }
}
