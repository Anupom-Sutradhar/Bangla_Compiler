# A Smart Bangla Programming Compiler

A Smart Bangla Programming Compiler is a Java-based compiler project that allows users to write programs using Bangla keywords and syntax. It translates Bangla source code (`.bng`) into Python code, making programming more accessible to Bangla-speaking beginners.

## Overview

Most popular programming languages use English-based keywords and syntax. This project aims to reduce that language barrier by introducing a Bangla-based programming language with basic programming features such as variables, arithmetic operations, conditional statements, and loops.

## Features

- Bangla keywords and syntax
- Bangla Unicode identifiers
- Bangla digit literals
- Integer and decimal data types
- Arithmetic expressions with operator precedence
- Comparison operators
- Conditional statements (`if-else`)
- `while` loops
- Symbol table and semantic analysis
- Lexical and syntax error reporting
- Python code generation

## Technologies Used

- **Language:** Java
- **Target Language:** Python 3
- **Compiler Design:** Lexer, Recursive Descent Parser, Abstract Syntax Tree (AST), Semantic Analysis, Code Generation

## Compiler Architecture

The compiler follows these stages:

1. **Lexical Analysis:** Converts source code into tokens.
2. **Syntax Analysis:** Checks the program structure using a recursive descent parser.
3. **AST Construction:** Builds an Abstract Syntax Tree representing the program.
4. **Semantic Analysis:** Checks variable declarations, data types, and scope-related rules.
5. **Code Generation:** Translates the validated program into Python code.

## Example

A simplified example of Bangla-style source code:

```text
সংখ্যা x = 10
সংখ্যা y = 20
দেখাও x + y
```

The compiler translates the source program into equivalent Python code. The exact syntax depends on the implemented grammar.

## Project Structure

```text
Bangla_Compiler/
├── src/
│   └── ...
├── run.bat
├── README.md
└── ...
```

*Note: The directory structure above is illustrative. Update it to match the actual project files.*

## Getting Started

### Prerequisites

- Java Development Kit (JDK)
- Python 3
- Git

### Clone the Repository

```bash
git clone https://github.com/Anupom-Sutradhar/bangla__compiler.git
cd bangla__compiler
```

### Run the Compiler

On Windows PowerShell, if the project includes a working `run.bat` script:

```powershell
.\run.bat
```

Follow the instructions displayed by the program to compile a Bangla source file.

## Project Goals

- Make introductory programming easier for Bangla-speaking learners.
- Demonstrate the fundamental stages of compiler construction.
- Translate Bangla-based source code into executable Python code.
- Provide a foundation for future development of a Bangla programming language.

## Future Improvements

- Improved error messages in Bangla
- More data types and language constructs
- Functions and arrays
- Better scope and type checking
- IDE support and syntax highlighting
- Improved debugging tools

## Contributors

Developed as an academic project for the Compiler Design and Construction course.

- **Anupom Sutradhar** — Lexer and lexical analysis
- **Team Members** — Parser, semantic analysis, code generation, and other components

## License

This project is intended for educational and academic purposes. Add a license file if you plan to distribute it under a specific open-source license.
