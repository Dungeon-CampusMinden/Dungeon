package feature.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import engine.input.GameInput;
import engine.input.InputDevice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests where the D-pad moves the pad cursor in a dialog, on a stage without a graphical context.
 */
class PadNavigationTest {
  private Graphics previousGraphics;
  private GL20 previousGl;
  private Stage stage;
  private final Group dialog = new Group();

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
    dialog.setSize(800, 600);
    stage.addActor(dialog);
  }

  @AfterEach
  void tearDown() {
    GameInput.reset();
    Gdx.graphics = previousGraphics;
    Gdx.gl = previousGl;
  }

  private static Actor button(Group parent, float x, float y) {
    Actor button = new Actor();
    button.setBounds(x, y, 100, 40);
    button.addListener(new InputListener());
    parent.addActor(button);
    return button;
  }

  private void pointAt(float x, float y) {
    Vector2 screen = stage.stageToScreenCoordinates(new Vector2(x, y));
    GameInput.resolver().pointer(0, screen.x, screen.y);
  }

  private void pointAt(Actor actor) {
    Vector2 center =
        actor.localToStageCoordinates(new Vector2(actor.getWidth() / 2, actor.getHeight() / 2));
    pointAt(center.x, center.y);
  }

  private Vector2 pointer() {
    return stage.screenToStageCoordinates(
        new Vector2(GameInput.pointer().x(), GameInput.pointer().y()));
  }

  private static void scroll(ScrollPane pane, float y) {
    pane.setScrollY(y);
    pane.updateVisualScroll();
    pane.layout();
  }

  private Actor underPointer() {
    Vector2 pointer = pointer();
    return stage.hit(pointer.x, pointer.y, true);
  }

  @Test
  void rightGoesToTheNeighbourAndUpOrDownStaysInTheRow() {
    Actor left = button(dialog, 100, 300);
    // Less than a pixel higher, as rounding leaves buttons of one row.
    Actor right = button(dialog, 220, 300.4f);
    pointAt(left);

    assertTrue(PadNavigation.move(dialog, 1, 0));
    assertSame(right, underPointer());
    assertFalse(PadNavigation.move(dialog, 0, -1));
    assertSame(right, underPointer());
    pointAt(left);
    assertFalse(PadNavigation.move(dialog, 0, 1));
    assertSame(left, underPointer());
  }

  @Test
  void atTheEdgeTheCursorStays() {
    button(dialog, 100, 300);
    Actor last = button(dialog, 220, 300);
    pointAt(last);
    Vector2 before = pointer();

    assertFalse(PadNavigation.move(dialog, 1, 0));
    assertEquals(before, pointer());
  }

  @Test
  void aHiddenControlIsSkipped() {
    Actor first = button(dialog, 100, 300);
    Actor hidden = button(dialog, 220, 300);
    Actor third = button(dialog, 340, 300);
    Actor cover = new Actor();
    cover.setBounds(hidden.getX(), hidden.getY(), hidden.getWidth(), hidden.getHeight());
    dialog.addActor(cover);
    pointAt(first);

    assertTrue(PadNavigation.move(dialog, 1, 0));
    assertSame(third, underPointer());
  }

  @Test
  void theScrollPaneKeepsTheDpadUntilItsEnd() {
    Actor page = new Actor();
    page.setSize(200, 1000);
    ScrollPane pane = new ScrollPane(page);
    pane.setBounds(100, 300, 200, 200);
    dialog.addActor(pane);
    pane.layout();
    Actor below = button(dialog, 150, 100);
    pointAt(200, 400);

    assertFalse(PadNavigation.move(dialog, 0, -1));
    pane.setScrollY(pane.getMaxY());
    pane.updateVisualScroll();
    assertTrue(PadNavigation.move(dialog, 0, -1));
    assertSame(below, underPointer());
  }

  @Test
  void aControlScrolledOutIsScrolledIntoView() {
    Group page = new Group();
    page.setSize(200, 600);
    Actor middle = button(page, 50, 200);
    Actor bottom = button(page, 50, 0);
    ScrollPane pane = new ScrollPane(page);
    pane.setBounds(100, 300, 200, 100);
    dialog.addActor(pane);
    pane.layout();
    // Upwards: scrolled by its bottom edge instead of its top, the control would stay just below.
    pane.setScrollY(pane.getMaxY());
    pane.updateVisualScroll();
    pane.layout();
    pointAt(bottom);

    assertTrue(PadNavigation.move(dialog, 0, 1));
    assertSame(middle, underPointer());
  }

  @Test
  void withoutAControlUnderTheCursorTheDirectionComesFirst() {
    button(dialog, 100, 300);
    Actor ahead = button(dialog, 400, 300);
    // 30 pixels right of the first button, 200 left of the other.
    pointAt(230, 320);

    assertTrue(PadNavigation.move(dialog, 1, 0));
    assertSame(ahead, underPointer());
  }

  @Test
  void overTextThePaneKeepsTheDpadApartFromControlsInViewInIt() {
    Group page = new Group();
    page.setSize(200, 1000);
    Actor inside = button(page, 0, 480);
    ScrollPane pane = new ScrollPane(page);
    pane.setBounds(100, 300, 200, 200);
    dialog.addActor(pane);
    pane.layout();
    Actor tab = button(dialog, 150, 520);
    button(dialog, 150, 100);

    // The button in the page is out of view, the field below the pane does not count yet.
    pointAt(250, 450);
    assertFalse(PadNavigation.move(dialog, 0, -1));
    scroll(pane, 400);
    assertTrue(PadNavigation.move(dialog, 0, -1));
    assertSame(inside, underPointer());

    // The tab above the pane counts only once the text is at its top.
    pointAt(250, 450);
    assertFalse(PadNavigation.move(dialog, 0, 1));
    scroll(pane, 0);
    assertTrue(PadNavigation.move(dialog, 0, 1));
    assertSame(tab, underPointer());
  }

  @Test
  void withNothingInTheDirectionTheCursorGoesToTheNearestControl() {
    Actor button = button(dialog, 100, 300);
    pointAt(500, 320);

    assertTrue(PadNavigation.move(dialog, 1, 0));
    assertSame(button, underPointer());
  }

  @Test
  void theFrameGoesWithAControlMadeInvisible() {
    GdxNativesLoader.load();
    Batch batch = mock(Batch.class);
    when(batch.getColor()).thenReturn(new Color());
    Actor left = button(dialog, 100, 300);
    Actor right = button(dialog, 220, 300);
    pointAt(left);
    assertTrue(PadNavigation.move(dialog, 1, 0));
    GameInput.resolver().device(InputDevice.PAD);
    Actor frame = dialog.getChildren().peek();

    frame.draw(batch, 1);
    verify(batch, atLeastOnce())
        .draw(any(TextureRegion.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    right.setVisible(false);
    clearInvocations(batch);
    frame.draw(batch, 1);
    verify(batch, never())
        .draw(any(TextureRegion.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
  }

  @Test
  void overTextControlsInViewInAnInnerPaneCount() {
    Group inner = new Group();
    inner.setSize(200, 300);
    Actor button = button(inner, 0, 220);
    ScrollPane innerPane = new ScrollPane(inner);
    innerPane.setBounds(0, 700, 200, 100);
    Group page = new Group();
    page.setSize(300, 1000);
    page.addActor(innerPane);
    ScrollPane pane = new ScrollPane(page);
    pane.setBounds(100, 100, 300, 400);
    dialog.addActor(pane);
    innerPane.layout();
    pane.layout();
    pointAt(350, 450);

    assertTrue(PadNavigation.move(dialog, 0, -1));
    assertSame(button, underPointer());
  }

  @Test
  void scrollingScrollsThePaneUnderTheCursorWhoeverHasTheScrollFocus() {
    Group page = new Group();
    page.setSize(200, 1000);
    Actor canvas = new Actor();
    canvas.setSize(200, 1000);
    page.addActor(canvas);
    ScrollPane pane = new ScrollPane(page);
    pane.setBounds(100, 300, 200, 200);
    dialog.addActor(pane);
    pane.layout();
    stage.setScrollFocus(canvas);
    GameInput.stage(stage);
    pointAt(200, 400);

    GameInput.scroll(1);
    assertTrue(pane.getScrollY() > 0);
    assertSame(canvas, stage.getScrollFocus());
  }
}
