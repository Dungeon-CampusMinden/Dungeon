package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.IIdleAI;
import contrib.utils.components.ai.idle.PatrouilleWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.idle.StaticRadiusWalk;

public class IIdleAiSerializer extends Serializer<IIdleAI> {
    @Override
    public void write(Kryo kryo, Output output, IIdleAI object) {

    }

    @Override
    public IIdleAI read(Kryo kryo, Input input, Class<IIdleAI> type) {
        Class<? extends IIdleAI> concreteClass = kryo.readClass(input).getType();
        if (concreteClass == PatrouilleWalk.class){
            return kryo.readObject(input, PatrouilleWalk.class);
        }else if (concreteClass == RadiusWalk.class) {
            return kryo.readObject(input, RadiusWalk.class);
        }else if (concreteClass == StaticRadiusWalk.class) {
            return kryo.readObject(input, StaticRadiusWalk.class);
        }
        return null;
    }
}
