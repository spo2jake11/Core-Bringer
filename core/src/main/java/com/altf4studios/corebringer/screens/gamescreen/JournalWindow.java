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
        tutorialContent.getStyle().fontColor = com.badlogic.gdx.graphics.Color.WHITE;
        // Set black background for the content area
        com.badlogic.gdx.graphics.Pixmap blackPixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        blackPixmap.setColor(com.badlogic.gdx.graphics.Color.BLACK);
        blackPixmap.fill();
        com.badlogic.gdx.graphics.Texture blackTexture = new com.badlogic.gdx.graphics.Texture(blackPixmap);
        blackPixmap.dispose();
        tutorialContent.getStyle().background = new com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable(new com.badlogic.gdx.graphics.g2d.TextureRegion(blackTexture));
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
                String content = " "+" VARIABLES & DATA TYPES IN JAVA\n" +
                    "=====================================\n\n" +
                    " "+"Variables are containers that store data values in Java.\n\n" +
                    " "+"PRIMITIVE DATA TYPES:\n" +
                    " "+" int:     32-bit integer (-2,147,483,648 to 2,147,483,647)\n" +
                    " "+" long:    64-bit integer\n" +
                    " "+" float:   32-bit floating point\n" +
                    " "+" double:  64-bit floating point\n" +
                    " "+" boolean: true or false\n" +
                    " "+" char:    single Unicode character\n" +
                    " "+" byte:    8-bit integer (-128 to 127)\n" +
                    " "+" short:   16-bit integer\n\n" +
                    " "+"REFERENCE DATA TYPES:\n" +
                    " "+" String:  sequence of characters\n" +
                    " "+" Arrays:  collections of elements\n" +
                    " "+" Classes: custom data types\n\n" +
                    " "+"DECLARATION EXAMPLES:\n" +
                    " "+" int age = 25;\n" +
                    " "+" String name = \"John Doe\";\n" +
                    " "+" double salary = 50000.50;\n" +
                    " "+" boolean isStudent = true;\n\n" +
                    " "+"VARIABLE NAMING RULES:\n" +
                    " "+" - Start with letter, underscore, or dollar sign\n" +
                    " "+" - Can contain letters, digits, underscore, dollar sign\n" +
                    " "+" - Case sensitive\n" +
                    " "+" - Cannot use Java keywords\n" +
                    " "+" - Use camelCase convention (e.g., firstName)\n\n" +
                    " "+"DO's AND DON'Ts:\n" +
                    " "+" DO:\n" +
                    " "+"  ✔ Use descriptive variable names.\n" +
                    " "+"     Example: int playerScore = 100;\n" +
                    " "+"  ✔ Match variable type with value.\n" +
                    " "+"     Example: double price = 9.99;\n" +
                    " "+"  ✔ Initialize variables before use.\n" +
                    " "+"     Example: int lives = 3;\n\n" +
                    " "+" DON'T:\n" +
                    " "+"  ✘ Mismatch variable type and value.\n" +
                    " "+"     Example: int age = \"twenty\"; // ERROR: incompatible types\n" +
                    " "+"  ✘ Start variable names with numbers.\n" +
                    " "+"     Example: int 1score = 50; // ERROR: not a valid identifier\n" +
                    " "+"  ✘ Use Java keywords as variable names.\n" +
                    " "+"     Example: int class = 5; // ERROR: 'class' is a reserved word\n" +
                    " "+"  ✘ Use uninitialized variables.\n" +
                    " "+"     Example:\n" +
                    " "+"       int level;\n" +
                    " "+"       System.out.println(level); // ERROR: variable not initialized\n\n" +
                    " "+"Remember: clear naming and correct data types help prevent bugs!";
                tutorialContent.setText(content);
            }
        });
        btnLoops.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content =  " "+"LOOPS & CONTROL FLOW IN JAVA\n" +
                    "================================\n\n" +
                    " "+"Loops allow you to execute code blocks multiple times.\n\n" +
                    " "+"FOR LOOP:\n" +
                    " "+"  for (int i = 0; i < 5; i++) {\n" +
                    " "+"      System.out.println(\"Count: \" + i);\n" +
                    " "+"  }\n\n" +
                    " "+"WHILE LOOP:\n" +
                    " "+"  int count = 0;\n" +
                    " "+"  while (count < 5) {\n" +
                    " "+"      System.out.println(\"Count: \" + count);\n" +
                    " "+"      count++;\n" +
                    " "+"  }\n\n" +
                    " "+"DO-WHILE LOOP:\n" +
                    " "+"  int num = 1;\n" +
                    " "+"  do {\n" +
                    " "+"     System.out.println(\"Number: \" + num);\n" +
                    " "+"     num++;\n" +
                    " "+"  } while (num <= 5);\n\n" +
                    " "+"ENHANCED FOR LOOP (for arrays):\n" +
                    " "+"  String[] fruits = {\"Apple\", \"Banana\", \"Orange\"};\n" +
                    " "+"  for (String fruit : fruits) {\n" +
                    " "+"      System.out.println(fruit);\n" +
                    " "+"  }\n\n" +
                    " "+"CONTROL STATEMENTS:\n" +
                    " "+" break:    exits the loop\n" +
                    " "+" continue: skips current iteration\n" +
                    " "+" return:   exits the method\n\n" +
                    " "+"NESTED LOOPS:\n" +
                    " "+"  for (int i = 1; i <= 3; i++) {\n" +
                    " "+"     for (int j = 1; j <= 3; j++) {\n" +
                    " "+"         System.out.print(i * j + \" \");\n" +
                    " "+"     }\n" +
                    " "+"      System.out.println();\n" +
                    " "+"  }\n\n" +
                    " "+"DO's AND DON'Ts:\n" +
                    " "+" DO:\n" +
                    " "+"  ✔ Use loop conditions that will eventually become false.\n" +
                    " "+"     Example:\n" +
                    " "+"       int i = 0;\n" +
                    " "+"       while (i < 5) {\n" +
                    " "+"           System.out.println(i);\n" +
                    " "+"           i++; // increments properly\n" +
                    " "+"       }\n\n" +
                    " "+"  ✔ Use 'break' to exit early if needed.\n" +
                    " "+"     Example:\n" +
                    " "+"       for (int n = 0; n < 10; n++) {\n" +
                    " "+"           if (n == 5) break;\n" +
                    " "+"       }\n\n" +
                    " "+"  ✔ Use 'continue' to skip specific iterations.\n" +
                    " "+"     Example:\n" +
                    " "+"       for (int x = 1; x <= 5; x++) {\n" +
                    " "+"           if (x == 3) continue;\n" +
                    " "+"           System.out.println(x);\n" +
                    " "+"       }\n\n" +
                    " "+" DON'T:\n" +
                    " "+"  ✘ Forget to update loop variables.\n" +
                    " "+"     Example:\n" +
                    " "+"       int x = 0;\n" +
                    " "+"       while (x < 5) {\n" +
                    " "+"           System.out.println(x);\n" +
                    " "+"           // ERROR: missing x++ causes infinite loop\n" +
                    " "+"       }\n\n" +
                    " "+"  ✘ Use semicolon after 'for' or 'while' accidentally.\n" +
                    " "+"     Example:\n" +
                    " "+"       for (int i = 0; i < 5; i++); // ERROR: empty loop body\n" +
                    " "+"       System.out.println(i); // variable i not visible here\n\n" +
                    " "+"  ✘ Modify array size while looping through it.\n" +
                    " "+"     Example:\n" +
                    " "+"       for (int i = 0; i < list.size(); i++) {\n" +
                    " "+"           list.remove(i); // ERROR: causes ConcurrentModificationException\n" +
                    " "+"       }\n\n" +
                    " "+"  ✘ Misuse 'break' or 'continue' outside loops.\n" +
                    " "+"     Example:\n" +
                    " "+"       if (true) break; // ERROR: illegal use of break\n\n" +
                    " "+"Remember: Loops are powerful but must always have clear exit conditions!";
                tutorialContent.setText(content);
            }
        });
        btnOOP.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "OBJECT-ORIENTED PROGRAMMING IN JAVA\n" +
                    "==========================================\n\n" +
                    " "+"OOP is a programming paradigm based on objects.\n\n" +
                    " "+"CORE CONCEPTS:\n" +
                    " "+"1. ENCAPSULATION:  Bundling data and methods\n" +
                    " "+"2. INHERITANCE:    Creating new classes from existing ones\n" +
                    " "+"3. POLYMORPHISM:   Same interface, different implementations\n" +
                    " "+"4. ABSTRACTION:    Hiding complex implementation details\n\n" +
                    " "+"CLASS DEFINITION:\n" +
                    " "+"  public class Student {\n" +
                    " "+"      // Private fields (encapsulation)\n" +
                    " "+"      private String name;\n" +
                    " "+"      private int age;\n\n" +
                    " "+"      // Constructor\n" +
                    " "+"      public Student(String name, int age) {\n" +
                    " "+"          this.name = name;\n" +
                    " "+"          this.age = age;\n" +
                    " "+"      }\n\n" +
                    " "+"      // Getter methods\n" +
                    " "+"      public String getName() { return name; }\n" +
                    " "+"      public int getAge() { return age; }\n\n" +
                    " "+"      // Setter methods\n" +
                    " "+"      public void setName(String name) { this.name = name; }\n" +
                    " "+"      public void setAge(int age) { this.age = age; }\n" +
                    " "+"  }\n\n" +
                    " "+"INHERITANCE EXAMPLE:\n" +
                    " "+"  public class GraduateStudent extends Student {\n" +
                    " "+"      private String major;\n\n" +
                    " "+"      public GraduateStudent(String name, int age, String major) {\n" +
                    " "+"          super(name, age); // Call parent constructor\n" +
                    " "+"          this.major = major;\n" +
                    " "+"      }\n" +
                    " "+"  }\n\n" +
                    " "+"INTERFACE EXAMPLE:\n" +
                    " "+"  public interface Drawable {\n" +
                    " "+"      void draw();\n" +
                    " "+"      void erase();\n" +
                    " "+"  }\n\n" +
                    " "+"DO's AND DON'Ts:\n" +
                    " "+" DO:\n" +
                    " "+"  ✔ Use private fields and public getters/setters to protect data.\n" +
                    " "+"     Example:\n" +
                    " "+"       private int hp;\n" +
                    " "+"       public int getHp() { return hp; }\n" +
                    " "+"       public void setHp(int hp) { this.hp = hp; }\n\n" +
                    " "+"  ✔ Use 'super()' to call the parent constructor when inheriting.\n" +
                    " "+"     Example:\n" +
                    " "+"       super(name, age);\n\n" +
                    " "+"  ✔ Use interfaces to define behaviors multiple classes can share.\n" +
                    " "+"     Example:\n" +
                    " "+"       public class Enemy implements Drawable {\n" +
                    " "+"           public void draw() {}\n" +
                    " "+"           public void erase() {}\n" +
                    " "+"       }\n\n" +
                    " "+" DON'T:\n" +
                    " "+"  ✘ Access private fields directly outside the class.\n" +
                    " "+"     Example:\n" +
                    " "+"       Student s = new Student(\"Alex\", 20);\n" +
                    " "+"       System.out.println(s.name); // ERROR: name has private access\n\n" +
                    " "+"  ✘ Forget to use 'super' in subclass constructors.\n" +
                    " "+"     Example:\n" +
                    " "+"       public GraduateStudent(String n, int a, String m) {\n" +
                    " "+"           this.major = m; // ERROR: no default constructor in parent\n" +
                    " "+"       }\n\n" +
                    " "+"  ✘ Try to inherit from multiple classes.\n" +
                    " "+"     Example:\n" +
                    " "+"       public class Child extends Parent1, Parent2 { }\n" +
                    " "+"       // ERROR: Java does not support multiple inheritance\n\n" +
                    " "+"  ✘ Forget to implement all methods from an interface.\n" +
                    " "+"     Example:\n" +
                    " "+"       public class Shape implements Drawable {\n" +
                    " "+"           public void draw() {}\n" +
                    " "+"           // ERROR: erase() not implemented\n" +
                    " "+"       }\n\n" +
                    " "+"  ✘ Confuse method overloading with overriding.\n" +
                    " "+"     Example:\n" +
                    " "+"       public void print(int a) {}\n" +
                    " "+"       public void print(int a, int b) {}\n" +
                    " "+"       // Overloading: different parameters\n" +
                    " "+"       // Overriding: same method signature in subclass\n\n" +
                    " "+"Remember: OOP helps make your code reusable, organized, and easier to maintain!";
                tutorialContent.setText(content);
            }
        });
        btnArrays.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = " "+"ARRAYS & COLLECTIONS IN JAVA\n" +
                    "==============================\n\n" +
                    " "+"Arrays store multiple values of the same type.\n\n" +
                    " "+"ARRAY DECLARATION:\n" +
                    " "+"  // Declare and initialize\n" +
                    " "+"  int[] numbers = {1, 2, 3, 4, 5};\n\n" +
                    " "+"  // Declare with size\n" +
                    " "+"  int[] scores = new int[10];\n\n" +
                    " "+"  // Access elements (0-based indexing)\n" +
                    " "+"  int first = numbers[0]; // 1\n" +
                    " "+"  int last = numbers[numbers.length - 1]; // 5\n\n" +
                    " "+"MULTIDIMENSIONAL ARRAYS:\n" +
                    " "+"  int[][] matrix = {\n" +
                    " "+"      {1, 2, 3},\n" +
                    " "+"      {4, 5, 6},\n" +
                    " "+"      {7, 8, 9}\n" +
                    " "+"  };\n\n" +
                    " "+"ARRAY METHODS:\n" +
                    " "+"  // Sort array\n" +
                    " "+"  Arrays.sort(numbers);\n\n" +
                    " "+"  // Fill array with value\n" +
                    " "+"  Arrays.fill(scores, 0);\n\n" +
                    " "+"  // Copy array\n" +
                    " "+"  int[] copy = Arrays.copyOf(numbers, numbers.length);\n\n" +
                    " "+"COLLECTIONS FRAMEWORK:\n" +
                    " "+"  // ArrayList (dynamic array)\n" +
                    " "+"  ArrayList<String> names = new ArrayList<>();\n" +
                    " "+"  names.add(\"Alice\");\n" +
                    " "+"  names.add(\"Bob\");\n" +
                    " "+"  names.remove(0);\n\n" +
                    " "+"  // HashMap (key-value pairs)\n" +
                    " "+"  HashMap<String, Integer> ages = new HashMap<>();\n" +
                    " "+"  ages.put(\"Alice\", 25);\n" +
                    " "+"  ages.put(\"Bob\", 30);\n" +
                    " "+"  int aliceAge = ages.get(\"Alice\"); // 25\n\n" +
                    " "+"  // HashSet (unique elements)\n" +
                    " "+"  HashSet<String> uniqueNames = new HashSet<>();\n" +
                    " "+"  uniqueNames.add(\"Alice\");\n" +
                    " "+"  uniqueNames.add(\"Alice\"); // Won't add duplicate\n\n" +
                    " "+"ITERATING OVER COLLECTIONS:\n" +
                    " "+"  for (String name : names) {\n" +
                    " "+"      System.out.println(name);\n" +
                    " "+"  }\n\n" +
                    " "+"DO's AND DON'Ts:\n" +
                    " "+" DO:\n" +
                    " "+"  ✔ Always check array length before accessing elements.\n" +
                    " "+"     Example:\n" +
                    " "+"       if (index < numbers.length) {\n" +
                    " "+"           System.out.println(numbers[index]);\n" +
                    " "+"       }\n\n" +
                    " "+"  ✔ Use collections (ArrayList, HashMap, HashSet) for flexible data.\n" +
                    " "+"     Example:\n" +
                    " "+"       ArrayList<Integer> list = new ArrayList<>();\n" +
                    " "+"       list.add(10);\n" +
                    " "+"       list.add(20);\n\n" +
                    " "+"  ✔ Use enhanced for-loops to avoid index errors.\n" +
                    " "+"     Example:\n" +
                    " "+"       for (int n : numbers) {\n" +
                    " "+"           System.out.println(n);\n" +
                    " "+"       }\n\n" +
                    " "+" DON'T:\n" +
                    " "+"  ✘ Access invalid array indexes.\n" +
                    " "+"     Example:\n" +
                    " "+"       int[] nums = {1, 2, 3};\n" +
                    " "+"       System.out.println(nums[3]); // ERROR: ArrayIndexOutOfBoundsException\n\n" +
                    " "+"  ✘ Forget to initialize arrays before use.\n" +
                    " "+"     Example:\n" +
                    " "+"       int[] data;\n" +
                    " "+"       data[0] = 10; // ERROR: NullPointerException\n\n" +
                    " "+"  ✘ Modify collection while iterating over it.\n" +
                    " "+"     Example:\n" +
                    " "+"       for (String n : names) {\n" +
                    " "+"           names.remove(n); // ERROR: ConcurrentModificationException\n" +
                    " "+"       }\n\n" +
                    " "+"  ✘ Assume HashMap preserves order.\n" +
                    " "+"       HashMap<String, Integer> map = new HashMap<>();\n" +
                    " "+"       map.put(\"A\", 1);\n" +
                    " "+"       map.put(\"B\", 2);\n" +
                    " "+"       System.out.println(map); // Order not guaranteed\n\n" +
                    " "+"  ✘ Add null elements carelessly to collections.\n" +
                    " "+"       ArrayList<String> items = new ArrayList<>();\n" +
                    " "+"       items.add(null);\n" +
                    " "+"       System.out.println(items.get(0).length()); // ERROR: NullPointerException\n\n" +
                    " "+"Remember: Arrays are fixed in size, but Collections grow dynamically!";
                tutorialContent.setText(content);
            }
        });
        btnMethods.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = " "+"METHODS & FUNCTIONS IN JAVA\n" +
                    " "+"============================\n\n" +
                    " "+"Methods are blocks of code that perform specific tasks.\n\n" +
                    " "+"METHOD STRUCTURE:\n" +
                    " "+"  accessModifier returnType methodName(parameters) {\n" +
                    " "+"      // method body\n" +
                    " "+"      return value; // if not void\n" +
                    " "+"  }\n\n" +
                    " "+"BASIC METHOD EXAMPLES:\n" +
                    " "+"  // Simple method with no parameters\n" +
                    " "+"  public void sayHello() {\n" +
                    " "+"      System.out.println(\"Hello, World!\");\n" +
                    " "+"  }\n\n" +
                    " "+"  // Method with parameters and return value\n" +
                    " "+"  public int add(int a, int b) {\n" +
                    " "+"      return a + b;\n" +
                    " "+"  }\n\n" +
                    " "+"  // Method with multiple parameters\n" +
                    " "+"  public String createGreeting(String name, int age) {\n" +
                    " "+"      return \"Hello \" + name + \", you are \" + age + \" years old.\";\n" +
                    " "+"  }\n\n" +
                    " "+"METHOD OVERLOADING:\n" +
                    " "+"  public int multiply(int a, int b) { return a * b; }\n" +
                    " "+"  public double multiply(double a, double b) { return a * b; }\n" +
                    " "+"  public int multiply(int a, int b, int c) { return a * b * c; }\n\n" +
                    " "+"RECURSION EXAMPLE:\n" +
                    " "+"  public int factorial(int n) {\n" +
                    " "+"      if (n <= 1) { return 1; }\n" +
                    " "+"      return n * factorial(n - 1);\n" +
                    " "+"  }\n\n" +
                    " "+"VARARGS (Variable Arguments):\n" +
                    " "+"  public int sum(int... numbers) {\n" +
                    " "+"      int total = 0;\n" +
                    " "+"      for (int num : numbers) { total += num; }\n" +
                    " "+"      return total;\n" +
                    " "+"  }\n\n" +
                    " "+"  // Usage:\n" +
                    " "+"  int result1 = sum(1, 2, 3); // 6\n" +
                    " "+"  int result2 = sum(1, 2, 3, 4, 5); // 15\n\n" +
                    " "+"DO's AND DON'Ts:\n" +
                    " "+" DO:\n" +
                    " "+"  ✔ Use descriptive names for methods.\n" +
                    " "+"     Example:\n" +
                    " "+"       public void calculateAverage() {}\n\n" +
                    " "+"  ✔ Always return a value if the method is not void.\n" +
                    " "+"     Example:\n" +
                    " "+"       public int getNumber() { return 5; }\n\n" +
                    " "+"  ✔ Keep methods focused on one task only.\n" +
                    " "+"     Example:\n" +
                    " "+"       public double computeArea(double radius) { return Math.PI * radius * radius; }\n\n" +
                    " "+"  ✔ Use method overloading for different parameter types.\n" +
                    " "+"     Example:\n" +
                    " "+"       public double sum(double a, double b) { return a + b; }\n\n" +
                    " "+" DON'T:\n" +
                    " "+"  ✘ Forget the return statement in a non-void method.\n" +
                    " "+"     Example:\n" +
                    " "+"       public int add(int a, int b) {\n" +
                    " "+"           // Missing 'return a + b;' causes compile error!\n" +
                    " "+"       }\n\n" +
                    " "+"  ✘ Call non-static methods from a static context.\n" +
                    " "+"     Example:\n" +
                    " "+"       public void greet() { System.out.println(\"Hi\"); }\n" +
                    " "+"       public static void main(String[] args) {\n" +
                    " "+"           greet(); // ERROR: Non-static method cannot be called from static context\n" +
                    " "+"       }\n\n" +
                    " "+"  ✘ Define two methods with the same name AND same parameters.\n" +
                    " "+"     Example:\n" +
                    " "+"       public void print() {}\n" +
                    " "+"       public void print() {} // ERROR: Duplicate method\n\n" +
                    " "+"  ✘ Cause infinite recursion (no base case).\n" +
                    " "+"     Example:\n" +
                    " "+"       public void recurse() { recurse(); } // ERROR: StackOverflowError\n\n" +
                    " "+"  ✘ Confuse method parameters with local variables.\n" +
                    " "+"     Example:\n" +
                    " "+"       public void show(int x) {\n" +
                    " "+"           int x = 5; // ERROR: Duplicate variable name\n" +
                    " "+"       }\n\n" +
                    " "+"REMEMBER:\n" +
                    " "+"  • Methods improve reusability and readability.\n" +
                    " "+"  • Keep them short, meaningful, and well-scoped.";
                tutorialContent.setText(content);
            }
        });
        btnExceptions.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String content = "EXCEPTION HANDLING IN JAVA\n" +
                    "============================\n\n" +
                    " "+"Exceptions are events that disrupt normal program flow.\n\n" +
                    " "+"TYPES OF EXCEPTIONS:\n" +
                    " "+"1. CHECKED EXCEPTIONS:    Must be handled (IOException, SQLException)\n" +
                    " "+"2. UNCHECKED EXCEPTIONS:  Runtime exceptions (NullPointerException, ArrayIndexOutOfBoundsException)\n" +
                    " "+"3. ERRORS:                Serious problems (OutOfMemoryError, StackOverflowError)\n\n" +
                    " "+"BASIC TRY-CATCH BLOCK:\n" +
                    " "+"  try {\n" +
                    " "+"      // Code that might throw an exception\n" +
                    " "+"      int result = 10 / 0;\n" +
                    " "+"  } catch (ArithmeticException e) {\n" +
                    " "+"      // Handle the exception\n" +
                    " "+"      System.out.println(\"Error: \" + e.getMessage());\n" +
                    " "+"  }\n\n" +
                    " "+"MULTIPLE CATCH BLOCKS:\n" +
                    " "+"  try {\n" +
                    " "+"      // Risky code\n" +
                    " "+"      int[] numbers = {1, 2, 3};\n" +
                    " "+"      int value = numbers[5];\n" +
                    " "+"  } catch (ArrayIndexOutOfBoundsException e) {\n" +
                    " "+"      System.out.println(\"Array index out of bounds: \" + e.getMessage());\n" +
                    " "+"  } catch (Exception e) {\n" +
                    " "+"      System.out.println(\"General error: \" + e.getMessage());\n" +
                    " "+"  }\n\n" +
                    " "+"FINALLY BLOCK:\n" +
                    " "+"  try {\n" +
                    " "+"      // Open file\n" +
                    " "+"      File file = new File(\"data.txt\");\n" +
                    " "+"      // Process file\n" +
                    " "+"  } catch (IOException e) {\n" +
                    " "+"      System.out.println(\"File error: \" + e.getMessage());\n" +
                    " "+"  } finally {\n" +
                    " "+"      // Always executed, even if exception occurs\n" +
                    " "+"      System.out.println(\"Cleaning up resources...\");\n" +
                    " "+"  }\n\n" +
                    " "+"THROWING EXCEPTIONS:\n" +
                    " "+"  public void checkAge(int age) throws IllegalArgumentException {\n" +
                    " "+"      if (age < 0) {\n" +
                    " "+"          throw new IllegalArgumentException(\"Age cannot be negative\");\n" +
                    " "+"      }\n" +
                    " "+"      System.out.println(\"Valid age: \" + age);\n" +
                    " "+"  }\n\n" +
                    " "+"CUSTOM EXCEPTION:\n" +
                    " "+"  public class InvalidInputException extends Exception {\n" +
                    " "+"      public InvalidInputException(String message) {\n" +
                    " "+"          super(message);\n" +
                    " "+"      }\n" +
                    " "+"  }\n\n" +
                    " "+"  // Usage:\n" +
                    " "+"  try {\n" +
                    " "+"      throw new InvalidInputException(\"Invalid user input\");\n" +
                    " "+"  } catch (InvalidInputException e) {\n" +
                    " "+"      System.out.println(\"Custom error: \" + e.getMessage());\n" +
                    " "+"  }\n\n" +
                    " "+"DO's AND DON'Ts:\n" +
                    " "+" DO:\n" +
                    " "+"  ✔ Use try-catch to prevent program crashes.\n" +
                    " "+"  ✔ Use finally blocks to release resources (e.g., files, DB connections).\n" +
                    " "+"  ✔ Catch specific exceptions first before general ones.\n" +
                    " "+"  ✔ Create custom exceptions only for meaningful, domain-specific cases.\n" +
                    " "+"  ✔ Log exceptions for debugging instead of silently ignoring them.\n\n" +
                    " "+" DON'T:\n" +
                    " "+"  ✘ Use empty catch blocks — you’ll lose valuable error info.\n" +
                    " "+"  ✘ Catch generic Exception when you can handle specific types.\n" +
                    " "+"  ✘ Throw exceptions for normal program logic (e.g., using try-catch to control loops).\n" +
                    " "+"  ✘ Forget to close resources (use finally or try-with-resources).\n" +
                    " "+"  ✘ Swallow exceptions without logging or rethrowing.\n\n" +
                    " "+"REMEMBER:\n" +
                    " "+"  • Exception handling keeps your program stable and user-friendly.\n" +
                    " "+"  • Always handle predictable errors gracefully.";
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

