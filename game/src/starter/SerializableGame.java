package starter;

import java.io.*;
import java.util.logging.Logger;

/**
 * The game state must always be saved in a file and when the game is started,
 * the information must then be taken from the file.
 */
public class SerializableGame implements Serializable {
    static final long serialVersion = 42L;
    private int currentlevel;
    private final int healthPoints;
    private final static Logger logger = Logger.getLogger("SerializableGame");

    /**
     * Default constructor with default values
     */
    SerializableGame(){
        this(2,1);
    }

    /**
     * Constuctor
     * @param healthPoints The value from, will be saved in the attribute healthPoints.
     * @param currentlevel The value from, will be saved in the attribute current level.
     */
    SerializableGame(int healthPoints, int currentlevel){
        this.currentlevel = currentlevel;
        this.healthPoints = healthPoints;
    }
    /**
     * a file will be created with the name of the given filename, and save the values of the Attributes.
     * @param daten an object of the class SerializableGame.
     * @param filename the name will be saved with entered name.
     */

    public static void writeObject(SerializableGame daten, String filename){

        try (FileOutputStream fos = new FileOutputStream(filename); ObjectOutputStream oos = new ObjectOutputStream(fos)){
            oos.writeObject(daten);
            oos.flush();
            oos.close();
            logger.severe("The file has been successfully created");
        }catch (IOException ex){
            logger.severe("The file cannot be created");
        }
    }
    /**
     * the values of the stored attributes will be read and returned as an object.
     * @param filename the name of the stored file.
     * @return an Objekt with the saved values of the Attributes.
     */
    public static SerializableGame readObject (String filename){
        SerializableGame dataStorage = null;
        try (FileInputStream fis = new FileInputStream(filename); ObjectInputStream ois = new ObjectInputStream(fis)){
            dataStorage = (SerializableGame) ois.readObject();
            ois.close();
            logger.severe("File has been read.");
        }
        catch (Exception ex){
            logger.severe("File cannot be read.");
        }
        return dataStorage;
    }
    /**
     * Getter
     * @return the value of the Current level .
     */
    public int getCurrentlevel() {
        return currentlevel;
    }
    /**
     * Getter
     * @return the value of the Health Points.
     */
    public int getHealthPoints() {
        return healthPoints;

    }
    /**
     * Setter
     * @param currentlevel to save a ner value for the Current Attribute.
     */
    public void setCurrentlevel(int currentlevel) {
        this.currentlevel = currentlevel;
    }
}


