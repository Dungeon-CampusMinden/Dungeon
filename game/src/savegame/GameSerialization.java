package savegame;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.JsonValue;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import graphic.Animation;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.Base64;
import java.util.List;
import level.elements.tile.Tile;
import starter.Game;
import tools.Point;

public class GameSerialization {

    private static final URI baseURI = new File("").toURI();

    public static JsonValue serialize(Object object) {
        if (ISerializable.class.isAssignableFrom(object.getClass())) {
            return serializeISerializable((ISerializable) object);
        } else if (Serializable.class.isAssignableFrom(object.getClass())) {
            return serializeObject(object);
        } else {
            throw new RuntimeException(
                    "Object of class " + object.getClass().getName() + " is not serializable",
                    new NotSerializableException(object.getClass().getName()));
        }
    }

    public static <T extends Serializable & ISerializable> T deserialize(JsonValue data) {
        if (data.getString("type").equals(Serializable.class.getName())) {
            return (T) deserializeObject(data);
        } else if (data.getString("type").equals(ISerializable.class.getName())) {
            return (T) deserializeISerializable(data);
        } else {
            return null;
        }
    }

    public static JsonValue serializeAnimation(Animation animation) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        try {
            /* Get Frames */
            JsonValue framesArray = new JsonValue(JsonValue.ValueType.array);
            List<String> frames = Reflections.getFieldValue(animation, "animationFrames");
            for (String frame : frames) {
                framesArray.addChild(
                        new JsonValue(baseURI.relativize(new File(frame).toURI()).getPath()));
            }
            json.addChild("frames", framesArray);

            /* Get Frame Duration */
            Field fieldDuration = Animation.class.getDeclaredField("frameTime");
            fieldDuration.setAccessible(true);
            int duration = (int) fieldDuration.get(animation);
            json.addChild("duration", new JsonValue(duration));

            /* Get Current Frame */
            Field fieldCurrentFrameIndex = Animation.class.getDeclaredField("currentFrameIndex");
            fieldCurrentFrameIndex.setAccessible(true);
            int currentFrameIndex = (int) fieldCurrentFrameIndex.get(animation);
            json.addChild("currentFrameIndex", new JsonValue(currentFrameIndex));

            /* Get Current Frame Time */
            Field fieldFrameTimeCounter = Animation.class.getDeclaredField("frameTimeCounter");
            fieldFrameTimeCounter.setAccessible(true);
            int currentFrameTime = (int) fieldFrameTimeCounter.get(animation);
            json.addChild("frameTimeCounter", new JsonValue(currentFrameTime));

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return json;
    }

    public static Animation deserializeAnimation(JsonValue data) {
        Class<Animation> clazz = Animation.class;
        Animation obj = Reflections.generateInstance(clazz);

        Reflections.setFinalField(
                obj, "animationFrames", List.of(data.get("frames").asStringArray()));
        Reflections.setFinalField(obj, "frameTime", data.getInt("duration"));
        Reflections.setFinalField(obj, "currentFrameIndex", data.getInt("currentFrameIndex"));
        Reflections.setFinalField(obj, "frameTimeCounter", data.getInt("frameTimeCounter"));

        return obj;
    }

    public static JsonValue serializeDamage(Damage damage) {
        // TODO: Save associated entity
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("damageAmount", new JsonValue(damage.damageAmount()));
        json.addChild("damageType", new JsonValue(damage.damageType().name()));
        json.addChild("entity", new JsonValue("null"));
        return json;
    }

    public static Damage deserializeDamage(JsonValue data) {
        // TODO: Load associated entity
        return new Damage(
                data.getInt("damageAmount"),
                DamageType.valueOf(data.getString("damageType")),
                null);
    }

    private static JsonValue serializeObject(Object object) {
        if (object == null) {
            return new JsonValue(JsonValue.ValueType.nullValue);
        }
        if (Serializable.class.isAssignableFrom(object.getClass())) {
            JsonValue json = new JsonValue(JsonValue.ValueType.object);
            json.addChild("class", new JsonValue(object.getClass().getName()));
            json.addChild("type", new JsonValue(Serializable.class.getName()));
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                oos.close();
                json.addChild(
                        "data",
                        new JsonValue(Base64.getEncoder().encodeToString(baos.toByteArray())));
                baos.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return json;
        } else {
            throw new RuntimeException(
                    "Object of " + object.getClass().getName() + " could not be serialized.",
                    new NotSerializableException("Object is not serializable"));
        }
    }

    private static Object deserializeObject(JsonValue data) {
        if (data.isNull()) {
            return null;
        }
        try {
            Class<?> clazz = Class.forName(data.getString("class"));
            if (Serializable.class.isAssignableFrom(clazz)) {
                try {
                    byte[] dataBytes = Base64.getDecoder().decode(data.getString("data"));
                    ByteArrayInputStream bais = new ByteArrayInputStream(dataBytes);
                    ObjectInputStream ois = new ObjectInputStream(bais);
                    Object object = ois.readObject();
                    ois.close();
                    bais.close();
                    return object;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                throw new NotSerializableException("Object is not serializable");
            }
        } catch (ReflectiveOperationException | NotSerializableException e) {
            throw new RuntimeException(
                    "Could not deserialize object of class " + data.getString("class"), e);
        }
    }

    public static JsonValue serializePoint(Point point) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("x", new JsonValue(point.x));
        json.addChild("y", new JsonValue(point.y));
        return json;
    }

    public static Point deserializePoint(JsonValue data) {
        return new Point(data.getInt("x"), data.getInt("y"));
    }

    public static JsonValue serializeGraphPath(GraphPath<Tile> path) {
        if (path == null) {
            return new JsonValue(JsonValue.ValueType.nullValue);
        }
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("class", new JsonValue(path.getClass().getName()));
        json.addChild("geneticType", new JsonValue(Tile.class.getName()));
        JsonValue nodes = new JsonValue(JsonValue.ValueType.array);
        for (int i = 0; i < path.getCount(); i++) {
            nodes.addChild(serializePoint(path.get(i).getCoordinate().toPoint()));
        }
        json.addChild("nodes", nodes);
        return json;
    }

    /**
     * Deserializes a GraphPath from a JsonValue. REQUIRED: The Game musst have the current level
     * loaded.
     *
     * @param data The JsonValue to deserialize from
     * @return The deserialized GraphPath
     */
    public static GraphPath<Tile> deserializeGraphPath(JsonValue data) {
        GraphPath<Tile> path = new DefaultGraphPath<>();
        for (JsonValue node : data.get("nodes")) {
            path.add(Game.currentLevel.getTileAt(deserializePoint(node).toCoordinate()));
        }
        return path;
    }

    private static JsonValue serializeISerializable(ISerializable serializable) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("class", new JsonValue(serializable.getClass().getName()));
        json.addChild("type", new JsonValue(ISerializable.class.getName()));
        json.addChild("data", serializable.serialize());
        return json;
    }

    private static Object deserializeISerializable(JsonValue data) {
        try {
            Class<?> clazz = Class.forName(data.getString("class"));
            if (ISerializable.class.isAssignableFrom(clazz)) {
                Constructor<?> constructor = clazz.getConstructor();
                constructor.setAccessible(true);
                ISerializable object = (ISerializable) constructor.newInstance();
                object.deserialize(data.get("data"));
                return object;
            } else {
                throw new NotSerializableException("Object is not serializable");
            }
        } catch (ReflectiveOperationException | NotSerializableException e) {
            throw new RuntimeException(e);
        }
    }
}
