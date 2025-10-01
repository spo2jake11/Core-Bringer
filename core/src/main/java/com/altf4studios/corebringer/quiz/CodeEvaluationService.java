package com.altf4studios.corebringer.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Flexible evaluator for user-submitted code and output.
 * - Uses permissive regex heuristics (multiple ways to solve)
 * - Primary: output-based match (when expected can be inferred)
 * - Secondary: source pattern matches for core concepts (increment, print, concat, cast, etc.)
 *
 * This service does NOT use the 'chance' field.
 */
public final class CodeEvaluationService {

    public static final class EvaluationResult {
        public boolean passed;
        public int score;
        public int total;
        public List<String> feedback = new ArrayList<>();

        @Override public String toString() { return String.format(Locale.US, "passed=%s score=%d/%d", passed, score, total); }
    }

    /** Normalize source for lenient regex checks. */
    private static String normSource(String s) {
        if (s == null) return "";
        // Keep newlines for some checks, but collapse excessive spaces and remove CR
        return s.replace("\r","").replaceAll("\t"," ").replaceAll(" +"," ");
    }

    /** Normalize output: trim, collapse spaces, remove CR. */
    private static String normOutput(String s) {
        if (s == null) return "";
        return s.replace("\r","").trim().replaceAll("\\s+"," ");
    }

