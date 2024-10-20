package de.fwatermann.dungine.ui;

/**
 * Abstract class for all UI components.
 *
 * @param <T> the type of the UI component
 */
public abstract class UIComponent<T extends UIComponent<?>> {

  /** Creates a new UIComponent. */
  protected UIComponent() {}
}
