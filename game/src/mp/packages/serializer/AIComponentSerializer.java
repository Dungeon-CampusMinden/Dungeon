package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.AIComponent;
import contrib.utils.components.ai.IFightAI;
import contrib.utils.components.ai.IIdleAI;
import contrib.utils.components.ai.ITransition;
import core.Entity;

public class AIComponentSerializer extends Serializer<AIComponent> {
    private Entity entity;

    public AIComponentSerializer(){
        super();
    }

    public AIComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, AIComponent object) {
        kryo.writeObject(output, object.getFightAI());
        kryo.writeObject(output, object.getIdleAI());
        kryo.writeObject(output, object.getTransitionAI());
    }

    @Override
    public AIComponent read(Kryo kryo, Input input, Class<AIComponent> type) {
        IFightAI fightAI = kryo.readObject(input, IFightAI.class);
        IIdleAI idleAI = kryo.readObject(input, IIdleAI.class);
        ITransition transitionAI = kryo.readObject(input, ITransition.class);
        return new AIComponent(entity, fightAI, idleAI, transitionAI);
    }
}
