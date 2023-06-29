package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.skill.DamageProjectileColliderEnter;

public class DamageProjectileColliderEnterSerializer extends Serializer<DamageProjectileColliderEnter> {
    @Override
    public void write(Kryo kryo, Output output, DamageProjectileColliderEnter object) {
        kryo.writeClass(output, object.getClass());

    }

    //Todo - Projectile wont work for now (cyclic entity as member?)
    @Override
    public DamageProjectileColliderEnter read(Kryo kryo, Input input, Class<DamageProjectileColliderEnter> type) {
        return null;
    }
}
