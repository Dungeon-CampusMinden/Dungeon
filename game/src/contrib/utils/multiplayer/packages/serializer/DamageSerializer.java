package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.health.Damage;
import contrib.utils.components.health.DamageType;
import core.Entity;

public class DamageSerializer extends Serializer<Damage> {
    @Override
    public void write(Kryo kryo, Output output, Damage object) {
        output.writeInt(object.damageAmount());
        kryo.writeObject(output, object.damageType());
        kryo.writeObjectOrNull(output, object.cause(), Entity.class);
    }

    @Override
    public Damage read(Kryo kryo, Input input, Class<Damage> type) {
        int damageAmount = input.readInt();
        DamageType damageType = kryo.readObject(input, DamageType.class);
        Entity cause = kryo.readObjectOrNull(input, Entity.class);
        return new Damage(damageAmount, damageType, cause);
    }
}
