package com.altf4studios.corebringer.metrics;

import com.altf4studios.corebringer.utils.SimpleSaveManager;
import com.altf4studios.corebringer.utils.SaveData;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.JsonWriter;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * CodingMetricsManager tracks user performance across all coding levels
 * Provides detailed statistics, accuracy percentages, and improvement recommendations
 */
public class CodingMetricsManager {
    private static CodingMetricsManager instance;
    private static final String METRICS_FILE = "coding_metrics.json";
    
    // Level statistics
    private ObjectMap<Integer, LevelMetrics> levelStats;
    private ObjectMap<Integer, String> levelNames;
    
    // Overall statistics (calculated from save data)
    public int getTotalQuestionsAttempted() {
        try {
            SaveData saveData = SimpleSaveManager.loadData();
            if (saveData != null && saveData.totals != null) {
                return saveData.totals.correct + saveData.totals.wrong;
            }
        } catch (Exception e) {
            Gdx.app.error("CodingMetrics", "Failed to load totals: " + e.getMessage());
        }
        return 0;
    }
    
    public int getTotalQuestionsCorrect() {
        try {
            SaveData saveData = SimpleSaveManager.loadData();
            if (saveData != null && saveData.totals != null) {
                return saveData.totals.correct;
            }
        } catch (Exception e) {
            Gdx.app.error("CodingMetrics", "Failed to load totals: " + e.getMessage());
        }
        return 0;
    }
    
    public long getTotalTimeSpent() {
        return 0; // Not tracked in save data for now
    }
    
    /**
     * Represents metrics for a specific level
     */
    public static class LevelMetrics {
        public int level;
        public String levelName;
        public int questionsAttempted;
        public int questionsCorrect;
        public int questionsIncorrect;
        public float accuracyPercentage;
        public long totalTimeSpent; // in milliseconds
        public long averageTimePerQuestion; // in milliseconds
        public long fastestQuestionTime; // in milliseconds
        public long slowestQuestionTime; // in milliseconds
        public Array<Integer> incorrectQuestionIds;
        public Array<String> commonMistakes;
        public int consecutiveCorrect; // current streak
        public int bestConsecutiveCorrect; // best streak achieved
        public int consecutiveIncorrect; // current incorrect streak
        public int worstConsecutiveIncorrect; // worst incorrect streak
        
        public LevelMetrics() {
            incorrectQuestionIds = new Array<>();
            commonMistakes = new Array<>();
        }
        
        public LevelMetrics(int level, String levelName) {
            this();
            this.level = level;
            this.levelName = levelName;
        }
        
        public void updateAccuracy() {
            if (questionsAttempted > 0) {
                accuracyPercentage = ((float) questionsCorrect / questionsAttempted) * 100f;
            } else {
                accuracyPercentage = 0f;
            }
        }
        
        public void updateAverageTime() {
            if (questionsAttempted > 0) {
                averageTimePerQuestion = totalTimeSpent / questionsAttempted;
            } else {
                averageTimePerQuestion = 0;
            }
        }
    }
    
    /**
     * Represents improvement recommendations for the user
     */
    public static class ImprovementReport {
        public Array<LevelRecommendation> levelRecommendations;
        public String overallAssessment;
        public Array<String> strengthAreas;
        public Array<String> improvementAreas;
        public float overallAccuracy;
        
        public ImprovementReport() {
            levelRecommendations = new Array<>();
            strengthAreas = new Array<>();
            improvementAreas = new Array<>();
        }
    }
    
    /**
     * Represents specific recommendations for a level
     */
    public static class LevelRecommendation {
        public int level;
        public String levelName;
        public float accuracyPercentage;
        public String priority; // "HIGH", "MEDIUM", "LOW"
        public String recommendation;
        public Array<String> specificTopics;
        
        public LevelRecommendation() {
            specificTopics = new Array<>();
        }
    }
    
    private CodingMetricsManager() {
        levelStats = new ObjectMap<>();
        levelNames = new ObjectMap<>();
        initializeLevelNames();
        loadMetrics();
    }
    
    public static CodingMetricsManager getInstance() {
        if (instance == null) {
            instance = new CodingMetricsManager();
        }
        return instance;
    }
    
    private void initializeLevelNames() {
        levelNames.put(1, "Basic Java Fundamentals");
        levelNames.put(2, "Variables and Control Flow");
        levelNames.put(3, "Arrays and Methods");
        levelNames.put(4, "Classes and Object-Oriented Programming");
        levelNames.put(5, "Advanced OOP and Design Patterns");
    }
    
    
    /**
     * Record a common mistake for analysis
     */
    public void recordCommonMistake(int level, String mistake) {
        LevelMetrics metrics = getLevelMetrics(level);
        if (!metrics.commonMistakes.contains(mistake, false)) {
            metrics.commonMistakes.add(mistake);
        }
        saveMetrics();
    }
    
