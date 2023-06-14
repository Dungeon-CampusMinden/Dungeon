package ecs.components;

import ecs.entities.Entity;
import ecs.entities.Key;
import starter.Game;

import java.util.logging.Logger;

public class BunchOfKeysComponent extends Component{
    private Key[] keys;
    private transient final Logger bunchOfKeysLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity associated entity
     * @param maxNumberOfKey regulates the number of collectible keys
     */
    public BunchOfKeysComponent(Entity entity, int maxNumberOfKey) {
        super(entity);
        this.keys = new Key[maxNumberOfKey];
        bunchOfKeysLogger.info("BunchOfKeysComponent added to " + entity.getClass().getName());
    }

    /**
     * Adding a key to the BuchOfKeys
     *
     * @param key is the key to collect
     */
    public void addKey(Key key){
        for(int index = 0; index < keys.length; index++){
            if(keys[index] == null){
                keys[index] = key;
                Game.removeEntity(key);
                bunchOfKeysLogger.info(key.getClass().getName() + " was added to " + this.getClass().getName());
                break;
            }
        }
    }

    /**
     * Removing one Key of the BunchOfKeys
     *
     * @param key is the key to remove
     * @return true, if the Key was in the BunchOfKeys or false, if the Key was not in the BunchOfKeys
     */
    public boolean removeKey(Key key){
        for(int index = 0; index < keys.length; index++){
            if(keys[index].equals(key)){
                keys[index] = null;
                bunchOfKeysLogger.info(key.getClass().getName() + " was removed from " + this.getClass().getName());
                return true;
            }
        }
        return false;
    }
}
