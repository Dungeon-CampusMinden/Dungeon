package de.fwatermann.dungine.utils.functions;

@FunctionalInterface
public interface IFunction1P<R, P1> {

  R run(P1 p1);

}
