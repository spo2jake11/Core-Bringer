# Enhanced Interpreter System

The enhanced interpreter system allows users to create and execute Java classes and main methods within the editor, while maintaining security restrictions to prevent access to backend game logic.

## Features

### 1. Class Creation
- **Simple Classes**: Create basic classes with fields and methods
- **Main Classes**: Create classes with main methods for execution
- **Utility Classes**: Create classes with static utility methods

### 2. Security Restrictions
- Prevents access to backend game classes (`com.altf4studios.corebringer.*`)
- Blocks dangerous operations (System.exit, Runtime, etc.)
- Restricts class inheritance and implementation
- Prevents infinite loops and resource exhaustion

### 3. Editor Integration
- **Run Button**: Executes code snippets
- **Run Class Button**: Compiles and executes complete classes
- **Create Class Button**: Opens dialog for guided class creation

## Usage Examples

### Simple Class Creation
```java
// In the editor, use the "Create Class" button or write directly:
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
}
```

### Main Class Creation
```java
// Create a class with a main method:
public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hello from the editor!");
        for (int i = 1; i <= 5; i++) {
            System.out.println("Count: " + i);
        }
    }
}
```

### Utility Class Creation
```java
// Create a utility class with static methods:
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
}
```

## Security Features

### Forbidden Keywords
- `System.exit`
- `Runtime`
- `Thread`
- `File`
- `Process`
- `reflect`
- `exec`
- `getRuntime`
- `new ProcessBuilder`
- `SecurityManager`
- `AccessController`
- `PrivilegedAction`
- `doPrivileged`

### Restricted Keywords (Backend Access)
- `com.altf4studios.corebringer`
- `Main`
- `GameScreen`
- `EditorStageUI`

### Forbidden Patterns
- Infinite loops: `while(true)`, `for(;;)`
- Backend imports: `import com.altf4studios.corebringer.*`
- Class inheritance: `extends`, `implements`

## API Reference

### CodeSimulator
```java
// Execute code snippets
String result = codeSimulator.simulate(code);

// Compile and execute classes
String result = codeSimulator.compileAndExecute(code);

// Create simple class
String result = codeSimulator.createClass(className, classContent);

// Create main class
String result = codeSimulator.createMainClass(className, mainMethodContent);

// Create utility class
String result = codeSimulator.createUtilityClass(className, methods);
```

### CodeBuilder
```java
CodeBuilder builder = new CodeBuilder();

// Add imports
builder.addImport("import java.util.*;");

// Add classes
builder.addClass("MyClass", "private int value; public int getValue() { return value; }");

// Add main class
builder.addMainClass("MyMain", "System.out.println(\"Hello World\");");

// Add utility class
builder.addUtilityClass("Utils", "public static void helper() { }");

// Build final code
String code = builder.build();
```

### ClassManager
```java
ClassManager manager = ClassManager.getInstance();

// Compile classes
CompilationResult result = manager.compileClasses(code);

// Execute main method
ExecutionResult result = manager.executeMainMethod(className);

// Execute with timeout
ExecutionResult result = manager.executeWithTimeout(methodCall, timeoutMinutes);
```

## Editor Integration

The enhanced interpreter is integrated into the `EditorStageUI` with three main buttons:

1. **Run**: Executes code snippets (original functionality)
2. **Run Class**: Compiles and executes complete class definitions
3. **Create Class**: Opens a dialog for guided class creation

### Class Creation Dialog
The dialog provides:
- Class name input
- Class type selection (Simple, Main, Utility)
- Content text area
- Create/Cancel buttons

## Error Handling

The system provides detailed error messages for:
- Validation errors (forbidden keywords/patterns)
- Compilation errors (syntax, missing imports)
- Runtime errors (exceptions during execution)
- Timeout errors (execution taking too long)

## Best Practices

1. **Use meaningful class names**: Avoid reserved names like `Main`, `GameScreen`
2. **Keep classes simple**: Focus on utility functions and data structures
3. **Test incrementally**: Start with simple classes before complex ones
4. **Handle exceptions**: Use try-catch blocks for robust code
5. **Use static methods**: For utility classes, prefer static methods

## Limitations

- No access to backend game classes or systems
- No file I/O operations
- No network operations
- No reflection or dynamic code generation
- No inheritance or interface implementation
- Limited to standard Java libraries (with restrictions) 