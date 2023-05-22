package ecs.components.Traps;

import ecs.entities.Entity;
import ecs.entities.Chort;
import ecs.entities.DarkKnight;
import ecs.entities.Imp;
import starter.Game;

import java.lang.reflect.*;

/**
 * Trap-Strategy to summon 3 monsters of the same class
 */
public class Summon implements ITrigger {

    public static final Class IMP = Imp.class;
    public static final Class CHORT = Chort.class;
    public static final Class DARKKNIGHT = DarkKnight.class;

    private Class klass;

    public Summon(Class klass) {
        this.klass = klass;
    }

    public void trigger(Entity entity) {
        try {
            Constructor constructor = klass.getConstructor(int.class);
            Game.addEntity((Entity) (constructor.newInstance(Game.getLevel())));
            Game.addEntity((Entity) (constructor.newInstance(Game.getLevel())));
            Game.addEntity((Entity) (constructor.newInstance(Game.getLevel())));
        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

}
