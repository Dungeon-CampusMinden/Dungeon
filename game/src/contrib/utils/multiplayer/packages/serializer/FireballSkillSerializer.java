package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import contrib.utils.components.skill.FireballSkill;

/** Custom serializer to send and retrieve objects of {@link FireballSkill}. */
public class FireballSkillSerializer extends Serializer<FireballSkill> {
    @Override
    public void write(Kryo kryo, Output output, FireballSkill object) {}

    @Override
    public FireballSkill read(Kryo kryo, Input input, Class<FireballSkill> type) {
        return new FireballSkill(null);
    }
}
