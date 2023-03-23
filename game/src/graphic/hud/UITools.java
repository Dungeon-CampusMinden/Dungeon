package graphic.hud;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Align;
import tools.Constants;

/**Formatierung verschiedener Inhalte der Widgets*/
public class UITools {
    private TextDialog dialog;
    public UITools ()
    {
    }

    /**function showInfoText display the content in the GUI
     * @param infoMsg Inhalt des Textes der im label ausgegeben wird
     * @param arrayOfMessages weitere messeges, die erste von denen die beschriftung des Buttons ist (var-args)*/
    public static TextDialog showInfoText(String infoMsg, String... arrayOfMessages )
    {
        String buttonMsg = "";

        if(arrayOfMessages.length > 0)
            buttonMsg = arrayOfMessages[0];

        /**Removes all wraps and replaces them with blanks*/
        infoMsg = infoMsg.replaceAll("\n", " ");

        String[] words = infoMsg.split(" ");
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

        TextDialog textDlg = new TextDialog("pause-message", buttonMsg, skin, formatedMsg, false );
        textDlg.setColor(myC);
        textDlg.setWidth(Constants.WINDOW_WIDTH -100);
        textDlg.setHeight(Constants.WINDOW_HEIGHT -100);
        textDlg.setPosition(( Constants.WINDOW_WIDTH)/2f,
            ( Constants.WINDOW_HEIGHT)/2f,
            Align.center|Align.top/2);
        return textDlg;
    }
}
