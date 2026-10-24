package io.jenkins.plugins.json_editor_parameter;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

@WithJenkins
class JsonEditorParameterDefinitionTest {
    private static final String FIRST_PARAM = "first";
    private static final String STATIC = "{\"key\":\"static\"}";

    @Test
    void configSimple(JenkinsRule j) throws Exception {
        URL url = getClass().getResource("car.json");
        assertNotNull(url);

        JsonEditorParameterDefinition def = new JsonEditorParameterDefinition(FIRST_PARAM);
        def.setSchema(Files.readString(Paths.get(url.toURI())));
        def.setStartvalUrl("https://example.com/config.json");

        final FreeStyleProject p = j.createFreeStyleProject();
        p.addProperty(new ParametersDefinitionProperty(def));
        j.configRoundtrip(p);
    }

    @Test
    void startvalUrlFetchesAndFallsBack(JenkinsRule j) throws Exception {
        var server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/ok", ex -> respond(ex, 200, "{\"key\":\"fresh\"}"));
        server.createContext("/404", ex -> respond(ex, 404, ""));
        server.createContext("/bad", ex -> respond(ex, 200, "<html>"));
        server.start();
        var base = "http://localhost:" + server.getAddress().getPort();

        var def = new JsonEditorParameterDefinition(FIRST_PARAM);
        def.setSchema("{\"type\":\"object\",\"properties\":{}}");
        def.setStartval(STATIC);

        def.setStartvalUrl(base + "/ok");
        assertTrue(def.getMergedOptions().contains("fresh"), "should use URL data");

        def.setStartvalUrl(base + "/404");
        assertTrue(def.getMergedOptions().contains("static"), "should fall back on 404");

        def.setStartvalUrl(base + "/bad");
        assertTrue(def.getMergedOptions().contains("static"), "should fall back on invalid JSON");

        def.setStartvalUrl("http://localhost:1/x");
        assertTrue(def.getMergedOptions().contains("static"), "should fall back on unreachable host");

        server.stop(0);
    }

    private static void respond(HttpExchange ex, int code, String body) throws IOException {
        var bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }
}