    /**
     * Get metrics for a specific level
     */
    public LevelMetrics getLevelMetrics(int level) {
        if (!levelStats.containsKey(level)) {
            String levelName = levelNames.get(level, "Unknown Level");
            levelStats.put(level, new LevelMetrics(level, levelName));
        }
        return levelStats.get(level);
    }
    
    /**
     * Get all level metrics from save data
     */
    public Array<LevelMetrics> getAllLevelMetrics() {
        Array<LevelMetrics> allMetrics = new Array<>();
        
        try {
            SaveData saveData = SimpleSaveManager.loadData();
            if (saveData != null && saveData.questionData != null) {
                // Process levels 1-5
                for (int level = 1; level <= 5; level++) {
                    String levelKey = "level" + level;
                    if (saveData.questionData.containsKey(levelKey)) {
                        SaveData.QuestionLevelData levelData = saveData.questionData.get(levelKey);
                        
                        LevelMetrics metrics = new LevelMetrics(level, levelData.title);
                        
                        // Calculate totals from level data
                        metrics.questionsAttempted = levelData.correct + levelData.wrong;
                        metrics.questionsCorrect = levelData.correct;
                        metrics.questionsIncorrect = levelData.wrong;
                        
                        metrics.updateAccuracy();
                        allMetrics.add(metrics);
                    }
                }
            }
        } catch (Exception e) {
            Gdx.app.error("CodingMetrics", "Failed to load metrics from save data: " + e.getMessage());
        }
        
        // Sort by level
        allMetrics.sort((a, b) -> Integer.compare(a.level, b.level));
        return allMetrics;
    }
    
    /**
     * Generate comprehensive improvement report
     */
    public ImprovementReport generateImprovementReport() {
        ImprovementReport report = new ImprovementReport();
        
        // Calculate overall accuracy
        int totalAttempted = getTotalQuestionsAttempted();
        int totalCorrect = getTotalQuestionsCorrect();
        if (totalAttempted > 0) {
            report.overallAccuracy = ((float) totalCorrect / totalAttempted) * 100f;
        }
        
        // Analyze each level
        Array<LevelMetrics> allMetrics = getAllLevelMetrics();
        for (LevelMetrics metrics : allMetrics) {
            if (metrics.questionsAttempted > 0) {
                LevelRecommendation rec = new LevelRecommendation();
                rec.level = metrics.level;
                rec.levelName = metrics.levelName;
                rec.accuracyPercentage = metrics.accuracyPercentage;
                
                // Determine priority based on accuracy and attempts
                if (metrics.accuracyPercentage < 50f && metrics.questionsAttempted >= 3) {
                    rec.priority = "HIGH";
                    rec.recommendation = "Requires immediate attention. Review fundamental concepts.";
                    report.improvementAreas.add(metrics.levelName);
                } else if (metrics.accuracyPercentage < 70f && metrics.questionsAttempted >= 2) {
                    rec.priority = "MEDIUM";
                    rec.recommendation = "Needs improvement. Practice more problems in this area.";
                    report.improvementAreas.add(metrics.levelName);
                } else if (metrics.accuracyPercentage >= 80f) {
                    rec.priority = "LOW";
                    rec.recommendation = "Good performance. Continue practicing to maintain proficiency.";
                    report.strengthAreas.add(metrics.levelName);
                } else {
                    rec.priority = "LOW";
                    rec.recommendation = "More practice needed to establish proficiency.";
                }
                
                // Add specific topics based on level
                addSpecificTopics(rec, metrics);
                
                report.levelRecommendations.add(rec);
            }
        }
        
        // Generate overall assessment
        report.overallAssessment = generateOverallAssessment(report.overallAccuracy, allMetrics);
        
        return report;
    }
    
    private void addSpecificTopics(LevelRecommendation rec, LevelMetrics metrics) {
        switch (metrics.level) {
            case 1:
                rec.specificTopics.addAll("Variables", "Data Types", "Basic I/O", "Syntax");
                break;
            case 2:
                rec.specificTopics.addAll("If-else statements", "Loops", "Boolean logic", "Operators");
                break;
            case 3:
                rec.specificTopics.addAll("Arrays", "Methods", "Parameters", "Return values");
                break;
            case 4:
                rec.specificTopics.addAll("Classes", "Objects", "Constructors", "Encapsulation", "Static members");
                break;
            case 5:
                rec.specificTopics.addAll("Inheritance", "Polymorphism", "Abstract classes", "Interfaces", "Design patterns");
                break;
        }
    }
    
