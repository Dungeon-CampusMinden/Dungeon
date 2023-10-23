package graphconverter;

import contrib.entities.EntityFactory;

import core.Entity;

import task.Task;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TaskBuilder {

    public static void DUMMY_TASK_BUILDER(Task t) {
        Set<Set<Entity>> sets = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            Set<Entity> s = new HashSet<>();
            try {
                s.add(EntityFactory.newChest());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            sets.add(s);
        }
        t.entitieSets(sets);
        // THIS IS A DUMMY
    }
}
