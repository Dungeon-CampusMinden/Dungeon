package ecs.entities;

import ecs.components.AnimationComponent;
import ecs.components.HitboxComponent;
import ecs.components.collision.ICollide;
import graphic.Animation;
import level.elements.tile.Tile;

import java.util.List;

public class Teleportpads extends Traps implements ICollide {

    private Teleportsystem mySystem;

    /**Teleportpads aussehen verändern*/


    public static final List<String> DEFAULT_UNUSED_ANIMATION_FRAMES =
        List.of("dungeon/ice/floor/floor_1.png");

    public static final List<String> DEFAULT_USED_ANIMATION_FRAMES =
        List.of("dungeon/ice/floor/floor_ladder.png");

    /**teleportpads werden mit nutzungen und refferenz auf das Teleportsystem*/

    public Teleportpads(int usages, Teleportsystem mySystem) {
        super();
        this.usages = usages;
        this.mySystem = mySystem;
        new HitboxComponent(this, this, this::onCollisionleave);
        setupAnimationComponent();
    }

    /** stellt Animationen zum änddern des Aussehens der Falle*/
    private void setupAnimationComponent() {
        AnimationComponent ac =
            new AnimationComponent(
                this,
                new Animation(DEFAULT_UNUSED_ANIMATION_FRAMES, 100, false),
                new Animation(DEFAULT_USED_ANIMATION_FRAMES, 100, false));
    }

    /** benachrichtigt das Teleportsystem über die nutzung des Pads und ändert dann das Aussehens*/

    public void Aniused(Entity entity) {
        mySystem.usedPad(this);
        entity.getComponent(AnimationComponent.class)
            .map(AnimationComponent.class::cast)
            .ifPresent(x -> x.setCurrentAnimation(x.getIdleRight()));
    }
    /**Ändert das Aussehen der Pads zum ungenutzten*/

    public void Aniusable(Entity entity) {
        entity.getComponent(AnimationComponent.class)
            .map(AnimationComponent.class::cast)
            .ifPresent(x -> x.setCurrentAnimation(x.getIdleLeft()));
    }

    /** wenn der Held das Pad betritt wird die Methoden Aniused und reduceUsages*/


    @Override
    public void onCollision(Entity a, Entity b, Tile.Direction from) {
        if(b instanceof Hero){
            Teleportpads trap = (Teleportpads) a;
            trap.Aniused(this);
            trap.reduceUsages();
        }

    }

    /** Wenn der Held das Pad verlässt wird das Teleportsystem benachrichtigt das man sich wieder Teleportieren kann*/


    public void onCollisionleave(Entity a, Entity b, Tile.Direction from) {
        Teleportpads trap = (Teleportpads) a;
        trap.getTeleportsystem().setUsable();

    }

    public Teleportsystem getTeleportsystem(){
        return mySystem;
    }

    public void reduceUsages(){
        usages-=1;
    }
}
