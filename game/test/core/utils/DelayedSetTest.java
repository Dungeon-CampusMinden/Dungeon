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
        assertTrue(set.add(toAdd));
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd)));
        set.update();
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd)));
    }

    @Test
    public void add_exisiting() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        assertTrue(set.add(toAdd));
        assertFalse(set.add(toAdd));
        set.update();
        assertFalse(set.add(toAdd));
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
        assertTrue(set.addAll(addSet));
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd3)));
        set.update();
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd3)));
    }

    @Test
    public void addAll_exisiting() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        assertTrue(set.addAll(addSet));
        assertFalse(set.addAll(addSet));
        set.update();
        assertFalse(set.addAll(addSet));
    }

    @Test
    public void addAll_oneExisiting() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        assertTrue(set.addAll(addSet));
        addSet.add(toAdd2);
        assertTrue(set.addAll(addSet));
        set.update();
        addSet.add(toAdd3);
        assertTrue(set.addAll(addSet));
    }

    @Test
    public void remove() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        assertTrue(set.add(toAdd));
        set.update();
        assertTrue(set.remove(toAdd));
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd)));
        set.update();
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd)));
    }

    @Test
    public void remove_nonExisting() {
        DelayedSet<String> set = new DelayedSet<>();
        assertFalse(set.remove(""));
    }

    @Test
    public void remove_existingInToAdd() {
        DelayedSet<String> set = new DelayedSet<>();
        String toAdd = "3";
        assertTrue(set.add(toAdd));
        assertTrue(set.remove(toAdd));
        set.update();
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd)));
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
        assertTrue(set.addAll(addSet));
        set.update();
        addSet.remove(toAdd2);
        assertTrue(set.removeAll(addSet));
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd3)));
        set.update();
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd1)));
        assertTrue(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd2)));
        assertFalse(set.getSetAsStream().anyMatch(e -> Objects.equals(e, toAdd3)));
    }

    @Test
    public void removeAll_nonExisiting() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        assertFalse(set.removeAll(addSet));
    }

    @Test
    public void removeAll_oneExisiting() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        set.add(toAdd1);
        set.update();
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        assertTrue(set.removeAll(addSet));
    }

    @Test
    public void removeAll_oneInToAdd() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        set.add(toAdd1);
        assertTrue(set.removeAll(addSet));
    }

    @Test
    public void removeAll_AllInToAdd() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        String toAdd2 = "B";
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        set.add(toAdd1);
        assertTrue(set.removeAll(addSet));
    }

    @Test
    public void removeAll_mixed() {
        DelayedSet<String> set = new DelayedSet<>();
        Set<String> addSet = new HashSet<>();
        String toAdd1 = "A";
        set.add(toAdd1);
        set.update();
        String toAdd2 = "B";
        set.add(toAdd2);
        String toAdd3 = "C";
        addSet.add(toAdd1);
        addSet.add(toAdd2);
        addSet.add(toAdd3);
        assertTrue(set.removeAll(addSet));
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
