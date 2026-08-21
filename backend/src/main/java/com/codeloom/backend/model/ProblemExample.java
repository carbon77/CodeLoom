package com.codeloom.backend.model;

public record ProblemExample(String input, String output, String explanation) {
    public ProblemExample(String input, String output) {
        this(input, output, null);
    }
}
