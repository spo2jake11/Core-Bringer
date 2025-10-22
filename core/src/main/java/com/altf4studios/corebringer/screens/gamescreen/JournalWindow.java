package com.altf4studios.corebringer.screens.gamescreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.InputEvent;

public class JournalWindow extends Window {
    public JournalWindow(final Skin skin, final float width, final float height) {
        super("Java Tutorial Journal", skin);
        this.setZIndex(1000);
        this.getTitleLabel().setAlignment(Align.center);
        this.getTitleTable().padLeft(20).padRight(20);
        this.setModal(false);
        this.setMovable(false);
        this.pad(-40);
        this.setSize(width * 0.8f, height * 0.8f);
        float windowWidth = width * 0.8f;
        float windowHeight = height * 0.8f;
        this.setPosition(
            (Gdx.graphics.getWidth() - windowWidth) / 2f,
            (Gdx.graphics.getHeight() - windowHeight) / 2f
        );
        Texture optionBG = new Texture(Gdx.files.internal("ui/optionsBG.png"));
        Drawable optionBGDrawable = new TextureRegionDrawable(new TextureRegion(optionBG));
        this.background(optionBGDrawable);
        this.setColor(1, 1, 1, 1);

        Table contentTable = new Table();
        contentTable.setFillParent(true);

        Table buttonTable = new Table();
        buttonTable.top().left();
        // Make button sizing responsive
        float buttonWidth = width * 0.25f;  // 25% of screen width
        float buttonHeight = height * 0.04f; // 4% of screen height
        float leftPadding = width * 0.08f;   // 8% of screen width for left padding
        buttonTable.defaults().pad(20).padLeft(leftPadding).width(buttonWidth).height(buttonHeight);

        TextButton btnVariables = new TextButton("Variables & Data Types", skin);
        TextButton btnLoops = new TextButton("Loops", skin);
        TextButton btnOOP = new TextButton("OOP", skin);
        TextButton btnArrays = new TextButton("Arrays & Collections", skin);
        TextButton btnMethods = new TextButton("Methods & Functions", skin);
        TextButton btnExceptions = new TextButton("Exception Handling", skin);
        TextButton btnClose = new TextButton("Close Journal", skin);

        btnVariables.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnLoops.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnOOP.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnArrays.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnMethods.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnExceptions.setColor(0.8f, 0.9f, 1.0f, 1.0f);
        btnClose.setColor(1.0f, 0.7f, 0.7f, 1.0f);

        buttonTable.add(btnVariables).row();
        buttonTable.add(btnLoops).row();
        buttonTable.add(btnOOP).row();
        buttonTable.add(btnArrays).row();
        buttonTable.add(btnMethods).row();
        buttonTable.add(btnExceptions).row();
        buttonTable.add(btnClose).row();

        final TextArea tutorialContent = new TextArea("Welcome to the Java Tutorial Journal!\n\nSelect a topic from the left to learn about Java programming concepts.\n\nEach tutorial includes:\n• Theory and explanation\n• Code examples\n• Best practices\n• Common pitfalls to avoid", skin);
        tutorialContent.setDisabled(true);
        tutorialContent.setPrefRows(150);
        ScrollPane contentScroll = new ScrollPane(tutorialContent, skin);
        contentScroll.setFadeScrollBars(false);
        contentScroll.setScrollBarPositions(false, true);

        contentTable.add(buttonTable).top().left().padRight(10);
        // Make content area responsive
        float contentWidth = width * 0.4f;   // 40% of screen width
        float contentHeight = height * 0.6f; // 60% of screen height
        float rightPadding = width * 0.08f;  // 8% of screen width for right padding
        contentTable.add(contentScroll).size(contentWidth, contentHeight).top().left().padRight(rightPadding);
        this.add(contentTable).grow().pad(20);

        btnVariables.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "VARIABLES & DATA TYPES IN JAVA\n" +
                    "=====================================\n\n" +
                    "Variables are containers that store data values in Java.\n\n" +
                    "PRIMITIVE DATA TYPES:\n" +
                    "• int: 32-bit integer (-2,147,483,648 to 2,147,483,647)\n" +
                    "• long: 64-bit integer\n" +
                    "• float: 32-bit floating point\n" +
                    "• double: 64-bit floating point\n" +
                    "• boolean: true or false\n" +
                    "• char: single Unicode character\n" +
                    "• byte: 8-bit integer (-128 to 127)\n" +
                    "• short: 16-bit integer\n\n" +
                    "REFERENCE DATA TYPES:\n" +
                    "• String: sequence of characters\n" +
                    "• Arrays: collections of elements\n" +
                    "• Classes: custom data types\n\n" +
                    "DECLARATION EXAMPLES:\n" +
                    "int age = 25;\n" +
                    "String name = \"John Doe\";\n" +
                    "double salary = 50000.50;\n" +
                    "boolean isStudent = true;\n\n" +
                    "VARIABLE NAMING RULES:\n" +
                    "• Start with letter, underscore, or dollar sign\n" +
                    "• Can contain letters, digits, underscore, dollar sign\n" +
                    "• Case sensitive\n" +
                    "• Cannot use Java keywords\n" +
                    "• Use camelCase convention (e.g., firstName)";
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
                    "for (int i = 0; i < 5; i++) {\n    System.out.println(\"Count: \" + i);\n}\n\n" +
                    "WHILE LOOP:\n" +
                    "int count = 0;\nwhile (count < 5) {\n    System.out.println(\"Count: \" + count);\n    count++;\n}\n\n" +
                    "DO-WHILE LOOP:\n" +
                    "int num = 1;\ndo {\n    System.out.println(\"Number: \" + num);\n    num++;\n} while (num <= 5);\n\n" +
                    "ENHANCED FOR LOOP (for arrays):\n" +
                    "String[] fruits = {\"Apple\", \"Banana\", \"Orange\"};\nfor (String fruit : fruits) {\n    System.out.println(fruit);\n}\n\n" +
                    "CONTROL STATEMENTS:\n" +
                    "• break: exits the loop\n" +
                    "• continue: skips current iteration\n" +
                    "• return: exits the method\n\n" +
                    "NESTED LOOPS:\n" +
                    "for (int i = 1; i <= 3; i++) {\n    for (int j = 1; j <= 3; j++) {\n        System.out.print(i * j + \" \" );\n    }\n    System.out.println();\n}";
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
                    "public class Student {\n    // Private fields (encapsulation)\n    private String name;\n    private int age;\n\n    // Constructor\n    public Student(String name, int age) {\n        this.name = name;\n        this.age = age;\n    }\n\n    // Getter methods\n    public String getName() { return name; }\n    public int getAge() { return age; }\n\n    // Setter methods\n    public void setName(String name) { this.name = name; }\n    public void setAge(int age) { this.age = age; }\n}\n\n" +
                    "INHERITANCE EXAMPLE:\n" +
                    "public class GraduateStudent extends Student {\n    private String major;\n\n    public GraduateStudent(String name, int age, String major) {\n        super(name, age); // Call parent constructor\n        this.major = major;\n    }\n}\n\n" +
                    "INTERFACE EXAMPLE:\n" +
                    "public interface Drawable {\n    void draw();\n    void erase();\n}";
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
                    "// Declare and initialize\nint[] numbers = {1, 2, 3, 4, 5};\n\n// Declare with size\nint[] scores = new int[10];\n\n// Access elements (0-based indexing)\nint first = numbers[0]; // 1\nint last = numbers[numbers.length - 1]; // 5\n\n" +
                    "MULTIDIMENSIONAL ARRAYS:\n" +
                    "int[][] matrix = {\n    {1, 2, 3},\n    {4, 5, 6},\n    {7, 8, 9}\n};\n\n" +
                    "ARRAY METHODS:\n" +
                    "// Sort array\nArrays.sort(numbers);\n\n// Fill array with value\nArrays.fill(scores, 0);\n\n// Copy array\nint[] copy = Arrays.copyOf(numbers, numbers.length);\n\n" +
                    "COLLECTIONS FRAMEWORK:\n" +
                    "// ArrayList (dynamic array)\nArrayList<String> names = new ArrayList<>();\nnames.add(\"Alice\");\nnames.add(\"Bob\");\nnames.remove(0);\n\n// HashMap (key-value pairs)\nHashMap<String, Integer> ages = new HashMap<>();\nages.put(\"Alice\", 25);\nages.put(\"Bob\", 30);\nint aliceAge = ages.get(\"Alice\"); // 25\n\n// HashSet (unique elements)\nHashSet<String> uniqueNames = new HashSet<>();\nuniqueNames.add(\"Alice\");\nuniqueNames.add(\"Alice\"); // Won't add duplicate\n\n" +
                    "ITERATING OVER COLLECTIONS:\n" +
                    "for (String name : names) {\n    System.out.println(name);\n}";
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
                    "accessModifier returnType methodName(parameters) {\n    // method body\n    return value; // if not void\n}\n\n" +
                    "BASIC METHOD EXAMPLES:\n" +
                    "// Simple method with no parameters\npublic void sayHello() {\n    System.out.println(\"Hello, World!\");\n}\n\n" +
                    "// Method with parameters and return value\npublic int add(int a, int b) {\n    return a + b;\n}\n\n" +
                    "// Method with multiple parameters\npublic String createGreeting(String name, int age) {\n    return \"Hello \" + name + \", you are \" + age + \" years old.\";\n}\n\n" +
                    "METHOD OVERLOADING:\n" +
                    "public int multiply(int a, int b) { return a * b; }\npublic double multiply(double a, double b) { return a * b; }\npublic int multiply(int a, int b, int c) { return a * b * c; }\n\n" +
                    "RECURSION EXAMPLE:\n" +
                    "public int factorial(int n) {\n    if (n <= 1) { return 1; }\n    return n * factorial(n - 1);\n}\n\n" +
                    "VARARGS (Variable Arguments):\n" +
                    "public int sum(int... numbers) {\n    int total = 0;\n    for (int num : numbers) { total += num; }\n    return total;\n}\n\n// Usage:\nint result1 = sum(1, 2, 3); // 6\nint result2 = sum(1, 2, 3, 4, 5); // 15";
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
                    "try {\n    // Code that might throw an exception\n    int result = 10 / 0;\n} catch (ArithmeticException e) {\n    // Handle the exception\n    System.out.println(\"Error: \" + e.getMessage());\n}\n\n" +
                    "MULTIPLE CATCH BLOCKS:\n" +
                    "try {\n    // Risky code\n    int[] numbers = {1, 2, 3};\n    int value = numbers[5];\n} catch (ArrayIndexOutOfBoundsException e) {\n    System.out.println(\"Array index out of bounds: \" + e.getMessage());\n} catch (Exception e) {\n    System.out.println(\"General error: \" + e.getMessage());\n}\n\n" +
                    "FINALLY BLOCK:\n" +
                    "try {\n    // Open file\n    File file = new File(\"data.txt\");\n    // Process file\n} catch (IOException e) {\n    System.out.println(\"File error: \" + e.getMessage());\n} finally {\n    // Always executed, even if exception occurs\n    System.out.println(\"Cleaning up resources...\");\n}\n\n" +
                    "THROWING EXCEPTIONS:\n" +
                    "public void checkAge(int age) throws IllegalArgumentException {\n    if (age < 0) {\n        throw new IllegalArgumentException(\"Age cannot be negative\");\n    }\n    System.out.println(\"Valid age: \" + age);\n}\n\n" +
                    "CUSTOM EXCEPTION:\n" +
                    "public class InvalidInputException extends Exception {\n    public InvalidInputException(String message) {\n        super(message);\n    }\n}\n\n// Usage:\ntry {\n    throw new InvalidInputException(\"Invalid user input\");\n} catch (InvalidInputException e) {\n    System.out.println(\"Custom error: \" + e.getMessage());\n}";
                tutorialContent.setText(content);
            }
        });
        btnClose.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                JournalWindow.this.remove();
            }
        });
    }

    public void showOnStage() {
        this.setVisible(true);
        this.toFront();
        if (this.getStage() == null) {
            throw new IllegalStateException("JournalWindow must be added to a stage before calling showOnStage()");
        }
    }
}

