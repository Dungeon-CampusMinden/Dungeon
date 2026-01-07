# DGIR Compiler Plugin (SPI)
This module contains the compiler plugins for converting implementation languages to DGIR.

This module provides a pluggable compiler backend for Blockly. It is packaged as a separate JAR so the main application can swap compilation backends in future iterations.

## Structure
- `blockly.compiler.java` — Dummy Java backend implementation for demonstration.
- `blockly.compiler.api` — SPI interfaces (`CompilerPlugin`, `CompilationException`) and a simple loader helper.

