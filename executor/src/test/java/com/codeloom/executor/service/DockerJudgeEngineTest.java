package com.codeloom.executor.service;

import static org.junit.jupiter.api.Assertions.*;

import com.codeloom.common.language.LanguageSpec;
import com.codeloom.executor.engine.SubmissionContext;
import com.codeloom.executor.model.TestCase;
import java.util.UUID;
import org.junit.jupiter.api.*;

class DockerJudgeEngineTest extends DockerTestBase {
    @AfterEach
    void removeVolume() {
        try {
            dockerClient.removeVolumeCmd("submission-" + submissionId).exec();
        } catch (Exception ignored) {
        }
    }

    SubmissionContext context(LanguageSpec l, String code) {
        return new SubmissionContext(submissionId, UUID.randomUUID(), 1, code, l, null, null);
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
        TestCase test = new TestCase(UUID.randomUUID(), 1, "2 3", "5", true);

        @Test
        void pythonReturnsOutput() {
            var c = context(LanguageSpec.PYTHON, "print(sum(map(int,input().split())),end='')");
            dockerJudgeEngine.compile(c);
            var r = dockerJudgeEngine.runTestCase(c, test);
            assertEquals(0, r.exitCode());
            assertEquals("5", r.stdout());
        }

        @Test
        void javaReturnsOutput() {
            var c = context(
                    LanguageSpec.JAVA,
                    "import java.util.*; public class Main {public static void main(String[]a){Scanner s=new Scanner(System.in);System.out.print(s.nextInt()+s.nextInt());}}");
            assertTrue(dockerJudgeEngine.compile(c).isSuccessful());
            assertEquals("5", dockerJudgeEngine.runTestCase(c, test).stdout());
        }

        @Test
        void cppReturnsOutput() {
            var c = context(
                    LanguageSpec.CPP, "#include <iostream>\nint main(){int a,b;std::cin>>a>>b;std::cout<<a+b;}");
            assertTrue(dockerJudgeEngine.compile(c).isSuccessful());
            assertEquals("5", dockerJudgeEngine.runTestCase(c, test).stdout());
        }
    }
}
