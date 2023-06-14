package contrib.crafting.result;

import com.badlogic.gdx.utils.JsonValue;

import core.Entity;

public abstract class CraftingResult {

    public enum Type {
        ITEM,
    }

    private final Type type;

    public CraftingResult(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    /**
     * Execute the result on the provided entity.
     *
     * @param entity Entity to execute the result on.
     */
    public abstract void execute(Entity entity);

    public abstract void parse(JsonValue value);
}
