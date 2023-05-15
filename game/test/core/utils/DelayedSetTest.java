package core.utils;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class DelayedSetTest {

    @Test
    public void testAdd() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        set.add(toAdd);
        assertFalse(set.getSet().contains(toAdd));
        set.update();
        assertTrue(set.getSet().contains(toAdd));
    }

    @Test
    public void testAddAll() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        set.addAll(addSet);
        assertFalse(set.getSet().contains(toAdd1));
        assertFalse(set.getSet().contains(toAdd2));
        assertFalse(set.getSet().contains(toAdd3));
        set.update();
        assertTrue(set.getSet().contains(toAdd1));
        assertTrue(set.getSet().contains(toAdd2));
        assertTrue(set.getSet().contains(toAdd3));
    }

    @Test
    public void testRemove() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        set.add(toAdd);
        set.update();
        set.remove(toAdd);
        assertTrue(set.getSet().contains(toAdd));
        set.update();
        assertFalse(set.getSet().contains(toAdd));
    }

    @Test
    public void testRemoveAll() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        set.addAll(addSet);
        set.update();
        addSet.remove(toAdd2);
        set.removeAll(addSet);
        assertTrue(set.getSet().contains(toAdd1));
        assertTrue(set.getSet().contains(toAdd2));
        assertTrue(set.getSet().contains(toAdd3));
        set.update();
        assertFalse(set.getSet().contains(toAdd1));
        assertTrue(set.getSet().contains(toAdd2));
        assertFalse(set.getSet().contains(toAdd3));
    }

    @Test
    public void clear() {
        DelayedSet<String> set = new DelayedSet<>();
        set.add("3");
        set.update();
        set.add("4");
        set.clear();
        assertTrue(set.getSet().isEmpty());
    }
}
