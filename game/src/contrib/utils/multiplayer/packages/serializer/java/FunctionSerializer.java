package contrib.utils.multiplayer.packages.serializer.java;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.components.draw.Animation;

import java.util.function.Function;

/**
 * Custom serializer to send and retrieve objects of {@link Function}.
 */
public class FunctionSerializer extends Serializer<Function> {
    @Override
    public void write(Kryo kryo, Output output, Function object) {

    }

    @Override
    public Function read(Kryo kryo, Input input, Class<Function> type) {
        Class<? extends Function> concreteClass = kryo.readClass(input).getType();
        return kryo.readObject(input, concreteClass);
//        if (concreteClass == RangeTransition.class){
//            return kryo.readObject(input, RangeTransition.class);
//        }else if (concreteClass == SelfDefendTransition.class){
//            return kryo.readObject(input, SelfDefendTransition.class);
//        }
//        return null;
    }
}
