package ecs.components;

/**
 * A class to simply count testing of functional interfaces without mocking.
 *
 * <p>Sometimes the overhead of Mocking is way too much for simply checking if something got called
 */
public final class SimpleCounter {
    private int count = 0;

    public void inc() {
        count++;
    }

    public int getCount() {
        return count;
    }
}
