package com.codeloom.executor.dto

data class LanguageSpec(
    val image: String,
    val sourceFileName: String,
    val runSteps: List<String>
) {
    fun script() = runSteps.joinToString(" && ")

    companion object {
        private val javaSpec = LanguageSpec(
            image = "openjdk:21-jdk-slim",
            sourceFileName = "Main.java",
            runSteps = listOf("javac Main.java", "java Main < input.txt")
        )

        private val cppSpec = LanguageSpec(
            image = "gcc:13.2",
            sourceFileName = "main.cpp",
            runSteps = listOf("g++ -std=c++20 -O2 -o main main.cpp", "./main < input.txt")
        )

        private val pythonSpec = LanguageSpec(
            image = "python:3.14-slim",
            sourceFileName = "main.py",
            runSteps = listOf("python3 main.py < input.txt")
        )

        private val specs = mapOf(
            "java" to javaSpec,
            "cpp" to cppSpec,
            "python" to pythonSpec,
        )

        fun fromLanguage(language: String): LanguageSpec = specs[language]
            ?: throw IllegalArgumentException("Unknown language $language")
    }
}