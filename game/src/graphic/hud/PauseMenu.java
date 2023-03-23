package graphic.hud;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import controller.ScreenController;

/**creates menu in the form of a dialogue to pause the game*/
    public class PauseMenu<T extends Actor> extends ScreenController<T> {

    /**Dialogue with info field and button to cancel play pause*/
    private static TextDialog dialog;

    /** Creates a new PauseMenu with a new Spritebatch */
    public PauseMenu() {
        this(new SpriteBatch());
    }

    /** Creates a new PauseMenu with a given Spritebatch
     * @param batch zur Darstellung der Texturen*/
    public PauseMenu(SpriteBatch batch) {
        super(batch);
    /**pauseMsg is only used for testing the function showInfoText().
    * ToDo: Transfer content from external source via parameter!!! */
        final String pauseMsg = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        UITools tool = new UITools();
        dialog = tool.showInfoText( pauseMsg );
        dialog.setResult(true);
        add((T)dialog );
        hideMenu();
    }

/**zeigt ob menü das Programm pausiert oder nicht*/
    public boolean isPaused() {
        if(!dialog.getResult())
        {
            hideMenu();
            return false;
        }
        showMenu();
        return true;
    }
/**teilt mit ob dialog versteckt werden muss
 * @return true wenn dialog sichtbar sonst false*/
    public boolean mustBeHidden() {
        return !dialog.getResult();
    }

    /** shows the Dialog */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
    }

    /** hides the Dialog */
    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
        dialog.setResult(true);
    }
}
