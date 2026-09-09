package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
import engine.utils.Scene2dElementFactory;
import engine.utils.components.draw.TextureMap;
import engine.utils.components.path.SimpleIPath;
import feature.hud.UIUtils;
import feature.hud.dialogs.DialogCallbackResolver;
import feature.hud.dialogs.DialogContext;
import feature.hud.dialogs.DialogFactory;
import feature.hud.dialogs.DialogType;
import feature.hud.dialogs.HeadlessDialogGroup;
import rooms.programming.modules.variables.GolemProperty;
import rooms.programming.modules.variables.VariablePuzzle;

/** Valerius' separate reference book, using the open-book artwork from MushRoom. */
final class ProgrammingBindingBook extends Group {
  private static final Color INK = Color.valueOf("302b29");
  private final Group pages = new Group();

  private enum Type implements DialogType {
    BINDING_BOOK;

    @Override
    public String type() {
      return "programming.binding-book";
    }
  }

  static void register() {
    DialogFactory.register(
        Type.BINDING_BOOK,
        context ->
            Game.isHeadless()
                ? new HeadlessDialogGroup()
                : new ProgrammingBindingBook(context.dialogId()));
  }

  static void open(Entity who) {
    ProgrammingTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            DialogContext.builder().type(Type.BINDING_BOOK).build(), false, true, false, who.id());
    ui.registerCallback("close", ignored -> UIUtils.closeDialog(ui));
  }

  private ProgrammingBindingBook(String dialogId) {
    setSize(Game.windowWidth(), Game.windowHeight());
    addActor(pages);
    Image paper =
        new Image(TextureMap.instance().textureAt(new SimpleIPath("images/open-book.png")));
    paper.setBounds(0, 0, 1260, 900);
    pages.addActor(paper);

    label("Valerius' Werkstatt", 24, 80, 782, 470, 38);
    label("Nox", 48, 80, 708, 470, 68);
    TextureRegion golem =
        new TextureRegion(
            TextureMap.instance()
                .textureAt(
                    new SimpleIPath("character/monster/programming_golem/programming_golem.png")),
            0,
            0,
            64,
            72);
    Image drawing = new Image(golem);
    drawing.setBounds(210, 464, 190, 214);
    pages.addActor(drawing);
    label("Bindungsplan · Arbeitsgolem", 26, 80, 400, 470, 44);
    label(
        "Jede Eigenschaft erhält ein eigenes Gefäß. Für ganze Zahlen verwende ich Eisenkisten, "
            + "für Bruchteile Kristallflaschen.\n\n"
            + "Eine neue Essenz ersetzt den bisherigen Inhalt. Das Gefäß bleibt.\n\n"
            + "Erst nach vollständiger Bindung den Seelenkern aktivieren.",
        24,
        80,
        130,
        470,
        255);

    label("Füllungen", 38, 710, 760, 460, 64);
    int row = 0;
    for (GolemProperty property : GolemProperty.values()) {
      float y = 650 - row++ * 92;
      label(property.label(), 22, 710, y + 38, 460, 32);
      label(VariablePuzzle.essenceSolution().get(property).literal(), 32, 710, y, 460, 42);
    }
    Label close = label("Zuklappen", 24, 905, 76, 265, 44);
    close.setAlignment(Align.right);
    close.setTouchable(Touchable.enabled);
    close.addListener(
        new ClickListener() {
          @Override
          public void clicked(InputEvent event, float x, float y) {
            DialogCallbackResolver.createButtonCallback(dialogId, "close").accept(null);
          }
        });
  }

  private Label label(String text, int size, float x, float y, float width, float height) {
    Label label = Scene2dElementFactory.createLabel(text, size, INK);
    label.setWrap(true);
    label.setBounds(x, y, width, height);
    label.setTouchable(Touchable.disabled);
    pages.addActor(label);
    return label;
  }

  @Override
  public void draw(Batch batch, float alpha) {
    setSize(Game.windowWidth(), Game.windowHeight());
    float scale = Math.min(1, Math.min((getWidth() - 48) / 1260, (getHeight() - 48) / 900));
    pages.setScale(scale);
    pages.setPosition((getWidth() - 1260 * scale) / 2, (getHeight() - 900 * scale) / 2);
    super.draw(batch, alpha);
  }
}
