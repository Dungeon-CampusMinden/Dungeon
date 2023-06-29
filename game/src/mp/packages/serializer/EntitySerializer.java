package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.*;
import core.Component;
import core.Entity;
import core.components.DrawComponent;
import core.components.PlayerComponent;
import core.components.PositionComponent;
import core.components.VelocityComponent;

public class EntitySerializer extends Serializer<Entity> {
    @Override
    public void write(Kryo kryo, Output output, Entity object) {
        int size = object.getComponents().size();
        output.writeInt(size);
        object.getComponents().forEach((key, value) -> {
            kryo.writeClass(output, key);
            kryo.writeObject(output, value);
        });
    }

    @Override
    public Entity read(Kryo kryo, Input input, Class<Entity> type) {
        int size = input.readInt();
        //Todo - Look if creating a clone corrupts the given ids
        Entity e = new Entity();
        for (int i = 0; i < size; i++){
            Class <? extends Component> klass = kryo.readClass(input).getType();
            switch (klass.getSimpleName()){
                case "DrawComponent":
                    kryo.readObject(input,klass,new DrawComponentSerializer(e));
                    break;
                case "PlayerComponent":
                    kryo.readObject(input,klass,new PlayerComponentSerializer(e));
                    break;
                case "PositionComponent":
                    kryo.readObject(input,klass,new PositionComponentSerializer(e));
                    break;
                case "VelocityComponent":
                    kryo.readObject(input,klass,new VelocityComponentSerializer(e));
                    break;
                case "AIComponent":
                    kryo.readObject(input,klass,new AIComponentSerializer(e));
                    break;
                case "CollideComponent":
                    kryo.readObject(input,klass,new CollideComponentSerializer(e));
                    break;
                case "HealthComponent":
                    kryo.readObject(input,klass,new HealthComponentSerializer(e));
                    break;
                case "InteractionComponent":
                    kryo.readObject(input,klass,new InteractionComponentSerializer(e));
                    break;
                case "InventoryComponent":
                    kryo.readObject(input,klass,new InventoryComponentSerializer(e));
                    break;
                case "ItemComponent":
                    kryo.readObject(input,klass,new ItemComponentSerializer(e));
                    break;
                case "MultiplayerComponent":
                    kryo.readObject(input,klass,new MultiplayerComponentSerializer(e));
                    break;
                case "ProjectileComponent":
                    kryo.readObject(input,klass,new ProjectileComponentSerializer(e));
                    break;
                case "SkillComponent":
                    kryo.readObject(input,klass,new SkillComponentSerializer(e));
                    break;
                case "StatsComponent":
                    kryo.readObject(input,klass,new StatsComponentSerializer(e));
                    break;
                case "XPComponent":
                    kryo.readObject(input,klass,new XPComponentSerializer(e));
                    break;
                default:
                    break;
            }
        }
        return e;
    }
}
