package core.ui.gdx;

import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import core.ui.StageHandle;
import core.ui.UiNodeHandle;
import java.util.Optional;

/** libGDX-backed {@link UiNodeHandle}. */
public final class GdxUiNodeHandle implements UiNodeHandle {

  private final Group group;

  public GdxUiNodeHandle(Group group) {
    this.group = group;
  }

  @Override
  public void remove() {
    if (group != null) {
      group.remove();
    }
  }

  @Override
  public int getZIndex() {
    return group != null ? group.getZIndex() : 0;
  }

  @Override
  public boolean isVisible() {
    return group != null && group.isVisible();
  }

  @Override
  public void setVisible(boolean visible) {
    if (group != null) {
      group.setVisible(visible);
    }
  }

  @Override
  public boolean isAttached() {
    return group != null && group.getStage() != null;
  }

  @Override
  public void attachTo(StageHandle stageHandle) {
    if (group == null || stageHandle == null) {
      return;
    }

    Stage targetStage = stageHandle.unwrap(Stage.class).orElse(null);
    if (targetStage == null) {
      return;
    }

    if (group.getStage() != targetStage) {
      if (group.getStage() != null) {
        group.remove();
      }
      targetStage.addActor(group);
    }
  }

  @Override
  public void toFront() {
    if (group != null) {
      group.toFront();
    }
  }

  @Override
  public void centerOn(StageHandle stageHandle) {
    if (group == null || stageHandle == null) {
      return;
    }

    group.setPosition(
      (stageHandle.getWidth() - group.getWidth()) / 2f,
      (stageHandle.getHeight() - group.getHeight()) / 2f);
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    if (type.isInstance(group)) {
      return Optional.of(type.cast(group));
    }
    return Optional.empty();
  }
}
