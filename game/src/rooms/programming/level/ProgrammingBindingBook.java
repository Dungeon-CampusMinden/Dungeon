package rooms.programming.level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import engine.Entity;
import engine.Game;
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
  private static final Color FADED_INK = Color.valueOf("786955");
  private final Group pages = new Group();
  private final Group content = new Group();
  private final com.badlogic.gdx.scenes.scene2d.ui.TextButton previous;
  private final com.badlogic.gdx.scenes.scene2d.ui.TextButton next;
  private final com.badlogic.gdx.scenes.scene2d.ui.Table shell =
      new com.badlogic.gdx.scenes.scene2d.ui.Table();
  private final com.badlogic.gdx.scenes.scene2d.ui.Table pageLayout =
      new com.badlogic.gdx.scenes.scene2d.ui.Table();
  private final Group pageViewport = new Group();
  private final com.badlogic.gdx.scenes.scene2d.ui.ScrollPane scroll;
  private final Label pageCount = ProgrammingUI.label("", 16, ProgrammingUI.MUTED);
  private int spread;

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
    String reference =
        java.util.Arrays.stream(GolemProperty.values())
            .map(
                property ->
                    property.label()
                        + ": "
                        + VariablePuzzle.essenceSolution().get(property).literal())
            .collect(java.util.stream.Collectors.joining("\n"));
    ProgrammingProgress.discover(
        "variables-translation", "Valerius · Bindungsplan", reference, who);
    ProgrammingTerminal.stopWalking(who);
    var ui =
        DialogFactory.show(
            DialogContext.builder()
                .type(Type.BINDING_BOOK)
                .put(feature.hud.dialogs.DialogContextKeys.BLOCKS_GAMEPLAY_INPUT, true)
                .build(),
            false,
            true,
            false,
            who.id());
    ui.registerCallback("close", ignored -> UIUtils.closeDialog(ui));
    ui.registerCallback("quest-log", ignored -> feature.questlog.QuestLogUI.requestQuestLog(who));
  }

  private ProgrammingBindingBook(String dialogId) {
    setUserObject(engine.utils.Cursors.DEFAULT);
    setSize(Game.windowWidth(), Game.windowHeight());
    pageViewport.addActor(pages);
    Image paper =
        new Image(TextureMap.instance().textureAt(new SimpleIPath("images/open-book.png")));
    paper.setBounds(0, 0, 1260, 900);
    pages.addActor(paper);
    pages.addActor(content);
    shell.setFillParent(true);
    shell.top().pad(20);
    shell.setBackground(ProgrammingUI.background(ProgrammingUI.INK, false));
    shell
        .add(
            ProgrammingUI.header(
                "Valerius · Bindungsplan",
                new com.badlogic.gdx.scenes.scene2d.ui.Table(),
                () -> DialogCallbackResolver.createButtonCallback(dialogId, "close").accept(null)))
        .growX()
        .padBottom(16)
        .row();
    pageLayout.add(pageViewport);
    scroll = new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane(pageLayout, UIUtils.defaultSkin());
    var style =
        new com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle(scroll.getStyle());
    style.background = null;
    scroll.setStyle(style);
    scroll.setScrollingDisabled(true, false);
    scroll.setFadeScrollBars(false);
    scroll.setFlickScroll(false);
    scroll.addListener(
        new com.badlogic.gdx.scenes.scene2d.InputListener() {
          @Override
          public boolean mouseMoved(InputEvent event, float x, float y) {
            if (getStage() != null) getStage().setScrollFocus(scroll);
            return false;
          }
        });
    shell.add(scroll).grow().minSize(0).row();
    previous = ProgrammingUI.button("Zurück", false, () -> turn(-1));
    next = ProgrammingUI.button("Weiter", true, () -> turn(1));
    var navigation = new com.badlogic.gdx.scenes.scene2d.ui.Table();
    navigation.add(previous).width(125).minHeight(44);
    navigation
        .add(
            ProgrammingUI.button(
                "Questlog",
                false,
                () ->
                    DialogCallbackResolver.createButtonCallback(dialogId, "quest-log")
                        .accept(null)))
        .width(140)
        .minHeight(44)
        .padLeft(12);
    pageCount.setAlignment(Align.center);
    navigation.add(pageCount).growX();
    navigation.add(next).width(150).minHeight(44);
    shell.add(navigation).growX().padTop(12);
    addActor(shell);
    showSpread();
  }

  private void turn(int direction) {
    int target = spread + direction;
    if (target < 0 || target >= GolemProperty.values().length / 2) return;
    spread = target;
    showSpread();
    content.clearActions();
    content.getColor().a = 0;
    content.addAction(Actions.fadeIn(.14f));
  }

  private void showSpread() {
    content.clearChildren();
    page(GolemProperty.values()[spread * 2], 80, spread * 2 + 1);
    page(GolemProperty.values()[spread * 2 + 1], 710, spread * 2 + 2);
    previous.setDisabled(spread == 0);
    next.setDisabled((spread + 1) * 2 >= GolemProperty.values().length);
    pageCount.setText("Seiten " + (spread * 2 + 1) + " und " + (spread * 2 + 2) + " von 6");
    scroll.setScrollY(0);
  }

  /**
   * One property per paper page; the value stays tied to the actual puzzle solution.
   *
   * @param property property illustrated on this page
   * @param x left edge of the page content
   * @param number printed page number, starting at one
   */
  private void page(GolemProperty property, float x, int number) {
    Label heading = label("Valerius · Bindungsplan für Nox", 22, x, 782, 460, 38);
    heading.setColor(FADED_INK);
    label(property.label(), 40, x, 708, 460, 68);
    String path =
        switch (property) {
          case NAME -> "character/monster/programming_golem/programming_golem.png";
          case LIFE_ENERGY -> "items/pickups/heart_pickup.png";
          case MANA -> "items/rpg/item_gem_amethyst.png";
          case ACTIVATED -> "items/rpg/item_orb.png";
          case VIEW_DIRECTION -> "items/rpg/item_compass.png";
          case STEPS -> "items/rpg/armor_boots_iron.png";
        };
    TextureRegion region =
        new TextureRegion(TextureMap.instance().textureAt(new SimpleIPath(path)));
    if (property == GolemProperty.NAME) region.setRegion(0, 0, 64, 72);
    Image drawing = new Image(region);
    float size = property == GolemProperty.NAME ? 214 : 176;
    float width = size * region.getRegionWidth() / region.getRegionHeight();
    drawing.setBounds(x + (460 - width) / 2, 475 + (214 - size) / 2, width, size);
    content.addActor(drawing);

    Label value =
        label(VariablePuzzle.essenceSolution().get(property).literal(), 48, x, 362, 460, 76);
    value.setAlignment(Align.center);
    String note =
        switch (property) {
          case NAME ->
              "So habe ich ihn genannt. Der Name bleibt, auch wenn ich seinen Kern erneuere.";
          case LIFE_ENERGY ->
              "Die Ladung für seinen schweren Steinkörper. Weniger war beim letzten Versuch nicht genug.";
          case MANA -> "Die Menge für die Seelenbindung. Den halben Anteil nicht weglassen.";
          case ACTIVATED ->
              "Die Freigabe für den Antrieb. Den Seelenkern erst aktivieren, wenn die Bindung vollständig ist.";
          case VIEW_DIRECTION -> "O wie Osten. Dorthin soll er sich nach dem Erwachen wenden.";
          case STEPS ->
              "Stand des Schrittzählers bei der letzten Wartung. Diesen Wert bei der Bindung wiederherstellen.";
        };
    label(note, 26, x + 12, 195, 436, 128).setAlignment(Align.topLeft);
    Label pageNumber = label(number + " / " + GolemProperty.values().length, 22, x, 126, 460, 36);
    pageNumber.setAlignment(Align.center);
    pageNumber.setColor(FADED_INK);
  }

  private Label label(String text, int size, float x, float y, float width, float height) {
    Label label = ProgrammingUI.zoomLabel(text, size, INK);
    label.setAlignment(Align.left);
    label.setWrap(true);
    label.setBounds(x, y, width, height);
    label.setTouchable(Touchable.disabled);
    content.addActor(label);
    return label;
  }

  @Override
  public void draw(Batch batch, float alpha) {
    setSize(Game.windowWidth(), Game.windowHeight());
    shell.validate();
    float scale =
        Math.min(Math.max(1, scroll.getWidth() - 18) / 1260, Math.max(1, scroll.getHeight()) / 900);
    pages.setScale(scale);
    pageLayout.getCell(pageViewport).size(1260 * scale, 900 * scale);
    pageLayout.invalidateHierarchy();
    shell.validate();
    super.draw(batch, alpha);
  }
}
