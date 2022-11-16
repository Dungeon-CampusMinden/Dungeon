package character.skills;

import basiselements.AnimatableElement;
import collision.Collidable;

public abstract class BaseSkillEffect extends AnimatableElement implements Collidable {
    protected int alive;
    protected Collidable caster;
    public int damage = 2;

    public BaseSkillEffect(int alive, Collidable caster) {
        this.alive = alive;
        this.caster = caster;
    }

    @Override
    public void update() {
        alive--;
    }

    @Override
    public boolean removable() {
        return alive <= 0;
    }
}
