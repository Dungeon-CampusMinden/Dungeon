package contrib.utils.multiplayer.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import core.utils.components.draw.Animation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AnimationSerializer extends Serializer<Animation> {
    @Override
    public void write(Kryo kryo, Output output, Animation object) {
        output.writeInt(object.timeBetweenFrames());
        output.writeBoolean(object.isLooping());
        List<String> animationFrames = object.getAnimationFrames();
        int size = animationFrames.size();
        output.writeInt(size);
        for (String frame: animationFrames){
            output.writeString(frame);
        }
    }

    @Override
    public Animation read(Kryo kryo, Input input, Class<Animation> type) {
        int frameTime = input.readInt();
        boolean looping = input.readBoolean();
        int size = input.readInt();
        Collection<String> animationFrames = new ArrayList<>();
        for (int i = 0; i < size; i++){
            String frame = input.readString();
            animationFrames.add(frame);
        }
        return new Animation(animationFrames, frameTime, looping);
    }
}
