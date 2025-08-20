package com.altf4studios.corebringer.screens.gamescreen;

import com.altf4studios.corebringer.Main;
import com.altf4studios.corebringer.entities.Enemy;
import com.altf4studios.corebringer.entities.Player;
import com.altf4studios.corebringer.compiler.JavaExternalRunner;
import com.altf4studios.corebringer.compiler.CodePolicyValidator;
import com.altf4studios.corebringer.utils.LoggingUtils;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.*;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.utils.StringBuilder;

public class EditorStageUI {

    private TextButton btnJournal;
    private Stage editorStage;
    private Skin skin;
    private Main corebringer;


    // UI Components
    private Table editorTable;
    private Table submenuTable;
    private TextArea codeInputArea;
    private Window outputWindow;
    private TextArea outputArea;
    private TextButton btnRunCode;
    private TextButton btnRunClass;
    private TextButton btnOptions;
    private TextButton btnLog;
    private TextButton btnCheckDeck;
    private TextButton btnCharacter;
    private Label outputLabel;
    private final JavaExternalRunner javaRunner = new JavaExternalRunner();
    private Player player;
    private Enemy enemy;
    private List<String> listofcards;
    private ScrollPane scrolllistofcards;
    private Array<String> carddescription;
    private SampleCardHandler selectedcard;

    public EditorStageUI(Stage editorStage, Skin skin, Main corebringer, Player player, Enemy enemy) {
        this.editorStage = editorStage;
        this.skin = skin;
        this.corebringer = corebringer;

        this.player = player;
        this.enemy = enemy;
        setupEditorUI();
        Gdx.app.log("EditorStageUI", "Editor stage UI initialized successfully");
    }

