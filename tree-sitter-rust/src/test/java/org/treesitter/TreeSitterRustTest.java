package org.treesitter;

import org.junit.jupiter.api.Test;
import org.treesitter.tests.CorpusTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeSitterRustTest {
    @Test
    void corpusTest() throws IOException {
        CorpusTest.runAllTestsInDefaultFolder(new TreeSitterRust(), "rust");
    }

    @Test
    void testIntegrationQuery() {
        TSParser parser = new TSParser();
        parser.setLanguage(new TreeSitterRust());
        String source = "#[derive(Is)] \n" +
                "pub enum Status {\n" +
                "  Running,\n" +
                "  Stopped,\n" +
                "  Initial,\n" +
                "}";
        TSTree tree = parser.parseString(null, source);
        TSNode rootNode = tree.getRootNode();

        String queryString = "(attribute_item\n" +
                "  (attribute\n" +
                "    (identifier) @_derive\n" +
                "    (#eq? @_derive \"derive\")\n" +
                "    (token_tree\n" +
                "      (identifier) @macro.derive.name\n" +
                "    )\n" +
                "  )\n" +
                ") @macro.derive";

        TSQuery query = new TSQuery(parser.getLanguage(), queryString);
        TSQueryCursor cursor = new TSQueryCursor();
        cursor.exec(query, rootNode, source);

        TSQueryMatch match = new TSQueryMatch();
        assertTrue(cursor.nextMatch(match), "Query should have matched");

        boolean foundMacroDerive = false;
        boolean foundDeriveName = false;
        for (TSQueryCapture capture : match.getCaptures()) {
            String captureName = query.getCaptureNameForId(capture.getIndex());
            TSNode node = capture.getNode();
            String matchedText = source.substring(node.getStartByte(), node.getEndByte());

            if ("macro.derive".equals(captureName)) {
                foundMacroDerive = true;
                assertTrue(matchedText.contains("#[derive(Is)]"),
                    "Matched text should contain the attribute. Found: " + matchedText);
            } else if ("macro.derive.name".equals(captureName)) {
                foundDeriveName = true;
                assertEquals("Is", matchedText, "macro.derive.name should match 'Is'");
            }
        }
        assertTrue(foundMacroDerive, "Should have found @macro.derive capture");
        assertTrue(foundDeriveName, "Should have found @macro.derive.name capture");
    }

    @Test
    void testIntegrationQueryNotEqShouldNotMatch() {
        TSParser parser = new TSParser();
        parser.setLanguage(new TreeSitterRust());

        String source = "#[derive(Other)] \n" +
                "pub enum Status {\n" +
                "  Running,\n" +
                "  Stopped,\n" +
                "  Initial,\n" +
                "}";

        TSTree tree = parser.parseString(null, source);
        TSNode rootNode = tree.getRootNode();

        String queryString = "(attribute_item\n" +
                "  (attribute\n" +
                "    (identifier) @_derive\n" +
                "    (#not-eq? @_derive \"derive\")\n" +
                "    (token_tree\n" +
                "      (identifier) @macro.derive.name\n" +
                "    )\n" +
                "  )\n" +
                ") @macro.derive";

        TSQuery query = new TSQuery(parser.getLanguage(), queryString);
        TSQueryCursor cursor = new TSQueryCursor();
        cursor.exec(query, rootNode, source);

        TSQueryMatch match = new TSQueryMatch();
        assertFalse(cursor.nextMatch(match), "Query should not match because #not-eq? predicate fails");
    }

    @Test
    void testIntegrationQueryEqShouldNotMatch() {
        TSParser parser = new TSParser();
        parser.setLanguage(new TreeSitterRust());

        String source = "#[foo(Other)] \n" +
                "pub enum Status {\n" +
                "  Running,\n" +
                "  Stopped,\n" +
                "  Initial,\n" +
                "}";

        TSTree tree = parser.parseString(null, source);
        TSNode rootNode = tree.getRootNode();

        String queryString = "(attribute_item\n" +
                "  (attribute\n" +
                "    (identifier) @_derive\n" +
                "    (#eq? @_derive \"derive\")\n" +
                "    (token_tree\n" +
                "      (identifier) @macro.derive.name\n" +
                "    )\n" +
                "  )\n" +
                ") @macro.derive";

        TSQuery query = new TSQuery(parser.getLanguage(), queryString);
        TSQueryCursor cursor = new TSQueryCursor();
        cursor.exec(query, rootNode, source);

        TSQueryMatch match = new TSQueryMatch();
        assertFalse(cursor.nextMatch(match), "Query should not match because #eq? predicate fails");
    }


    @Test
    void testIntegrationQueryNotEqShouldMatch() {
        TSParser parser = new TSParser();
        parser.setLanguage(new TreeSitterRust());

        String source = "#[is_macro::Is]\n" +
                "pub enum Status {\n" +
                "  Running,\n" +
                "  Stopped,\n" +
                "  Initial,\n" +
                "}";

        TSTree tree = parser.parseString(null, source);
        TSNode rootNode = tree.getRootNode();

        String queryString = "(attribute_item\n" +
                "  (attribute\n" +
                "    [\n" +
                "      ((identifier) @macro.attribute.name\n" +
                "        (#not-eq? @macro.attribute.name \"derive\"))\n" +
                "      (scoped_identifier) @macro.attribute.name\n" +
                "    ]\n" +
                "  )\n" +
                ") @macro.attribute";

        TSQuery query = new TSQuery(parser.getLanguage(), queryString);
        TSQueryCursor cursor = new TSQueryCursor();
        cursor.exec(query, rootNode, source);

        TSQueryMatch match = new TSQueryMatch();
        assertTrue(cursor.nextMatch(match), "Query should have matched is_macro::Is");

        boolean foundAttribute = false;
        boolean foundName = false;
        for (TSQueryCapture capture : match.getCaptures()) {
            String captureName = query.getCaptureNameForId(capture.getIndex());
            TSNode node = capture.getNode();
            String matchedText = source.substring(node.getStartByte(), node.getEndByte());

            if ("macro.attribute".equals(captureName)) {
                foundAttribute = true;
                assertTrue(matchedText.contains("#[is_macro::Is]"));
            } else if ("macro.attribute.name".equals(captureName)) {
                foundName = true;
                assertEquals("is_macro::Is", matchedText);
            }
        }
        assertTrue(foundAttribute, "Should have found @macro.attribute capture");
        assertTrue(foundName, "Should have found @macro.attribute.name capture");
    }
}
