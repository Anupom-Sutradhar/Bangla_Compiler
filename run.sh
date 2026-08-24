#!/usr/bin/env bash
set -euo pipefail

# Bangla Compiler - Clean Build Runner (Linux/macOS)
# All generated .class/.java files go under build/
# Usage: ./run.sh [source.bng]

SOURCE="${1:-input.bng}"
if [[ ! -f "$SOURCE" ]]; then
  echo "ERROR: Source file not found: $SOURCE" >&2
  exit 1
fi

BASE="$(basename "$SOURCE")"
PROGRAM="${BASE%.*}"
COMPILER_BUILD="build/compiler"
RUN_BUILD="build/runs/$PROGRAM"

mkdir -p "$COMPILER_BUILD" "$RUN_BUILD"
rm -f "$RUN_BUILD/Output.java" "$RUN_BUILD/Output.class"

echo "[1/4] Compiling Bangla compiler..."
javac -encoding UTF-8 -d "$COMPILER_BUILD" \
  Main.java TokenType.java Token.java Diagnostic.java ValueType.java \
  Lexer.java Ast.java Parser.java SemanticAnalyzer.java CodeGenerator.java

echo "[2/4] Compiling $SOURCE to $RUN_BUILD/Output.java..."
java -cp "$COMPILER_BUILD" Main "$SOURCE" "$RUN_BUILD/Output.java"

echo "[3/4] Compiling generated Java..."
javac -encoding UTF-8 -d "$RUN_BUILD" "$RUN_BUILD/Output.java"

echo "[4/4] Running generated program..."
echo "----------------------------------------"
java -cp "$RUN_BUILD" Output
echo "----------------------------------------"

echo "Done successfully."
echo "Generated files are inside: $RUN_BUILD"
