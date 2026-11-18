package engine.input;

/**
 * Sees every press before the resolver routes it, see {@link InputResolver#capture(InputCapture)}.
 * Key presses arrive as {@link Binding.Key} without prefixes, mouse buttons as {@link
 * Binding.MouseButton}, pad buttons, triggers and stick directions as pad bindings.
 */
public interface InputCapture {

  /**
   * Called when an input goes down.
   *
   * @param input the input
   * @return true to take the input; it then reaches neither the UI nor an action
   */
  boolean down(Binding input);

  /**
   * Called when a key comes up, whether or not its press was taken.
   *
   * @param input the key
   */
  void up(Binding input);
}
