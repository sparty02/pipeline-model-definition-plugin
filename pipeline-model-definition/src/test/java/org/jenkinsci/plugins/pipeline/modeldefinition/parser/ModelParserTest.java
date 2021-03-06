package org.jenkinsci.plugins.pipeline.modeldefinition.parser;

import net.sf.json.JSONObject;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.jenkinsci.plugins.pipeline.modeldefinition.BaseParserLoaderTest;
import org.jenkinsci.plugins.pipeline.modeldefinition.Messages;
import org.jenkinsci.plugins.pipeline.modeldefinition.ast.ModelASTPipelineDef;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;

import static org.junit.Assert.*;

/**
 * @author Kohsuke Kawaguchi
 */
public class ModelParserTest extends BaseParserLoaderTest {

    @Test(expected = MultipleCompilationErrorsException.class)
    public void emptyStages() throws Exception {
        parse(getClass().getResource("/errors/emptyStages.groovy"));
    }

    /**
     * Look ma! THERE IS NO STACK TRACE!
     */
    @Test
    public void stageWithoutName() throws Exception {
        ErrorCollector ec = parseForError(getClass().getResource("/errors/stageWithoutName.groovy"));
        String msg = write(ec);
        System.out.println("----");
        System.out.println(msg);
        System.out.println("----");
        assertTrue(msg.contains(Messages.ModelParser_ExpectedStringLiteral()));
        assertFalse(msg.contains("Exception")); // we don't want stack trace please
    }

    @Issue("JENKINS-41118")
    @Test
    public void labelWithOptionsBecomesNode() throws Exception {
        ModelASTPipelineDef origRoot = Converter.urlToPipelineDef(getClass().getResource("/inRelativeCustomWorkspace.groovy"));

        assertNotNull(origRoot);

        JSONObject origJson = origRoot.toJSON();
        assertNotNull(origJson);

        JSONParser jp = new JSONParser(Converter.jsonTreeFromJSONObject(origJson));
        ModelASTPipelineDef newRoot = jp.parse();

        assertEquals(getJSONErrorReport(jp, "inRelativeCustomWorkspace"), 0, jp.getErrorCollector().getErrorCount());
        assertNotNull("Pipeline null for inRelativeCustomWorkspace", newRoot);

        JSONObject nodeJson = JSONObject.fromObject(fileContentsFromResources("json/inRelativeCustomWorkspace.json"));

        JSONParser nodeParser = new JSONParser(Converter.jsonTreeFromJSONObject(nodeJson));
        ModelASTPipelineDef nodeRoot = nodeParser.parse();

        assertEquals(getJSONErrorReport(nodeParser, "inRelativeCustomWorkspace"),
                0, nodeParser.getErrorCollector().getErrorCount());
        assertNotNull("Pipeline null for inRelativeCustomWorkspace", nodeRoot);

        assertEquals(nodeRoot, newRoot);
    }

}
