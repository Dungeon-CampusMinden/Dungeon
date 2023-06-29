package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.skill.FireballSkill;
import contrib.utils.components.skill.ISkillFunction;

public class ISkillFunctionSerializer extends Serializer<ISkillFunction> {
    @Override
    public void write(Kryo kryo, Output output, ISkillFunction object) {

    }

    @Override
    public ISkillFunction read(Kryo kryo, Input input, Class<ISkillFunction> type) {
        // Read FireballSkill because it is the only implementation of ISkillFunction yet
        return kryo.readObject(input, FireballSkill.class);
    }
}
