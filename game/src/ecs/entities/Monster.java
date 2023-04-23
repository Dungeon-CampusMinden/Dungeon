package ecs.entities;
import ecs.components.*;

/**
 * The monster is an entity. It is defined as an abstract class because we never have a Monster object.
 * All future existing monsters will inherit from this class
 */

public class Monster extends Entity {

    private float xSpeed = 0;
    private float ySpeed = 0;
    private int dmg = 0;

    /** Entity with Components */
    public Monster(){
        super();

        new PositionComponent(this);
       // setupHealthComponent();
    }




}