    /**
     * Heuristically determines concept checks from the question prompt and keyPoints.
     * Returns a list of regex patterns. Passing any subset earns partial credit.
     */
    private static List<Pattern> derivePatterns(QuestionnaireManager.Question q) {
        List<Pattern> pats = new ArrayList<>();
        String prompt = (q.questions == null ? "" : q.questions.toLowerCase(Locale.ROOT));
        String blob = (q.keyPoints == null ? "" : String.join("\n", q.keyPoints)).toLowerCase(Locale.ROOT);
        String all = prompt + "\n" + blob;

        // Generic patterns
        Pattern PRINT = Pattern.compile("System\\.out\\.print(ln)?\\s*\\(");
        Pattern CONCAT_PLUS = Pattern.compile("\\+\\s*\\w+|\\w+\\s*\\+\\s*\\\"|\\\"\\s*\\+|\\+\\s*\\\"");
        Pattern CONCAT_METHOD = Pattern.compile("\\.concat\\s*\\(");
        Pattern EQUALS_METHOD = Pattern.compile("\\.equals\\s*\\(");
        Pattern PRE_POST_INC = Pattern.compile("(\\+\\+|--)\\s*\\w+|\\w+\\s*(\\+\\+|--)");
        Pattern TERNARY = Pattern.compile("=\\s*\\(?.*\\\\?.*:.*\\)?;");
        Pattern INT_DECL = Pattern.compile("int\\s+\\w+");
        Pattern DOUBLE_DECL = Pattern.compile("double\\s+\\w+");
        Pattern BOOLEAN_DECL = Pattern.compile("boolean\\s+\\w+");
        Pattern CAST_TO_INT = Pattern.compile("\\(int\\)\\s*[-+]?\\d*(?:\\.\\d+)?");
        Pattern NUMERIC_LITERAL_UNDERSCORE = Pattern.compile("\\d+_\\d+");
        Pattern BINARY_LITERAL = Pattern.compile("0b[01]+");
        Pattern HEX_LITERAL = Pattern.compile("0x[0-9a-f]+");
        Pattern OCT_LITERAL = Pattern.compile("0[0-7]+");
        Pattern BYTE_CAST_OVERFLOW = Pattern.compile("\\(byte\\)\\s*\\d+");
        Pattern PROMOTION = Pattern.compile("byte\\s+\\w+\\s*=\\s*\\d+;.*=\\s*\\w+\\s*\\+\\s*\\d+;", Pattern.DOTALL);
        Pattern WIDENING = Pattern.compile("double\\s+\\w+\\s*=\\s*[-+]?\\d+\\s*;");
        Pattern NARROWING = Pattern.compile("int\\s+\\w+\\s*=\\s*\\(int\\)\\s*[-+]?\\d+(?:\\.\\d+)?\\s*;");
        Pattern PARSE_INT = Pattern.compile("Integer\\.parseInt\\s*\\(");
        Pattern STRING_VALUEOF = Pattern.compile("String\\.valueOf\\s*\\(");
        Pattern TO_STRING_PLUS = Pattern.compile("\\+\\s*\\\"\\\"");
        Pattern SCANNER = Pattern.compile("new\\s+Scanner\\s*\\(System\\.in\\)");
        Pattern READ_NEXT = Pattern.compile("next(Line|Int)\\s*\\(");
        Pattern FORMAT_PRINTF = Pattern.compile("System\\.out\\.printf|String\\.format");

        // Add based on prompt keywords (heuristics)
        if (all.contains("print")) pats.add(PRINT);
        if (all.contains("concatenate") || all.contains("concat")) { pats.add(CONCAT_PLUS); pats.add(CONCAT_METHOD); }
        if (all.contains("equals")) pats.add(EQUALS_METHOD);
        if (all.contains("increment") || all.contains("decrement")) pats.add(PRE_POST_INC);
        if (all.contains("ternary")) pats.add(TERNARY);
        if (all.contains("variable") || all.contains("declare")) { pats.add(INT_DECL); pats.add(DOUBLE_DECL); pats.add(BOOLEAN_DECL); }
        if (all.contains("cast") || all.contains("narrow")) { pats.add(CAST_TO_INT); pats.add(NARROWING); }
        if (all.contains("underscore")) pats.add(NUMERIC_LITERAL_UNDERSCORE);
        if (all.contains("binary")) pats.add(BINARY_LITERAL);
        if (all.contains("hex")) pats.add(HEX_LITERAL);
        if (all.contains("octal")) pats.add(OCT_LITERAL);
        if (all.contains("overflow")) pats.add(BYTE_CAST_OVERFLOW);
        if (all.contains("promotion")) pats.add(PROMOTION);
        if (all.contains("widening")) pats.add(WIDENING);
        if (all.contains("parse")) pats.add(PARSE_INT);
        if (all.contains("convert") || all.contains("to string")) { pats.add(STRING_VALUEOF); pats.add(TO_STRING_PLUS); }
        if (all.contains("scanner") || all.contains("user input") || all.contains("input")) { pats.add(SCANNER); pats.add(READ_NEXT); }
        if (all.contains("format") || all.contains("printf")) pats.add(FORMAT_PRINTF);

        // JSON keyPoints-driven patterns (primary)
        if (q.keyPoints != null) {
            for (String kp : q.keyPoints) {
                String k = kp.toLowerCase(Locale.ROOT).trim();
                if (!k.startsWith("needs:") && !k.startsWith("op:")) continue;
                if (k.startsWith("needs:")) {
                    String need = k.substring(6);
                    // OR group like double|float|string
                    if (need.contains("|")) {
                        // We'll handle OR in evaluate() rather than as patterns here
                        // No direct pattern added now.
                        continue;
                    }
                    if (need.startsWith("print")) pats.add(PRINT);
                    else if (need.startsWith("string")) pats.add(Pattern.compile("String\\s+\\w+"));
                    else if (need.startsWith("char")) pats.add(Pattern.compile("char\\s+\\w+"));
                    else if (need.startsWith("double")) pats.add(DOUBLE_DECL);
                    else if (need.startsWith("float")) pats.add(Pattern.compile("float\\s+\\w+"));
                    else if (need.startsWith("int")) {
                        // Handled with counts in evaluate(); still add one generic int decl pattern for exposure
                        pats.add(INT_DECL);
                    }
                } else if (k.startsWith("op:")) {
                    String op = k.substring(3);
                    if (op.equals("add")) pats.add(Pattern.compile("(\\w+|\\d+)\\s*\\+\\s*(\\w+|\\d+)"));
                    if (op.equals("sub")) pats.add(Pattern.compile("(\\w+|\\d+)\\s*-\\s*(\\w+|\\d+)"));
                    if (op.equals("mul")) pats.add(Pattern.compile("(\\w+|\\d+)\\s*\\*\\s*(\\w+|\\d+)"));
                    if (op.equals("div")) pats.add(Pattern.compile("(\\w+|\\d+)\\s*/\\s*(\\w+|\\d+)"));
                }
            }
        }

        // Fallback: if still empty, allow print as a basic check
        if (pats.isEmpty()) pats.add(PRINT);
        return pats;
    }

