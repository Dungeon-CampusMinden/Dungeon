package ecs.entities;

import dslToGame.AnimationBuilder;
import ecs.components.AnimationComponent;
import ecs.components.HealthComponent;
import ecs.components.HitboxComponent;
import ecs.components.PositionComponent;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import ecs.graphic.Animation;

import java.util.ArrayList;

public class BearTrap extends Trap{
    private final String idle = "character/monster/beartrap";

    ArrayList<Entity> inRange = new ArrayList<>();

    public BearTrap(){
        super();
        new PositionComponent(this);
        setupAnimationComponent();
        setupHitboxComponent();
    }

    private void setupAnimationComponent() {
        Animation idleRight = AnimationBuilder.buildAnimation(idle);
        new AnimationComponent(this, idleRight);
    }

    private void setupHitboxComponent() {
        new HitboxComponent(
            this,
            (you, other, direction) -> triggerAction(other),
            (you, other, direction) -> System.out.println("Leaves Hitbox"));
    }

    void triggerAction(Entity other) {
        this.inRange.add(other);
        if(!this.isTriggered()) doDmg(other);
        if(other.getComponent(HealthComponent.class).isPresent()) this.setTriggered(true);
    }
    public void doDmg(Entity other){
            if(other.getComponent(HealthComponent.class).isPresent()){
                HealthComponent ofE = (HealthComponent) other.getComponent(HealthComponent.class).get();

                System.out.println("HP before:"+ ofE.getCurrentHealthpoints());
                ofE.receiveHit(new Damage((int) this.getTrapDmg(), DamageType.PHYSICAL, this));
                System.out.println("HP after:"+ ofE.getCurrentHealthpoints());
            }
        }
}


