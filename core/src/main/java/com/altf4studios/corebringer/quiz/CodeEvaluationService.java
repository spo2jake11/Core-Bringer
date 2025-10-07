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

        // 4) Additional keyPoints: var:, literal:, format:, constraint:
        if (q.keyPoints != null) {
            for (String raw : q.keyPoints) {
                if (raw == null) continue;
                String tag = raw.trim();
                String lower = tag.toLowerCase(Locale.ROOT);

                // var:<name> — ensure a variable with that identifier exists (any primitive/String)
                if (lower.startsWith("var:")) {
                    String varName = tag.substring(4).trim();
                    if (varName.isEmpty()) continue;
                    res.total += 1;
                    boolean declared = false;
                    // Accept common Java types; also allow any type identifier followed by name
                    Pattern decl = Pattern.compile("(int|double|float|char|String|boolean)\\s+" + Pattern.quote(varName) + "\\b");
                    if (decl.matcher(src).find()) declared = true;
                    // Also accept usage with assignment without prior explicit type (unlikely), or reuse
                    if (!declared) {
                        Pattern assign = Pattern.compile("\\b" + Pattern.quote(varName) + "\\s*=\\s*");
                        if (assign.matcher(src).find()) declared = true;
                    }
                    if (declared) res.score += 1; else res.feedback.add("Hint: declare variable '" + varName + "'");
                    continue;
                }

                // literal:<text> — ensure the literal appears either in source string literals or in program output
                if (lower.startsWith("literal:")) {
                    String lit = tag.substring(8).trim();
                    if (lit.isEmpty()) continue;
                    res.total += 1;
                    boolean ok = false;
                    // Check output contains literal (case-insensitive for words)
                    String litNorm = normOutput(lit).toLowerCase(Locale.ROOT);
                    if (!litNorm.isEmpty() && out.toLowerCase(Locale.ROOT).contains(litNorm)) ok = true;
                    // Check source contains literal inside quotes or numeric token
                    if (!ok) {
                        // If numeric, look for the exact digits in source
                        if (lit.matches("[-+]?\\d+(?:\\.\\d+)?")) {
                            if (Pattern.compile("(^|[^\\w])" + Pattern.quote(lit) + "([^\\w]|$)").matcher(src).find()) ok = true;
                        } else {
                            if (src.toLowerCase(Locale.ROOT).contains(litNorm)) ok = true;
                        }
                    }
                    if (ok) res.score += 1; else res.feedback.add("Hint: include literal '" + lit + "' in your output or string");
                    continue;
                }

                // format:<tag> — lightweight structural hints
                if (lower.startsWith("format:")) {
                    String fmt = lower.substring(7).trim();
                    if (fmt.isEmpty()) continue;
                    res.total += 1;
                    boolean ok = false;
                    switch (fmt) {
                        case "equation":
                            ok = out.contains("=") || Pattern.compile("=").matcher(src).find();
                            break;
                        case "same_line":
                            // One println that appears to combine two values (concat via + or multiple tokens)
                            ok = Pattern.compile("System\\.out\\.print(ln)?\\s*\\([^)]*\\+[^)]*\\)").matcher(src).find();
                            break;
                        case "full_name":
                            // Look for firstName and lastName used together in a single print
                            ok = Pattern.compile("System\\.out\\.print(ln)?\\s*\\([^)]*firstName[^)]*\\+[^)]*lastName[^)]*\\)").matcher(src).find()
                                 || Pattern.compile("System\\.out\\.print(ln)?\\s*\\([^)]*lastName[^)]*\\+[^)]*firstName[^)]*\\)").matcher(src).find();
                            break;
                        case "parentheses":
                        case "nested_parentheses":
                            ok = Pattern.compile("\\([^(]*[+\\-*/][^)]*\\)").matcher(src).find();
                            break;
                        case "average":
                            ok = Pattern.compile("/\\s*\\d+").matcher(src).find() || out.contains("/");
                            break;
                        case "unit_convert":
                            ok = Pattern.compile("\\*\\s*(60|24|7|30)").matcher(src).find() || Pattern.compile("(seconds|minutes|hours|days|weeks)", Pattern.CASE_INSENSITIVE).matcher(out).find();
                            break;
                        case "area_rectangle":
                        case "perimeter_rectangle":
                        case "area_square":
                        case "area_triangle":
                        case "salary_total":
                        case "series_sum":
                        case "square":
                        case "cube":
                        case "two_expressions":
                            // Treat as informational; grant credit if arithmetic with two+ ops exists
                            ok = Pattern.compile("(\\+|\\-|\\*|/)\\s*(\\w+|\\d+).*(\\+|\\-|\\*|/)", Pattern.DOTALL).matcher(src).find();
                            break;
                        case "sentence":
                            ok = out.split("\\s+").length >= 3; // at least a few words
                            break;
                        case "separate_prints":
                            ok = countMatches(src, Pattern.compile("System\\.out\\.print(ln)?\\s*\\(")) >= 3; // 3 separate prints
                            break;
                        default:
                            // Unknown format tag – don't penalize
                            res.total -= 1; // neutralize since we can't check it
                            break;
                    }
                    if (ok) res.score += 1; else res.feedback.add("Hint: follow format '" + fmt + "'");
                    continue;
                }

                // constraint:<tag>
                if (lower.startsWith("constraint:")) {
                    String c = lower.substring(11).trim();
                    if (c.isEmpty()) continue;
                    res.total += 1;
                    boolean ok = false;
                    switch (c) {
                        case "same_value":
                            // Two different variables assigned the same numeric literal OR output repeats same number twice
                            Pattern declVal = Pattern.compile("(int|double|float|char|String|boolean)\\s+(\\w+)\\s*=\\s*([-+]?\\d+(?:\\.\\d+)?)");
                            java.util.regex.Matcher m = declVal.matcher(src);
                            java.util.Map<String,String> varToVal = new java.util.HashMap<>();
                            java.util.Map<String,Integer> valCount = new java.util.HashMap<>();
                            while (m.find()) {
                                String v = m.group(2);
                                String val = m.group(3);
                                varToVal.put(v, val);
                                valCount.put(val, valCount.getOrDefault(val, 0) + 1);
                            }
                            for (int cnt : valCount.values()) if (cnt >= 2) { ok = true; break; }
                            if (!ok) {
                                // Output contains same number twice
                                Pattern num = Pattern.compile("([-+]?\\d+(?:\\.\\d+)?)");
                                java.util.List<String> nums = new java.util.ArrayList<>();
                                java.util.regex.Matcher n = num.matcher(out);
                                while (n.find()) nums.add(n.group(1));
                                java.util.Set<String> seen = new java.util.HashSet<>();
                                for (String sVal : nums) { if (!seen.add(sVal)) { ok = true; break; } }
                            }
                            break;
                        default:
                            res.total -= 1; // neutralize unknown constraints
                            break;
                    }
                    if (ok) res.score += 1; else res.feedback.add("Hint: satisfy constraint '" + c + "'");
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
