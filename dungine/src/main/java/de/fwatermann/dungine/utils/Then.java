package de.fwatermann.dungine.utils;

import de.fwatermann.dungine.utils.functions.IVoidFunction;

/**
 * The Then class provides a mechanism to chain actions that should be executed sequentially. It
 * holds a primary action to run and an optional subsequent action to execute after the primary
 * action.
 */
public class Then {

  /** The function to be executed after the primary action. */
  private IVoidFunction then;

  /** The primary function to be executed. */
  public final IVoidFunction run;

  /**
   * Constructs a Then object with the specified primary action.
   *
   * @param run the primary action to be executed
   */
  public Then(IVoidFunction run) {
    this.run = run;
  }

  /**
   * Sets the function to be executed after the primary action.
   *
   * @param func the function to be executed after the primary action
   */
  public void then(IVoidFunction func) {
    this.then = func;
  }

  /**
   * Returns the function to be executed after the primary action.
   *
   * @return the function to be executed after the primary action
   */
  public IVoidFunction then() {
    return this.then;
  }
}
