# Enhanced Interpreter User Guide

## How to Use the Enhanced Interpreter in Core-Bringer

This guide will walk you through how to use the simplified interpreter features in the game's editor.

---

## Getting Started

### Step 1: Access the Editor
1. Start the Core-Bringer game
2. Navigate to the main menu
3. Start a new game or load an existing save
4. In the game, find and click on the **Editor** button or access the editor stage
5. You'll see the editor interface with a code input area and two buttons

### Step 2: Understanding the Interface
The editor has two main buttons:
- **Run Code**: For executing code snippets, variables, and expressions
- **Run Class**: For compiling and executing complete classes with main methods

---

## Method 1: Running Code Snippets

### What it's for:
- Quick calculations
- Testing simple logic
- Learning Java basics
- Experimenting with variables and expressions
- Testing individual methods and functions

### How to use:
1. **Type your code snippet** directly in the text area
2. **Click "Run Code"** to execute
3. **Check the console output** for results

### Examples:

**Basic Math:**
```java
int x = 10;
int y = 5;
int result = x + y;
System.out.println("Result: " + result);
```

**String Operations:**
```java
String name = "Player";
String greeting = "Hello, " + name + "!";
System.out.println(greeting);
```

**Arrays:**
```java
int[] numbers = {1, 2, 3, 4, 5};
int sum = 0;
for (int num : numbers) {
    sum += num;
}
System.out.println("Sum: " + sum);
```

---

## Method 2: Running Complete Classes

### What it's for:
- Creating reusable code structures
- Implementing algorithms
- Building utility functions
- Learning object-oriented programming
- Running complete programs with main methods

### How to use:
1. **Write your complete class** directly in the text area
2. **Include the main method** if you want it to execute automatically
3. **Click "Run Class"** to compile and execute
4. **Check the console output** for results

### Example 1: Simple Class (Calculator)
```java
public class Calculator {
    private int value;
    
    public Calculator() {
        this.value = 0;
    }
    
    public void add(int x) {
        this.value += x;
    }
    
    public int getValue() {
        return this.value;
    }
    
    public static void main(String[] args) {
        Calculator calc = new Calculator();
        calc.add(10);
        calc.add(5);
        System.out.println("Result: " + calc.getValue());
    }
}
```

### Example 2: Main Class (HelloWorld)
```java
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from the editor!");
        for (int i = 1; i <= 5; i++) {
            System.out.println("Count: " + i);
        }
    }
}
```

### Example 3: Utility Class (MathUtils)
```java
public class MathUtils {
    public static int add(int a, int b) {
        return a + b;
    }
    
    public static int multiply(int a, int b) {
        return a * b;
    }
    
    public static double power(double base, double exponent) {
        return Math.pow(base, exponent);
    }
    
    public static void main(String[] args) {
        System.out.println("Add: " + add(5, 3));
        System.out.println("Multiply: " + multiply(4, 6));
        System.out.println("Power: " + power(2, 8));
    }
}
```

---

## Method 3: Advanced Code Writing

### What it's for:
- Advanced users who want full control
- Complex class structures
- Multiple classes in one file
- Custom imports and dependencies
- Testing complex algorithms

### How to use:
1. **Write complete code** directly in the text area
2. **Click "Run Code"** for snippets or "Run Class" for complete classes
3. **Check console output** for results

### Example: Complete Class with Main Method
```java
import java.util.*;

public class StringProcessor {
    public static String reverse(String input) {
        return new StringBuilder(input).reverse().toString();
    }
    
    public static void main(String[] args) {
        String test = "Hello World";
        System.out.println("Original: " + test);
        System.out.println("Reversed: " + reverse(test));
        
        // Test with different strings
        String[] words = {"Java", "Programming", "Game"};
        for (String word : words) {
            System.out.println(word + " -> " + reverse(word));
        }
    }
}
```

### Example: Complex Algorithm
```java
import java.util.*;

public class SortingDemo {
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (arr[j] > arr[j+1]) {
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
    }
    
    public static void main(String[] args) {
        int[] numbers = {64, 34, 25, 12, 22, 11, 90};
        System.out.println("Original array: " + Arrays.toString(numbers));
        
        bubbleSort(numbers);
        System.out.println("Sorted array: " + Arrays.toString(numbers));
    }
}
```

---

## Learning Path for Beginners

### Level 1: Basic Snippets
Start with simple code snippets to learn Java basics:

**Variables and Output:**
```java
String playerName = "Hero";
int playerLevel = 5;
double playerHealth = 100.0;
System.out.println("Player: " + playerName);
System.out.println("Level: " + playerLevel);
System.out.println("Health: " + playerHealth);
```

