package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.AIComponent;
import core.Entity;
import core.components.PlayerComponent;

import java.util.function.Consumer;
import java.util.function.Function;

public class AIComponentSerializer extends Serializer<AIComponent> {
//    private Entity entity;
//
//    public AIComponentSerializer(){
//        super();
//    }
//
//    public AIComponentSerializer(Entity e){
//        super();
//        entity = e;
//    }
//    @Override
//    public void write(Kryo kryo, Output output, AIComponent object) {
//        kryo.writeObject(output, object.fightBehavior());
//        kryo.writeObject(output, object.idleBehavior());
//        kryo.writeObject(output, object.shouldFight());
//    }
//
//    @Override
//    public AIComponent read(Kryo kryo, Input input, Class<AIComponent> type) {
//        Consumer<Entity> fightBehavior = kryo.readObject(input, Consumer.class);
//        Consumer<Entity> idleBehavior = kryo.readObject(input, Consumer.class);
//        Function<Entity, Boolean> shouldFight = kryo.readObject(input, Function.class);
//        return new AIComponent(entity, fightBehavior, idleBehavior, shouldFight);
//    }

    @Override
    public void write(Kryo kryo, Output output, AIComponent object) {

    }

    @Override
    public AIComponent read(Kryo kryo, Input input, Class<AIComponent> type) {
        return null;
    }
}
