package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.codeloom.common.language.LanguageSpec;
import org.junit.jupiter.api.Test;

class LanguageSpecTest {
    @Test
    void usesDedicatedImageForCompilation() {
        var language = new LanguageSpec(
                "Java",
                "eclipse-temurin:21-jre",
                "eclipse-temurin:21-jdk",
                "Main.java",
                "javac Main.java",
                "java Main");

        assertEquals("eclipse-temurin:21-jdk", language.image(true));
        assertEquals("eclipse-temurin:21-jre", language.image(false));
    }

    @Test
    void compilationFallsBackToRunImage() {
        var language = new LanguageSpec("Python", "python:3.14-slim", null, "main.py", null, "python3 main.py");

        assertEquals("python:3.14-slim", language.compileImage());
        assertEquals("python:3.14-slim", language.image(true));
        assertEquals("python:3.14-slim", language.image(false));
    }
}
