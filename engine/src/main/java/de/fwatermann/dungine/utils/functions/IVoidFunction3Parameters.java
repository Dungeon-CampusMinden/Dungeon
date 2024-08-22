package de.fwatermann.dungine.utils.functions;

@FunctionalInterface
public interface IVoidFunction3Parameters<P1, P2, P3> {
  void run(P1 p1, P2 p2, P3 p3);
}
