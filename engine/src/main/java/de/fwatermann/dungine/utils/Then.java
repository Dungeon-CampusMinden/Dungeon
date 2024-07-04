package de.fwatermann.dungine.utils;

public class Then {

  private IVoidFunction then;
  public final IVoidFunction run;

  public Then(IVoidFunction run) {
    this.run = run;
  }

  public void then(IVoidFunction func) {
    this.then = func;
  }

  public IVoidFunction then() {
    return this.then;
  }

}
