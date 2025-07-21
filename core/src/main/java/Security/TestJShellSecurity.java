package Security;

public class TestJShellSecurity {
    public static void main(String[] args) {
        String[] testCodes = {
            "System.exit(0);",           // Should be blocked
            "while(true) {}",            // Should be blocked
            "while ( true ) {}",         // Should be blocked
            "while(1==1) {}",            // Should be blocked
            "for(;;) {}",                // Should be blocked
            "for(;true;) {}",            // Should be blocked
            "int x = 5;",                // Should be allowed
            "System.out.println(\"Hello\");" // Should be allowed
        };

        for (String code : testCodes) {
            String result = JShellSecurity.checkSafety(code);
            if (result != null) {
                System.out.println("Blocked: " + code + " | Reason: " + result);
            } else {
                System.out.println("Allowed: " + code);
            }
        }
    }
}
