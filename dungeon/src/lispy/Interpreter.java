package lispy;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import lispy.ast.*;
import lispy.values.*;

/** Lispy interpreter. */
public class Interpreter {
  /**
   * create a new environment containing builtin functions.
   *
   * @return new environment containing builtin functions
   */
  public static Env newGlobalEnv() {
    Env env = new Env();
    return installBuiltins(env);
  }

  /**
   * Evaluate an AST.
   *
   * @param program ast to evaluate
   * @return result of valuation
   */
  public static Value evaluate(AST program) {
    return eval(program, newGlobalEnv());
  }

  /**
   * Evaluate an AST in a given environment.
   *
   * @param program ast to evaluate
   * @param env environment to evaluate in
   * @return result of valuation
   */
  public static Value evaluate(AST program, Env env) {
    return eval(program, env);
  }

  // main interpreter dispatch
  private static Value eval(AST expr, Env env) {
    return switch (expr) {
      case Program p -> evalProgram(p, env);
      case NumberLiteral n -> num(n.value());
      case StringLiteral s -> str(s.value());
      case BoolLiteral b -> bool(b.value());
      case SymbolExpr s -> env.get(s.name());
      case ListExpr list -> evalList(list, env);
    };
  }

  private static Value evalProgram(Program program, Env env) {
    return program.expressions().stream()
        .map(e -> eval(e, env))
        .reduce(bool(false), (prev, curr) -> curr);
  }

  private static Value evalList(ListExpr list, Env env) {
    List<Expr> elems = list.elements();
    if (elems.isEmpty()) throw error("cannot evaluate empty list");

    return switch (elems.getFirst()) {
      case SymbolExpr s -> evalList(s, list, env);
      default -> throw error("first element must be a symbol or op, got" + elems.getFirst());
    };
  }

  private static Value evalList(SymbolExpr headExpr, ListExpr list, Env env) {
    List<Expr> elems = list.elements();
    if (elems.isEmpty()) throw error("cannot evaluate empty list");

    return switch (headExpr.name()) {
      case "let" -> evalLet(elems, env);
      case "if" -> evalIf(elems, env);
      case "while" -> evalWhile(elems, env);
      case "and" -> evalAnd(elems, env);
      case "or" -> evalOr(elems, env);
      default -> evalFn(headExpr, elems, env);
    };
  }

  private static Value evalLet(List<Expr> elems, Env env) {
    if (elems.size() < 3) throw error("let: too few arguments");

    return switch (elems.get(1)) {
      // variable: (let vname expr)
      case SymbolExpr nameSym -> evalLetVariable(nameSym, elems.get(2), env);
      // function: (let (fname p1 p2 ...) body)
      case ListExpr fnSig -> evalLetFunction(fnSig, elems.get(2), env);
      default -> throw error("let: expected '(let vname expr)' or '(let (fname p1 p2 ...) body)'");
    };
  }

  private static Value evalLetVariable(SymbolExpr nameSym, Expr expr, Env env) {
    // variable: (let vname expr)
    String vname = nameSym.name();
    Value val = eval(expr, env);
    env.define(vname, val);

    return val;
  }

  private static ClosureFn evalLetFunction(ListExpr fnSig, Expr body, Env env) {
    // function: (let (fname p1 p2 ...) body)
    List<Expr> sigElems = fnSig.elements();
    if (sigElems.isEmpty()) throw error("let: function signature expected (fname p1 p2 ...)");

    // fname
    String fname =
        switch (sigElems.getFirst()) {
          case SymbolExpr(String name) -> name;
          default -> throw error("let: function name needs to be lispy.ast.SymbolExpr");
        };

    // parameters
    List<String> params =
        sigElems.stream()
            .skip(1)
            .map(
                e ->
                    switch (e) {
                      case SymbolExpr(String name) -> name;
                      default ->
                          throw error(
                              "let: function parameters needs to be" + " lispy.ast.SymbolExpr");
                    })
            .toList();

    // define new function
    ClosureFn fn = new ClosureFn(fname, params, body, env);
    env.define(fname, fn);

    return fn;
  }

