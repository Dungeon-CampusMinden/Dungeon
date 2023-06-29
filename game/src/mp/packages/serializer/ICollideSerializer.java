package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.IIdleAI;
import contrib.utils.components.ai.idle.PatrouilleWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.idle.StaticRadiusWalk;
import contrib.utils.components.collision.DefaultCollider;
import contrib.utils.components.collision.ICollide;
import contrib.utils.components.item.ItemColliderEnter;
import contrib.utils.components.skill.CursorPositionTargetSelection;
import contrib.utils.components.skill.DamageProjectileColliderEnter;

public class ICollideSerializer extends Serializer<ICollide> {
    @Override
    public void write(Kryo kryo, Output output, ICollide object) {

    }

    @Override
    public ICollide read(Kryo kryo, Input input, Class<ICollide> type) {
        Class<? extends ICollide> concreteClass = kryo.readClass(input).getType();
        if (concreteClass == DefaultCollider.class){
            return kryo.readObject(input, DefaultCollider.class);
        }else if (concreteClass == ItemColliderEnter.class) {
            return kryo.readObject(input, ItemColliderEnter.class);
        }else if (concreteClass == DamageProjectileColliderEnter.class) {
            return kryo.readObject(input, DamageProjectileColliderEnter.class);
        }
        return null;
    }
}
