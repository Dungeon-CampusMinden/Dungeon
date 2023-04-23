package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.*;
import ecs.graphic.Animation;

import java.util.ArrayList;

/**
 * An ap-mine that deals damage to all entities within it's hitbox that have a healthcomponent
 *
 */
public class Mine extends Trap{

    private final String idle = "character/monster/apmine";
    ArrayList<Entity> inRange = new ArrayList<>();

    public Mine(){
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupHitboxComponent();
        this.setTrapDmg(1);
    }


    /**
     * If entities enters hitbox add it to the entites inRange list, then check if the trap was already triggered and call doDmg if not
     * Set triggered to true afterwards
     *
     */
    void triggerAction(Entity other) {
        this.inRange.add(other);
        if(!this.isTriggered()) doDmg();
        if(other.getComponent(HealthComponent.class).isPresent()) this.setTriggered(true);
    }

    /** Deletes entitie from inRange list**/
    public void deleteFromHitbox(Entity other){
        this.inRange.remove(other);
    }

    /**
     *  Iterate through inRange list and check if entitie has health component and deal damage if true
     */
    public void doDmg(){
        for(Entity e : inRange){
            if(e.getComponent(HealthComponent.class).isPresent()){
                HealthComponent ofE = (HealthComponent) e.getComponent(HealthComponent.class).get();

                System.out.println("HP before:"+ ofE.getCurrentHealthpoints());
                ofE.setCurrentHealthpoints(ofE.getCurrentHealthpoints()-(int)this.getTrapDmg());
                System.out.println("HP after:"+ ofE.getCurrentHealthpoints());
            }
        }
    }


    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(idle);
        new AnimationComponent(this, idleRight);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> triggerAction(other),
            (you, other, direction) -> deleteFromHitbox(other));
    }


}
