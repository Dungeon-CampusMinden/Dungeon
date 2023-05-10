package SafeandLoad;



import java.io.Serializable;
import java.util.ArrayList;



public class DataStorage implements Serializable {
    private int levelCount;

    ArrayList<String> entities;

    public void setLevelCount(int count){
        this.levelCount=count;
    }
    public void setEntities(ArrayList<String> entities){
        this.entities=entities;
    }
    public int getLevelCount(){
        return levelCount;
    }
    public ArrayList<String> getEntities(){return entities;}
}


