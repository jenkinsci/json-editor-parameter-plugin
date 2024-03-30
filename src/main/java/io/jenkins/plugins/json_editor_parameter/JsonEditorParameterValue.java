package io.jenkins.plugins.json_editor_parameter;

import hudson.EnvVars;
import hudson.model.ParameterValue;
import hudson.model.Run;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.kohsuke.stapler.DataBoundConstructor;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JsonEditorParameterValue extends ParameterValue {

    private final String json;

    @DataBoundConstructor
    public JsonEditorParameterValue(String name, String value) {
        super(name);
        this.json = value;
    }

    public JsonEditorParameterValue(String name, String value, String description) {
        super(name, description);
        this.json = value;
    }

    @Override
    public void buildEnvironment(Run<?, ?> build, EnvVars env) {
        env.put(name, json);
        env.put(name.toUpperCase(Locale.ENGLISH), json); // backward compatibility pre 1.345
    }

    @Override
    public Object getValue() {
        return JsonUtil.toObject(json);
    }
}
