package contrib.platform.gdx.hud.dialogs;

import static contrib.hud.UIUtils.defaultSkin;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import contrib.hud.dialogs.DialogContext;
import contrib.hud.dialogs.DialogContextKeys;
import contrib.hud.dialogs.HeadlessDialogGroup;
import contrib.hud.elements.AttributeBarDialogData;
import contrib.hud.elements.AttributeBarHandle;
import contrib.hud.elements.AttributeBarHandleProvider;
import contrib.hud.utils.AttributeBarUtil;
import core.Game;
import core.ui.gdx.GdxAttributeBarHandle;

/**
 * Builds the libGDX-backed progress bar dialog used for entity attribute bars.
 */
public final class GdxProgressBarDialogBuilder {

  private static final float MIN = 0f;
  private static final float MAX = 1f;
  private static final float STEP_SIZE = 0.01f;
  private static final float UPDATE_DURATION = 0.1f;
  private static final int DEFAULT_BAR_WIDTH = 50;
  private static final int DEFAULT_BAR_HEIGHT = 10;

  private GdxProgressBarDialogBuilder() {}

  /**
   * Builds a libGDX Scene2D progress bar dialog from the given context.
   *
   * @param ctx the dialog context
   * @return the Scene2D group representing the progress bar
   */
  public static Group build(DialogContext ctx) {
    if (Game.isHeadless()) {
      return new HeadlessDialogGroup("ProgressBar", null);
    }

    AttributeBarDialogData data =
      ctx.require(DialogContextKeys.PROGRESS_BAR, AttributeBarDialogData.class);
    return new AttributeBarGroup(data);
  }

  /**
   * Scene2D container that exposes an engine-agnostic {@link AttributeBarHandle}.
   */
  private static final class AttributeBarGroup extends Container<ProgressBar>
    implements AttributeBarHandleProvider {

    private final AttributeBarHandle handle;

    private AttributeBarGroup(AttributeBarDialogData data) {
      ProgressBar progressBar =
        new ProgressBar(MIN, MAX, STEP_SIZE, false, defaultSkin(), data.styleName());
      progressBar.setAnimateDuration(UPDATE_DURATION);
      progressBar.setSize(DEFAULT_BAR_WIDTH, DEFAULT_BAR_HEIGHT);

      GdxAttributeBarHandle gdxHandle = new GdxAttributeBarHandle(progressBar);
      AttributeBarUtil.updatePosition(gdxHandle, data.pc(), data.verticalOffset());
      progressBar.setVisible(true);

      this.handle = gdxHandle;

      setActor(progressBar);
      setLayoutEnabled(false);
      pack();
      setPosition(0, 0);
    }

    @Override
    public AttributeBarHandle attributeBarHandle() {
      return handle;
    }
  }
}
