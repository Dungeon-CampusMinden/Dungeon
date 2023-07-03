package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.TriConsumer;

public class TriConsumerSerializer extends Serializer<TriConsumer> {
    @Override
    public void write(Kryo kryo, Output output, TriConsumer object) {

    }

    @Override
    public TriConsumer read(Kryo kryo, Input input, Class<TriConsumer> type) {
        Class<? extends TriConsumer> concreteClass = kryo.readClass(input).getType();
        return kryo.readObject(input, concreteClass);
//        if (concreteClass == DefaultCollider.class){
//            kryo.readObject(input, DefaultCollider.class);
//        } else if (concreteClass == ItemCollider.class){
//            kryo.readObject(input, ItemCollider.class);
//        }
//        return null;
    }
}
