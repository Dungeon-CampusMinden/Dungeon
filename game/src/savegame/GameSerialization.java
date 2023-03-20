package savegame;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.utils.JsonValue;
import ecs.damage.Damage;
import ecs.damage.DamageType;
import graphic.Animation;
import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import level.elements.tile.Tile;
import level.tools.Coordinate;
import starter.Game;
import tools.Point;

public class GameSerialization {

    /*
     * IMPORTANT: You might want to read this...
     * 
     * All Methods in this class have a purpose. They may seem unused but are invoked via reflections based
     * on the parameters type.
     * You need a new custom Serializer/Deserializer? Add a new static method with the @Serializer or @Deserializer annotation.
     *
     * Before doing this, you may want to check if the class you want to serialize can be made serializable by using the
     * ISerializable or IFieldSerializing interface.
     *
     * For serializing:
     * @Serializer(Example.class)
     * public static JsonValue serialize(Example object) {...}
     *
     * For deserializing:
     * @Deserializer(Example.class)
     * public static Example deserialize(JsonValue object) {...}
     *
     * The last Option to make a class serializable is to use javas Serializable interface.
     * This will store the object in a binary format and is not human-readable.
     *
     */

    private static final String TYPE_FIELD = "__serialize_type";
    private static final String CLASS_FIELD = "__serialize_class";
    private static final String DATA_FIELD = "__serialize_data";
    private static final String GENETIC_FIELD = "__serialize_genetic";
    private static final URI baseURI = new File("").toURI();
    private static final HashMap<String, Method> customSerializers = new HashMap<>();
    private static final HashMap<String, Method> customDeserializers = new HashMap<>();

