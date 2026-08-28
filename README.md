# Compiler Construction Project - VVPL Interpreter

This repository contains the final project for DM546: Compiler Construction at the University of Southern Denmark (SDU).

Our group implemented a compiler/interpreter pipeline for a custom educational language called **VVPL** *(Very Verbose Programming Language)*.
The projct focuses on building the language from the ground up: scanning, parsing, semantic analysis, and interpretation.

## What We Built
This project includes the core stages of a compiler/interpreter pipeline:
- **Scanner / lexical analysis** to tokenize source code
- **Parser / syntactic analysis** to build the program structure
- **Semantic analysis** to ensure the given program is valid
- **Interpreter** to execute valid VVPL programs
- **Test coverage** for language features and semantic rules

A major focus of the assignment was semantic validation before execution. The interpreter only runs after the program has passed analysis.
This includes checking for undeclared or invalid names, scope violations, type mismatches, incorrect function calls, invalid conditions, 
return-type errors, and illegal casts. Errors are collected and reported in the format `<error-type>, line <lineNo> <message>`.

## Language Overview
VVPL is small but expressive language with:
- statically typed variables
- functions with typed parameters and optional return types
- `if` / `else` conditionals
- `loop_while` loops
- block scoping
- explicit type casting
- console output through `write_to_console`
- arithmetic and logical operators

## Takeaways
This project gave me practical experience with:
- compiler design and the many caveats that come with language implementation
- structuring a Java project with Maven
- test-driven development for language features
- working as part of a team on a technical systems project
