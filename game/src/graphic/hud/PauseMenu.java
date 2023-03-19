package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import controller.ScreenController;
import tools.Constants;
import widgets.PauseDialog;

    /**Class creates menu in the form of a dialogue to pause the game*/
    public class PauseMenu<T extends Actor> extends ScreenController<T> {

    /**Dialogue with info field and button to cancel play pause*/
    private static PauseDialog dialog;

    /** Creates a new PauseMenu with a new Spritebatch */
    public PauseMenu() {
        this(new SpriteBatch());
    }

    /** Creates a new PauseMenu with a given Spritebatch */
    public PauseMenu(SpriteBatch batch) {
        super(batch);
    /**pauseMsg is only used for testing the function showInfoText().
    * ToDo: Transfer content from external source via parameter!!! */
        final String pauseMsg = "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.";
        showInfoText( pauseMsg );
        dialog.SetResult(true);
        add((T)dialog );
        hideMenu();
    }
/**function showInfoText to pause the game, display the content in the GUI, after pressing the button "End Pause" the game is continued.*/
    static void showInfoText(String msg)
    {
        /**Removes all wraps and replaces them with blanks*/
        msg = msg.replaceAll("\n", " ");

        String[] words = msg.split(" ");
        String formatedMsg = "";

        /**maxRowLength limits line length to a maximum of 40 characters*/
        final int maxRowLength = 40;
        int sumLength = 0;

        /**String formatting after certain line length*/
        for (String word : words) {
            sumLength += word.length();
            formatedMsg =  formatedMsg + word + " ";

            if(sumLength > maxRowLength) {
                formatedMsg += "\n";
                sumLength =0;
            }
        }

        Skin skin = new Skin(Gdx.files.internal("skin/uiskin.json"));
        Color myC = new Color(1,1,1,1);

        PauseDialog pauseDlg = new PauseDialog("pause-message", skin, formatedMsg, false );
        pauseDlg.setColor(myC);
        pauseDlg.setWidth(Constants.WINDOW_WIDTH -100);
        pauseDlg.setHeight(Constants.WINDOW_HEIGHT -100);
        pauseDlg.setPosition(( Constants.WINDOW_WIDTH)/2f,
                             ( Constants.WINDOW_HEIGHT)/2f,
                       Align.center|Align.top/2);
        dialog = pauseDlg;
    }

    public boolean isPaused() {
        if(!dialog.GetResult())
        {
            hideMenu();
            return false;
        }
        showMenu();
        return true;
    }

    public boolean MustBeHidden() {
        return !dialog.GetResult();
    }

    /** shows the Menu */
    public void showMenu() {
        this.forEach((Actor s) -> s.setVisible(true));
    }

    /** hides the Menu */
    public void hideMenu() {
        this.forEach((Actor s) -> s.setVisible(false));
        dialog.SetResult(true);
    }
}
