package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import tools.Constants;
/**Stellt Dialog mit scrollbarem Label und einem Button dar*/
public final class TextDialog extends Dialog {
    public enum Visibility
    {
        VISIBLE, NOT_VISIBLE
    }

    Visibility result;
    /**
     * @param title Titel des Dialoges
     * @param skin Skin für den Dialog(Style der Elemente)
     */
    public TextDialog(String title, Skin skin, String windowStyleName)
    {
        super( title, skin, windowStyleName );
        result = Visibility.NOT_VISIBLE;
    }

    /**
     * @param title Titel des Dialoges
     * @param skin Skin für den Dialog(Style der Elemente)*/
    public TextDialog(String title, Skin skin )
    {
        super( title, skin );
        result = Visibility.NOT_VISIBLE;
    }
    /**
     * @param title Titel des Dialoges
     * @param windowStyle Styling des Fensters */
    public TextDialog(String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        result = Visibility.NOT_VISIBLE;
    }
    /**
     * @param title Titel des Dialoges
     * @param buttonMsg text für den Button
     * @param skin Skin für den Dialog(Style der Elemente)
     * @param msg Inhalt der im scrollbaren Label dargestellt wird
     * @param flagOutputDefaultMsg ausgabe einer Default message, wenn kein Text zum Ausgeben vorhanden ist*/

    public TextDialog(String title, String buttonMsg, Skin skin, String msg, boolean flagOutputDefaultMsg )
    {
        super( title, skin );
        result = Visibility.NOT_VISIBLE;

        if( flagOutputDefaultMsg || msg =="" )
            msg = "No message was load.";

        Label gamelog = new Label(msg, skin);
        gamelog.setAlignment(Align.left);
        gamelog.setColor(Color.BLUE);

        Table scrollTable = new Table();
        scrollTable.add(gamelog);
        scrollTable.row();

        ScrollPane scroller = new ScrollPane(scrollTable, skin);
        scroller.setFadeScrollBars(false);
        scroller.setScrollbarsVisible(true);

        Table table = new Table();
        table.setFillParent(true);
        table.add(scroller).size(Constants.WINDOW_WIDTH -200, Constants.WINDOW_HEIGHT -200);
        this.addActor(table);

        if( buttonMsg == "" )
            buttonMsg = "Ok";

        button(buttonMsg, "confirm exit" );
    }
/**zeigt an ob der Dialog sichtbar oder unsichtbar ist*/
    public boolean getResult()
    {
        if( result == Visibility.VISIBLE )
            return true;

        return false;
    }
/**setzen eines Wertes ob Dialog sichtbar oder unsichtbar ist
 * @param res zeigt an ob Dialog sichtbar oder unsichtbar ist*/
    public void setResult( boolean res)
    {
        if( res == false)
            result = Visibility.NOT_VISIBLE;

        result = Visibility.VISIBLE;
    }

    /**Zeigt resultat nach drücken des Buttons
     * @param object Objekt, welches mit dem Button assoziiert wird */
    @Override
    protected void result(final Object object)
    {
        if( object.toString() == "confirm exit" )
            result = Visibility.NOT_VISIBLE;
    }
}
