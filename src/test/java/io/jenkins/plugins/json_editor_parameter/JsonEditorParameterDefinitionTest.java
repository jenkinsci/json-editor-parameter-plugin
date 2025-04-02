package io.jenkins.plugins.json_editor_parameter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JsonEditorParameterDefinitionTest {
    private static final String FIRST_PARAM = "first";

    @Test
    void configSimple(JenkinsRule j) throws Exception {
        URL url = getClass().getResource("car.json");
        assertNotNull(url);

        JsonEditorParameterDefinition def = new JsonEditorParameterDefinition(FIRST_PARAM);
        def.setSchema(Files.readString(Paths.get(url.toURI())));

        final FreeStyleProject p = j.createFreeStyleProject();
        p.addProperty(new ParametersDefinitionProperty(def));
        j.configRoundtrip(p);
    }
}
