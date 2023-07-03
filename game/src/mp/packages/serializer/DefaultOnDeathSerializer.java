package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.health.DefaultOnDeath;

import java.util.function.Consumer;

public class DefaultOnDeathSerializer extends Serializer<DefaultOnDeath> {
    @Override
    public void write(Kryo kryo, Output output, DefaultOnDeath object) {
        Class<? extends Consumer> concreteClass = object.getClass();
        kryo.writeClass(output, concreteClass);
    }

    @Override
    public DefaultOnDeath read(Kryo kryo, Input input, Class<DefaultOnDeath> type) {
        return new DefaultOnDeath();
    }
}
