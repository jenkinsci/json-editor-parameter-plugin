package io.jenkins.plugins.json_editor_parameter;

import hudson.EnvVars;
import hudson.model.ParameterValue;
import hudson.model.Run;
import java.util.Locale;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.kohsuke.stapler.DataBoundConstructor;
import edu.umd.cs.findbugs.annotations.NonNull;

@Getter
@EqualsAndHashCode(callSuper = true)
public class JsonEditorParameterValue extends ParameterValue {

    private static final long serialVersionUID = 1L;
    private final String json;

    @DataBoundConstructor
    public JsonEditorParameterValue(@NonNull String name, @NonNull String value) {
        super(name);
        this.json = value;
    }

    public JsonEditorParameterValue(@NonNull String name, @NonNull String value, String description) {
        super(name, description);
        this.json = value;
    }

    @Override
    public void buildEnvironment(@NonNull Run<?, ?> build, @NonNull EnvVars env) {
        if (json != null && !json.isEmpty()) {
            env.put(name, json);
            env.put(name.toUpperCase(Locale.ENGLISH), json); // Backward compatibility pre 1.345
        }
    }

    @Override
    public Object getValue() {
        try {
            return JsonUtil.toObject(json);
        } catch (Exception e) {
            return null; // Handle invalid JSON gracefully
        }
    }
}
