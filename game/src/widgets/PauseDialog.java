package widgets;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;
import tools.Constants;

public final class PauseDialog extends Dialog {
    public enum Visibility
    {
        VISIBLE, NOT_VISIBLE
    }

    Visibility result;
    public PauseDialog (String title, Skin skin, String windowStyleName)
    {
        super( title, skin, windowStyleName );
        result = Visibility.NOT_VISIBLE;
    }

    public PauseDialog (String title, Skin skin )
    {
        super( title, skin );
        result = Visibility.NOT_VISIBLE;
    }

    public PauseDialog (String title, WindowStyle windowStyle) {
        super(title, windowStyle);
        result = Visibility.NOT_VISIBLE;
    }

    public PauseDialog (String title, Skin skin, String msg, boolean flagOutputDefaultMsg )
    {
        super( title, skin );
        result = Visibility.NOT_VISIBLE;

        if( flagOutputDefaultMsg || msg =="" )
            text("No message was entered.");
        else {
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

            button("End Pause",  "confirm exit" );
        }

    }

    public boolean GetResult()
    {
        if( result == Visibility.VISIBLE )
            return true;

        return false;
    }

    public void SetResult( boolean res)
    {
        if( res == false)
            result = Visibility.NOT_VISIBLE;

        result = Visibility.VISIBLE;
    }

    @Override
    protected void result(final Object object)
    {
        if( object.toString() == "confirm exit" )
            result = Visibility.NOT_VISIBLE;
    }
}
