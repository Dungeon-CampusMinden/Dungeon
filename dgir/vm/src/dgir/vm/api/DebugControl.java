package dgir.vm.api;

/**
 * Tells the {@link VM} how to proceed after a {@link Debugger} callback.
 *
 * <ul>
 *   <li>{@link #CONTINUE} — execute the current operation and advance normally.
 *   <li>{@link #PAUSE}    — suspend execution; the VM will block in {@code VM.run()} until
 *       {@code VM.resume()} or {@code VM.stepOver()} is called from another thread.
 * </ul>
 */
public enum DebugControl {
  /** Keep running — execute the current operation and advance normally. */
  CONTINUE,
  /**
   * Suspend execution after delivering this callback. The VM will block until
   * {@code VM.resume()} is called.
   */
  PAUSE
}


