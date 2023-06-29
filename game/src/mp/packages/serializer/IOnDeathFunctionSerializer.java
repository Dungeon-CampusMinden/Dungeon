package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.health.DefaultOnDeath;
import contrib.utils.components.health.DropLoot;
import contrib.utils.components.health.IOnDeathFunction;

public class IOnDeathFunctionSerializer extends Serializer<IOnDeathFunction> {
    @Override
    public void write(Kryo kryo, Output output, IOnDeathFunction object) {
        //implemented in derived classes serializers
    }

    @Override
    public IOnDeathFunction read(Kryo kryo, Input input, Class<IOnDeathFunction> type) {
        Class<? extends IOnDeathFunction> concreteClass = kryo.readClass(input).getType();
        if (concreteClass == DefaultOnDeath.class){
            return new DefaultOnDeath();
        }else if (concreteClass == DropLoot.class) {
            return new DropLoot();
        }
        return null;
    }
}
