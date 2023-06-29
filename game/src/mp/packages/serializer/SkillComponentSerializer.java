package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.SkillComponent;
import contrib.utils.components.skill.Skill;
import core.Entity;

import java.util.Set;

public class SkillComponentSerializer extends Serializer<SkillComponent> {
    private Entity entity;

    public SkillComponentSerializer(){
        super();
    }

    public SkillComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, SkillComponent object) {
        Set<Skill> skillset = object.getSkillSet();
        output.writeInt(skillset.size());
        for (Skill skill : skillset){
            kryo.writeObject(output, skill);
        }
    }

    @Override
    public SkillComponent read(Kryo kryo, Input input, Class<SkillComponent> type) {
        int size = input.readInt();
        SkillComponent skillComp = new SkillComponent(entity);
        for (int i = 0; i < size; i++) {
            skillComp.addSkill(kryo.readObject(input,Skill.class));
        }
        return skillComp;
    }
}
