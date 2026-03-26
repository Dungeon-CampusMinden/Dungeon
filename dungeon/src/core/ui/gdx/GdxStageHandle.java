package core.ui.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import core.ui.StageHandle;
import java.util.Objects;
import java.util.Optional;

public final class GdxStageHandle implements StageHandle {
  private final Stage stage;

  public GdxStageHandle(Stage stage) {
    this.stage = Objects.requireNonNull(stage);
  }

  @Override
  public Object raw() {
    return stage;
  }

  @Override
  public <T> Optional<T> unwrap(Class<T> type) {
    return type.isInstance(stage) ? Optional.of(type.cast(stage)) : Optional.empty();
  }

  @Override
  public float getWidth() {
    return stage.getWidth();
  }

  @Override
  public float getHeight() {
    return stage.getHeight();
  }

  @Override
  public void addActor(Object actor) {
    if (!(actor instanceof Actor a)) {
      throw new IllegalArgumentException(
        "addActor expects a libGDX Actor, but got: " + (actor == null ? "null" : actor.getClass()));
    }
    stage.addActor(a);
  }

  @Override
  public void setKeyboardFocus(Object actor) {
    if (!(actor instanceof Actor a)) {
      throw new IllegalArgumentException(
        "setKeyboardFocus expects a libGDX Actor, but got: "
          + (actor == null ? "null" : actor.getClass()));
    }
    stage.setKeyboardFocus(a);
  }

  @Override
  public int mouseX() {
    return Gdx.input != null ? Gdx.input.getX() : 0;
  }

  @Override
  public int mouseY() {
    return Gdx.input != null ? Gdx.input.getY() : 0;
  }
}
