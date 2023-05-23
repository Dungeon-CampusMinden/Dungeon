package contrib.utils.components.draw;

import core.utils.components.draw.IPath;

public enum AdditionAnimations implements IPath {
    DIE("die"),
    HIT("hit"),
    ATTACK("attack");

    private final String value;

    AdditionAnimations(String value) {
        this.value = value;
    }

    @Override
    public String getPathString() {
        return value;
    }
}
