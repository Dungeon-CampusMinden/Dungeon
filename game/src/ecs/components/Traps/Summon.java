package game.src.ecs.components.Traps;

import ecs.entities.Entity;
import game.src.ecs.entities.Imp;
import starter.Game;

import java.lang.reflect.*;

public class Summon implements ITrigger {

    public static final Class IMP = Imp.class;

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
