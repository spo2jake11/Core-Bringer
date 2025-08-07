package com.altf4studios.corebringer.interpreter;

/**
 * Example class demonstrating the enhanced interpreter functionality
 * This shows how to create classes and main methods in the editor
 */
public class InterpreterExample {

    public static void main(String[] args) {
        // Example of how to use the enhanced interpreter
        CodeSimulator simulator = new CodeSimulator();

        // Example 1: Simple class creation
//        String simpleClassResult = simulator.createClass("Calculator",
//            "private int value;\n" +
//            "public Calculator() { this.value = 0; }\n" +
//            "public void add(int x) { this.value += x; }\n" +
//            "public int getValue() { return this.value; }"
//        );
//        System.out.println("Simple Class Result: " + simpleClassResult);
//
//        // Example 2: Main class creation
//        String mainClassResult = simulator.createMainClass("HelloWorld",
//            "System.out.println(\"Hello from the editor!\");\n" +
//            "for (int i = 1; i <= 5; i++) {\n" +
//            "    System.out.println(\"Count: \" + i);\n" +
//            "}"
//        );
//        System.out.println("Main Class Result: " + mainClassResult);
//
//        // Example 3: Utility class creation
//        String utilityClassResult = simulator.createUtilityClass("MathUtils",
//            "public static int add(int a, int b) { return a + b; }\n" +
//            "public static int multiply(int a, int b) { return a * b; }\n" +
//            "public static double power(double base, double exponent) {\n" +
//            "    return Math.pow(base, exponent);\n" +
//            "}"
//        );
//        System.out.println("Utility Class Result: " + utilityClassResult);

        // Example 4: Direct class compilation
        String directClassCode =
            "import java.util.*;\n\n" +
            "public class StringProcessor {\n" +
            "    public static String reverse(String input) {\n" +
            "        return new StringBuilder(input).reverse().toString();\n" +
            "    }\n" +
            "    public static void main(String[] args) {\n" +
            "        String test = \"Hello World\";\n" +
            "        System.out.println(\"Original: \" + test);\n" +
            "        System.out.println(\"Reversed: \" + reverse(test));\n" +
            "    }\n" +
            "}";

        String directResult = simulator.compileAndExecute(directClassCode);
        System.out.println("Direct Class Result: " + directResult);
    }
}