    private String generateOverallAssessment(float overallAccuracy, Array<LevelMetrics> allMetrics) {
        if (overallAccuracy >= 85f) {
            return "Excellent! You have strong programming fundamentals across all levels.";
        } else if (overallAccuracy >= 70f) {
            return "Good progress! Focus on improving weaker areas to reach excellence.";
        } else if (overallAccuracy >= 50f) {
            return "Making progress. Concentrate on fundamental concepts and practice regularly.";
        } else {
            return "Needs significant improvement. Review basic concepts and practice consistently.";
        }
    }
    
    /**
     * Get the level that needs the most improvement (highest error rate)
     */
    public LevelMetrics getMostNeedingImprovement() {
        LevelMetrics worstLevel = null;
        float worstAccuracy = 100f;
        
        for (ObjectMap.Entry<Integer, LevelMetrics> entry : levelStats.entries()) {
            LevelMetrics metrics = entry.value;
            if (metrics.questionsAttempted >= 2 && metrics.accuracyPercentage < worstAccuracy) {
                worstAccuracy = metrics.accuracyPercentage;
                worstLevel = metrics;
            }
        }
        
        return worstLevel;
    }
    
    /**
     * Get formatted statistics string for display
     */
    public String getFormattedStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== CODING PERFORMANCE METRICS ===\n\n");
        
        // Calculate overall stats from save data
        int totalAttempted = 0;
        int totalCorrect = 0;
        Array<LevelMetrics> allMetrics = getAllLevelMetrics();
        
        for (LevelMetrics metrics : allMetrics) {
            totalAttempted += metrics.questionsAttempted;
            totalCorrect += metrics.questionsCorrect;
        }
        
        float overallAccuracy = totalAttempted > 0 ? 
            ((float) totalCorrect / totalAttempted) * 100f : 0f;
        
        sb.append(String.format("Overall Performance: %.1f%% (%d/%d correct)\n", 
            overallAccuracy, totalCorrect, totalAttempted));
        sb.append("Data loaded from save file\n\n");
        
        // Level-by-level breakdown
        sb.append("LEVEL BREAKDOWN:\n");
        for (LevelMetrics metrics : allMetrics) {
            if (metrics.questionsAttempted > 0) {
                sb.append(String.format("Level %d - %s:\n", metrics.level, metrics.levelName));
                sb.append(String.format("  Accuracy: %.1f%% (%d/%d)\n", 
                    metrics.accuracyPercentage, metrics.questionsCorrect, metrics.questionsAttempted));
                sb.append("\n");
            }
        }
        
