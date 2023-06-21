package ecs.components;

import ecs.entities.Entity;
import ecs.entities.Key;
import starter.Game;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class BunchOfKeysComponent extends Component {
    private List<Key> keys;
    private transient final Logger bunchOfKeysLogger = Logger.getLogger(this.getClass().getName());

    /**
     * Create a new component and add it to the associated entity
     *
     * @param entity         associated entity
     */
    public BunchOfKeysComponent(Entity entity) {
        super(entity);
        this.keys = new LinkedList<>();
        bunchOfKeysLogger.info("BunchOfKeysComponent added to " + entity.getClass().getName());
    }

    /**
     * Adding a key to the BuchOfKeys
     *
     * @param key is the key to collect
     */
    public void addKey(Key key) {
        this.keys.add(key);
        bunchOfKeysLogger.info(key + " added to " + this.getClass().getName());
    }

    /**
     * Removing one Key of the BunchOfKeys
     *
     * @param key is the key to remove
     * @return true, if the Key was in the BunchOfKeys or false, if the Key was not
     *         in the BunchOfKeys
     */
    public boolean removeKey(Key key) {
        boolean b = this.keys.remove(key);
        if(b)
            bunchOfKeysLogger.info(key + " removed from " + this.getClass().getName());
        return b;
    }
}
