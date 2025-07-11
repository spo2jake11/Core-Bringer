package com.altf4studios.corebringer.interpreter;

public class CodeSimulator {
    private final JShellExecutor executor;
    private final Validator validator;

    public CodeSimulator() {
        this.executor = new JShellExecutor();
        this.validator = new Validator();
    }

    /**
     * Simulates code execution: validates, then executes if valid.
     * @param code User-submitted code
     * @return Output or error message
     */
    public String simulate(String code) {
        if (!validator.isValid(code)) {
            return "Validation Error: " + validator.getLastError();
        }
        return executor.submitCode(code);
    }

    public void reset() {
        executor.resetSession();
    }
}