        // Improvement recommendations
        ImprovementReport report = generateImprovementReport();
        sb.append("IMPROVEMENT RECOMMENDATIONS:\n");
        for (LevelRecommendation rec : report.levelRecommendations) {
            if ("HIGH".equals(rec.priority) || "MEDIUM".equals(rec.priority)) {
                sb.append(String.format("• %s (%.1f%%) - %s: %s\n", 
                    rec.levelName, rec.accuracyPercentage, rec.priority, rec.recommendation));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Reset all metrics (for testing or new user)
     */
    public void resetAllMetrics() {
        // Clear save data question tracking
        SimpleSaveManager.updateData(data -> {
            data.questionData.clear();
            Gdx.app.log("CodingMetrics", "All metrics reset");
        });
    }
    
    /**
     * Save metrics to file
     */
    private void saveMetrics() {
        try {
            Json json = new Json();
            json.setOutputType(JsonWriter.OutputType.json);
            
            JsonValue root = new JsonValue(JsonValue.ValueType.object);
            
            // Overall stats (calculated from save data)
            root.addChild("totalQuestionsAttempted", new JsonValue(getTotalQuestionsAttempted()));
            root.addChild("totalQuestionsCorrect", new JsonValue(getTotalQuestionsCorrect()));
            root.addChild("totalTimeSpent", new JsonValue(getTotalTimeSpent()));
            
            // Level stats
            JsonValue levelsArray = new JsonValue(JsonValue.ValueType.array);
            for (ObjectMap.Entry<Integer, LevelMetrics> entry : levelStats.entries()) {
                LevelMetrics metrics = entry.value;
                JsonValue levelObj = new JsonValue(JsonValue.ValueType.object);
                
                levelObj.addChild("level", new JsonValue(metrics.level));
                levelObj.addChild("levelName", new JsonValue(metrics.levelName));
                levelObj.addChild("questionsAttempted", new JsonValue(metrics.questionsAttempted));
                levelObj.addChild("questionsCorrect", new JsonValue(metrics.questionsCorrect));
                levelObj.addChild("questionsIncorrect", new JsonValue(metrics.questionsIncorrect));
                levelObj.addChild("accuracyPercentage", new JsonValue(metrics.accuracyPercentage));
                levelObj.addChild("totalTimeSpent", new JsonValue(metrics.totalTimeSpent));
                levelObj.addChild("averageTimePerQuestion", new JsonValue(metrics.averageTimePerQuestion));
                levelObj.addChild("fastestQuestionTime", new JsonValue(metrics.fastestQuestionTime));
                levelObj.addChild("slowestQuestionTime", new JsonValue(metrics.slowestQuestionTime));
                levelObj.addChild("consecutiveCorrect", new JsonValue(metrics.consecutiveCorrect));
                levelObj.addChild("bestConsecutiveCorrect", new JsonValue(metrics.bestConsecutiveCorrect));
                levelObj.addChild("consecutiveIncorrect", new JsonValue(metrics.consecutiveIncorrect));
                levelObj.addChild("worstConsecutiveIncorrect", new JsonValue(metrics.worstConsecutiveIncorrect));
                
                // Incorrect question IDs
                JsonValue incorrectIds = new JsonValue(JsonValue.ValueType.array);
                for (Integer id : metrics.incorrectQuestionIds) {
                    incorrectIds.addChild(new JsonValue(id));
                }
                levelObj.addChild("incorrectQuestionIds", incorrectIds);
                
                // Common mistakes
                JsonValue mistakes = new JsonValue(JsonValue.ValueType.array);
                for (String mistake : metrics.commonMistakes) {
                    mistakes.addChild(new JsonValue(mistake));
                }
                levelObj.addChild("commonMistakes", mistakes);
                
                levelsArray.addChild(levelObj);
            }
            root.addChild("levels", levelsArray);
            
            FileHandle file = Gdx.files.local(METRICS_FILE);
            file.writeString(json.prettyPrint(root), false);
            
        } catch (Exception e) {
            Gdx.app.error("CodingMetrics", "Failed to save metrics: " + e.getMessage());
        }
    }
    
    /**
     * Load metrics from file
     */
    private void loadMetrics() {
        try {
            FileHandle file = Gdx.files.local(METRICS_FILE);
            if (!file.exists()) {
                Gdx.app.log("CodingMetrics", "No existing metrics file found, starting fresh");
                return;
            }
            
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(file);
            
            // Overall stats are now calculated from save data, not loaded from metrics file
            
            // Load level stats
            JsonValue levelsArray = root.get("levels");
            if (levelsArray != null) {
                for (JsonValue levelObj = levelsArray.child; levelObj != null; levelObj = levelObj.next) {
                    LevelMetrics metrics = new LevelMetrics();
                    metrics.level = levelObj.getInt("level");
                    metrics.levelName = levelObj.getString("levelName");
                    metrics.questionsAttempted = levelObj.getInt("questionsAttempted");
                    metrics.questionsCorrect = levelObj.getInt("questionsCorrect");
                    metrics.questionsIncorrect = levelObj.getInt("questionsIncorrect");
                    metrics.accuracyPercentage = levelObj.getFloat("accuracyPercentage");
                    metrics.totalTimeSpent = levelObj.getLong("totalTimeSpent");
                    metrics.averageTimePerQuestion = levelObj.getLong("averageTimePerQuestion");
                    metrics.fastestQuestionTime = levelObj.getLong("fastestQuestionTime", 0);
                    metrics.slowestQuestionTime = levelObj.getLong("slowestQuestionTime", 0);
                    metrics.consecutiveCorrect = levelObj.getInt("consecutiveCorrect", 0);
                    metrics.bestConsecutiveCorrect = levelObj.getInt("bestConsecutiveCorrect", 0);
                    metrics.consecutiveIncorrect = levelObj.getInt("consecutiveIncorrect", 0);
                    metrics.worstConsecutiveIncorrect = levelObj.getInt("worstConsecutiveIncorrect", 0);
                    
                    // Load incorrect question IDs
                    JsonValue incorrectIds = levelObj.get("incorrectQuestionIds");
                    if (incorrectIds != null) {
                        for (JsonValue id = incorrectIds.child; id != null; id = id.next) {
                            metrics.incorrectQuestionIds.add(id.asInt());
                        }
                    }
                    
                    // Load common mistakes
                    JsonValue mistakes = levelObj.get("commonMistakes");
                    if (mistakes != null) {
                        for (JsonValue mistake = mistakes.child; mistake != null; mistake = mistake.next) {
                            metrics.commonMistakes.add(mistake.asString());
                        }
                    }
                    
                    levelStats.put(metrics.level, metrics);
                }
            }
            
            Gdx.app.log("CodingMetrics", "Loaded metrics: " + getTotalQuestionsAttempted() + " total attempts");
            
        } catch (Exception e) {
            Gdx.app.error("CodingMetrics", "Failed to load metrics: " + e.getMessage());
        }
    }
}
