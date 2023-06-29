package mp.packages.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import contrib.components.ProjectileComponent;
import core.Entity;
import core.utils.Point;

public class ProjectileComponentSerializer extends Serializer<ProjectileComponent> {
    private Entity entity;

    public ProjectileComponentSerializer(){
        super();
    }

    public ProjectileComponentSerializer(Entity e){
        super();
        entity = e;
    }
    @Override
    public void write(Kryo kryo, Output output, ProjectileComponent object) {
        kryo.writeObject(output, object.getGoalLocation());
        kryo.writeObject(output, object.getStartPosition());
    }

    @Override
    public ProjectileComponent read(Kryo kryo, Input input, Class<ProjectileComponent> type) {
        Point goalPosition = kryo.readObject(input, Point.class);
        Point startPosition = kryo.readObject(input, Point.class);
        return new ProjectileComponent(entity, startPosition, goalPosition);
    }
}
