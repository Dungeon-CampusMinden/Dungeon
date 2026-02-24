# Language Reference

This section provides a detailed reference for the syntax of the DGIR language used in the Blockly VM.
It also provides a list of all the currently available dialects and their respective features and instructions.

The core language only describes the syntax of the DGIR language and only contains the structural elements of the language.

---

## Dialects

### `builtin` — Builtin Dialect

The builtin dialect is the default dialect that is always loaded. It provides the fundamental building
blocks required by all other dialects. Its namespace is the empty string, so its ident strings carry
no prefix (e.g. `program` rather than `builtin.program`).

**Operations**

| Ident     | Class        | Description |
|-----------|--------------|-------------|
| `program` | `ProgramOp`  | Top-level container; must contain exactly one `func.func` named `main`. |

**Types**

| Ident        | Class      | Description |
|--------------|------------|-------------|
| `int`        | `IntegerT` | Fixed-width integer: `INT1` (bool), `INT8`, `INT16`, `INT32`, `INT64`. |
| `float32`    | `FloatT`   | 32-bit IEEE 754 single-precision float. |
| `float64`    | `FloatT`   | 64-bit IEEE 754 double-precision float. |
| `string`     | `StringT`  | UTF-16 Java string. |

**Attributes**

| Ident            | Class                | Description |
|------------------|----------------------|-------------|
| `integerAttr`    | `IntegerAttribute`   | An integer value together with its `IntegerT` type. |
| `stringAttr`     | `StringAttribute`    | A Java `String` value. |
| `typeAttr`       | `TypeAttribute`      | Wraps a `Type` instance as an attribute. |
| `symbolRefAttr`  | `SymbolRefAttribute` | References a symbol by name. |

---

### `arith` — Arithmetic Dialect

The `arith` dialect provides basic arithmetic operations. Its namespace is `arith`.

**Operations**

| Ident             | Class        | Description |
|-------------------|--------------|-------------|
| `arith.constant`  | `ConstantOp` | Produces a single constant value. The value and its type are held in the mandatory `"value"` typed attribute. |

---

### `cf` — Control-Flow Dialect

The `cf` dialect provides low-level, explicit control-flow operations. Its namespace is `cf`.
Both ops are terminators and must be the last operation in their parent block.

**Operations**

| Ident           | Class          | Description |
|-----------------|----------------|-------------|
| `cf.br`         | `BranchOp`     | Unconditional branch to a single target block. |
| `cf.br_cond`    | `BranchCondOp` | Conditional branch: transfers control to one of two blocks depending on a `bool` (`int1`) condition. |

---

### `func` — Function Dialect

The `func` dialect models function definitions, calls and returns. Its namespace is `func`.

**Operations**

| Ident          | Class       | Description |
|----------------|-------------|-------------|
| `func.func`    | `FuncOp`    | Declares a named function. Carries a `"sym_name"` string attribute and a `"type"` attribute containing the `FuncType`. |
| `func.call`    | `CallOp`    | Calls a named function. The callee is referenced by a `"callee"` symbol-ref attribute, verified against the enclosing symbol table. |
| `func.return`  | `ReturnOp`  | Returns from the enclosing function, optionally yielding a value whose type must match the function's output type. |

**Types**

| Ident                             | Class      | Description |
|-----------------------------------|------------|-------------|
| `func.func<(inputs) -> (output)>` | `FuncType` | A function signature: an ordered list of parameter types and an optional return type. |

---

### `io` — I/O Dialect

The `io` dialect provides console input/output operations. Its namespace is `io`.

**Operations**

| Ident             | Class          | Description |
|-------------------|----------------|-------------|
| `io.print`        | `PrintOp`      | Prints one or more values to the console. Requires at least one operand. |
| `io.consoleIn`    | `ConsoleInOp`  | Blocking console read. Returns a single value whose type must be `IntegerT`, `FloatT`, or `StringT`. |

---

### `scf` — Structured Control-Flow Dialect

The `scf` dialect provides high-level, structured control-flow operations. Its namespace is `scf`.

**Operations**

| Ident          | Class         | Description |
|----------------|---------------|-------------|
| `scf.for`      | `ForOp`       | A counted `for` loop. Body region receives the induction variable, lower bound, upper bound and step as block arguments. |
| `scf.if`       | `IfOp`        | Conditional execution. Has one region for the `then` body and optionally a second region for `else`. Condition must be `bool` (`int1`). |
| `scf.scope`    | `ScopeOp`     | Opens a new variable scope without affecting control flow. |
| `scf.break`    | `BreakOp`     | Terminates the enclosing `scf.for` loop. Valid parent: `ForOp`. |
| `scf.continue` | `ContinueOp`  | Marks the end of a structured control-flow region. Valid parents: `IfOp`, `ScopeOp`, `ForOp`. |
