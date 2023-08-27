package contrib.item;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class Items {

    private static final Logger LOGGER = Logger.getLogger(Items.class.getName());
    private static final Map<String, Class<? extends Item>> items = new HashMap<>();

    static {
        items.put("sword_fire", ItemSwordFire.class);
        items.put("sword_ice", ItemSwordIce.class);
    }

    public static Class<? extends Item> getItem(String id) {
        return items.get(id);
    }

    protected static boolean isRegistered(Class<? extends Item> clazz) {
        return items.containsValue(clazz);
    }
}
