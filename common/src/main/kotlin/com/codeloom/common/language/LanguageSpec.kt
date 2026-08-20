package com.codeloom.common.language

enum class LanguageSpec(
    val id: String,
    val image: String,
    val sourceFileName: String,
    val compileCommand: String? = null,
    val runCommand: String,
) {
    JAVA(
        id = "java",
        image = "eclipse-temurin:21-jdk",
        sourceFileName = "Main.java",
        compileCommand = "javac Main.java",
        runCommand = "java Main < input.txt",
    ),

    CPP(
        id = "cpp",
        image = "gcc:15",
        sourceFileName = "main.cpp",
        compileCommand = "g++ -std=c++20 -O2 -o main main.cpp",
        runCommand = "./main < input.txt",
    ),

    PYTHON(
        id = "python",
        image = "python:3.14-slim",
        sourceFileName = "main.py",
        compileCommand = null,
        runCommand = "python3 main.py < input.txt",
    ),
    ;

    companion object {
        private val byId = entries.associateBy(LanguageSpec::id)

        fun fromLanguage(language: String): LanguageSpec =
            byId[language.lowercase()]
                ?: throw InvalidLanguageException(language)
    }
}