    private void setupEditorUI() {
        float worldHeight = editorStage.getViewport().getWorldHeight();
        float worldWidth = editorStage.getViewport().getWorldWidth();

        editorTable = new Table();
        Texture editorBG = new Texture(Gdx.files.internal("ui/UI_v3.png"));
        Drawable editorTableDrawable = new TextureRegionDrawable(new TextureRegion(editorBG));


        // Create code input area and run buttons
        codeInputArea = new TextArea("// Write your code here\n// Examples:\n// - Snippets: int x = 5; System.out.println(x);\n// - Classes: public class MyClass { ... }\n// - Methods: public static void main(String[] args) { ... }\n", skin);
        codeInputArea.setPrefRows(5);
        codeInputArea.setScale(0.8f);

        // Create output window (initially hidden)
        createOutputWindow();

        btnRunCode = new TextButton("Run Code", skin);
        btnRunClass = new TextButton("Run Class", skin);

        // Table for code input and run button
        Table codeInputTable = new Table();
        codeInputTable.left().top();
        codeInputTable.add(codeInputArea).growX().padRight(10).padLeft(15);

        // IDE-like behavior: Tab inserts 4 spaces; Enter auto-indents +4 spaces
        codeInputArea.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.TAB) {
                    insertAtCursor(codeInputArea, "    ");
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.NUMPAD_ENTER) {
                    String text = codeInputArea.getText();
                    int cursor = codeInputArea.getCursorPosition();
                    int lineStart = text.lastIndexOf('\n', Math.max(0, cursor - 1));
                    lineStart = (lineStart == -1) ? 0 : lineStart + 1;
                    int leading = countLeadingSpaces(text, lineStart);
                    String indent = buildSpaces(leading + 4);
                    insertAtCursor(codeInputArea, "\n" + indent);
                    return true;
                }
                return false;
            }
        });

        // Button table with output display
        Table buttonTable = new Table();
        buttonTable.add(btnRunCode).width(100).height(40).center().row();

        // Add output label below buttons (smaller, just for status)
        outputLabel = new Label("Ready to run code", skin);
        outputLabel.setColor(Color.WHITE);
        outputLabel.setWrap(true);
        outputLabel.setAlignment(Align.center);
        outputLabel.setFontScale(0.6f);
        buttonTable.add(outputLabel).width(100).height(30).center().row();

        // Add small instruction label
        Label instructionLabel = new Label("Click Run Code to compile & execute", skin);
        instructionLabel.setColor(Color.LIGHT_GRAY);
        instructionLabel.setFontScale(0.5f);
        instructionLabel.setAlignment(Align.center);
        buttonTable.add(instructionLabel).width(100).height(20).center().row();

        // Add clear button for output
        TextButton btnClearOutput = new TextButton("Clear", skin);
        btnClearOutput.setScale(0.6f);
        btnClearOutput.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                outputLabel.setText("Ready to run code");
                outputLabel.setColor(Color.WHITE);

                // Also clear output window if it's visible
                if (outputWindow.isVisible()) {
                    hideOutputWindow();
                }
            }
        });
        buttonTable.add(btnClearOutput).width(70).height(20);

        codeInputTable.add(buttonTable).top();

        // When Run Code is clicked, execute the code using CodeSimulator
        btnRunCode.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String code = codeInputArea.getText();
                System.out.println("Executing code with external javac/java: " + code);

                // Update output label to show compiling status
                outputLabel.setText("Compiling...");
                outputLabel.setColor(Color.YELLOW);

                // Execute code in a separate thread to avoid blocking UI
                new Thread(() -> {
                    try {
                        // Validate first
                        CodePolicyValidator.ValidationResult vr = CodePolicyValidator.validate(code);
                        if (!vr.valid) {
                            String result = "❌ VALIDATION FAILED:\n" + vr.message + "\n\n" + CodePolicyValidator.policyTemplate();
                            Gdx.app.postRunnable(() -> {
                                outputArea.setText(result);
                                showOutputWindow("Validation Error", result);
                                outputLabel.setText("❌ Validation failed - See output window");
                                outputLabel.setColor(Color.RED);
                            });
                            return;
                        }

                        String result = javaRunner.compileAndRun(code);

                        // Update UI on main thread
                        Gdx.app.postRunnable(() -> {
                            // Show detailed result in output window
                            outputArea.setText(result);
                            showOutputWindow("Code Execution Result", result);

                            // Update button area with brief status
                            if (result.contains("✅")) {
                                outputLabel.setText("✅ Success - See output window");
                                outputLabel.setColor(Color.GREEN);
                            } else {
                                outputLabel.setText("❌ Failed - See output window");
                                outputLabel.setColor(Color.RED);
                            }
                        });
                    } catch (Exception e) {
                        Gdx.app.postRunnable(() -> {
                            String errorResult = "❌ Unexpected error: " + e.getMessage();

                            // Show error in output window
                            outputArea.setText(errorResult);
                            showOutputWindow("Code Execution Error", errorResult);

                            // Update button area
                            outputLabel.setText("❌ Error occurred");
                            outputLabel.setColor(Color.RED);
                        });
                    }
                }).start();
            }
        });


        editorTable.bottom();
        editorTable.setFillParent(false);
        editorTable.setSize(worldWidth, worldHeight * 0.3f);
        editorTable.background(editorTableDrawable);

        // Submenu buttons
        submenuTable = new Table();
        submenuTable.bottom();
        submenuTable.setFillParent(false);
        submenuTable.setSize(editorTable.getWidth() * 0.2f, editorTable.getHeight());

        btnOptions = new TextButton("Options", skin);
        btnLog = new TextButton("Logs", skin);
        btnCheckDeck = new TextButton("Deck", skin);
        btnCharacter = new TextButton("Character", skin);
        btnJournal   = new TextButton("Journal",skin);


        submenuTable.defaults().padTop(30).padBottom(30).padRight(20).padLeft(20).fill().uniform();
        submenuTable.add(btnJournal).growX().row();
        submenuTable.add(btnOptions);
        submenuTable.add(btnCheckDeck).row();

        submenuTable.add(btnLog);
        submenuTable.add(btnCharacter);



        // Add code input table and submenu to the editor table
        Table leftEditorTable = new Table();
        leftEditorTable.add(codeInputTable).growX();

        editorTable.add(leftEditorTable).grow();
        editorTable.add(submenuTable).growY().right();

        editorStage.addActor(editorTable);

        // Add click listeners
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        btnOptions.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindowUI();
            }
        });

        btnJournal.addListener(new ClickListener(){
            @Override
            public void clicked(InputEvent event, float x, float y) {
                showBlankJournalWindow();
            }
        });
    }

    private void optionsWindowUI() {
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        Window optionsWindow = new Window("", skin);
        optionsWindow.setModal(true);
        optionsWindow.setMovable(false);
        optionsWindow.pad(20);
        optionsWindow.setSize(640, 480);
        optionsWindow.setPosition(
            Gdx.graphics.getWidth() / 2 / 2,
            Gdx.graphics.getHeight() / 2 / 2
        );
        optionsWindow.background(optionBGDrawable);
        optionsWindow.setColor(1, 1, 1, 1);

        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                optionsWindow.remove();
            }
        });

        TextButton btnToMain = new TextButton("Title", skin);
        btnToMain.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                corebringer.setScreen(corebringer.mainMenuScreen);
                optionsWindow.remove();
            }
        });

        optionsWindow.add(new Label("Options go here", skin)).top().colspan(2).row();
        optionsWindow.add(btnClose).growX().padLeft(20).padTop(20).space(15).bottom();
        optionsWindow.add(btnToMain).growX().padRight(20).padTop(20).space(15).bottom();

        editorStage.addActor(optionsWindow);
    }

    private void showBlankJournalWindow() {
        // ===== JOURNAL WINDOW SETTINGS =====
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        Window journalWindow = new Window("Java Tutorial Journal", skin);
        journalWindow.getTitleLabel().setAlignment(Align.center); // CENTER THE TITLE
        journalWindow.getTitleTable().padLeft(20).padRight(20); // adjust by pixels

        journalWindow.setModal(true);
        journalWindow.setMovable(false);
        journalWindow.pad(-40); // Window internal padding
        journalWindow.setSize(1200, 900); // WINDOW SIZE: width=1300, height=1000
        journalWindow.setPosition(
            Gdx.graphics.getWidth() / 2 - 800, // X position (center)
            Gdx.graphics.getHeight() / 2 - 800  // Y position (center)
        );
        journalWindow.background(optionBGDrawable);
        journalWindow.setColor(1, 1, 1, 1);

        // ===== MAIN LAYOUT TABLES =====
        // Main container table (split left/right)
        Table contentTable = new Table();
        contentTable.setFillParent(true);

        // LEFT SIDE (tutorial buttons)
        Table buttonTable = new Table();
        buttonTable.top().left();
        // BUTTON SETTINGS: padding=20, width=400, height=40
        buttonTable.defaults().pad(20).padLeft(120).width(400).height(40);

        TextButton btnVariables = new TextButton("Variables & Data Types", skin);
        TextButton btnLoops = new TextButton("Loops", skin);
        TextButton btnOOP = new TextButton("OOP", skin);
        TextButton btnArrays = new TextButton("Arrays & Collections", skin);
        TextButton btnMethods = new TextButton("Methods & Functions", skin);
        TextButton btnExceptions = new TextButton("Exception Handling", skin);
        TextButton btnClose = new TextButton("Close Journal", skin);

        // Style the buttons
        btnVariables.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnLoops.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnOOP.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnArrays.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnMethods.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnExceptions.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnClose.setColor(1.0f, 0.7f, 0.7f, 1.0f);

        // Add buttons to left table
        buttonTable.add(btnVariables).row();
        buttonTable.add(btnLoops).row();
        buttonTable.add(btnOOP).row();
        buttonTable.add(btnArrays).row();
        buttonTable.add(btnMethods).row();
        buttonTable.add(btnExceptions).row();
        buttonTable.add(btnClose).row();

        // ===== RIGHT SIDE CONTENT AREA =====
        // RIGHT SIDE (tutorial content)
        final TextArea tutorialContent = new TextArea("Welcome to the Java Tutorial Journal!\n\nSelect a topic from the left to learn about Java programming concepts.\n\nEach tutorial includes:\n• Theory and explanation\n• Code examples\n• Best practices\n• Common pitfalls to avoid", skin);
        tutorialContent.setDisabled(true); // read-only
        tutorialContent.setPrefRows(150); // CONTENT AREA ROWS: 150 (larger height for more content)

        // FONT SIZE: Use the correct method for TextArea


        ScrollPane contentScroll = new ScrollPane(tutorialContent, skin);
        // CONTENT WINDOW SIZE: Set specific dimensions for the gray area
        contentScroll.setFadeScrollBars(false); // Always show scroll bars
        contentScroll.setScrollBarPositions(false, true); // Show vertical scroll bar on right

        // ===== LAYOUT POSITIONING =====
        // Add both sides to contentTable
        contentTable.add(buttonTable).top().left().padRight(10); // LEFT PANEL: top-left, 20px right padding
        contentTable.add(contentScroll).size(550, 600).top().left().padRight(100); // RIGHT PANEL: Fixed size 700x600, positioned top-left

        // ===== FINAL WINDOW LAYOUT =====
        // Add the content table to the journal window
        journalWindow.add(contentTable).grow().pad(20); // WINDOW INTERNAL PADDING: 20px

        // Button click listeners
        btnVariables.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "VARIABLES & DATA TYPES IN JAVA\n" +
                    "=====================================\n\n" +
                    " "+  "Variables are containers that " + "store data values in Java.\n\n" +
                    " "+"PRIMITIVE DATA TYPES:\n" +
                    " "+"• int: 32-bit integer (-2,147,483,648 to 2,147,483,647)\n" +
                    " "+ "• long: 64-bit integer\n" +
                    " "+  "• float: 32-bit floating point\n" +
                    " "+   "• double: 64-bit floating point\n" +
                    " "+ "• boolean: true or false\n" +
                    " "+  "• char: single Unicode character\n" +
                    " "+ "• byte: 8-bit integer (-128 to 127)\n" +
                    " "+  "• short: 16-bit integer\n\n" +
                    " "+  "REFERENCE DATA TYPES:\n" +
                    " "+ "• String: sequence of characters\n" +
                    " "+  "• Arrays: collections of elements\n" +
                    " "+  "• Classes: custom data types\n\n" +
                    " "+  "DECLARATION EXAMPLES:\n" +
                    " "+  "int age = 25;\n" +
                    " "+ "String name = \"John Doe\";\n" +
                    " "+ "double salary = 50000.50;\n" +
                    " "+ "boolean isStudent = true;\n\n" +
                    " "+  "VARIABLE NAMING RULES:\n" +
                    " "+  "• Start with letter, underscore, or dollar sign\n" +
                    " "+ "• Can contain letters, digits, underscore, dollar sign\n" +
                    " "+ "• Case sensitive\n" +
                    " "+  "• Cannot use Java keywords\n" +
                    " "+  "• Use camelCase convention (e.g., firstName)";
                tutorialContent.setText(content);
            }
        });

        btnLoops.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "LOOPS & CONTROL FLOW IN JAVA\n" +
                    "================================\n\n" +
                    "Loops allow you to execute code blocks multiple times.\n\n" +
                    "FOR LOOP:\n" +
                    "for (int i = 0; i < 5; i++) {\n" +
                    "    System.out.println(\"Count: \" + i);\n" +
                    "}\n\n" +
                    "WHILE LOOP:\n" +
                    "int count = 0;\n" +
                    "while (count < 5) {\n" +
                    "    System.out.println(\"Count: \" + count);\n" +
                    "    count++;\n" +
                    "}\n\n" +
                    "DO-WHILE LOOP:\n" +
                    "int num = 1;\n" +
                    "do {\n" +
                    "    System.out.println(\"Number: \" + num);\n" +
                    "    num++;\n" +
                    "} while (num <= 5);\n\n" +
                    "ENHANCED FOR LOOP (for arrays):\n" +
                    "String[] fruits = {\"Apple\", \"Banana\", \"Orange\"};\n" +
                    "for (String fruit : fruits) {\n" +
                    "    System.out.println(fruit);\n" +
                    "}\n\n" +
                    "CONTROL STATEMENTS:\n" +
                    "• break: exits the loop\n" +
                    "• continue: skips current iteration\n" +
                    "• return: exits the method\n\n" +
                    "NESTED LOOPS:\n" +
                    "for (int i = 1; i <= 3; i++) {\n" +
                    "    for (int j = 1; j <= 3; j++) {\n" +
                    "        System.out.print(i * j + \" \");\n" +
                    "    }\n" +
                    "    System.out.println();\n" +
                    "}";
                tutorialContent.setText(content);
            }
        });

        btnOOP.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "OBJECT-ORIENTED PROGRAMMING IN JAVA\n" +
                    "==========================================\n\n" +
                    "OOP is a programming paradigm based on objects.\n\n" +
                    "CORE CONCEPTS:\n" +
                    "1. ENCAPSULATION: Bundling data and methods\n" +
                    "2. INHERITANCE: Creating new classes from existing ones\n" +
                    "3. POLYMORPHISM: Same interface, different implementations\n" +
                    "4. ABSTRACTION: Hiding complex implementation details\n\n" +
                    "CLASS DEFINITION:\n" +
                    "public class Student {\n" +
                    "    // Private fields (encapsulation)\n" +
                    "    private String name;\n" +
                    "    private int age;\n\n" +
                    "    // Constructor\n" +
                    "    public Student(String name, int age) {\n" +
                    "        this.name = name;\n" +
                    "        this.age = age;\n" +
                    "    }\n\n" +
                    "    // Getter methods\n" +
                    "    public String getName() { return name; }\n" +
                    "    public int getAge() { return age; }\n\n" +
                    "    // Setter methods\n" +
                    "    public void setName(String name) { this.name = name; }\n" +
                    "    public void setAge(int age) { this.age = age; }\n" +
                    "}\n\n" +
                    "INHERITANCE EXAMPLE:\n" +
                    "public class GraduateStudent extends Student {\n" +
                    "    private String major;\n\n" +
                    "    public GraduateStudent(String name, int age, String major) {\n" +
                    "        super(name, age); // Call parent constructor\n" +
                    "        this.major = major;\n" +
                    "    }\n" +
                    "}\n\n" +
                    "INTERFACE EXAMPLE:\n" +
                    "public interface Drawable {\n" +
                    "    void draw();\n" +
                    "    void erase();\n" +
                    "}";
                tutorialContent.setText(content);
            }
        });

        btnArrays.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "ARRAYS & COLLECTIONS IN JAVA\n" +
                    "==============================\n\n" +
                    "Arrays store multiple values of the same type.\n\n" +
                    "ARRAY DECLARATION:\n" +
                    "// Declare and initialize\n" +
                    "int[] numbers = {1, 2, 3, 4, 5};\n\n" +
                    "// Declare with size\n" +
                    "int[] scores = new int[10];\n\n" +
                    "// Access elements (0-based indexing)\n" +
                    "int first = numbers[0]; // 1\n" +
                    "int last = numbers[numbers.length - 1]; // 5\n\n" +
                    "MULTIDIMENSIONAL ARRAYS:\n" +
                    "int[][] matrix = {\n" +
                    "    {1, 2, 3},\n" +
                    "    {4, 5, 6},\n" +
                    "    {7, 8, 9}\n" +
                    "};\n\n" +
                    "ARRAY METHODS:\n" +
                    "// Sort array\n" +
                    "Arrays.sort(numbers);\n\n" +
                    "// Fill array with value\n" +
                    "Arrays.fill(scores, 0);\n\n" +
                    "// Copy array\n" +
                    "int[] copy = Arrays.copyOf(numbers, numbers.length);\n\n" +
                    "COLLECTIONS FRAMEWORK:\n" +
                    "// ArrayList (dynamic array)\n" +
                    "ArrayList<String> names = new ArrayList<>();\n" +
                    "names.add(\"Alice\");\n" +
                    "names.add(\"Bob\");\n" +
                    "names.remove(0);\n\n" +
                    "// HashMap (key-value pairs)\n" +
                    "HashMap<String, Integer> ages = new HashMap<>();\n" +
                    "ages.put(\"Alice\", 25);\n" +
                    "ages.put(\"Bob\", 30);\n" +
                    "int aliceAge = ages.get(\"Alice\"); // 25\n\n" +
                    "// HashSet (unique elements)\n" +
                    "HashSet<String> uniqueNames = new HashSet<>();\n" +
                    "uniqueNames.add(\"Alice\");\n" +
                    "uniqueNames.add(\"Alice\"); // Won't add duplicate\n\n" +
                    "ITERATING OVER COLLECTIONS:\n" +
                    "for (String name : names) {\n" +
                    "    System.out.println(name);\n" +
                    "}";
                tutorialContent.setText(content);
            }
        });

        btnMethods.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "METHODS & FUNCTIONS IN JAVA\n" +
                    "============================\n\n" +
                    "Methods are blocks of code that perform specific tasks.\n\n" +
                    "METHOD STRUCTURE:\n" +
                    "accessModifier returnType methodName(parameters) {\n" +
                    "    // method body\n" +
                    "    return value; // if not void\n" +
                    "}\n\n" +
                    "BASIC METHOD EXAMPLES:\n" +
                    "// Simple method with no parameters\n" +
                    "public void sayHello() {\n" +
                    "    System.out.println(\"Hello, World!\");\n" +
                    "}\n\n" +
                    "// Method with parameters and return value\n" +
                    "public int add(int a, int b) {\n" +
                    "    return a + b;\n" +
                    "}\n\n" +
                    "// Method with multiple parameters\n" +
                    "public String createGreeting(String name, int age) {\n" +
                    "    return \"Hello \" + name + \", you are \" + age + \" years old.\";\n" +
                    "}\n\n" +
                    "METHOD OVERLOADING:\n" +
                    "public int multiply(int a, int b) {\n" +
                    "    return a * b;\n" +
                    "}\n\n" +
                    "public double multiply(double a, double b) {\n" +
                    "    return a * b;\n" +
                    "}\n\n" +
                    "public int multiply(int a, int b, int c) {\n" +
                    "    return a * b * c;\n" +
                    "}\n\n" +
                    "RECURSION EXAMPLE:\n" +
                    "public int factorial(int n) {\n" +
                    "    if (n <= 1) {\n" +
                    "        return 1;\n" +
                    "    }\n" +
                    "    return n * factorial(n - 1);\n" +
                    "}\n\n" +
                    "VARARGS (Variable Arguments):\n" +
                    "public int sum(int... numbers) {\n" +
                    "    int total = 0;\n" +
                    "    for (int num : numbers) {\n" +
                    "        total += num;\n" +
                    "    }\n" +
                    "    return total;\n" +
                    "}\n\n" +
                    "// Usage:\n" +
                    "int result1 = sum(1, 2, 3); // 6\n" +
                    "int result2 = sum(1, 2, 3, 4, 5); // 15";
                tutorialContent.setText(content);
            }
        });

        btnExceptions.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "EXCEPTION HANDLING IN JAVA\n" +
                    "============================\n\n" +
                    "Exceptions are events that disrupt normal program flow.\n\n" +
                    "TYPES OF EXCEPTIONS:\n" +
                    "1. CHECKED EXCEPTIONS: Must be handled (IOException, SQLException)\n" +
                    "2. UNCHECKED EXCEPTIONS: Runtime exceptions (NullPointerException, ArrayIndexOutOfBoundsException)\n" +
                    "3. ERRORS: Serious problems (OutOfMemoryError, StackOverflowError)\n\n" +
                    "BASIC TRY-CATCH BLOCK:\n" +
                    "try {\n" +
                    "    // Code that might throw an exception\n" +
                    "    int result = 10 / 0;\n" +
                    "} catch (ArithmeticException e) {\n" +
                    "    // Handle the exception\n" +
                    "    System.out.println(\"Error: \" + e.getMessage());\n" +
                    "}\n\n" +
                    "MULTIPLE CATCH BLOCKS:\n" +
                    "try {\n" +
                    "    // Risky code\n" +
                    "    int[] numbers = {1, 2, 3};\n" +
                    "    int value = numbers[5];\n" +
                    "} catch (ArrayIndexOutOfBoundsException e) {\n" +
                    "    System.out.println(\"Array index out of bounds: \" + e.getMessage());\n" +
                    "} catch (Exception e) {\n" +
                    "    System.out.println(\"General error: \" + e.getMessage());\n" +
                    "}\n\n" +
                    "FINALLY BLOCK:\n" +
                    "try {\n" +
                    "    // Open file\n" +
                    "    File file = new File(\"data.txt\");\n" +
                    "    // Process file\n" +
                    "} catch (IOException e) {\n" +
                    "    System.out.println(\"File error: \" + e.getMessage());\n" +
                    "} finally {\n" +
                    "    // Always executed, even if exception occurs\n" +
                    "    System.out.println(\"Cleaning up resources...\");\n" +
                    "}\n\n" +
                    "THROWING EXCEPTIONS:\n" +
                    "public void checkAge(int age) throws IllegalArgumentException {\n" +
                    "    if (age < 0) {\n" +
                    "        throw new IllegalArgumentException(\"Age cannot be negative\");\n" +
                    "    }\n" +
                    "    System.out.println(\"Valid age: \" + age);\n" +
                    "}\n\n" +
                    "CUSTOM EXCEPTION:\n" +
                    "public class InvalidInputException extends Exception {\n" +
                    "    public InvalidInputException(String message) {\n" +
                    "        super(message);\n" +
                    "    }\n" +
                    "}\n\n" +
                    "// Usage:\n" +
                    "try {\n" +
                    "    throw new InvalidInputException(\"Invalid user input\");\n" +
                    "} catch (InvalidInputException e) {\n" +
                    "    System.out.println(\"Custom error: \" + e.getMessage());\n" +
                    "}";
                tutorialContent.setText(content);
            }
        });

        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                journalWindow.remove();
            }
        });

        editorStage.addActor(journalWindow);
    }

    // --- Helpers for editor behavior ---
    private static void insertAtCursor(TextArea area, String toInsert) {
        String text = area.getText();
        int cursor = area.getCursorPosition();
        String before = text.substring(0, cursor);
        String after = text.substring(cursor);
        area.setText(before + toInsert + after);
        area.setCursorPosition(cursor + toInsert.length());
    }

    private static int countLeadingSpaces(String text, int fromIndex) {
        int i = 0;
        while (fromIndex + i < text.length() && text.charAt(fromIndex + i) == ' ') {
            i++;
        }
        return i;
    }

    private static String buildSpaces(int n) {
        if (n <= 0) return "";
        StringBuilder sb = new StringBuilder(n);
        for (int i = 0; i < n; i++) sb.append(' ');
        return sb.toString();
    }

    public void resize(float screenWidth, float screenHeight, float bottomHeight) {
        editorTable.setSize(screenWidth, bottomHeight);
        submenuTable.setSize(screenWidth * 0.2f, bottomHeight);
    }

    public TextArea getCodeInputArea() {
        return codeInputArea;
    }

    public TextButton getBtnRunCode() {
        return btnRunCode;
    }

    public TextArea getOutputArea() {
        return outputArea;
    }

    public Label getOutputLabel() {
        return outputLabel;
    }

    public void setOutput(String output, Color color) {
        if (outputLabel != null) {
            outputLabel.setText(output);
            if (color != null) {
                outputLabel.setColor(color);
            }
        }
    }

    /**
     * Creates the output window (initially hidden)
     */
    private void createOutputWindow() {
        // Create output area
        outputArea = new TextArea("// Output will appear here\n", skin);
        outputArea.setPrefRows(6);
        outputArea.setScale(0.8f);
        outputArea.setDisabled(true); // Make it read-only

        // Create output window
        outputWindow = new Window("Output", skin);
        outputWindow.setModal(false);
        outputWindow.setMovable(true);
        outputWindow.setResizable(false);
        outputWindow.pad(20);
        outputWindow.setSize(900, 600); // 30% bigger: 600*1.3=780, 400*1.3=520
        outputWindow.setPosition(
            Gdx.graphics.getWidth() / 2 - 390, // 780/2 = 390
            Gdx.graphics.getHeight() / 2 - 260  // 520/2 = 260
        );

        // Add close button
        TextButton btnClose = new TextButton("Close", skin);
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hideOutputWindow();
            }
        });

        // Add clear button
        TextButton btnClear = new TextButton("Clear", skin);
        btnClear.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                outputArea.setText("// Output cleared\n");
            }
        });

        // Layout the window
        Table buttonTable = new Table();
        buttonTable.add(btnClear).padRight(10);
        buttonTable.add(btnClose);

        outputWindow.add(outputArea).grow().padBottom(10).row();
        outputWindow.add(buttonTable).right();

        // Initially hide the window
        outputWindow.setVisible(false);
        editorStage.addActor(outputWindow);
    }

    /**
     * Shows the output window with the given title
     */
    private void showOutputWindow(String title, String result) {
        outputWindow.setVisible(true);
        outputWindow.toFront(); // Bring to front

        // Update the output area with the result
        if (result != null && !result.isEmpty()) {
            outputArea.setText(result);
        }

        // Show and bring to front
        outputWindow.setVisible(true);
        outputWindow.toFront();

        // Position window in center of screen
        float centerX = Gdx.graphics.getWidth() / 2 - outputWindow.getWidth() / 2;
        float centerY = Gdx.graphics.getHeight() / 2 - outputWindow.getHeight() / 2;
        outputWindow.setPosition(centerX, centerY);
    }

    /**
     * Hides the output window
     */
    private void hideOutputWindow() {
        outputWindow.setVisible(false);
    }
}
