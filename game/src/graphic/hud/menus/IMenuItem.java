package graphic.hud.menus;

import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/** Provides custom functionality for menu items. */
public interface IMenuItem {

    /**
     * Used for implementing a custom listener to a menu item.
     *
     * @param clickListener the custom listener
     */
    void executeAction(ClickListener clickListener);
}
