package com.codeloom.executor.languages

enum class LanguageSpec(
    val id: String,
    val image: String,
    val sourceFileName: String,
    val runSteps: List<String>,
) {
    JAVA(
        id = "java",
        image = "eclipse-temurin:21-jdk",
        sourceFileName = "Main.java",
        runSteps = listOf(
            "javac Main.java",
            "java Main < input.txt"
        )
    ),

    CPP(
        id = "cpp",
        image = "gcc:15",
        sourceFileName = "main.cpp",
        runSteps = listOf(
            "g++ -std=c++20 -O2 -o main main.cpp",
            "./main < input.txt"
        )
    ),

    PYTHON(
        id = "python",
        image = "python:3.14-slim",
        sourceFileName = "main.py",
        runSteps = listOf(
            "python3 main.py < input.txt"
        )
    );

    fun script(): String = runSteps.joinToString(" && ")

    companion object {
        private val byId = entries.associateBy(LanguageSpec::id)

        fun fromLanguage(language: String): LanguageSpec =
            byId[language.lowercase()]
                ?: throw IllegalArgumentException("Unknown language: $language")
    }
}