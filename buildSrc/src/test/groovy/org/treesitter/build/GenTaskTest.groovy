package org.treesitter.build

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import static org.junit.jupiter.api.Assertions.*

class GenTaskTest {

    @TempDir
    File tempDir

    @Test
    void "should generate java file with TSLanguage extension and copy method"() {
        // Arrange
        String libName = "json"
        
        // Act
        GenTask.genJavaFile(tempDir, libName)
        
        // Assert
        File javaFile = new File(tempDir, "src/main/java/org/treesitter/TreeSitterJson.java")
        assertTrue(javaFile.exists(), "Java file should be generated")
        
        String content = javaFile.text
        assertTrue(content.contains("public class TreeSitterJson extends TSLanguage"), "Should extend TSLanguage")
        assertTrue(content.contains("NativeUtils.loadLib(\"lib/tree-sitter-json\")"), "Should load correct library")
        assertTrue(content.contains("@Override"), "Should have Override annotation")
        assertTrue(content.contains("public TSLanguage copy()"), "Should implement copy method")
        assertTrue(content.contains("return new TreeSitterJson(copyPtr())"), "Should call copyPtr")
    }

    @Test
    void "should generate java test file with CorpusTest call"() {
        // Arrange
        String libName = "json"
        
        // Act
        GenTask.genJavaTestFile(tempDir, libName)
        
        // Assert
        File testFile = new File(tempDir, "src/test/java/org/treesitter/TreeSitterJsonTest.java")
        assertTrue(testFile.exists(), "Test file should be generated")
        
        String content = testFile.text
        assertTrue(content.contains("import org.treesitter.tests.CorpusTest;"), "Should import CorpusTest")
        assertTrue(content.contains("CorpusTest.runAllTestsInDefaultFolder(new TreeSitterJson(), \"json\");"), "Should call runAllTestsInDefaultFolder")
    }

    @Test
    void "should generate build gradle file with downloadSource task"() {
        // Arrange
        String libName = "json"
        String url = "https://example.com/tree-sitter-json.zip"
        
        // Act
        GenTask.genBuildGradle(tempDir, libName, url)
        
        // Assert
        File gradleFile = new File(tempDir, "build.gradle")
        assertTrue(gradleFile.exists(), "build.gradle should be generated")
        
        String content = gradleFile.text
        assertTrue(content.contains("tasks.named('downloadSource')"), "Should configure downloadSource task")
        assertTrue(content.contains("url = \"$url\""), "Should contain the correct URL")
    }

    @Test
    void "should generate properties file with version"() {
        // Arrange
        String version = "0.20.0"
        
        // Act
        GenTask.genProperties(tempDir, version)
        
        // Assert
        File propsFile = new File(tempDir, "gradle.properties")
        assertTrue(propsFile.exists(), "gradle.properties should be generated")
        assertEquals("libVersion=0.20.0", propsFile.text.trim())
    }

    @Test
    void "should generate JNI C file with correct method mapping"() {
        // Arrange
        String libName = "html" // 'html' -> 'tree_sitter_html'
        
        // Act
        GenTask.genJniCFile(tempDir, libName)
        
        // Assert
        File cFile = new File(tempDir, "src/main/c/org_treesitter_TreeSitterHtml.c")
        assertTrue(cFile.exists(), "C file should be generated")
        
        String content = cFile.text
        assertTrue(content.contains("Java_org_treesitter_TreeSitterHtml_tree_1sitter_1html"), "Should handle underscore escaping for JNI")
        assertTrue(content.contains("return (jlong) tree_sitter_html();"), "Should call native symbol")
    }
}
