package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.components.VelocityComponent;
import graphic.Animation;
import level.tools.Coordinate;
import starter.Game;
import ecs.entities.Hero;
import java.util.List;
import tools.Point;
import java.util.logging.Logger;
import level.tools.LevelElement;

/** A trap that slow the player when go walks over it.  */
public class SlowTrap extends Trap {
    // private final String pathToIdleLeft = "monster/imp/idleLeft";
    // private final String pathToIdleRight = "monster/imp/idleRight"
    public static final int frame_time_idle = 1;
    public static final int frame_time_triggered = 1;
    public static final List<String> DEFAULT_IDLE_ANIMATION_FRAMES =
            List.of("objects/slowtrap/slowtrap_idle_anim_f0.png")                    ;
    public static final List<String> DEFAULT_TRIGGERED_ANIMATION_FRAMES =
            List.of("objects/slowtrap/slowtrap_triggered_anim_f0.png");


    public SlowTrap(Point position){
        super(
            frame_time_idle,
            frame_time_triggered,
            DEFAULT_IDLE_ANIMATION_FRAMES,
            DEFAULT_TRIGGERED_ANIMATION_FRAMES,
            false,
            position
        );

        setupHitboxComponent();
    }

    /**
     * creates Instance of SlowTrap with random position
     * 
     * @return a new SlowTrap with a random position
     */
    public static SlowTrap createSlowTrap() {
        return new SlowTrap(
        Game.currentLevel.getRandomTile(LevelElement.FLOOR).getCoordinate().toPoint());
    }

    private void setupHitboxComponent(){
        // TODO: Refactoring

        new HitboxComponent(this,
            (you, other, direction) -> {
                System.out.println("TrapCollisionEnter");
                System.out.println();
                if (Game.getHero().isPresent()){
                    Hero hero = (Hero)Game.getHero().get();
                    if (other.equals(hero)){
                        onTrigger(this);
                    }
                }
            },
            (you, other, direction) -> System.out.println("TrapCollisionLeave")
        );
    }

    private void onTrigger(Entity entity) {
        // TODO: Remove this
        Logger logger = Logger.getLogger("SlowTrap");
        logger.info("SlowTrap triggered" + entity.toString());

        Game.getHero().stream()
            .flatMap(e -> e.getComponent(VelocityComponent.class).stream())
            .map(VelocityComponent.class::cast)
            .forEach(VelocityComponent -> {
                VelocityComponent.setXVelocity(0.1f);
                VelocityComponent.setYVelocity(0.1f);
            });

        entity.getComponent(AnimationComponent.class)
            .map(AnimationComponent.class::cast)
            .ifPresent(x -> x.setCurrentAnimation(x.getIdleRight()));
    }


}