  private static Value evalIf(List<Expr> elems, Env env) {
    if (elems.size() < 3) throw error("expected '(if cond then [else])'");

    Value res = bool(false);
    Expr cond = elems.get(1);
    Expr thenexpr = elems.get(2);
    Expr elseexpr = (elems.size() >= 4) ? elems.get(3) : new BoolLiteral(false);

    if (isTruthy(eval(cond, env))) res = eval(thenexpr, env);
    else if (elems.size() >= 4) res = eval(elseexpr, env);

    return res;
  }

  private static Value evalWhile(List<Expr> elems, Env env) {
    if (elems.size() < 2) throw error("expected '(while cond body)'");

    Value res = bool(false);
    Expr cond = elems.get(1);
    Expr body = elems.get(2);

    while (isTruthy(eval(cond, env))) res = eval(body, env);

    return res;
  }

  private static Value evalAnd(List<Expr> elems, Env env) {
    return bool(elems.stream().skip(1).map(e -> eval(e, env)).allMatch(Interpreter::isTruthy));
  }

  private static Value evalOr(List<Expr> elems, Env env) {
    return bool(elems.stream().skip(1).map(e -> eval(e, env)).anyMatch(Interpreter::isTruthy));
  }

  private static Value evalFn(SymbolExpr headExpr, List<Expr> elems, Env env) {
    // evaluate function name in current env
    FnVal fn =
        switch (eval(headExpr, env)) {
          case FnVal f -> f;
          default -> throw error("function expected: " + headExpr);
        };

    // evaluate args in current env
    List<Value> args = elems.stream().skip(1).map(e -> eval(e, env)).toList();

    // apply function to args
    return fn.apply(args);
  }

