package com.altf4studios.corebringer.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Compiles and runs user-provided Java code using external javac/java.
 * Returns IDE-like, formatted output including error categories and line numbers.
 */
public class JavaExternalRunner {
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir") + "/corebringer_compiler/";
    private static final int COMPILE_TIMEOUT_SEC = 30;
    private static final int EXEC_TIMEOUT_SEC = 60;

    public String compileAndRun(String code) {
      String session = UUID.randomUUID().toString().substring(0, 8);
      String baseDir = TEMP_DIR + session + "/";
      try {
        Files.createDirectories(Paths.get(baseDir));

        String mainClass = extractMainClassName(code);
        if (mainClass == null) {
          cleanup(baseDir);
          return "❌ SYNTAX ERROR:\n   Could not find a public class declaration with a name matching the file.\n   Tip: Wrap your code as: public class Main { public static void main(String[] args){ ... } }";
        }

        Path javaFile = Paths.get(baseDir + mainClass + ".java");
        Files.write(javaFile, code.getBytes());

        String compileOut = compile(javaFile.toString(), baseDir);
        if (!compileOut.startsWith("✅")) {
          cleanup(baseDir);
          return compileOut;
        }

        String runOut = run(mainClass, baseDir);
        cleanup(baseDir);
        return runOut;
      } catch (Exception e) {
        cleanup(baseDir);
        return "❌ COMPILATION ERROR:\n   " + e.getMessage();
      }
    }

    private String compile(String javaFilePath, String baseDir) throws IOException, InterruptedException {
      ProcessBuilder pb = new ProcessBuilder("javac", javaFilePath);
      pb.directory(new File(baseDir));
      pb.redirectErrorStream(true);
      Process p = pb.start();
      String out = readAll(p);
      boolean finished = p.waitFor(COMPILE_TIMEOUT_SEC, TimeUnit.SECONDS);
      if (!finished) {
        p.destroyForcibly();
        return "❌ COMPILATION TIMEOUT:\n   Took longer than " + COMPILE_TIMEOUT_SEC + " seconds.";
      }
      if (p.exitValue() != 0) {
        return formatJavacErrors(out);
      }
      return "✅ Compilation successful.";
    }

    private String run(String className, String baseDir) throws IOException, InterruptedException {
      ProcessBuilder pb = new ProcessBuilder("java", className);
      pb.directory(new File(baseDir));
      pb.redirectErrorStream(true);
      Process p = pb.start();
      String out = readAll(p);
      boolean finished = p.waitFor(EXEC_TIMEOUT_SEC, TimeUnit.SECONDS);
      if (!finished) {
        p.destroyForcibly();
        return "❌ RUNTIME TIMEOUT:\n   Took longer than " + EXEC_TIMEOUT_SEC + " seconds.";
      }
      if (p.exitValue() != 0) {
        return formatRuntimeErrors(out);
      }
      if (out.trim().isEmpty()) {
        return "Program executed successfully.\n(No output)";
      }
      return "Program output:\n" + out.trim();
    }

