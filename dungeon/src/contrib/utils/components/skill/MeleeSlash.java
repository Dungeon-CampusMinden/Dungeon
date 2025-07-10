package contrib.utils.components.skill;

import contrib.components.CollideComponent;
import contrib.components.HealthComponent;
import contrib.components.SpikyComponent;
import contrib.systems.EventScheduler;
import contrib.utils.IAction;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;
import core.Game;
import core.components.DrawComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;
import core.level.Tile;
import core.utils.Point;
import core.utils.TriConsumer;
import core.utils.components.path.IPath;
import core.utils.components.path.SimpleIPath;

import java.io.IOException;
import java.util.function.Consumer;

public class MeleeSlash extends Skill{

    private static final IPath SLASH_ANIMATION = new SimpleIPath("skills/slash");
    private static final float OFFSET=1f;

    private static final Consumer<Entity> SKILL_FUNCTION = entity -> {
        PositionComponent pc = entity.fetch(PositionComponent.class).orElseThrow();
        PositionComponent.Direction viewDirection = pc.viewDirection();
        Point position = pc.position();

        //move spawn position to the viewdirection
        switch (viewDirection){
            case UP -> position.y+=OFFSET;
            case DOWN -> position.y-=OFFSET;
            case LEFT -> position.x-=OFFSET;
            case RIGHT -> position.x+=OFFSET;
        }

        Entity swordSlash = new Entity();
        swordSlash.add(new PositionComponent(position));
        //todo move the slash with the caster
        //swordSlash.add(entity.fetch(VelocityComponent.class).get());
        try {
            DrawComponent dc =new DrawComponent(SLASH_ANIMATION);
            swordSlash.add(dc);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        swordSlash.add(new CollideComponent((a, b, direction) -> {
            if(b!=entity){
                b.fetch(HealthComponent.class).ifPresent(healthComponent -> healthComponent.receiveHit(new Damage(200,DamageType.PHYSICAL,entity)));
            }
        },CollideComponent.DEFAULT_COLLIDER));
        Game.add(swordSlash);

        EventScheduler.scheduleAction(() -> Game.remove(swordSlash),1000);
    };

    /**
     * Create a new {@link Skill}.
     *
     * @param coolDownInMilliSeconds The time that needs to pass between use of the skill and the next
     *                               possible use of the skill.
     */
    public MeleeSlash(int coolDownInMilliSeconds) {
        super(SKILL_FUNCTION, coolDownInMilliSeconds);
    }





}