    static {
        // Map all custom de-/serializers
        for (Method method : GameSerialization.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Serializer.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new RuntimeException(
                            "Serializer " + method.getName() + " must be static");
                }
                if (method.getParameterCount() != 1
                        || method.getParameterTypes()[0]
                                != method.getAnnotation(Serializer.class).value()) {
                    throw new RuntimeException(
                            "Serializer "
                                    + method.getName()
                                    + " must have exactly one parameter of type "
                                    + method.getAnnotation(Serializer.class).value().getName());
                }
                if (method.getReturnType() != JsonValue.class) {
                    throw new RuntimeException(
                            "Serializer " + method.getName() + " must return a JsonValue");
                }
                customSerializers.put(
                        method.getAnnotation(Serializer.class).value().getName(), method);
            } else if (method.isAnnotationPresent(Deserializer.class)) {
                if (!Modifier.isStatic(method.getModifiers())) {
                    throw new RuntimeException(
                            "Deserializer " + method.getName() + " must be static");
                }
                if (method.getParameterCount() != 1
                        || method.getParameterTypes()[0] != JsonValue.class) {
                    throw new RuntimeException(
                            "Deserializer "
                                    + method.getName()
                                    + " must have exactly one parameter of type JsonValue");
                }
                if (method.getReturnType() != method.getAnnotation(Deserializer.class).value()) {
                    throw new RuntimeException(
                            "Deserializer "
                                    + method.getName()
                                    + " must return an object of type "
                                    + method.getAnnotation(Deserializer.class).value().getName());
                }
                customDeserializers.put(
                        method.getAnnotation(Deserializer.class).value().getName(), method);
            }
        }
    }

    /**
     * Serialize an object. Object must be serializable by the ISerializable interface or by the
     * java.io.Serializable interface.
     *
     * @param object Object to serialize
     * @return Serialized object
     */
    public static JsonValue serialize(Object object) {
        if (object == null) {
            return new JsonValue(JsonValue.ValueType.nullValue);
        }
        JsonValue ret;

        if (ISerializable.class.isAssignableFrom(object.getClass())) {
            ret = serializeISerializable((ISerializable) object);
            if (!ret.has(TYPE_FIELD)
                    || !ret.getString(TYPE_FIELD).equals(ISerializable.class.getName())) {
                ret.remove(TYPE_FIELD);
                ret.addChild(TYPE_FIELD, new JsonValue(ISerializable.class.getName()));
            }
        } else if (Serializable.class.isAssignableFrom(object.getClass())) {
            ret = serializeObject(object);
            if (!ret.has(TYPE_FIELD)
                    || !ret.getString(TYPE_FIELD).equals(Serializable.class.getName())) {
                ret.remove(TYPE_FIELD);
                ret.addChild(TYPE_FIELD, new JsonValue(Serializable.class.getName()));
            }
        } else {
            Class<?> clazz = object.getClass();
            Method m = getSerializer(clazz);
            if (m == null) {
                throw new RuntimeException(
                        "Object of class " + object.getClass().getName() + " is not serializable",
                        new NotSerializableException(object.getClass().getName()));
            }
            try {
                ret = (JsonValue) m.invoke(null, object);
                if (!ret.has(TYPE_FIELD)
                        || !ret.getString(TYPE_FIELD)
                                .equals("@Serializer(" + clazz.getName() + ")")) {
                    ret.remove(TYPE_FIELD);
                    ret.addChild(TYPE_FIELD, new JsonValue("@Serializer(" + clazz.getName() + ")"));
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }

        if (!ret.has(CLASS_FIELD)
                || !ret.getString(CLASS_FIELD).equals(object.getClass().getName())) {
            ret.remove(CLASS_FIELD);
            ret.addChild(CLASS_FIELD, new JsonValue(object.getClass().getName()));
        }

        return ret;
    }

    /**
     * Deserialize an object from JsonValue. Object must be serializable by the ISerializable
     * interface or by the java.io.Serializable interface.
     *
     * @param data Serialized object
     * @param constructorArgs Parameters for the constructor of the object
     * @return Deserialized Object
     * @param <T> Type of the object
     */
    public static <T> T deserialize(JsonValue data, Object... constructorArgs) {
        if (data.isNull()) {
            return null;
        }
        if (data.getString(TYPE_FIELD).equals(Serializable.class.getName())) {
            return (T) deserializeObject(data);
        } else if (data.getString(TYPE_FIELD).equals(ISerializable.class.getName())) {
            return (T) deserializeISerializable(data, constructorArgs);
        } else {
            try {
                Class<?> clazz = Class.forName(data.getString(CLASS_FIELD));
                Method m = getDeserializer(clazz);
                if (m == null) {
                    throw new RuntimeException(
                            "Object of class "
                                    + data.getString(CLASS_FIELD)
                                    + " is not serializable",
                            new NotSerializableException(data.getString(CLASS_FIELD)));
                }
                return (T) m.invoke(null, data);
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static Method getSerializer(Class<?> clazz) {
        if (clazz == null) return null;
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            if (customSerializers.containsKey(currentClass.getName())) {
                return customSerializers.get(currentClass.getName());
            }
            currentClass = currentClass.getSuperclass();
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interface1 : interfaces) {
            if (customSerializers.containsKey(interface1.getName())) {
                return customSerializers.get(interface1.getName());
            }
        }
        return null;
    }

    private static Method getDeserializer(Class<?> clazz) {
        if (clazz == null) return null;
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            if (customDeserializers.containsKey(currentClass.getName())) {
                return customDeserializers.get(currentClass.getName());
            }
            currentClass = currentClass.getSuperclass();
        }
        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> interface1 : interfaces) {
            if (customDeserializers.containsKey(interface1.getName())) {
                return customDeserializers.get(interface1.getName());
            }
        }
        return null;
    }

    private static JsonValue serializeObject(Object object) {
        if (object == null) {
            return new JsonValue(JsonValue.ValueType.nullValue);
        }
        if (Serializable.class.isAssignableFrom(object.getClass())) {
            JsonValue json = new JsonValue(JsonValue.ValueType.object);
            json.addChild(CLASS_FIELD, new JsonValue(object.getClass().getName()));
            json.addChild(TYPE_FIELD, new JsonValue(Serializable.class.getName()));
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(object);
                oos.close();
                json.addChild(
                    DATA_FIELD,
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
            Class<?> clazz = Class.forName(data.getString(CLASS_FIELD));
            if (Serializable.class.isAssignableFrom(clazz)) {
                try {
                    byte[] dataBytes = Base64.getDecoder().decode(data.getString(DATA_FIELD));
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
                "Could not deserialize object of class " + data.getString(CLASS_FIELD), e);
        }
    }

    private static JsonValue serializeISerializable(ISerializable serializable) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild(CLASS_FIELD, new JsonValue(serializable.getClass().getName()));
        json.addChild(TYPE_FIELD, new JsonValue(ISerializable.class.getName()));
        json.addChild(DATA_FIELD, serializable.serialize());
        return json;
    }

    private static Object deserializeISerializable(JsonValue data, Object... constructorArgs) {
        try {
            Class<?> clazz1 = Class.forName(data.getString(CLASS_FIELD));
            if (ISerializable.class.isAssignableFrom(clazz1)) {
                Class<ISerializable> clazz = (Class<ISerializable>) clazz1;
                ISerializable object = Reflections.createInstance(clazz, constructorArgs);
                object.deserialize(data.get(DATA_FIELD));
                return object;
            } else {
                throw new NotSerializableException("Object is not serializable");
            }
        } catch (ReflectiveOperationException | NotSerializableException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * BEGIN: Custom De-/Serializers
     */

    @Serializer(Animation.class)
    private static JsonValue serializeAnimation(Animation animation) {
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

    @Deserializer(Animation.class)
    private static Animation deserializeAnimation(JsonValue data) {
        Animation obj =
                new Animation(List.of(data.get("frames").asStringArray()), data.getInt("duration"));
        Reflections.setFieldValue(
                obj, "animationFrames", List.of(data.get("frames").asStringArray()));
        Reflections.setFieldValue(obj, "frameTime", data.getInt("duration"));
        Reflections.setFieldValue(obj, "currentFrameIndex", data.getInt("currentFrameIndex"));
        Reflections.setFieldValue(obj, "frameTimeCounter", data.getInt("frameTimeCounter"));
        return obj;
    }

    @Serializer(Damage.class)
    private static JsonValue serializeDamage(Damage damage) {
        // TODO: Save associated entity
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("damageAmount", new JsonValue(damage.damageAmount()));
        json.addChild("damageType", new JsonValue(damage.damageType().name()));
        json.addChild("entity", new JsonValue("null"));
        return json;
    }

    @Deserializer(Damage.class)
    private static Damage deserializeDamage(JsonValue data) {
        // TODO: Load associated entity
        return new Damage(
                data.getInt("damageAmount"),
                DamageType.valueOf(data.getString("damageType")),
                null);
    }

    @Serializer(Point.class)
    private static JsonValue serializePoint(Point point) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("x", new JsonValue(point.x));
        json.addChild("y", new JsonValue(point.y));
        return json;
    }

    @Deserializer(Point.class)
    private static Point deserializePoint(JsonValue data) {
        return new Point(data.getFloat("x"), data.getFloat("y"));
    }

    @Serializer(Coordinate.class)
    private static JsonValue serializeCoordinate(Coordinate coordinate) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("x", new JsonValue(coordinate.x));
        json.addChild("y", new JsonValue(coordinate.y));
        return json;
    }

    @Deserializer(Coordinate.class)
    private static Coordinate deserializeCoordinate(JsonValue data) {
        return new Coordinate(data.getInt("x"), data.getInt("y"));
    }

    @Serializer(GraphPath.class)
    private static JsonValue serializeGraphPath(GraphPath<Tile> path) {
        if (path == null) {
            return new JsonValue(JsonValue.ValueType.nullValue);
        }
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild(GENETIC_FIELD, new JsonValue(Tile.class.getName()));
        JsonValue nodes = new JsonValue(JsonValue.ValueType.array);
        for (int i = 0; i < path.getCount(); i++) {
            nodes.addChild(serialize(path.get(i).getCoordinate()));
        }
        json.addChild("nodes", nodes);
        return json;
    }

    @Deserializer(GraphPath.class)
    private static GraphPath<Tile> deserializeGraphPath(JsonValue data) {
        if (data.isNull() || data.get("nodes").isNull()) return null;
        DefaultGraphPath<Tile> path = new DefaultGraphPath<>();
        for (JsonValue node : data.get("nodes")) {
            path.add(Game.currentLevel.getTileAt(Objects.requireNonNull(deserialize(node))));
        }
        return path;
    }

    @Serializer(Tile.class)
    private static JsonValue serializeTile(Tile tile) {
        JsonValue json = new JsonValue(JsonValue.ValueType.object);
        json.addChild("levelElement", new JsonValue(tile.getLevelElement().name()));
        json.addChild("location", serialize(tile.getCoordinate()));
        return json;
    }
}