    /** Evaluate using heuristics; 'expectedOutput' can be null if not applicable. */
    public EvaluationResult evaluate(QuestionnaireManager.Question q, String userSource, String userOutput, String expectedOutput) {
        EvaluationResult res = new EvaluationResult();
        res.total = 0;
        res.score = 0;

        final String src = normSource(userSource);
        final String out = normOutput(userOutput);
        final String exp = normOutput(expectedOutput);

        // 1) Output-based check, if expected is provided
        if (expectedOutput != null && !expectedOutput.isEmpty()) {
            res.total += 1;
            if (out.equalsIgnoreCase(exp)) {
                res.score += 1;
            } else {
                res.feedback.add("Output differs. Expected something like: '" + expectedOutput + "'");
            }
        }

        // 2) Source-based heuristic checks (patterns)
        List<Pattern> pats = derivePatterns(q);
        for (Pattern p : pats) {
            res.total += 1;
            if (p.matcher(src).find()) {
                res.score += 1;
            } else {
                res.feedback.add("Hint: consider using pattern like '" + p.pattern() + "'");
            }
        }

        // 3) KeyPoints-based quantified checks (counts and OR)
        if (q.keyPoints != null) {
            for (String kp : q.keyPoints) {
                String k = kp.toLowerCase(Locale.ROOT).trim();
                if (!k.startsWith("needs:")) continue;
                String need = k.substring(6);

                // OR semantics: e.g., double|float|string
                if (need.contains("|")) {
                    res.total += 1;
                    boolean ok = false;
                    for (String part : need.split("\\|")) {
                        String p = part.trim();
                        if (p.equals("double") && Pattern.compile("double\\s+\\w+").matcher(src).find()) ok = true;
                        if (p.equals("float") && Pattern.compile("float\\s+\\w+").matcher(src).find()) ok = true;
                        if (p.equals("string") && Pattern.compile("String\\s+\\w+").matcher(src).find()) ok = true;
                        if (ok) break;
                    }
                    if (ok) res.score += 1; else res.feedback.add("Hint: use one of: " + need);
                    continue;
                }

                // Quantified: type:N
                String[] parts = need.split(":");
                String type = parts[0];
                int required = 1;
                if (parts.length > 1) {
                    try { required = Integer.parseInt(parts[1]); } catch (Exception ignored) {}
                }

                if (type.equals("int")) {
                    int found = countMatches(src, Pattern.compile("int\\s+\\w+"));
                    res.total += required;
                    res.score += Math.min(found, required);
                    if (found < required) res.feedback.add("Hint: declare at least " + required + " int variable(s)");
                } else if (type.equals("string")) {
                    int found = countMatches(src, Pattern.compile("String\\s+\\w+"));
                    res.total += required;
                    res.score += Math.min(found, required);
                    if (found < required) res.feedback.add("Hint: declare at least " + required + " String variable(s)");
                } else if (type.equals("double")) {
                    int found = countMatches(src, Pattern.compile("double\\s+\\w+"));
                    res.total += required;
                    res.score += Math.min(found, required);
                    if (found < required) res.feedback.add("Hint: declare at least " + required + " double variable(s)");
                } else if (type.equals("float")) {
                    int found = countMatches(src, Pattern.compile("float\\s+\\w+"));
                    res.total += required;
                    res.score += Math.min(found, required);
                    if (found < required) res.feedback.add("Hint: declare at least " + required + " float variable(s)");
                } else if (type.equals("char")) {
                    int found = countMatches(src, Pattern.compile("char\\s+\\w+"));
                    res.total += required;
                    res.score += Math.min(found, required);
                    if (found < required) res.feedback.add("Hint: declare at least " + required + " char variable(s)");
                } else if (type.equals("print")) {
                    // already covered by pattern; no additional quantified scoring
                }
            }
        }

        // Passing threshold: at least 60% of total checks
        res.passed = res.total == 0 ? false : (res.score * 1.0f / res.total) >= 0.6f;
        if (res.passed) {
            res.feedback.add("Great job! You satisfied enough criteria for this task.");
        } else {
            res.feedback.add("Not quite there. Review the hints and adjust your code.");
        }
        return res;
    }

    private static int countMatches(String src, Pattern p) {
        int c = 0;
        var m = p.matcher(src);
        while (m.find()) c++;
        return c;
    }
}
