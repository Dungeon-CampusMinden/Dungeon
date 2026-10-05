package feature.hud.dialogs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import engine.Game;
import engine.network.messages.c2s.DialogResponseMessage;
import engine.sound.Sounds;
import engine.utils.BaseContainerUI;
import engine.utils.FontHelper;
import engine.utils.FontSpec;
import engine.utils.Scene2dElementFactory;
import escaperoom.foundation.ui.BlackFadeCutscene;
import feature.components.UIComponent;
import feature.hud.UIUtils;
import feature.input.configuration.KeyboardConfig;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

/** Exercises the real dialog renderers with an in-memory font and no graphical context. */
class DialogCloseRenderingTest {

  private MockedStatic<Game> game;
  private MockedStatic<FontHelper> fonts;
  private MockedStatic<UIUtils> ui;
  private MockedStatic<Sounds> sounds;
  private MockedStatic<DialogCallbackResolver> callbacks;
  private final AtomicInteger confirmations = new AtomicInteger();
  private final AtomicInteger selections = new AtomicInteger();
  private final AtomicInteger resumes = new AtomicInteger();

  @BeforeEach
  void setUp() {
    Texture texture = mock(Texture.class);
    when(texture.getWidth()).thenReturn(128);
    when(texture.getHeight()).thenReturn(128);
    BitmapFont.BitmapFontData data = new BitmapFont.BitmapFontData();
    data.capHeight = 10;
    data.lineHeight = 14;
    data.spaceXadvance = 6;
    for (char character = 32; character < 127; character++) {
      BitmapFont.Glyph glyph = new BitmapFont.Glyph();
      glyph.id = character;
      glyph.width = 6;
      glyph.height = 10;
      glyph.xadvance = 6;
      data.setGlyph(character, glyph);
    }
    BitmapFont font = new BitmapFont(data, new TextureRegion(texture), false);
    fonts = mockStatic(FontHelper.class);
    fonts.when(() -> FontHelper.getFont(any(FontSpec.class))).thenReturn(font);

    Skin skin = new Skin();
    Drawable background = new TextureRegionDrawable(new TextureRegion(texture));
    skin.add("default", new Label.LabelStyle(font, Color.BLACK));
    Window.WindowStyle window = new Window.WindowStyle(font, Color.BLACK, background);
    skin.add("default", window);
    skin.add("no-title", window);
    for (String name :
        List.of(
            "generic-area",
            "blue_square_depth_flat",
            "window_background_big",
            "window_background_big_blue",
            "white")) {
      skin.add(name, background, Drawable.class);
    }
    ui = mockStatic(UIUtils.class, Mockito.CALLS_REAL_METHODS);
    ui.when(UIUtils::defaultSkin).thenReturn(skin);
    game = mockStatic(Game.class, Mockito.CALLS_REAL_METHODS);
    game.when(Game::isHeadless).thenReturn(false);
    sounds = mockStatic(Sounds.class);
    callbacks = mockStatic(DialogCallbackResolver.class);
    callbacks
        .when(() -> DialogCallbackResolver.createButtonCallback(anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              String key = invocation.getArgument(1);
              return (Consumer<DialogResponseMessage.Payload>)
                  ignored -> {
                    if (DialogContextKeys.ON_CONFIRM.equals(key)) confirmations.incrementAndGet();
                    if (DialogContextKeys.ON_OPTION_SELECTED.equals(key))
                      selections.incrementAndGet();
                    if (DialogContextKeys.ON_RESUME.equals(key)) resumes.incrementAndGet();
                  };
            });
  }

  @AfterEach
  void tearDown() {
    callbacks.close();
    sounds.close();
    game.close();
    ui.close();
    fonts.close();
  }

  @Test
  void closeRequestSkipsTypewriterThenAdvancesOnlyOnePage() {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.DIALOG_DIALOG)
            .put(DialogContextKeys.DIALOG, "First page[p]Second page")
            .build();
    Group dialog = DialogDialog.build(context);
    UIComponent component = component(dialog);
    DialogScriptView script = UIUtils.findTypeInGroup(dialog, DialogScriptView.class).orElseThrow();

