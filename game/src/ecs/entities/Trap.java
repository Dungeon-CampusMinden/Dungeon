package ecs.entities;

/** Trap Class as a scheme for traps */
public abstract class Trap extends Entity {
    private boolean triggered;

    private boolean repeatable;
    private int trapDmg;

    public Trap(int trapDmg) {
        this.trapDmg = trapDmg;
    }

    public Trap() {}

    public Trap(boolean triggered, boolean repeatable, int trapDmg) {
        this.triggered = triggered;
        this.repeatable = repeatable;
        this.trapDmg = trapDmg;
    }

    public void setTrapDmg(int trapDmg) {
        this.trapDmg = trapDmg;
    }

    public int getTrapDmg() {
        return this.trapDmg;
    }

    public boolean isTriggered() {
        return this.triggered;
    }

    public void setTriggered(boolean set) {
        this.triggered = set;
    }
}