    private String readAll(Process p) throws IOException {
      StringBuilder sb = new StringBuilder();
      try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
        String line;
        while ((line = r.readLine()) != null) {
          sb.append(line).append('\n');
        }
      }
      return sb.toString();
    }

    private String extractMainClassName(String code) {
      String[] lines = code.split("\n");
      for (String raw : lines) {
        String line = raw.trim();
        if (line.startsWith("public class ")) {
          String name = line.substring("public class ".length());
          int brace = name.indexOf('{');
          if (brace >= 0) name = name.substring(0, brace).trim();
          int space = name.indexOf(' ');
          if (space >= 0) name = name.substring(0, space).trim();
          return name.isEmpty() ? null : name;
        }
      }
      return null;
    }

    private String formatJavacErrors(String out) {
      StringBuilder sb = new StringBuilder();
      String[] lines = out.split("\n");
      for (String line : lines) {
        if (line.contains(": error:")) {
          String[] parts = line.split(": error:");
          String location = parts[0];
          String msg = parts.length > 1 ? parts[1].trim() : "Compilation error";
          String[] loc = location.split(":");
          String lineNum = loc.length > 1 ? loc[1] : "?";
          String type = classifyCompileError(msg);
          sb.append("🔴 ").append(type).append(" at line ").append(lineNum).append(":\n   ").append(msg).append('\n');
        } else if (!line.trim().isEmpty()) {
          sb.append(line).append('\n');
        }
      }
      if (sb.length() == 0) {
        return "❌ COMPILATION ERROR:\n" + out.trim();
      }
      return "❌ Compilation failed.\n" + sb.toString().trim();
    }

    private String formatRuntimeErrors(String out) {
      StringBuilder sb = new StringBuilder();
      String[] lines = out.split("\n");
      for (String line : lines) {
        String lower = line.toLowerCase();
        if (lower.contains("exception in thread") || lower.contains("exception:")) {
          sb.append("🔴 ").append(classifyRuntimeError(line)).append(":\n   ").append(line.trim()).append('\n');
        } else if (line.contains("(") && line.contains(":") && line.contains(")")) {
          // Stack frame like: at pkg.Class.method(File.java:42)
          int open = line.indexOf('(');
          int close = line.indexOf(')', open + 1);
          if (open > 0 && close > open) {
            String filePart = line.substring(open + 1, close);
            if (filePart.contains(":")) {
              String[] fp = filePart.split(":");
              sb.append("   → at line ").append(fp[1]).append(" in ").append(fp[0]).append('\n');
              continue;
            }
          }
          sb.append("   ").append(line.trim()).append('\n');
        } else if (!line.trim().isEmpty()) {
          sb.append(line).append('\n');
        }
      }
      if (sb.length() == 0) return "❌ RUNTIME ERROR:\n" + out.trim();
      return sb.toString().trim();
    }

    private String classifyCompileError(String msg) {
      String m = msg.toLowerCase();
      if (m.contains("expected")) return "SYNTAX ERROR";
      if (m.contains("cannot find symbol") || m.contains("incompatible types") || m.contains("bad operand")) return "TYPE ERROR";
      if (m.contains("method") && (m.contains("not found") || m.contains("cannot be applied"))) return "METHOD ERROR";
      if (m.contains("variable") && (m.contains("not found") || m.contains("already defined"))) return "VARIABLE ERROR";
      if (m.contains("class") && (m.contains("not found") || m.contains("cannot be resolved"))) return "CLASS ERROR";
      if (m.contains("package") || m.contains("import")) return "IMPORT ERROR";
      return "COMPILATION ERROR";
    }

    private String classifyRuntimeError(String line) {
      String m = line.toLowerCase();
      if (m.contains("nullpointerexception")) return "NULL POINTER EXCEPTION";
      if (m.contains("arrayindexoutofboundsexception")) return "ARRAY INDEX OUT OF BOUNDS";
      if (m.contains("numberformatexception")) return "NUMBER FORMAT EXCEPTION";
      if (m.contains("classcastexception")) return "CLASS CAST EXCEPTION";
      if (m.contains("illegalargumentexception")) return "ILLEGAL ARGUMENT EXCEPTION";
      if (m.contains("arithmeticexception")) return "ARITHMETIC EXCEPTION";
      if (m.contains("outofmemoryerror")) return "OUT OF MEMORY ERROR";
      if (m.contains("stackoverflowerror")) return "STACK OVERFLOW ERROR";
      return "RUNTIME EXCEPTION";
    }

    private void cleanup(String baseDir) {
      try {
        Path dir = Paths.get(baseDir);
        if (!Files.exists(dir)) return;
        Files.walk(dir)
          .sorted((a, b) -> b.compareTo(a))
          .forEach(p -> { try { Files.delete(p); } catch (Exception ignored) {} });
      } catch (Exception ignored) {}
    }
}


