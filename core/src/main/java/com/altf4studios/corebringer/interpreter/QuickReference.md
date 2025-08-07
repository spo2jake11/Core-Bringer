# Enhanced Interpreter Quick Reference

## 🎮 How to Access
1. Start Core-Bringer game
2. Navigate to Editor stage
3. Use the two buttons: **Run Code**, **Run Class**

---

## ⚡ Quick Start Examples

### Basic Snippet (Use "Run Code" button)
```java
int x = 10;
int y = 5;
System.out.println("Sum: " + (x + y));
```

### Simple Class (Use "Run Class" button)
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

### Main Class (Use "Run Class" button)
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

### Complete Class (Use "Run Class" button)
```java
public class StringReverser {
    public static void main(String[] args) {
        String text = "Hello World";
        String reversed = new StringBuilder(text).reverse().toString();
        System.out.println("Original: " + text);
        System.out.println("Reversed: " + reversed);
    }
}
```

---

## 🎯 Two Ways to Code

| Method | Button | Use For | Example |
|--------|--------|---------|---------|
| **Snippets** | Run Code | Quick tests, calculations, variables | `int x = 5; System.out.println(x);` |
| **Classes** | Run Class | Complete classes, main methods, algorithms | `public class MyClass { ... }` |

---

## 🚫 What's Not Allowed

### Forbidden Keywords
- `System.exit`
- `Runtime`
- `Thread`
- `File`
- `Process`
- `reflect`
- `exec`
- `getRuntime`

### Restricted Access
- `com.altf4studios.corebringer.*`
- `Main`, `GameScreen`, `EditorStageUI`
- `extends`, `implements`
- `while(true)`, `for(;;)`

---

## 📚 Learning Path

### Level 1: Variables & Output
```java
String name = "Hero";
int level = 5;
double health = 100.0;
System.out.println("Name: " + name);
System.out.println("Level: " + level);
```

### Level 2: Conditionals
```java
int health = 75;
if (health > 50) {
    System.out.println("Healthy!");
} else {
    System.out.println("Wounded!");
}
```

### Level 3: Loops
```java
for (int i = 1; i <= 5; i++) {
    System.out.println("Count: " + i);
}
```

### Level 4: Arrays
```java
int[] numbers = {1, 2, 3, 4, 5};
int sum = 0;
for (int num : numbers) {
    sum += num;
}
System.out.println("Sum: " + sum);
```

### Level 5: Classes
```java
public class Player {
    private String name;
    private int level;
    
    public Player(String name) { 
        this.name = name; 
        this.level = 1; 
    }
    
    public void levelUp() { 
        this.level++; 
    }
    
    public int getLevel() { 
        return level; 
    }
    
    public static void main(String[] args) {
        Player player = new Player("Hero");
        System.out.println("Level: " + player.getLevel());
        player.levelUp();
        System.out.println("New Level: " + player.getLevel());
    }
}
```

---

## 🔧 Common Patterns

### ArrayList Operations
```java
import java.util.*;
ArrayList<String> items = new ArrayList<>();
items.add("Sword");
items.add("Shield");
for (String item : items) {
    System.out.println("- " + item);
}
```

### HashMap for Stats
```java
import java.util.*;
HashMap<String, Integer> stats = new HashMap<>();
stats.put("Strength", 15);
stats.put("Dexterity", 12);
for (Map.Entry<String, Integer> entry : stats.entrySet()) {
    System.out.println(entry.getKey() + ": " + entry.getValue());
}
```

### Random Numbers
```java
int randomDamage = (int)(Math.random() * 20) + 10;
boolean critical = Math.random() < 0.1;
```

---

## ⚠️ Troubleshooting

| Error | Cause | Solution |
|-------|-------|----------|
| "Forbidden keyword" | Using restricted keywords | Remove System.exit, Runtime, etc. |
| "Cannot find symbol" | Using unavailable classes | Stick to standard Java libraries |
| "Backend access" | Using game classes | Don't use com.altf4studios.corebringer |
| "Timeout" | Infinite loop | Add proper loop conditions |

---

## 💡 Pro Tips

1. **Start Simple**: Begin with snippets before classes
2. **Test Often**: Use "Run" button for quick tests
3. **Check Console**: All output appears in console
4. **Use Meaningful Names**: `playerHealth` not `x`
5. **Add Comments**: `// Calculate damage` for clarity
6. **Handle Errors**: Use try-catch for robust code

---

## 🎮 Game-Related Examples

### Combat Calculator
```java
public class CombatCalc {
    public static void main(String[] args) {
        int playerAttack = 25;
        int enemyDefense = 15;
        int damage = Math.max(0, playerAttack - enemyDefense);
        System.out.println("Damage dealt: " + damage);
    }
}
```

### Inventory System
```java
import java.util.*;
public class Inventory {
    public static void main(String[] args) {
        ArrayList<String> items = new ArrayList<>();
        items.add("Sword");
        items.add("Shield");
        items.add("Potion");
        
        System.out.println("Inventory:");
        for (String item : items) {
            System.out.println("- " + item);
        }
    }
}
```

### Character Stats
```java
public class Character {
    public static void main(String[] args) {
        String name = "Hero";
        int level = 5;
        double health = 100.0;
        double mana = 50.0;
        
        System.out.println("=== Character Stats ===");
        System.out.println("Name: " + name);
        System.out.println("Level: " + level);
        System.out.println("Health: " + health);
        System.out.println("Mana: " + mana);
    }
}
```

---

## 🆘 Need Help?

1. Check console output for error messages
2. Start with simple examples
3. Test one feature at a time
4. Use the examples above as templates
5. Remember: No backend access allowed

**Happy Coding! 🎮💻** 