**Conditional Statements:**
```java
int playerHealth = 75;
if (playerHealth > 50) {
    System.out.println("Player is healthy!");
} else if (playerHealth > 25) {
    System.out.println("Player is wounded!");
} else {
    System.out.println("Player is critical!");
}
```

### Level 2: Simple Classes
Move to creating basic classes:

**Player Class:**
```java
public class Player {
    private String name;
    private int level;
    private double health;
    
    public Player(String name) {
        this.name = name;
        this.level = 1;
        this.health = 100.0;
    }
    
    public void levelUp() {
        this.level++;
        this.health += 20;
        System.out.println(name + " leveled up to " + level);
    }
    
    public void takeDamage(double damage) {
        this.health -= damage;
        if (this.health < 0) this.health = 0;
        System.out.println(name + " took " + damage + " damage. Health: " + health);
    }
    
    public String getName() { return name; }
    public int getLevel() { return level; }
    public double getHealth() { return health; }
    
    public static void main(String[] args) {
        Player player = new Player("Hero");
        System.out.println("Player: " + player.getName());
        System.out.println("Level: " + player.getLevel());
        System.out.println("Health: " + player.getHealth());
        
        player.levelUp();
        player.takeDamage(15);
    }
}
```

### Level 3: Utility Classes
Create reusable utility functions:

**GameUtils Class:**
```java
public class GameUtils {
    public static int rollDice(int sides) {
        return (int)(Math.random() * sides) + 1;
    }
    
    public static boolean isCriticalHit(double chance) {
        return Math.random() < chance;
    }
    
    public static double calculateDamage(int baseDamage, double multiplier) {
        return baseDamage * multiplier;
    }
    
    public static String formatTime(int seconds) {
        int minutes = seconds / 60;
        int remainingSeconds = seconds % 60;
        return String.format("%02d:%02d", minutes, remainingSeconds);
    }
    
    public static void main(String[] args) {
        System.out.println("Dice roll (6 sides): " + rollDice(6));
        System.out.println("Critical hit (10% chance): " + isCriticalHit(0.1));
        System.out.println("Damage (20 base, 1.5x): " + calculateDamage(20, 1.5));
        System.out.println("Time format (125 seconds): " + formatTime(125));
    }
}
```

### Level 4: Complex Programs
Create complete programs with multiple features:

**Game Simulator:**
```java
import java.util.*;

public class GameSimulator {
    private static int playerHealth = 100;
    private static int playerLevel = 1;
    private static int experience = 0;
    
    public static void main(String[] args) {
        System.out.println("=== Game Simulator ===");
        
        // Simulate battles
        for (int battle = 1; battle <= 5; battle++) {
            System.out.println("\n--- Battle " + battle + " ---");
            simulateBattle();
        }
        
        System.out.println("\n=== Final Stats ===");
        System.out.println("Level: " + playerLevel);
        System.out.println("Experience: " + experience);
        System.out.println("Health: " + playerHealth);
    }
    
    private static void simulateBattle() {
        int enemyDamage = (int)(Math.random() * 30) + 10;
        int playerDamage = (int)(Math.random() * 25) + 15;
        
        System.out.println("Enemy attacks for " + enemyDamage + " damage!");
        playerHealth -= enemyDamage;
        
        System.out.println("Player attacks for " + playerDamage + " damage!");
        
        // Gain experience
        experience += 20;
        if (experience >= 100) {
            playerLevel++;
            experience -= 100;
            System.out.println("Level up! Now level " + playerLevel);
        }
        
        System.out.println("Player health: " + playerHealth);
    }
}
```

---

## Common Patterns and Examples

### 1. Data Structures
```java
// ArrayList operations
import java.util.*;

public class DataStructureDemo {
    public static void main(String[] args) {
        ArrayList<String> items = new ArrayList<>();
        items.add("Sword");
        items.add("Shield");
        items.add("Potion");
        
        System.out.println("Inventory:");
        for (String item : items) {
            System.out.println("- " + item);
        }
        
        // HashMap for stats
        HashMap<String, Integer> stats = new HashMap<>();
        stats.put("Strength", 15);
        stats.put("Dexterity", 12);
        stats.put("Intelligence", 10);
        
        System.out.println("\nCharacter Stats:");
        for (Map.Entry<String, Integer> entry : stats.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }
}
```

