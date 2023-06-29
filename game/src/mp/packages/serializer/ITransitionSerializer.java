package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.ai.IIdleAI;
import contrib.utils.components.ai.ITransition;
import contrib.utils.components.ai.idle.PatrouilleWalk;
import contrib.utils.components.ai.idle.RadiusWalk;
import contrib.utils.components.ai.transition.RangeTransition;
import contrib.utils.components.ai.transition.SelfDefendTransition;

public class ITransitionSerializer extends Serializer<ITransition> {
    @Override
    public void write(Kryo kryo, Output output, ITransition object) {

    }

    @Override
    public ITransition read(Kryo kryo, Input input, Class<ITransition> type) {
        Class<? extends ITransition> concreteClass = kryo.readClass(input).getType();
        if (concreteClass == RangeTransition.class){
            return kryo.readObject(input, RangeTransition.class);
        }else if (concreteClass == SelfDefendTransition.class) {
            return kryo.readObject(input, SelfDefendTransition.class);
        }
        return null;
    }
}
