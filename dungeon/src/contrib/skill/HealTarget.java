package contrib.skill;

import contrib.components.HealthComponent;
import core.Entity;
import core.Game;
import core.utils.Point;

import java.util.function.BiConsumer;

public class HealTarget extends CursorSkill{

    int healAmount;

    public HealTarget healTarget(int healAmount, int cooldown){
        return new CursorSkill("Heal target", 500, new BiConsumer<Entity, Point>() {
            @Override
            public void accept(Entity entity, Point point) {
                Game.entityAtTile(Game.tileAt(point).get()).findFirst().ifPresent(
                        target -> target.fetch(HealthComponent.class).ifPresent(hc -> hc.restoreHealthpoints(healAmount))
                );
            }
        });
        }

}
