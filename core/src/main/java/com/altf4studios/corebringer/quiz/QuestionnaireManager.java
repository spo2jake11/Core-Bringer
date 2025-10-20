package com.altf4studios.corebringer.quiz;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Loads and serves questions from assets/questionnaire.json.
 * Uses a weighted 'chance' field for random selection. If 'chance' is missing/zero,
 * it will be randomized into [0.2, 0.75] based on difficulty level.
 */
public final class QuestionnaireManager {
    private static final String DEFAULT_JSON = "assets/questionnaire.json";
    private static final String DEFAULT_TXT = "assets/questions.txt";

    private static QuestionnaireManager INSTANCE;

    public static class Question {
        public int id;
        public int isSolve; // 0 or 1
        public String questions; // prompt
        public Array<String> keyPoints = new Array<>();
        public float chance; // loaded but not used yet
    }

    public static class Questionnaire {
        public int level;
        public Array<Question> questions = new Array<>();
    }

    private Questionnaire loaded;

    private QuestionnaireManager() {}

    public static QuestionnaireManager get() {
        if (INSTANCE == null) INSTANCE = new QuestionnaireManager();
        return INSTANCE;
    }

    /** Load from default path: assets/questionnaire.json only */
    public void initDefault() {
        FileHandle json = Gdx.files.internal(DEFAULT_JSON);
        initFromJson(json);
    }

    /** Load from a specific level in the questionnaire.json file */
    public void initFromJsonWithLevel(FileHandle file, int level) {
        if (file == null || !file.exists()) {
            throw new IllegalStateException("Questionnaire JSON not found: " + (file == null ? "<null>" : file.path()));
        }
        // Read as UTF-8 string and sanitize any mojibake/smart quotes before parsing
        String raw = file.readString("UTF-8");
        String sanitized = sanitizeJson(raw);
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(sanitized);

        // Try to load from levelX key first (e.g., level1, level2, level3)
        String levelKey = "level" + level;
        JsonValue levelNode = root.get(levelKey);
        
        Questionnaire qn = new Questionnaire();
        qn.level = level;

        if (levelNode != null) {
            // Found levelX key, load from its questionArray
            JsonValue qArr = levelNode.get("questionArray");
            if (qArr != null && qArr.isArray()) {
                loadQuestionsIntoQuestionnaire(qArr, qn);
            }
        } else {
            // Fallback: try loading from root level (old format)
            JsonValue qArr = root.get("questions");
            if (qArr == null) qArr = root.get("questionArray");
            if (qArr != null && qArr.isArray()) {
                loadQuestionsIntoQuestionnaire(qArr, qn);
            }
        }

        this.loaded = qn;
        Gdx.app.log("QuestionnaireManager", "Loaded questions: " + qn.questions.size + " (level=" + qn.level + ") from " + file.path());
    }

    private void loadQuestionsIntoQuestionnaire(JsonValue qArr, Questionnaire qn) {
        for (JsonValue it = qArr.child; it != null; it = it.next) {
            Question q = new Question();
            q.id = it.getInt("id", 0);
            q.isSolve = it.getInt("isSolve", 0);
            q.questions = it.getString("questions", "");
            q.chance = it.getFloat("chance", 0.0f);
            JsonValue kp = it.get("keyPoints");
            if (kp != null && kp.isArray()) {
                for (JsonValue s = kp.child; s != null; s = s.next) {
                    q.keyPoints.add(s.asString());
                }
            }
            // Normalize/assign chance: clamp to [0.2, 0.75] or randomize in range if missing
            q.chance = normalizeOrRandomizeChance(q.chance, qn.level);
            qn.questions.add(q);
        }
    }

    /** Load from a specific file handle */
    public void initFromJson(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalStateException("Questionnaire JSON not found: " + (file == null ? "<null>" : file.path()));
        }
        // Read as UTF-8 string and sanitize any mojibake/smart quotes before parsing
        String raw = file.readString("UTF-8");
        String sanitized = sanitizeJson(raw);
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(sanitized);

