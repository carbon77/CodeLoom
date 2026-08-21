package com.codeloom.common.language;

import java.util.Arrays;

public enum LanguageSpec {
    JAVA("java", "eclipse-temurin:21-jdk", "Main.java", "javac Main.java", "java Main < input.txt"),
    CPP("cpp", "gcc:15", "main.cpp", "g++ -std=c++20 -O2 -o main main.cpp", "./main < input.txt"),
    PYTHON("python", "python:3.14-slim", "main.py", null, "python3 main.py < input.txt");

    private final String id;
    private final String image;
    private final String sourceFileName;
    private final String compileCommand;
    private final String runCommand;

    LanguageSpec(String id, String image, String sourceFileName, String compileCommand, String runCommand) {
        this.id = id;
        this.image = image;
        this.sourceFileName = sourceFileName;
        this.compileCommand = compileCommand;
        this.runCommand = runCommand;
    }

    public String getId() {
        return id;
    }

    public String getImage() {
        return image;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getCompileCommand() {
        return compileCommand;
    }

    public String getRunCommand() {
        return runCommand;
    }

    public static LanguageSpec fromLanguage(String language) {
        return Arrays.stream(values())
                .filter(v -> v.id.equalsIgnoreCase(language))
                .findFirst()
                .orElseThrow(() -> new InvalidLanguageException(language));
    }
}
