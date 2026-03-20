package org.treesitter;

import org.junit.jupiter.api.Test;
import org.treesitter.tests.CorpusTest;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
        for (TSQueryCapture capture : match.getCaptures()) {
            String captureName = query.getCaptureNameForId(capture.getIndex());
            if ("macro.derive".equals(captureName)) {
                foundMacroDerive = true;
                TSNode node = capture.getNode();
                String matchedText = source.substring(node.getStartByte(), node.getEndByte());
                assertTrue(matchedText.contains("#[derive(Is)]"), 
                    "Matched text should contain the attribute. Found: " + matchedText);
            }
        }
        assertTrue(foundMacroDerive, "Should have found @macro.derive capture");
    }
}
