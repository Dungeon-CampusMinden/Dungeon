# DGIR: An extendable intermediate representation for Blockly.

DGIR, short for ***D**un**g**eon **IR***, is an intermediate representation (IR) designed to facilitate the execution of Blockly programs within the Dungeon environment.
It serves as a bridge between the high-level programming languages such as Blockly or Java and the low-level virtual machine (VM) that executes the generated code.

It is closely based on [Bril](https://github.com/sampsyo/bril) and [MLIR](https://mlir.llvm.org/) and aims to provide an easy-to-use and flexible intermediate representation for Dungeon programs.

## Key Features
* Style: DGIR is a linear intermediate representation, meaning that programs are represented as a sequence of instructions executed in order.
* Extensibility: DGIR is designed to be easily extended to support new features via the use of dialects. These can be loaded at runtime and allow for custom instructions and behaviors.
* Debugging Support: DGIR includes support for debugging features such as breakpoints and source mapping, making it easier to diagnose and fix issues in programs. This is enforced via source positions attached to each instruction.
* Type System: DGIR includes a type system that allows for static type checking and inference.
* Portable: DGIR is fully textual and supports JSON as well as clear text formats.

See the [language reference](language-reference.md) for more details.
