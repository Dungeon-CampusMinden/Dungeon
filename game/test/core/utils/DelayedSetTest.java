package core.utils;

import static junit.framework.TestCase.assertEquals;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class DelayedSetTest {

    @Test
    public void add() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        set.add(toAdd);
        assertFalse(set.stream().anyMatch(e -> Objects.equals(e, toAdd)));
        set.update();
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd)));
    }

    @Test
    public void addAll() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        set.addAll(addSet);
        assertFalse(set.stream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertFalse(set.stream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertFalse(set.stream().anyMatch(e -> Objects.equals(e, toAdd3)));
        set.update();
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd3)));
    }

    @Test
    public void remove() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        set.add(toAdd);
        set.update();
        set.remove(toAdd);
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd)));
        set.update();
        assertFalse(set.stream().anyMatch(e -> Objects.equals(e, toAdd)));
    }

    @Test
    public void removeAll() {
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
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd3)));
        set.update();
        assertFalse(set.stream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertTrue(set.stream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertFalse(set.stream().anyMatch(e -> Objects.equals(e, toAdd3)));
    }

    @Test
    public void clear() {
        DelayedSet<String> set = new DelayedSet<>();
        set.add("3");
        set.update();
        set.add("4");
        set.clear();
        set.update();
        assertEquals(0, set.stream().count());
    }
}
