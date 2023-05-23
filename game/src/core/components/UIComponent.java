package core.components;

import com.badlogic.gdx.scenes.scene2d.Group;

import core.Component;
import core.Entity;

public class UIComponent extends Component {
    private final Group dialog;
    private final boolean pauses;

    public UIComponent(Entity entity, Group dialog, boolean pauses) {
        super(entity);
        this.dialog = dialog;
        this.pauses = pauses;
    }

    public UIComponent(Entity entity) {
        this(entity, new Group(), true);
    }

    public boolean isVisible() {
        return dialog.isVisible();
    }

    public boolean isPauses() {
        return pauses;
    }

    public Group getDialog() {
        return dialog;
    }
}
