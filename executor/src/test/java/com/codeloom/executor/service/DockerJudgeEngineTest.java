package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.*;

import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.engine.SubmissionContext;
import com.codeloom.executor.model.TestCase;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DockerJudgeEngineTest extends DockerTestBase {
    @AfterEach
    void removeVolume() {
        dockerClient.removeVolumeCmd("submission-" + submissionId).exec();
    }

    SubmissionContext context(LanguageSpec language, String code) {
        return SubmissionContext.builder()
                .submissionId(submissionId)
                .userId(UUID.randomUUID())
                .problemId(1)
                .code(code)
                .language(language)
                .executionTimeLimitMs(null)
                .memoryUsageLimitBytes(null)
                .build();
    }

    @Nested
    class Compile {
        @Test
        void correctPythonNeedsNoCompilation() {
            var r = dockerJudgeEngine.compile(context(LanguageSpec.PYTHON, "print('Hello world')"));
            assertTrue(r.isSuccessful());
            assertEquals("", r.stderr());
        }

        @Test
        void correctJavaCompiles() {
            var r = dockerJudgeEngine.compile(context(
                    LanguageSpec.JAVA,
                    "public class Main { public static void main(String[] a){System.out.println(\"Hello\");}}"));
            assertTrue(r.isSuccessful());
        }

        @Test
        void incorrectJavaFails() {
            var r = dockerJudgeEngine.compile(context(LanguageSpec.JAVA, "public class Main { broken"));
            assertFalse(r.isSuccessful());
            assertNotEquals("", r.stderr());
        }

        @Test
        void correctCppCompiles() {
            var r = dockerJudgeEngine.compile(context(LanguageSpec.CPP, "#include <iostream>\nint main(){return 0;}"));
            assertTrue(r.isSuccessful());
        }
    }

    @Nested
    class Run {
        TestCase test = TestCase.builder()
                .id(UUID.randomUUID())
                .problemId(1)
                .input("2 3")
                .expectedOutput("5")
                .isPublic(true)
                .build();

        @Test
        void pythonReturnsOutput() {
            var context = context(LanguageSpec.PYTHON, "print(sum(map(int,input().split())),end='')");
            dockerJudgeEngine.compile(context);
            var result = dockerJudgeEngine.runTestCase(context, test);
            assertEquals(0, result.exitCode());
            assertEquals("5", result.stdout());
        }

        @Test
        void javaReturnsOutput() {
            var context = context(
                    LanguageSpec.JAVA,
                    "import java.util.*; public class Main {public static void main(String[]a){Scanner s=new Scanner(System.in);System.out.print(s.nextInt()+s.nextInt());}}");
            assertTrue(dockerJudgeEngine.compile(context).isSuccessful());
            assertEquals("5", dockerJudgeEngine.runTestCase(context, test).stdout());
        }

        @Test
        void cppReturnsOutput() {
            var context = context(
                    LanguageSpec.CPP, "#include <iostream>\nint main(){int a,b;std::cin>>a>>b;std::cout<<a+b;}");
            assertTrue(dockerJudgeEngine.compile(context).isSuccessful());
            assertEquals("5", dockerJudgeEngine.runTestCase(context, test).stdout());
        }
    }
}
