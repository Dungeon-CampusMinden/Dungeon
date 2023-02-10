package hud;

import basiselements.hud.TextButtonListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import level.LevelEditor;
import tools.Constants;

/**
 * Defines the GUI of the built-in level editor
 *
 * @author Maxim Fruendt
 */
public class LevelEditorGui {

    /** Width of the editor GUI */
    protected static final int EDITOR_WIDTH = Constants.WINDOW_WIDTH / 4;
    /** Height of the editor GUI */
    protected static final int EDITOR_HEIGHT = Constants.WINDOW_HEIGHT;
    /** Padding of the editor GUI */
    protected static final int EDITOR_PADDING = 10;
    /** Spacing between two elements in the editor GUI */
    protected static final int EDITOR_ELEMENT_SPACING = 20;

    /** Width of the buttons in the editor GUI */
    protected static final int BUTTON_WIDTH = (int) (EDITOR_WIDTH * 0.75f);
    /** Height of the buttons in the editor GUI */
    protected static final int BUTTON_HEIGHT = 20;
    /** Width of the select boxes in the editor GUI */
    protected static final int SELECT_WIDTH = (int) (EDITOR_WIDTH * 0.75f);
    /** Height of the select boxes in the editor GUI */
    protected static final int SELECT_HEIGHT = 20;

    /** The level editor holds information about the editing tools */
    protected LevelEditor levelEditor;
    /** Stage to which this GUI is added to */
    protected Stage stage;

    /** Button for the LEVEL_ELEMENT editing tool */
    protected TextButton levelElementButton;
    /** Button for the OBJECT editing tool */
    protected TextButton objectButton;
    /** Button to continue/start the game */
    protected TextButton playButton;
    /** Button to pause the game */
    protected TextButton pauseButton;
    /** Select box to select the spawned level element */
    protected SelectBox<String> levelElementSelect;
    /** Select box to select the spawned object */
    protected SelectBox<String> objectSelect;
    /** Check box to toggle the free cam */
    protected CheckBox enableFreeCamCheckbox;
    /** Group for the brush buttons */
    protected ButtonGroup<TextButton> brushButtons;
    /** Parent window which contains all editing tools */
    protected Window levelEditorWindow;
    /** Skin used for the GUI objects */
    protected Skin skin;

    /**
     * Create the level editor GUI
     *
     * @param levelEditor The level editor which holds the information about the tools
     */
    public LevelEditorGui(LevelEditor levelEditor, Stage stage) {
        skin = new Skin(Gdx.files.internal("skins/default/uiskin.json"));
        this.levelEditor = levelEditor;
        this.stage = stage;
        initGuiObjects();
    }

    /** Initialize the GUI objects */
    protected void initGuiObjects() {
        initButtons();
        initSelectBoxes();
        enableFreeCamCheckbox = new CheckBox("Free cam", skin);
        enableFreeCamCheckbox.addListener(
                event -> {
                    levelEditor.setFreeCam(enableFreeCamCheckbox.isChecked());
                    return true;
                });
        enableFreeCamCheckbox.setPosition(
                EDITOR_PADDING,
                EDITOR_HEIGHT - EDITOR_PADDING - 7 * (SELECT_HEIGHT + EDITOR_ELEMENT_SPACING));

        levelEditorWindow = new Window("Level Editor", skin);
        levelEditorWindow.setBounds(
                Constants.WINDOW_WIDTH - EDITOR_WIDTH, 0, EDITOR_WIDTH, EDITOR_HEIGHT);
        levelEditorWindow.addActor(levelElementButton);
        levelEditorWindow.addActor(objectButton);
        levelEditorWindow.addActor(playButton);
        levelEditorWindow.addActor(pauseButton);
        levelEditorWindow.addActor(levelElementSelect);
        levelEditorWindow.addActor(objectSelect);
        levelEditorWindow.addActor(enableFreeCamCheckbox);
        stage.addActor(levelEditorWindow);
    }

    /** Initialize the select boxes */
    protected void initSelectBoxes() {
        levelElementSelect = new SelectBox<>(skin);
        levelElementSelect.setPosition(
                EDITOR_PADDING,
                EDITOR_HEIGHT - EDITOR_PADDING - (SELECT_HEIGHT + EDITOR_ELEMENT_SPACING));
        levelElementSelect.setItems(
                levelEditor.getSpawnableLevelElementNames().toArray(new String[0]));
        levelElementSelect.setSize(SELECT_WIDTH, SELECT_HEIGHT);
        levelElementSelect.addListener(
                event -> {
                    levelEditor.setLevelElementIndex(levelElementSelect.getSelectedIndex());
                    return true;
                });

        objectSelect = new SelectBox<>(skin);
        objectSelect.setPosition(
                EDITOR_PADDING,
                EDITOR_HEIGHT - EDITOR_PADDING - 3 * (SELECT_HEIGHT + EDITOR_ELEMENT_SPACING));
        objectSelect.setItems(levelEditor.getSpawnableObjectNames().toArray(new String[0]));
        objectSelect.setSize(SELECT_WIDTH, SELECT_HEIGHT);
        objectSelect.addListener(
                event -> {
                    levelEditor.setObjectIndex(objectSelect.getSelectedIndex());
                    return true;
                });
    }

    /** Initialize the buttons */
    protected void initButtons() {
        brushButtons = new ButtonGroup<>();
        brushButtons.setMinCheckCount(0);
        brushButtons.setMaxCheckCount(1);
        brushButtons.setUncheckLast(true);

        levelElementButton = new TextButton("Level Element", skin, "toggle");
        levelElementButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        levelElementButton.setPosition(
                EDITOR_PADDING,
                EDITOR_HEIGHT - EDITOR_PADDING - 2 * (BUTTON_HEIGHT + EDITOR_ELEMENT_SPACING));
        levelElementButton.addListener(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (levelEditor != null) {
                            levelEditor.setBrush(LevelEditor.EditorElement.LEVEL_ELEMENT);
                        }
                    }
                });

        objectButton = new TextButton("Object", skin, "toggle");
        objectButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        objectButton.setPosition(
                EDITOR_PADDING,
                EDITOR_HEIGHT - EDITOR_PADDING - 4 * (BUTTON_HEIGHT + EDITOR_ELEMENT_SPACING));
        objectButton.addListener(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (levelEditor != null) {
                            levelEditor.setBrush(LevelEditor.EditorElement.OBJECT);
                        }
                    }
                });

        playButton = new TextButton("Play", skin);
        playButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        playButton.setPosition(
                EDITOR_PADDING,
                EDITOR_HEIGHT - EDITOR_PADDING - 8 * (BUTTON_HEIGHT + EDITOR_ELEMENT_SPACING));
        playButton.addListener(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (levelEditor != null) {
                            levelEditor.startGame();
                        }
                    }
                });

        pauseButton = new TextButton("Pause", skin);
        pauseButton.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);
        pauseButton.setPosition(
                EDITOR_PADDING,
                EDITOR_HEIGHT - EDITOR_PADDING - 9 * (BUTTON_HEIGHT + EDITOR_ELEMENT_SPACING));
        pauseButton.addListener(
                new TextButtonListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (levelEditor != null) {
                            levelEditor.pauseGame();
                        }
                    }
                });

        brushButtons.add(levelElementButton);
        brushButtons.add(objectButton);
    }
}