    assertFalse(script.isCurrentTypewriterFinished());
    sendKey((Group) ((BaseContainerUI) dialog).getContent(), Input.Keys.ESCAPE);
    assertFalse(script.isCurrentTypewriterFinished());
    UIUtils.requestCloseDialog(component);
    assertTrue(script.isCurrentTypewriterFinished());
    assertFalse(script.isOnLastPage());
    assertEquals(0, confirmations.get());

    UIUtils.requestCloseDialog(component);
    assertTrue(script.isOnLastPage());
    assertEquals(0, confirmations.get());

    UIUtils.requestCloseDialog(component);
    assertEquals(0, confirmations.get());
    UIUtils.requestCloseDialog(component);
    assertEquals(1, confirmations.get());
  }

  @Test
  void closeRequestDoesNotConfirmAnActiveMultipleChoiceSelection() {
    DialogContext context =
        DialogContext.builder()
            .type(DialogType.DefaultTypes.MULTIPLE_CHOICE)
            .put(DialogContextKeys.DIALOG, "Question")
            .put(DialogContextKeys.OPTIONS, new ChoiceOptions(List.of(ChoiceOption.of("Yes"))))
            .build();
    Group dialog = MultipleChoiceDialog.build(context);
    UIComponent component = component(dialog);
    UIUtils.requestCloseDialog(component);
    Group content = (Group) ((BaseContainerUI) dialog).getContent();
    sendKey(content, Input.Keys.DOWN);
    sendKey(content, Input.Keys.ESCAPE);

    UIUtils.requestCloseDialog(component);
    UIUtils.requestCloseDialog(component);

    assertEquals(0, selections.get());
    sendKey(content, KeyboardConfig.INTERACT_WORLD.value());
    assertEquals(1, selections.get());
  }

  @Test
  void nonCloseableCutsceneAdvancesWithoutSkippingTheRemainingPages() {
    BitmapFont font = FontHelper.getFont(FontSpec.of(32));
    try (MockedStatic<Scene2dElementFactory> elements = mockStatic(Scene2dElementFactory.class)) {
      elements
          .when(() -> Scene2dElementFactory.createLabel(anyString(), any(FontSpec.class)))
          .thenAnswer(
              invocation ->
                  new Label(
                      invocation.getArgument(0, String.class),
                      new Label.LabelStyle(font, Color.WHITE)));
      DialogContext context =
          DialogContext.builder()
              .type(DialogType.DefaultTypes.TEXT)
              .put(DialogContextKeys.MESSAGE, new String[] {"First", "Second"})
              .put("font_sizes", new int[] {32, 32})
              .put("fadeIn", false)
              .put("fadeOut", false)
              .build();
      Group dialog = BlackFadeCutscene.build(context);
      UIComponent component = component(dialog);
      Label message = UIUtils.findTypeInGroup(dialog, Label.class).orElseThrow();
      finishFade(dialog);

      sendKey((Group) ((BaseContainerUI) dialog).getContent(), Input.Keys.ESCAPE);
      assertEquals("First", message.getText().toString());
      UIUtils.requestCloseDialog(component);
      UIUtils.requestCloseDialog(component);
      assertEquals(0, resumes.get());
      finishFade(dialog);
      assertEquals("Second", message.getText().toString());
      assertEquals(0, resumes.get());

      UIUtils.requestCloseDialog(component);
      assertEquals(1, resumes.get());
    }
  }

  private static UIComponent component(Group dialog) {
    UIComponent component = mock(UIComponent.class);
    when(component.dialog()).thenReturn(dialog);
    return component;
  }

  private static void finishFade(Group dialog) {
    // Sequence actions advance to their next action on a later frame.
    for (int frame = 0; frame < 6; frame++) dialog.act(1f);
  }

  private static void sendKey(Group actor, int keycode) {
    InputEvent event = new InputEvent();
    event.setType(InputEvent.Type.keyDown);
    event.setKeyCode(keycode);
    actor.fire(event);
  }
}
