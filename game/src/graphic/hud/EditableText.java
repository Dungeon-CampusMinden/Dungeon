package graphic.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class EditableText extends TextArea{

public EditableText(Skin skin ) {
    super("", skin );
    this.setMessageText("Click here...");
    this.setColor(Color.YELLOW);

    Table scrollTable = new Table();
    scrollTable.add(this).minHeight(500).expandX().fillX().colspan(2);
    scrollTable.setSize(395, 95);
    scrollTable.setColor(Color.GREEN);
    scrollTable.row();

    }
}
