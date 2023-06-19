package core.hud;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.Align;

import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

/** Some of the basic layout needed to show either a TextDialog or a QuestionDialog. */
public class DialogDesign extends VerticalGroup {
    // simple regex which allows path/to/image.png allowed file endings are png/bmp/tiff/jpeg
    private static final String PATTERN_IMAGE_FINDER = "(\\w+[\\\\|/])*\\w+.(?>png|bmp|tiff|jpeg)";

    /** Creates a Left aligned VerticalGroup which completely fills the Parent UI Element. */
    public DialogDesign() {
        super();
        setFillParent(true);
        left();
    }

    /**
     * Simple Helper with default ScollPane Configuration
     *
     * @param skin how the ScrollPane should look like
     * @param container a container which should be scrollable
     * @return the ScrollPane which then can be added to any UI Element
     */
    public static ScrollPane createScrollPane(Skin skin, Actor container) {
        ScrollPane scrollPane = new ScrollPane(container, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollbarsVisible(true);

        return scrollPane;
    }

    /**
     * Simple default Textarea with default Text
     *
     * @param skin how the ScrollPane should look like
     * @return the TextArea which then can be added to any UI Element
     */
    public static TextArea createEditableText(Skin skin) {
        return new TextArea("Click here...", skin);
    }

    /**
     * Creates a simple Dialog which only has static Text shown.
     *
     * @param skin Skin for the dialogue (resources that can be used by UI widgets)
     * @param outputMsg Content displayed in the scrollable label
     */
    public static Group createTextDialog(Skin skin, String outputMsg) {
        return createScrollPane(
                skin, new Container<>(new Label(outputMsg, skin)).align(Align.center));
    }

    /**
     * a simple implementation to find a filepath in a String
     *
     * @param quizQuestion the string which may contain a path
     * @return an Optional of either the path or empty when there is no path in the given question
     */
    public static Optional<String> imagePathExtractor(String quizQuestion) {
        Optional<MatchResult> first =
                Pattern.compile(PATTERN_IMAGE_FINDER).matcher(quizQuestion).results().findFirst();
        return first.map(MatchResult::group);
    }
}
