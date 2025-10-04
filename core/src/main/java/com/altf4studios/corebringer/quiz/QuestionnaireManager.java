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
 * Does NOT use the 'chance' weighting yet (as requested). Random selection is uniform for now.
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

    /** Load from a specific file handle */
    public void initFromJson(FileHandle file) {
        if (file == null || !file.exists()) {
            throw new IllegalStateException("Questionnaire JSON not found: " + (file == null ? "<null>" : file.path()));
        }
        JsonReader reader = new JsonReader();
        JsonValue root = reader.parse(file);

        Questionnaire qn = new Questionnaire();
        qn.level = root.getInt("level", 1);
        JsonValue qArr = root.get("questions");
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
                qn.questions.add(q);
            }
        }
        this.loaded = qn;
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

    /** Uniform random among unsolved questions (isSolve == 0). Falls back to all if all are solved. */
    public Question getRandomUnsolved() {
        if (!isReady()) return null;
        Array<Question> pool = new Array<>();
        for (Question q : loaded.questions) if (q.isSolve == 0) pool.add(q);
        if (pool.size == 0) pool.addAll(loaded.questions);
        return pool.get(MathUtils.random(pool.size - 1));
    }

    /** Mark a question as solved (in-memory only). Persist externally if needed. */
    public void markSolved(int id) {
        Question q = getById(id);
        if (q != null) q.isSolve = 1;
    }
}
