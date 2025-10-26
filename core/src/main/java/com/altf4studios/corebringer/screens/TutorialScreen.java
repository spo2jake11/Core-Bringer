package com.altf4studios.corebringer.screens;

import com.altf4studios.corebringer.Main;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;

import java.util.*;
import java.util.List;

public class TutorialScreen implements Screen {
    private final Main corebringer;
    private final Stage stage;
    private final Skin skin;
    private final AssetManager assets;

    // Layout containers
    private final Table root;           // fills parent
    private final Table panel;          // 80% area (achieved via responsive padding on root)
    private final Table chaptersRow;    // row 1
    private final Table imageRow;       // row 2
    private final Table navRow;         // row 3

    // Image display
    private final Container<Actor> imageContainer;
    private Image currentImage;

    // Navigation
    private TextButton prevBtn;
    private TextButton nextBtn;
    private Label pageIndicator;

    // Data
    private static class Chapter {
        String key;           // e.g., "battle"
        String display;       // e.g., "Battle"
        String location;      // base path
        List<String> pages = new ArrayList<>();
    }
    private final Map<String, Chapter> chapters = new LinkedHashMap<>();
    private Chapter currentChapter;
    private int currentPageIndex = 0; // 0-based

    // Asset tracking for cleanup (only used if AssetManager is present)
    private final Set<String> loadedTexturePaths = new HashSet<>();
    // Fallback direct textures if no AssetManager
    private final Map<String, Texture> directTextures = new HashMap<>();

