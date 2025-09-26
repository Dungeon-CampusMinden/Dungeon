package contrib.components;

import contrib.utils.components.showImage.ShowImageText;
import contrib.utils.components.showImage.TransitionSpeed;
import core.Component;
import core.Entity;
import java.util.function.BiConsumer;

/** Component that marks an entity as one that can show an image in fullscreen when triggered. */
public class ShowImageComponent implements Component {

  private String imagePath;
  private ShowImageText textConfig;
  private boolean isUIOpen = false;
  private TransitionSpeed transitionSpeed = TransitionSpeed.MEDIUM;
  private BiConsumer<Entity, Entity> onOpenAction;
  private BiConsumer<Entity, Entity> onCloseAction;
  private Entity overlay;

  /** Defines the maximum size the image should occupy on the screen, in its biggest axis. */
  private float maxSize = 0.85f;

  /**
   * Creates a ShowImageComponent with the specified image path.
   *
   * @param imagePath the path of the image to be shown
   */
  public ShowImageComponent(String imagePath) {
    this.imagePath = imagePath;
  }

  /**
   * Executes the onOpenAction if it is set.
   *
   * @param entity the entity that triggered the action
   * @param overlay the overlay entity displaying the image
   */
  public void onOpen(Entity entity, Entity overlay) {
    if (onOpenAction != null) {
      onOpenAction.accept(entity, overlay);
    }
  }

  /**
   * Executes the onCloseAction if it is set.
   *
   * @param entity the entity that triggered the action
   * @param overlay the overlay entity displaying the image
   */
  public void onClose(Entity entity, Entity overlay) {
    if (onCloseAction != null) {
      onCloseAction.accept(entity, overlay);
    }
  }

  /**
   * Gets the path of the image to be shown.
   *
   * @return the path of the image
   */
  public String imagePath() {
    return imagePath;
  }

  /**
   * Sets the path of the image to be shown.
   *
   * @param imagePath the path of the image
   * @return this component for chaining
   */
  public ShowImageComponent imagePath(String imagePath) {
    this.imagePath = imagePath;
    return this;
  }

  /**
   * Gets the text config for the image.
   *
   * @return the text config
   */
  public ShowImageText textConfig() {
    return textConfig;
  }

  /**
   * Sets the text config for the image.
   *
   * @param textConfig the text config
   * @return this component for chaining
   */
  public ShowImageComponent textConfig(ShowImageText textConfig) {
    this.textConfig = textConfig;
    return this;
  }

  /**
   * Get whether the UI is currently open.
   *
   * @return true if the UI is open, false otherwise
   */
  public boolean isUIOpen() {
    return isUIOpen;
  }

  /**
   * Sets whether the UI is currently open.
   *
   * @param isUIOpen true if the UI is open, false otherwise
   * @return this component for chaining
   */
  public ShowImageComponent isUIOpen(boolean isUIOpen) {
    this.isUIOpen = isUIOpen;
    return this;
  }

  /**
   * Sets the action to be performed when the image is opened.
   *
   * @param onOpenAction the action to be performed
   * @return this component for chaining
   */
  public ShowImageComponent onOpenAction(BiConsumer<Entity, Entity> onOpenAction) {
    this.onOpenAction = onOpenAction;
    return this;
  }

  /**
   * Sets the action to be performed when the image is closed.
   *
   * @param onCloseAction the action to be performed
   * @return this component for chaining
   */
  public ShowImageComponent onCloseAction(BiConsumer<Entity, Entity> onCloseAction) {
    this.onCloseAction = onCloseAction;
    return this;
  }

  /**
   * Gets the overlay entity used to display the image.
   *
   * @return the overlay entity
   */
  public Entity overlay() {
    return overlay;
  }

  /**
   * Sets the overlay entity used to display the image.
   *
   * @param overlay the overlay entity
   * @return this component for chaining
   */
  public ShowImageComponent overlay(Entity overlay) {
    this.overlay = overlay;
    return this;
  }

  /**
   * Gets the factor that defines how much of the screen the image should take up in its biggest
   * axis.
   *
   * @return the max size factor
   */
  public float maxSize() {
    return maxSize;
  }

  /**
   * Sets the factor that defines how much of the screen the image should take up in its biggest
   * axis.
   *
   * @param maxSize the max size factor (can be above 1)
   * @return this component for chaining
   */
  public ShowImageComponent maxSize(float maxSize) {
    this.maxSize = maxSize;
    return this;
  }

  /**
   * Gets the speed of the transition animation when showing the image.
   *
   * @return the transition speed
   */
  public TransitionSpeed transitionSpeed() {
    return transitionSpeed;
  }

  /**
   * Sets the speed of the transition animation when showing the image.
   *
   * @param transitionSpeed the transition speed
   * @return this component for chaining
   */
  public ShowImageComponent transitionSpeed(TransitionSpeed transitionSpeed) {
    this.transitionSpeed = transitionSpeed;
    return this;
  }
}
