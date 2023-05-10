package SafeandLoad;

import ecs.entities.Entity;

import ecs.entities.monsters.Chort;
import ecs.entities.monsters.Goblin;
import ecs.entities.monsters.LittleChort;
import ecs.entities.nps.Ghost;
import ecs.entities.objects.Tombstone;
import starter.Game;

import java.io.*;
import java.util.ArrayList;


public class SafeandLoad {
    private final Game game;
    public SafeandLoad(Game game){
        this.game = game;
    }

    /** Writes Savedata into a savefile*/
    public void writeSave(){
        DataStorage data = new DataStorage();

        data.setLevelCount(game.getLevelcounter());

        ArrayList<String> entityList = new ArrayList<>();
        for(Entity entity : Game.getEntities()){
            entityList.add(entity.getClass().getSimpleName());
        }
        data.setEntities(entityList);
        FileOutputStream fos;
        ObjectOutputStream out;
        try {
            fos = new FileOutputStream("save.ser");
            out = new ObjectOutputStream(fos);
            out.writeObject(data);
            System.out.println("gespeichert");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /** Loads savedata from a savefile*/
    public void loadSave(){
        FileInputStream fis;
        ObjectInputStream in;
        DataStorage data = new DataStorage();
        try {
            fis = new FileInputStream("save.ser");
            in = new ObjectInputStream(fis);
            data = (DataStorage) in.readObject();
            System.out.println(data.getEntities().toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        game.setLevelcounter(data.getLevelCount());
        game.getTeleportsystem().makePads();
        for(String entity : data.getEntities()){
            switch (entity) {
                case "Chort" -> Game.getEntitiesToAdd().add(new Chort());
                case "Goblin" -> Game.getEntitiesToAdd().add(new Goblin());
                case "LittleChort" -> Game.getEntitiesToAdd().add(new LittleChort());
            }
            System.out.println("geladen");
        }
    }
}