        Questionnaire qn = new Questionnaire();
        qn.level = root.getInt("level", 1);
        // Accept either "questions" or the alternate key "questionArray"
        JsonValue qArr = root.get("questions");
        if (qArr == null) qArr = root.get("questionArray");
        if (qArr != null && qArr.isArray()) {
            for (JsonValue it = qArr.child; it != null; it = it.next) {
                Question q = new Question();
                q.id = it.getInt("id", 0);
                q.isSolve = it.getInt("isSolve", 0);
                q.questions = it.getString("questions", "");
                q.chance = it.getFloat("chance", 0.0f);
                JsonValue kp = it.get("keyPoints");
                if (kp != null && kp.isArray()) {
                    for (JsonValue s = kp.child; s != null; s = s.next) {
                        q.keyPoints.add(s.asString());
                    }
                }
                // Normalize/assign chance: clamp to [0.2, 0.75] or randomize in range if missing
                q.chance = normalizeOrRandomizeChance(q.chance, qn.level);
                qn.questions.add(q);
            }
        }
        this.loaded = qn;
        Gdx.app.log("QuestionnaireManager", "Loaded questions: " + qn.questions.size + " (level=" + qn.level + ") from " + file.path());
    }

    /** Load simple prompts from a plain text file. Each non-empty line is one question. */
    public void initFromTxt(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalStateException("Questions TXT not found: " + (file == null ? "<null>" : file.path()));
        }
        String content = file.readString("UTF-8");
        String[] lines = content.replace("\r", "").split("\n");
        Questionnaire qn = new Questionnaire();
        qn.level = 1;
        int id = 1;
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty()) continue;
            // Skip header-like markers such as (== ... ==)
            if ((line.startsWith("(=") && line.endsWith("=)"))) continue;
            Question q = new Question();
            q.id = id++;
            q.isSolve = 0;
            q.questions = line;
            q.chance = 0.5f; // loaded but unused for now
            qn.questions.add(q);
        }
        this.loaded = qn;
    }

    public boolean isReady() {
        return loaded != null && loaded.questions != null && loaded.questions.size > 0;
    }

    public int getLevel() { return loaded == null ? 0 : loaded.level; }

    public Array<Question> getAll() {
        return loaded == null ? new Array<>() : loaded.questions;
    }

    public Question getById(int id) {
        if (!isReady()) return null;
        for (Question q : loaded.questions) if (q.id == id) return q;
        return null;
    }

    /** Weighted random among unsolved questions using 'chance'. Falls back to all if all are solved. */
    public Question getRandomUnsolved() {
        if (!isReady()) return null;
        Array<Question> pool = new Array<>();
        for (Question q : loaded.questions) if (q.isSolve == 0) pool.add(q);
        if (pool.size == 0) pool.addAll(loaded.questions);
        return weightedPick(pool);
    }

    /** Mark a question as solved (in-memory only). Persist externally if needed. */
    public void markSolved(int id) {
        Question q = getById(id);
        if (q != null) q.isSolve = 1;
    }

    // --- Helpers ---
    private String sanitizeJson(String s) {
        if (s == null) return null;
        // Replace common mojibake and smart punctuation with ASCII equivalents
        String out = s;
        // Strip UTF-8 BOM if present
        if (!out.isEmpty() && out.charAt(0) == '\uFEFF') {
            out = out.substring(1);
        }
        // Smart quotes
        out = out.replace("\u201C", "\""); // “
        out = out.replace("\u201D", "\""); // ”
        out = out.replace("\u2018", "'");   // ‘
        out = out.replace("\u2019", "'");   // ’
        // Mojibake sequences from UTF-8 read as ISO-8859-1
        out = out.replace("â€œ", "\"");
        out = out.replace("â€", "\"");
        out = out.replace("â€™", "'");
        out = out.replace("â€˜", "'");
        // Dashes
        out = out.replace("\u2013", "-"); // –
        out = out.replace("\u2014", "-"); // —
        out = out.replace("â€“", "-");
        out = out.replace("â€”", "-");
        // NBSP
        out = out.replace("\u00A0", " ");
        return out;
    }
    private float normalizeOrRandomizeChance(float chance, int level) {
        // Base allowed min/max
        float globalMin = 0.20f;
        float globalMax = 0.75f;
        // Adjust range based on difficulty level (lower level → higher chance range)
        float min;
        float max;
        if (level <= 1) { // easy
            min = 0.55f; max = 0.75f;
        } else if (level == 2) { // medium
            min = 0.40f; max = 0.65f;
        } else { // hard+
            min = 0.20f; max = 0.50f;
        }
        // Clamp provided chance into [globalMin, globalMax]
        if (chance > 0f) {
            return MathUtils.clamp(chance, globalMin, globalMax);
        }
        // Otherwise randomize within level-adjusted range
        float val = MathUtils.random(min, max);
        // Ensure also within global bounds
        return MathUtils.clamp(val, globalMin, globalMax);
    }

    private Question weightedPick(Array<Question> items) {
        if (items == null || items.size == 0) return null;
        float total = 0f;
        for (Question q : items) total += Math.max(0.0001f, q.chance);
        float r = MathUtils.random(total);
        float acc = 0f;
        for (Question q : items) {
            acc += Math.max(0.0001f, q.chance);
            if (r <= acc) return q;
        }
        return items.peek();
    }
}