    public TutorialScreen(Main main) {
        this.corebringer = main;
        this.skin = (main != null ? main.testskin : new Skin(Gdx.files.internal("assets/ui/uiskin.json")));
        this.assets = (main != null ? main.getAssets() : null);
        this.stage = new Stage(new FitViewport(1280, 720));

        // Root and inner panel
        this.root = new Table();
        this.root.setFillParent(true);
        this.stage.addActor(root);

        this.panel = new Table();
        this.panel.setBackground(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.35f)));
        this.panel.defaults().pad(10);
        this.root.add(panel).expand().fill();

        // Rows
        this.chaptersRow = new Table();
        this.imageRow = new Table();
        this.navRow = new Table();

        // Image container (second row)
        this.imageContainer = new Container<Actor>();
        this.imageContainer.fill();
        this.imageContainer.align(Align.center);

        // Build static UI skeleton
        buildStaticUI();

        // Load tutorial data and select a default chapter
        loadTutorialData();
        selectDefaultChapter();
        updateNavigationState();
    }

    private void buildStaticUI() {
        // Row 1: Chapter buttons (Battle, Coding, Map, Merchant, Rest)
        panel.add(chaptersRow).growX().padBottom(10).row();

        // Row 2: Image display area
        imageRow.add(imageContainer).expand().fill().row();
        panel.add(imageRow).expand().fill().row();

        // Row 3: Prev / Page / Next
        prevBtn = new TextButton("Prev", skin);
        nextBtn = new TextButton("Next", skin);
        TextButton closeBtn = new TextButton("Close", skin);
        pageIndicator = new Label("Page 0/0", skin);
        pageIndicator.setAlignment(Align.center);

        prevBtn.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                goPrev();
            }
        });
        nextBtn.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y) {
                goNext();
            }
        });
        closeBtn.addListener(new ClickListener(){
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(new MainMenuScreen(corebringer));
            }
        });

        navRow.defaults().pad(5);
        navRow.add(prevBtn).left();
        navRow.add(pageIndicator).expandX().center();
        navRow.add(nextBtn).right().padRight(15);
        navRow.add(closeBtn).right();
        panel.add(navRow).growX();
    }

    private void loadTutorialData() {
        try {
            JsonReader reader = new JsonReader();
            JsonValue root = reader.parse(Gdx.files.internal("assets/Tutorials/tutorial.json"));

            // Expected keys
            addChapterFromJson(root, "battle", "Battle");
            addChapterFromJson(root, "coding", "Coding");
            addChapterFromJson(root, "map", "Map");
            addChapterFromJson(root, "merchant", "Merchant");
            addChapterFromJson(root, "rest", "Rest");

            // Create buttons only for present chapters
            for (Chapter ch : chapters.values()) {
                final String key = ch.key;
                TextButton btn = new TextButton(ch.display, skin);
                btn.addListener(new ClickListener(){
                    @Override public void clicked(InputEvent event, float x, float y) {
                        selectChapter(key);
                    }
                });
                chaptersRow.add(btn).pad(5).padLeft(20).padRight(20);
            }
        } catch (Exception e) {
            Gdx.app.error("TutorialScreen", "Failed to parse tutorial.json: " + e.getMessage());
            Label error = new Label("Failed to load tutorial data.", skin);
            imageRow.clear();
            imageRow.add(error).center();
        }
    }

    private void addChapterFromJson(JsonValue root, String key, String display) {
        JsonValue node = root.get(key);
        if (node == null) return;
        Chapter ch = new Chapter();
        ch.key = key;
        ch.display = display;
        ch.location = node.getString("location", null);
        if (ch.location == null) return; // invalid; skip

        JsonValue pagesNode = node.get("pages");
        if (pagesNode != null) {
            if (pagesNode.isArray()) {
                // Accept both ["file1.png", "file2.png"] or [{"1":"file1.png"}, ...]
                for (JsonValue child : pagesNode) {
                    if (child.isString()) {
                        ch.pages.add(child.asString());
                    } else if (child.isObject()) {
                        for (JsonValue c = child.child; c != null; c = c.next) {
                            if (c.isString()) ch.pages.add(c.asString());
                        }
                    } else if (child.name() != null && child.isString()) {
                        ch.pages.add(child.asString());
                    }
                }
            } else if (pagesNode.isObject()) {
                // Handle object form: {"1":"file1.png","2":"file2.png"}
                List<Map.Entry<Integer,String>> tmp = new ArrayList<>();
                for (JsonValue c = pagesNode.child; c != null; c = c.next) {
                    try {
                        int idx = Integer.parseInt(c.name());
                        tmp.add(new AbstractMap.SimpleEntry<>(idx, c.asString()));
                    } catch (Exception ignored) {
                        // fallback to insertion order if non-numeric
                        tmp.add(new AbstractMap.SimpleEntry<>(Integer.MAX_VALUE, c.asString()));
                    }
                }
                tmp.sort(Comparator.comparingInt(Map.Entry::getKey));
                for (Map.Entry<Integer, String> e : tmp) ch.pages.add(e.getValue());
            }
        }

        if (!ch.pages.isEmpty()) {
            chapters.put(key, ch);
        }
    }

    private void selectDefaultChapter() {
        // Prefer battle, else any available
        if (chapters.containsKey("battle")) {
            selectChapter("battle");
        } else if (!chapters.isEmpty()) {
            selectChapter(chapters.keySet().iterator().next());
        }
    }

    private void selectChapter(String key) {
        Chapter ch = chapters.get(key);
        if (ch == null) return;
        this.currentChapter = ch;
        this.currentPageIndex = 0;
        showCurrentPage();
        updateNavigationState();
    }

    private void goPrev() {
        if (currentChapter == null) return;
        if (currentPageIndex > 0) {
            currentPageIndex--;
            showCurrentPage();
            updateNavigationState();
        }
    }

    private void goNext() {
        if (currentChapter == null) return;
        if (currentPageIndex < currentChapter.pages.size() - 1) {
            currentPageIndex++;
            showCurrentPage();
            updateNavigationState();
        }
    }

    private void updateNavigationState() {
        int total = (currentChapter != null ? currentChapter.pages.size() : 0);
        int pageOneBased = (currentChapter != null ? currentPageIndex + 1 : 0);
        pageIndicator.setText("Page " + pageOneBased + "/" + total);
        prevBtn.setDisabled(currentChapter == null || currentPageIndex <= 0);
        nextBtn.setDisabled(currentChapter == null || currentPageIndex >= total - 1);
    }

    private void showCurrentPage() {
        if (currentChapter == null || currentChapter.pages.isEmpty()) {
            imageContainer.setActor(new Label("No pages.", skin));
            return;
        }
        String fileName = currentChapter.pages.get(currentPageIndex);
        String fullPath = currentChapter.location + "/" + fileName;

        Texture tex = null;
        try {
            if (assets != null) {
                if (!assets.isLoaded(fullPath, Texture.class)) {
                    assets.load(fullPath, Texture.class);
                    assets.finishLoadingAsset(fullPath);
                }
                tex = assets.get(fullPath, Texture.class);
                loadedTexturePaths.add(fullPath);
            } else {
                tex = directTextures.get(fullPath);
                if (tex == null) {
                    tex = new Texture(Gdx.files.internal(fullPath));
                    directTextures.put(fullPath, tex);
                }
            }
        } catch (Exception e) {
            Gdx.app.error("TutorialScreen", "Failed to load image: " + fullPath + ", " + e.getMessage());
            imageContainer.setActor(new Label("Missing: " + fileName, skin));
            return;
        }

        // Build image with scaling
        currentImage = new Image(tex);
        currentImage.setScaling(Scaling.fit);
        currentImage.setAlign(Align.center);
        imageContainer.setActor(currentImage);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
        if (corebringer != null) corebringer.playMusic("menu"); // neutral music
        // Apply responsive padding for 80% area on first show
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        // Set 10% padding on each side so inner panel is ~80% of width and height
        float padH = height * 0.10f; // top/bottom
        float padW = width * 0.10f;  // left/right
        root.pad(padH, padW, padH, padW);
        root.invalidateHierarchy();
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        stage.dispose();
        // Unload or dispose textures we loaded
        if (assets != null) {
            for (String path : loadedTexturePaths) {
                try { if (assets.isLoaded(path, Texture.class)) assets.unload(path); } catch (Exception ignored) {}
            }
            loadedTexturePaths.clear();
        } else {
            for (Texture t : directTextures.values()) {
                try { if (t != null) t.dispose(); } catch (Exception ignored) {}
            }
            directTextures.clear();
        }
    }
}
