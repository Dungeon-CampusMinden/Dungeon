package core.utils;

import static junit.framework.TestCase.assertEquals;

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
        assertTrue(set.add(toAdd));
        assertFalse(set.getSetAsStream().anyMatch(e -> e == toAdd));
        set.update();
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd));
        ;
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
        assertTrue(set.addAll(addSet));
        assertFalse(set.getSetAsStream().anyMatch(e -> e == toAdd1));
        assertFalse(set.getSetAsStream().anyMatch(e -> e == toAdd2));
        assertFalse(set.getSetAsStream().anyMatch(e -> e == toAdd3));
        set.update();
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd1));
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd2));
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd3));
    }

    @Test
    public void testRemove() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        assertTrue(set.add(toAdd));
        set.update();
        assertTrue(set.remove(toAdd));
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd));
        set.update();
        assertFalse(set.getSetAsStream().anyMatch(e -> e == toAdd));
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
        assertTrue(set.addAll(addSet));
        set.update();
        addSet.remove(toAdd2);
        assertTrue(set.removeAll(addSet));
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd1));
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd2));
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd3));
        set.update();
        assertFalse(set.getSetAsStream().anyMatch(e -> e == toAdd1));
        assertTrue(set.getSetAsStream().anyMatch(e -> e == toAdd2));
        assertFalse(set.getSetAsStream().anyMatch(e -> e == toAdd3));
    }

    @Test
    public void clear() {
        DelayedSet<String> set = new DelayedSet<>();
        assertTrue(set.add("3"));
        set.update();
        assertTrue(set.add("4"));
        set.clear();
        set.update();
        assertEquals(0, set.getSetAsStream().count());
    }
}