### 2. Algorithms
```java
public class AlgorithmDemo {
    public static void main(String[] args) {
        int[] numbers = {64, 34, 25, 12, 22, 11, 90};
        
        System.out.println("Original array:");
        printArray(numbers);
        
        bubbleSort(numbers);
        
        System.out.println("Sorted array:");
        printArray(numbers);
    }
    
    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        for (int i = 0; i < n-1; i++) {
            for (int j = 0; j < n-i-1; j++) {
                if (arr[j] > arr[j+1]) {
                    int temp = arr[j];
                    arr[j] = arr[j+1];
                    arr[j+1] = temp;
                }
            }
        }
    }
    
    public static void printArray(int[] arr) {
        for (int num : arr) {
            System.out.print(num + " ");
        }
        System.out.println();
    }
}
```

### 3. Game Logic
```java
public class CombatSystem {
    public static void main(String[] args) {
        int playerAttack = 25;
        int enemyDefense = 15;
        int playerHealth = 100;
        int enemyHealth = 80;
        
        System.out.println("=== Combat Start ===");
        System.out.println("Player Health: " + playerHealth);
        System.out.println("Enemy Health: " + enemyHealth);
        
        while (playerHealth > 0 && enemyHealth > 0) {
            // Player attacks
            int damage = Math.max(0, playerAttack - enemyDefense);
            enemyHealth -= damage;
            System.out.println("Player deals " + damage + " damage!");
            
            if (enemyHealth <= 0) {
                System.out.println("Enemy defeated!");
                break;
            }
            
            // Enemy attacks
            int enemyDamage = (int)(Math.random() * 20) + 10;
            playerHealth -= enemyDamage;
            System.out.println("Enemy deals " + enemyDamage + " damage!");
            
            System.out.println("Player: " + playerHealth + " HP, Enemy: " + enemyHealth + " HP");
        }
        
        if (playerHealth <= 0) {
            System.out.println("Player defeated!");
        }
    }
}
```

---

## Editor Integration

The enhanced interpreter is integrated into the `EditorStageUI` with two main buttons:

1. **Run Code**: Executes code snippets, variables, and expressions
2. **Run Class**: Compiles and executes complete class definitions

### Simple Interface
The interface provides:
- Large text area for direct code input
- Clear examples in the placeholder text
- Two straightforward buttons for different execution modes
- Immediate feedback through console output

### Quick Start
1. **Type your code** directly in the text area
2. **Choose the right button**:
   - Use "Run Code" for snippets and expressions
   - Use "Run Class" for complete classes with main methods
3. **Check the console** for results and error messages

---

## Troubleshooting

### Common Errors and Solutions

**1. "Validation Error: Forbidden keyword detected"**
- **Problem**: Using restricted keywords like `System.exit`, `Runtime`, etc.
- **Solution**: Remove the forbidden keywords and use alternative approaches

**2. "Compilation Error: Cannot find symbol"**
- **Problem**: Using classes or methods that aren't available
- **Solution**: Stick to standard Java libraries and basic operations

**3. "Restricted keyword detected (backend access)"**
- **Problem**: Trying to access game backend classes
- **Solution**: Don't use `com.altf4studios.corebringer` or game-specific class names

**4. "Timeout: Execution took too long"**
- **Problem**: Code is running too long (infinite loop or heavy computation)
- **Solution**: Add proper loop conditions and limit iterations

### Best Practices

1. **Start Simple**: Begin with basic snippets before complex classes
2. **Test Incrementally**: Add features one at a time
3. **Use Meaningful Names**: Choose descriptive class and variable names
4. **Handle Errors**: Use try-catch blocks for robust code
5. **Keep Classes Focused**: Each class should have a single responsibility
6. **Document Your Code**: Add comments to explain complex logic

---

## Advanced Tips

### 1. Code Organization
- Use consistent indentation
- Group related methods together
- Add comments for complex logic
- Use meaningful variable names

### 2. Performance
- Avoid infinite loops
- Use efficient algorithms
- Limit array sizes for large operations
- Use StringBuilder for string concatenation in loops

### 3. Debugging
- Add print statements to track execution
- Test with small inputs first
- Check console output for error messages
- Use the "Run" button for quick tests

### 4. Learning Resources
- Practice with simple mathematical problems
- Implement classic algorithms (sorting, searching)
- Create game-related simulations
- Experiment with different data structures

---

## What You Can't Do

For security reasons, the interpreter restricts:
- Access to game backend classes
- File system operations
- Network connections
- System-level operations
- Reflection or dynamic code generation
- Inheritance from existing classes

This ensures the game remains secure while still providing a powerful learning and experimentation environment.

---

## Getting Help

If you encounter issues:
1. Check the console output for error messages
2. Verify your code syntax
3. Test with simpler examples first
4. Review the security restrictions
5. Use the examples in this guide as templates

The enhanced interpreter is designed to be both powerful and safe, allowing you to learn Java programming while playing the game! 