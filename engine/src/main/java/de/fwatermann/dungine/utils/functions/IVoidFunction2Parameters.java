package de.fwatermann.dungine.utils.functions;

@FunctionalInterface
public interface IVoidFunction2Parameters<P1, P2> {
  void run(P1 p1, P2 p2);
}