  // helper functions
  private static Env installBuiltins(Env env) {
    // math
    env.define(
        "+",
        new BuiltinFn(
            "+",
            args -> {
              if (args.isEmpty()) throw error("+: expected at least one argument");
              return num(args.stream().map(Interpreter::asNum).reduce(0, Integer::sum));
            }));
    env.define(
        "-",
        new BuiltinFn(
            "-",
            args -> {
              if (args.isEmpty()) throw error("-: expected at least one argument");

              int res = asNum(args.getFirst());
              if (args.size() == 1) return num(-1 * res);
              return num(
                  args.stream().skip(1).map(Interpreter::asNum).reduce(res, (a, b) -> a - b));
            }));
    env.define(
        "*",
        new BuiltinFn(
            "*",
            args -> {
              if (args.isEmpty()) throw error("*: expected at least one argument");
              return num(args.stream().map(Interpreter::asNum).reduce(1, (a, b) -> a * b));
            }));
    env.define(
        "/",
        new BuiltinFn(
            "/",
            args -> {
              if (args.isEmpty()) throw error("/: expected at least one argument");

              int res = asNum(args.getFirst());
              if (args.size() == 1) return num(1 / res);
              return num(
                  args.stream().skip(1).map(Interpreter::asNum).reduce(res, (a, b) -> a / b));
            }));

    // comparison
    env.define(
        "=",
        new BuiltinFn(
            "=",
            args -> {
              if (args.isEmpty()) throw error("=: expected at least one argument");

              if (args.size() == 1) return bool(true);
              Value res = args.getFirst();
              return bool(args.stream().skip(1).allMatch(v -> valueEquals(res, v)));
            }));
    env.define(
        ">",
        new BuiltinFn(
            ">",
            args -> {
              if (args.isEmpty()) throw error(">: expected at least one argument");

              List<Integer> list = args.stream().map(Interpreter::asNum).toList();
              return bool(
                  IntStream.range(1, list.size())
                      .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) > 0));
            }));
    env.define(
        "<",
        new BuiltinFn(
            "<",
            args -> {
              if (args.isEmpty()) throw error("<: expected at least one argument");

              List<Integer> list = args.stream().map(Interpreter::asNum).toList();
              return bool(
                  IntStream.range(1, list.size())
                      .allMatch(i -> list.get(i - 1).compareTo(list.get(i)) < 0));
            }));

    // logic
    env.define(
        "not",
        new BuiltinFn(
            "not",
            args -> {
              if (args.size() != 1) throw error("not: expected one argument");
              return bool(!isTruthy(args.getFirst()));
            }));

    // print
    env.define(
        "print",
        new BuiltinFn(
            "print",
            args -> {
              String line =
                  args.stream().map(Value::pretty).reduce((a, b) -> a + " " + b).orElse("");
              System.out.println(line);
              return (args.isEmpty()) ? bool(true) : args.getLast();
            }));

    // native support for lists
    env.define("list", new BuiltinFn("list", ListVal::of));
    env.define(
        "cons",
        new BuiltinFn(
            "cons",
            args -> {
              if (args.size() != 2) throw error("cons: expected two arguments");

              Value head = args.getFirst();
              ListVal tail = asList(args.getLast());
              List<Value> out = new ArrayList<>(tail.elements().size() + 1);
              out.add(head);
              out.addAll(tail.elements());
              return ListVal.of(out);
            }));
    env.define(
        "head",
        new BuiltinFn(
            "head",
            args -> {
              if (args.size() != 1) throw error("head: expected one argument");

              ListVal l = asList(args.getFirst());
              if (l.isEmpty()) throw error("head: got empty list");
              return l.elements().getFirst();
            }));
    env.define(
        "tail",
        new BuiltinFn(
            "tail",
            args -> {
              if (args.size() != 1) throw error("tail: expected one argument");

              ListVal l = asList(args.getFirst());
              if (l.isEmpty()) throw error("tail: got empty list");
              return ListVal.of(l.elements().subList(1, l.elements().size()));
            }));
    env.define(
        "empty?",
        new BuiltinFn(
            "empty?",
            args -> {
              if (args.size() != 1) throw error("empty?: expected one argument");

              ListVal l = asList(args.getFirst());
              return bool(l.isEmpty());
            }));
    env.define(
        "length",
        new BuiltinFn(
            "length",
            args -> {
              if (args.size() != 1) throw error("length: expected one argument");

              ListVal l = asList(args.getFirst());
              return num(l.elements().size());
            }));
    env.define(
        "append",
        new BuiltinFn(
            "append",
            args ->
                ListVal.of(
                    args.stream()
                        .map(Interpreter::asList)
                        .flatMap(l -> l.elements().stream())
                        .toList())));
    env.define(
        "nth",
        new BuiltinFn(
            "nth",
            args -> {
              if (args.size() != 2) throw error("nth: expected two arguments");

              int i = asNum(args.getFirst());
              ListVal l = asList(args.getLast());
              if (i < 0 || i >= l.elements().size()) throw error("nth: index out of bounds");
              return l.elements().get(i);
            }));

    // allow for chaining of operations
    return env;
  }

  private static NumVal num(int v) {
    return new NumVal(v);
  }

  private static StrVal str(String v) {
    return new StrVal(v);
  }

  private static BoolVal bool(boolean v) {
    return new BoolVal(v);
  }

  private static Integer asNum(Value v) {
    return switch (v) {
      case NumVal(int value) -> value;
      default -> throw error("number expected, got: " + v.pretty());
    };
  }

  private static ListVal asList(Value v) {
    return switch (v) {
      case ListVal l -> l;
      default -> throw error("list expected, got: " + v.pretty());
    };
  }

  private static boolean isTruthy(Value v) {
    return switch (v) {
      case BoolVal(boolean value) -> value;
      default -> true;
    };
  }

  private static boolean valueEquals(Value a, Value b) {
    return switch (a) {
      case NumVal an ->
          switch (b) {
            case NumVal bn -> an.value() == bn.value();
            default -> false;
          };
      case StrVal as ->
          switch (b) {
            case StrVal bs -> as.value().equals(bs.value());
            default -> false;
          };
      case BoolVal ab ->
          switch (b) {
            case BoolVal bb -> ab.value() == bb.value();
            default -> false;
          };
      case ListVal al ->
          switch (b) {
            case ListVal bl -> {
              List<Value> aElems = al.elements();
              List<Value> bElems = bl.elements();
              yield aElems.size() == bElems.size()
                  && IntStream.range(0, aElems.size())
                      .allMatch(i -> valueEquals(aElems.get(i), bElems.get(i)));
            }
            default -> false;
          };

      case FnVal af -> af == b; // functions: use identity
    };
  }

  private static RuntimeException error(String msg) {
    return new RuntimeException(msg);
  }
}
