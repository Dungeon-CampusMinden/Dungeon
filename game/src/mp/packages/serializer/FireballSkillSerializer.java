package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.utils.components.skill.FireballSkill;

public class FireballSkillSerializer extends Serializer<FireballSkill> {
    @Override
    public void write(Kryo kryo, Output output, FireballSkill object) {
//        kryo.writeObject(output, object.getSelectionFunction());
    }

    @Override
    public FireballSkill read(Kryo kryo, Input input, Class<FireballSkill> type) {
//        ITargetSelection selectionFunction = kryo.readObject(input, ITargetSelection.class);
//        return new FireballSkill(selectionFunction);
        return null;
    }
}
