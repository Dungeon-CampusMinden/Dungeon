package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.interaction.DefaultInteraction;
import contrib.utils.components.interaction.DropItemsInteraction;
import contrib.utils.components.interaction.IInteraction;

public class IInteractionSerializer extends Serializer<IInteraction> {
    @Override
    public void write(Kryo kryo, Output output, IInteraction object) {

    }

    @Override
    public IInteraction read(Kryo kryo, Input input, Class<IInteraction> type) {
        Class<? extends IInteraction> concreteClass = kryo.readClass(input).getType();
        if (concreteClass == DefaultInteraction.class){
            return new DefaultInteraction();
        }else if (concreteClass == DropItemsInteraction.class) {
            return new DropItemsInteraction();
        }
        return null;
    }
}
