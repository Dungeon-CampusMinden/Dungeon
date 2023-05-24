package saving;

import java.io.Serializable;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.ClassNotFoundException;
import java.util.Optional;
import ecs.entities.*;

public record GameData(

    Entity hero,
    int level

    ) implements Serializable {

    public static void save(GameData gameData, String filename) throws IOException {
        FileOutputStream fos = new FileOutputStream(filename);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(gameData);
        oos.close();
    }

    public static Optional<GameData> load(String filename) throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream(filename);
        ObjectInputStream ois = new ObjectInputStream(fis);
        GameData gameData = (GameData) ois.readObject();
        ois.close();
        return Optional.ofNullable(gameData);
    }

}