package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.IFightAI;
import contrib.utils.components.ai.fight.CollideAI;
import contrib.utils.components.ai.fight.MeleeAI;

public class IFightAISerializer extends Serializer<IFightAI> {
    @Override
    public void write(Kryo kryo, Output output, IFightAI object) {

    }

    @Override
    public IFightAI read(Kryo kryo, Input input, Class<IFightAI> type) {
        Class<? extends IFightAI> concreteClass = kryo.readClass(input).getType();
        if (concreteClass == MeleeAI.class){
            return kryo.readObject(input, MeleeAI.class);
        }else if (concreteClass == CollideAI.class) {
            return kryo.readObject(input, CollideAI.class);
        }
        return null;
    }
}
