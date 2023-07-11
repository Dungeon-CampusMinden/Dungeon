package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.multiplayer.packages.serializer.components.CollideComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.DrawComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.HealthComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.InteractionComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.InventoryComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.ItemComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.PositionComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.ProjectileComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.StatsComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.VelocityComponentSerializer;
import contrib.utils.multiplayer.packages.serializer.components.XPComponentSerializer;

import core.Component;
import core.Entity;

/** Custom serializer to send and retrieve objects of {@link Entity}. */
public class EntitySerializer extends Serializer<Entity> {
    @Override
    public void write(Kryo kryo, Output output, Entity object) {
        output.writeString(object.name());
        output.writeInt(object.localID());
        output.writeInt(object.globalID());
        final long size = object.componentStream().count();
        output.writeLong(size);
        object.componentStream()
                .forEach(
                        (component) -> {
                            kryo.writeClass(output, component.getClass());
                            kryo.writeObject(output, component);
                        });
    }

    @Override
    public Entity read(Kryo kryo, Input input, Class<Entity> type) {
        final String name = input.readString();
        final int localeID = input.readInt();
        final int globalID = input.readInt();
        final long size = input.readLong();
        final Entity e = new Entity(name, localeID, globalID);

        for (int i = 0; i < size; i++) {
            Class<? extends Component> klass = kryo.readClass(input).getType();
            switch (klass.getSimpleName()) {
                case "DrawComponent":
                    kryo.readObject(input, klass, new DrawComponentSerializer(e));
                    break;
                case "PositionComponent":
                    kryo.readObject(input, klass, new PositionComponentSerializer(e));
                    break;
                case "VelocityComponent":
                    kryo.readObject(input, klass, new VelocityComponentSerializer(e));
                    break;
                case "CollideComponent":
                    kryo.readObject(input, klass, new CollideComponentSerializer(e));
                    break;
                case "HealthComponent":
                    kryo.readObject(input, klass, new HealthComponentSerializer(e));
                    break;
                case "InteractionComponent":
                    kryo.readObject(input, klass, new InteractionComponentSerializer(e));
                    break;
                case "InventoryComponent":
                    kryo.readObject(input, klass, new InventoryComponentSerializer(e));
                    break;
                case "ItemComponent":
                    kryo.readObject(input, klass, new ItemComponentSerializer(e));
                    break;
                case "ProjectileComponent":
                    kryo.readObject(input, klass, new ProjectileComponentSerializer(e));
                    break;
                case "StatsComponent":
                    kryo.readObject(input, klass, new StatsComponentSerializer(e));
                    break;
                case "XPComponent":
                    kryo.readObject(input, klass, new XPComponentSerializer(e));
                    break;
                default:
                    break;
            }
        }
        return e;
    }
}
