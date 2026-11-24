package engine.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Tests the Tab focus chain of a group on a stage without a graphical context. */
class KeyboardFocusTest {
  private Graphics previousGraphics;
  private GL20 previousGl;
  private Stage stage;
  private final Group group = new Group();
  private final KeyboardFocus focus = KeyboardFocus.of(group);

  @BeforeEach
  void setUp() {
    previousGraphics = Gdx.graphics;
    previousGl = Gdx.gl;
    Gdx.graphics = mock(Graphics.class);
    when(Gdx.graphics.getWidth()).thenReturn(800);
    when(Gdx.graphics.getHeight()).thenReturn(600);
    Gdx.gl = mock(GL20.class);
    GameInput.reset();
    stage = new Stage(new ScreenViewport(), mock(Batch.class));
    GameInput.stage(stage);
    group.setSize(800, 600);
    stage.addActor(group);
  }

  @AfterEach
  void tearDown() {
    GameInput.reset();
    Gdx.graphics = previousGraphics;
    Gdx.gl = previousGl;
  }

  private Button button(float y) {
    Button button = new Button(new Button.ButtonStyle());
    button.setBounds(100, y, 200, 40);
    group.addActor(button);
    return button;
  }

  private TextField field(float y) {
    BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
    data.capHeight = 10;
    data.lineHeight = 14;
    // The game's font has a glyph for Tab, so a field would type it.
    for (char character : new char[] {'a', 'b', '\t'}) {
      BitmapFont.Glyph glyph = new BitmapFont.Glyph();
      glyph.id = character;
      glyph.xadvance = 6;
      data.setGlyph(character, glyph);
    }
    Texture texture = mock(Texture.class);
    TextField field =
        new TextField(
            "ab",
            new TextField.TextFieldStyle(
                new BitmapFont(data, new TextureRegion(texture), false),
                Color.BLACK,
                null,
                null,
                null));
    field.setBounds(100, y, 200, 40);
    group.addActor(field);
    return field;
  }

  @Test
  void tabGoesThroughShownEnabledControlsInOrderAndWrapsAround() {
    Button first = button(500);
    TextField name = field(440);
    button(380).setVisible(false);
    button(320).setDisabled(true);
    Button last = button(260);

    focus.move(1);
    assertSame(null, stage.getKeyboardFocus());
    focus.move(1);
    assertSame(name, stage.getKeyboardFocus());
    focus.move(1);
    assertSame(group, stage.getKeyboardFocus());
    AtomicInteger lastClicks = clicks(last);
    focus.press();
    assertEquals(1, lastClicks.get());
    focus.move(1);
    AtomicInteger firstClicks = clicks(first);
    focus.press();
    assertEquals(1, firstClicks.get());
    focus.move(-1);
    focus.press();
    assertEquals(2, lastClicks.get());
  }

  @Test
  void tabInATextFieldTypesNothingAndMovesTheFocusOn() {
    TextField name = field(500);
    Button back = button(440);
    TextField port = field(380);
    stage.setKeyboardFocus(name);
    GameInput.update(0);

    GameInput.resolver().keyDown(Keys.TAB);
    GameInput.resolver().keyTyped('\t');
    GameInput.update(0);
    GameInput.resolver().keyUp(Keys.TAB);

    assertEquals("ab", name.getText());
    assertSame(group, stage.getKeyboardFocus());
    AtomicInteger clicks = clicks(back);
    focus.press();
    assertEquals(1, clicks.get());
    focus.move(1);
    assertSame(port, stage.getKeyboardFocus());
  }

  @Test
  void enterClicksOnlyWhenTheFocusedButtonIsHit() {
    Button button = button(500);
    AtomicInteger clicks = clicks(button);
    focus.move(1);
    Actor cover = new Actor();
    cover.setBounds(0, 0, 800, 600);
    group.addActor(cover);

    assertTrue(focus.pressFocused());
    assertEquals(0, clicks.get());
    cover.remove();
    focus.press();
    assertEquals(1, clicks.get());
  }

  private static AtomicInteger clicks(Button button) {
    AtomicInteger clicks = new AtomicInteger();
    button.addListener(
        new ChangeListener() {
          @Override
          public void changed(ChangeEvent event, Actor actor) {
            clicks.incrementAndGet();
          }
        });
    return clicks;
  }
}
