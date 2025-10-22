package com.altf4studios.corebringer.metrics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

/**
 * MetricsDisplayWindow provides a comprehensive UI for viewing coding performance metrics
 */
public class MetricsDisplayWindow {
    private Window metricsWindow;
    private Stage stage;
    private Skin skin;
    private ScrollPane scrollPane;
    private Table contentTable;

    private Runnable returnToMenuCallback;

    public MetricsDisplayWindow(Stage stage, Skin skin, Runnable returnToMenuCallback) {
        this.stage = stage;
        this.skin = skin;
        this.returnToMenuCallback = returnToMenuCallback;
        createWindow();
    }

    private void returnToMainMenu() {
        this.hide();
        if (returnToMenuCallback != null) {
            returnToMenuCallback.run();
        }
    }

    private void createWindow() {
        metricsWindow = new Window("Coding Performance Analytics", skin);
        metricsWindow.setModal(true); // Changed to false to allow input
        metricsWindow.setMovable(false);
        metricsWindow.setResizable(false);

        metricsWindow.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        // Make window responsive
        float windowWidth = Gdx.graphics.getWidth() * 0.8f;
        float windowHeight = Gdx.graphics.getHeight() * 0.8f;
        metricsWindow.setSize(windowWidth, windowHeight);

        // Center the window
        metricsWindow.setPosition(
            (Gdx.graphics.getWidth() - windowWidth) / 2f,
            (Gdx.graphics.getHeight() - windowHeight) / 2f
        );

        // Create content table
        contentTable = new Table();
        contentTable.pad(10);
        contentTable.top();

        // Create scroll pane for content
        scrollPane = new ScrollPane(contentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setOverscroll(false, false);

        // Add return to main menu button
        TextButton returnToMenuButton = new TextButton("Return to Main Menu", skin);
        returnToMenuButton.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        returnToMenuButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.log("MetricsDisplayWindow", "Return to Main Menu clicked");
                // Call the return to menu functionality
                returnToMainMenu();
            }
        });

        // Layout window with single button
        Table buttonTable = new Table();
        buttonTable.add(returnToMenuButton);

        metricsWindow.add(scrollPane).expand().fill().row();
        metricsWindow.add(buttonTable).right().pad(10);

        metricsWindow.setVisible(false);
        metricsWindow.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        stage.addActor(metricsWindow);

        // Initial content load
        refreshContent();
    }

    private void refreshContent() {
        contentTable.clear();

        CodingMetricsManager metrics = CodingMetricsManager.getInstance();


        // Overall Performance Section
        addSectionHeader("📊 Overall Performance");
        addOverallStats(metrics);

        // Level Breakdown Section
        addSectionHeader("📈 Level-by-Level Analysis");
        addLevelBreakdown(metrics);
    }


    private void addSectionHeader(String title) {
        Label header = new Label(title, skin, "default");
        header.setAlignment(Align.left);
        contentTable.add(header).expandX().fillX().padTop(20).padBottom(10).row();

        // Add separator line
        Image separator = new Image(skin.newDrawable("white", 0.3f, 0.3f, 0.3f, 1f));
        contentTable.add(separator).expandX().fillX().height(2).padBottom(10).row();
    }

    private void addOverallStats(CodingMetricsManager metrics) {
        int totalAttempted = metrics.getTotalQuestionsAttempted();
        int totalCorrect = metrics.getTotalQuestionsCorrect();
        int totalWrong = totalAttempted - totalCorrect;
        float overallAccuracy = totalAttempted > 0 ? ((float) totalCorrect / totalAttempted) * 100f : 0f;

        Table statsTable = new Table();
        statsTable.defaults().pad(5).left();

        // Total questions attempted
        Label totalLabel = new Label(String.format("Total Coding Questions: %d", totalAttempted), skin);
        totalLabel.setFontScale(1.1f);
        statsTable.add(totalLabel).expandX().fillX().row();

        // Correct vs Wrong breakdown
        Label correctLabel = new Label(String.format("✅ Correct: %d", totalCorrect), skin);
        correctLabel.setColor(0, 1, 0, 1);
        statsTable.add(correctLabel).expandX().fillX().row();

        Label wrongLabel = new Label(String.format("❌ Wrong: %d", totalWrong), skin);
        wrongLabel.setColor(1, 0, 0, 1);
        statsTable.add(wrongLabel).expandX().fillX().row();

        // Overall accuracy percentage
        String accuracyColor = overallAccuracy >= 80 ? "[GREEN]" : overallAccuracy >= 60 ? "[YELLOW]" : "[RED]";
        Label accuracyLabel = new Label(String.format("Overall Accuracy: [%s]%.1f%%[]",
            accuracyColor, overallAccuracy), skin);
        accuracyLabel.setFontScale(1.2f);
        statsTable.add(accuracyLabel).expandX().fillX().row();

        contentTable.add(statsTable).expandX().fillX().padLeft(10).row();
    }

    private void addLevelBreakdown(CodingMetricsManager metrics) {
        Array<CodingMetricsManager.LevelMetrics> allLevels = metrics.getAllLevelMetrics();

        if (allLevels.size == 0) {
            Label noDataLabel = new Label("No coding attempts recorded yet. Start practicing to see your progress!", skin);
            noDataLabel.setWrap(true);
            contentTable.add(noDataLabel).expandX().fillX().padLeft(10).row();
            return;
        }

        // Find level with most wrongs
        CodingMetricsManager.LevelMetrics worstLevel = null;
        int maxWrongs = 0;
        for (CodingMetricsManager.LevelMetrics level : allLevels) {
            if (level.questionsIncorrect > maxWrongs) {
                maxWrongs = level.questionsIncorrect;
                worstLevel = level;
            }
        }

        Table levelTable = new Table();
        levelTable.defaults().pad(3).left();

        // Show which level has most wrongs
        if (worstLevel != null && maxWrongs > 0) {
            Label worstLevelLabel = new Label(String.format("⚠️ Level with Most Wrongs: Level %d (%s) - %d wrong answers",
                worstLevel.level, worstLevel.levelName, maxWrongs), skin);
            worstLevelLabel.setColor(1f, 0.5f, 0f, 1f);
            worstLevelLabel.setFontScale(1.1f);
            levelTable.add(worstLevelLabel).expandX().fillX().row();
            levelTable.add().height(10).row(); // Spacing
        }

        // Show each level's details
        for (CodingMetricsManager.LevelMetrics level : allLevels) {
            if (level.questionsAttempted > 0) {
                // Level header
                Label levelHeader = new Label(String.format("Level %d - %s", level.level, level.levelName), skin);
                levelHeader.setFontScale(1.0f);
                levelHeader.setColor(0.2f, 0.6f, 1f, 1f);
                levelTable.add(levelHeader).expandX().fillX().row();

                // Level stats
                String levelAccuracyColor = getAccuracyColor(level.accuracyPercentage);
                Label levelStats = new Label(String.format("  ✅ Correct: %d | ❌ Wrong: %d | Accuracy: [%s]%.1f%%[]",
                    level.questionsCorrect, level.questionsIncorrect, levelAccuracyColor, level.accuracyPercentage), skin);
                levelTable.add(levelStats).expandX().fillX().row();

                levelTable.add().height(5).row(); // Spacing
            }
        }

        contentTable.add(levelTable).expandX().fillX().padLeft(10).row();
    }

    private void addImprovementRecommendations(CodingMetricsManager metrics) {
        CodingMetricsManager.ImprovementReport report = metrics.generateImprovementReport();

        // Overall assessment
        Label assessmentLabel = new Label("Overall Assessment: " + report.overallAssessment, skin);
        assessmentLabel.setWrap(true);
        contentTable.add(assessmentLabel).expandX().fillX().padLeft(10).padBottom(10).row();

        // Priority recommendations
        boolean hasHighPriority = false;
        for (CodingMetricsManager.LevelRecommendation rec : report.levelRecommendations) {
            if ("HIGH".equals(rec.priority) || "MEDIUM".equals(rec.priority)) {
                hasHighPriority = true;

                String priorityColor = "HIGH".equals(rec.priority) ? "RED" : "YELLOW";
                Label recLabel = new Label(String.format("[%s]%s Priority:[] %s (%.1f%% accuracy)",
                    priorityColor, rec.priority, rec.levelName, rec.accuracyPercentage), skin);
                recLabel.setWrap(true);
                contentTable.add(recLabel).expandX().fillX().padLeft(20).row();

                Label detailLabel = new Label("• " + rec.recommendation, skin);
                detailLabel.setWrap(true);
                contentTable.add(detailLabel).expandX().fillX().padLeft(30).row();

                if (rec.specificTopics.size > 0) {
                    StringBuilder topics = new StringBuilder("Focus areas: ");
                    for (int i = 0; i < rec.specificTopics.size; i++) {
                        topics.append(rec.specificTopics.get(i));
                        if (i < rec.specificTopics.size - 1) topics.append(", ");
                    }
                    Label topicsLabel = new Label("• " + topics.toString(), skin);
                    topicsLabel.setWrap(true);
                    contentTable.add(topicsLabel).expandX().fillX().padLeft(30).padBottom(10).row();
                }
            }
        }

        if (!hasHighPriority) {
            Label noIssuesLabel = new Label("Great job! No critical areas need immediate attention. Keep practicing to maintain your skills.", skin);
            noIssuesLabel.setWrap(true);
            contentTable.add(noIssuesLabel).expandX().fillX().padLeft(10).row();
        }

        // Strength areas
        if (report.strengthAreas.size > 0) {
            Label strengthHeader = new Label("[GREEN]Strength Areas:[]", skin);
            contentTable.add(strengthHeader).expandX().fillX().padLeft(10).padTop(10).row();

            for (String strength : report.strengthAreas) {
                Label strengthLabel = new Label("✓ " + strength, skin);
                contentTable.add(strengthLabel).expandX().fillX().padLeft(20).row();
            }
        }
    }

    private void addDetailedStats(CodingMetricsManager metrics) {
        // Most challenging level
        CodingMetricsManager.LevelMetrics worstLevel = metrics.getMostNeedingImprovement();
        if (worstLevel != null) {
            Label challengingLabel = new Label(String.format("[RED]Most Challenging Level:[] %s (%.1f%% accuracy)",
                worstLevel.levelName, worstLevel.accuracyPercentage), skin);
            challengingLabel.setWrap(true);
            contentTable.add(challengingLabel).expandX().fillX().padLeft(10).row();

            if (worstLevel.incorrectQuestionIds.size > 0) {
                Label incorrectLabel = new Label(String.format("Questions to review: %d incorrect attempts",
                    worstLevel.incorrectQuestionIds.size), skin);
                contentTable.add(incorrectLabel).expandX().fillX().padLeft(20).padBottom(10).row();
            }
        }

        // Practice recommendations
        Label practiceHeader = new Label("[BLUE]Practice Recommendations:[]", skin);
        contentTable.add(practiceHeader).expandX().fillX().padLeft(10).padTop(10).row();

        Array<CodingMetricsManager.LevelMetrics> allLevels = metrics.getAllLevelMetrics();
        boolean hasRecommendations = false;

        for (CodingMetricsManager.LevelMetrics level : allLevels) {
            if (level.questionsAttempted > 0 && level.accuracyPercentage < 80f) {
                hasRecommendations = true;
                int questionsNeeded = Math.max(5 - level.questionsAttempted, 0);
                if (questionsNeeded > 0) {
                    Label recLabel = new Label(String.format("• Practice %d more questions in %s",
                        questionsNeeded, level.levelName), skin);
                    contentTable.add(recLabel).expandX().fillX().padLeft(20).row();
                } else {
                    Label reviewLabel = new Label(String.format("• Review and retry incorrect questions in %s",
                        level.levelName), skin);
                    contentTable.add(reviewLabel).expandX().fillX().padLeft(20).row();
                }
            }
        }

        if (!hasRecommendations) {
            Label noRecLabel = new Label("• Continue practicing to maintain your excellent performance!", skin);
            contentTable.add(noRecLabel).expandX().fillX().padLeft(20).row();
        }
    }

    private void addAdvancedStats(CodingMetricsManager metrics) {
        Array<CodingMetricsManager.LevelMetrics> allLevels = metrics.getAllLevelMetrics();

        if (allLevels.size == 0) {
            Label noDataLabel = new Label("No advanced metrics available yet. Start practicing to see detailed performance data!", skin);
            noDataLabel.setWrap(true);
            contentTable.add(noDataLabel).expandX().fillX().padLeft(10).row();
            return;
        }

        // Timing analysis
        Label timingHeader = new Label("[BLUE]Timing Analysis:[]", skin);
        contentTable.add(timingHeader).expandX().fillX().padLeft(10).padTop(10).row();

        for (CodingMetricsManager.LevelMetrics level : allLevels) {
            if (level.questionsAttempted > 0) {
                Table timingTable = new Table();
                timingTable.defaults().pad(3).left();

                // Fastest and slowest times
                if (level.fastestQuestionTime > 0) {
                    Label fastestLabel = new Label(String.format("• %s - Fastest: %.1fs, Slowest: %.1fs",
                        level.levelName, level.fastestQuestionTime / 1000f, level.slowestQuestionTime / 1000f), skin);
                    timingTable.add(fastestLabel).expandX().fillX().row();
                }

                // Average time
                if (level.averageTimePerQuestion > 0) {
                    Label avgTimeLabel = new Label(String.format("  Average: %.1fs per question",
                        level.averageTimePerQuestion / 1000f), skin);
                    timingTable.add(avgTimeLabel).expandX().fillX().row();
                }

                contentTable.add(timingTable).expandX().fillX().padLeft(20).row();
            }
        }

        // Streak analysis
        Label streakHeader = new Label("[GREEN]Streak Analysis:[]", skin);
        contentTable.add(streakHeader).expandX().fillX().padLeft(10).padTop(15).row();

        for (CodingMetricsManager.LevelMetrics level : allLevels) {
            if (level.questionsAttempted > 0) {
                Table streakTable = new Table();
                streakTable.defaults().pad(3).left();

                // Current and best streaks
                Label currentStreakLabel = new Label(String.format("• %s - Current: %d correct, %d incorrect",
                    level.levelName, level.consecutiveCorrect, level.consecutiveIncorrect), skin);
                streakTable.add(currentStreakLabel).expandX().fillX().row();

                // Best and worst streaks
                if (level.bestConsecutiveCorrect > 0 || level.worstConsecutiveIncorrect > 0) {
                    Label bestWorstLabel = new Label(String.format("  Best: %d correct, Worst: %d incorrect",
                        level.bestConsecutiveCorrect, level.worstConsecutiveIncorrect), skin);
                    streakTable.add(bestWorstLabel).expandX().fillX().row();
                }

                contentTable.add(streakTable).expandX().fillX().padLeft(20).row();
            }
        }

        // Performance insights
        Label insightsHeader = new Label("[YELLOW]Performance Insights:[]", skin);
        contentTable.add(insightsHeader).expandX().fillX().padLeft(10).padTop(15).row();

        // Find best performing level
        CodingMetricsManager.LevelMetrics bestLevel = null;
        float bestAccuracy = 0f;
        for (CodingMetricsManager.LevelMetrics level : allLevels) {
            if (level.questionsAttempted >= 3 && level.accuracyPercentage > bestAccuracy) {
                bestAccuracy = level.accuracyPercentage;
                bestLevel = level;
            }
        }

        if (bestLevel != null) {
            Label bestLevelLabel = new Label(String.format("• Strongest area: %s (%.1f%% accuracy)",
                bestLevel.levelName, bestLevel.accuracyPercentage), skin);
            contentTable.add(bestLevelLabel).expandX().fillX().padLeft(20).row();
        }

        // Find most improved area (if we had historical data)
        Label improvementLabel = new Label("• Focus on areas with lower accuracy for maximum improvement", skin);
        contentTable.add(improvementLabel).expandX().fillX().padLeft(20).row();
    }

    private String getAccuracyColor(float accuracy) {
        if (accuracy >= 85f) return "GREEN";
        if (accuracy >= 70f) return "YELLOW";
        if (accuracy >= 50f) return "ORANGE";
        return "RED";
    }

    private String getStatusText(float accuracy) {
        if (accuracy >= 85f) return "Excellent";
        if (accuracy >= 70f) return "Good";
        if (accuracy >= 50f) return "Needs Work";
        return "Critical";
    }

    private String getStatusColor(float accuracy) {
        if (accuracy >= 85f) return "GREEN";
        if (accuracy >= 70f) return "BLUE";
        if (accuracy >= 50f) return "YELLOW";
        return "RED";
    }

    private void showResetConfirmation() {
        Dialog confirmDialog = new Dialog("Confirm Reset", skin) {
            @Override
            protected void result(Object object) {
                if ((Boolean) object) {
                    Gdx.app.log("MetricsDisplayWindow", "Reset confirmed - clearing all metrics");
                    CodingMetricsManager.getInstance().resetAllMetrics();
                    refreshContent();
                } else {
                    Gdx.app.log("MetricsDisplayWindow", "Reset cancelled");
                }
            }
        };

        confirmDialog.text("Are you sure you want to reset ALL coding metrics?\nThis action cannot be undone!");
        confirmDialog.button("Yes, Reset", true);
        confirmDialog.button("Cancel", false);
        confirmDialog.setTouchable(com.badlogic.gdx.scenes.scene2d.Touchable.enabled);
        confirmDialog.show(stage);
    }

    public void show() {
        refreshContent();
        metricsWindow.setVisible(true);
        metricsWindow.toFront();
        // NEW: ensure the stage focuses this window and its scrollable content
        stage.setKeyboardFocus(metricsWindow);
        stage.setScrollFocus(scrollPane);
    }

    public void hide() {
        metricsWindow.setVisible(false);
    }

    public boolean isVisible() {
        return metricsWindow.isVisible();
    }
}
