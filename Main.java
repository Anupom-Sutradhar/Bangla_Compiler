import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class Main {
    public static void main(String[] args) {
        int exitCode = run(args);
        if (exitCode != 0) System.exit(exitCode);
    }

    private static int run(String[] args) {
        String inputFile = args.length >= 1 ? args[0] : "input.bng";
        String outputFile = args.length >= 2 ? args[1] : "Output.java";

        Path inputPath = Path.of(inputFile);
        Path outputPath = Path.of(outputFile);
        Path outputNamePath = outputPath.getFileName();
        if (outputNamePath == null || !outputNamePath.toString().endsWith(".java")) {
            System.err.println("IO ERROR: Output filename must end with .java");
            return 1;
        }

        String outputName = outputNamePath.toString();
        String className = outputName.substring(0, outputName.length() - 5);
        if (!isValidJavaClassName(className)) {
            System.err.println("IO ERROR: Output filename must have a valid Java class name, e.g. Output.java");
            return 1;
        }

        try {
            // Never leave a stale target from an earlier successful compilation.
            Files.deleteIfExists(outputPath);

            String source = Files.readString(inputPath, StandardCharsets.UTF_8);

            Lexer lexer = new Lexer(source);
            List<Token> tokens = lexer.scanTokens();
            List<Diagnostic> allErrors = new ArrayList<>(lexer.getDiagnostics());

            Parser parser = new Parser(tokens);
            Ast.Program program = parser.parse();
            allErrors.addAll(parser.getDiagnostics());

            // Semantic analysis is still useful for correctly parsed statements even if parser
            // recovered from another line. We only generate code when every stage is error-free.
            SemanticAnalyzer analyzer = new SemanticAnalyzer();
            allErrors.addAll(analyzer.analyze(program));

            if (!allErrors.isEmpty()) {
                System.err.println("Compilation failed with " + allErrors.size() + " error(s):");
                for (Diagnostic d : allErrors) System.err.println("  " + d);
                System.err.println("No target Java file was generated.");
                return 1;
            }

            String javaCode = new CodeGenerator(className).generate(program);
            Path parent = outputPath.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.writeString(outputPath, javaCode, StandardCharsets.UTF_8);

            System.out.println("Compilation Successful");
            System.out.println("Source : " + inputFile);
            System.out.println("Target : " + outputFile);
            System.out.println("Next   : javac -encoding UTF-8 \"" + outputFile + "\"");
            System.out.println("         java -cp \"" + (outputPath.getParent() == null ? "." : outputPath.getParent()) + "\" " + className);
            return 0;

        } catch (IOException e) {
            System.err.println("IO ERROR: " + e.getMessage());
            return 1;
        } catch (Exception e) {
            // Graceful failure instead of exposing a NullPointerException or stack trace to users.
            System.err.println("INTERNAL COMPILER ERROR: " + e.getClass().getSimpleName() + ": " + safeMessage(e));
            return 2;
        }
    }

    private static boolean isValidJavaClassName(String name) {
        if (name.isEmpty() || !Character.isJavaIdentifierStart(name.charAt(0))) return false;
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) return false;
        }
        return !isJavaKeyword(name);
    }

    private static boolean isJavaKeyword(String s) {
        return switch (s) {
            case "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
                 "class", "const", "continue", "default", "do", "double", "else", "enum",
                 "extends", "final", "finally", "float", "for", "goto", "if", "implements",
                 "import", "instanceof", "int", "interface", "long", "native", "new", "package",
                 "private", "protected", "public", "return", "short", "static", "strictfp",
                 "super", "switch", "synchronized", "this", "throw", "throws", "transient",
                 "try", "void", "volatile", "while", "true", "false", "null" -> true;
            default -> false;
        };
    }

    private static String safeMessage(Exception e) {
        String msg = e.getMessage();
        return msg == null || msg.isBlank() ? "Unexpected internal failure." : msg;
    }
}
