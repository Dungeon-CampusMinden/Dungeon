package de.fwatermann.dungine.utils.functions;

@FunctionalInterface
public interface IFunction2P<R, P1, P2> {

  R run(P1 p1, P2 p2);

}
