package contrib.modules.interaction;


import contrib.hud.DialogUtils;
import contrib.utils.EntityUtils;
import core.Entity;

import java.util.function.BiConsumer;

public class Interaction {
    /** The default interaction radius. */
    public static final int DEFAULT_INTERACTION_RADIUS = 5;

    /** If it is repeatable by default. */
    public static final boolean DEFAULT_REPEATABLE = true;

    private  final BiConsumer<Entity,Entity> onInteract;
    private final float range;
    private final boolean repeatable;
    private boolean active=true;

    public Interaction(BiConsumer<Entity, Entity> onInteract, float range, boolean repeatable) {
        this.onInteract = onInteract;
        this.range = range;
        this.repeatable = repeatable;
    }
    public Interaction(BiConsumer<Entity, Entity> onInteract){
        this(onInteract,DEFAULT_INTERACTION_RADIUS,DEFAULT_REPEATABLE);
    }

    public void interact(Entity entity, Entity who){
        if (range>= EntityUtils.getDistance(entity,who)){
        if(active){
            onInteract.accept(entity,who);
            if(!repeatable) active=false;
        }
        else {
            DialogUtils.showTextPopup("Das habe ich schon erledigt","Erledigt");
        }
    }
        else {
            DialogUtils.showTextPopup("Dafür bin ich zu weit weg.","Zu weit weg.");
        }
}
