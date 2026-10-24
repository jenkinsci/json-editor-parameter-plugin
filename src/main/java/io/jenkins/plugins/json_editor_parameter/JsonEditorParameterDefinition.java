package io.jenkins.plugins.json_editor_parameter;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.Extension;
import hudson.Util;
import hudson.model.ParameterValue;
import hudson.model.SimpleParameterDefinition;
import hudson.util.FormValidation;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import jenkins.model.Jenkins;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;
import org.jenkinsci.Symbol;
import org.kohsuke.accmod.Restricted;
import org.kohsuke.accmod.restrictions.DoNotUse;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest2;
import org.kohsuke.stapler.verb.POST;

@EqualsAndHashCode(callSuper = true)
@Getter
public class JsonEditorParameterDefinition extends SimpleParameterDefinition {

    private static final Logger LOGGER = Logger.getLogger(JsonEditorParameterDefinition.class.getName());
    private static final Pattern OK_NAME = Pattern.compile("[A-Za-z][\\w-]{0,63}");
    private static final int URL_TIMEOUT = 10_000;

    private String schema;
    private String startval;
    private String startvalUrl;
    private String options = "{}";

    @DataBoundConstructor
    public JsonEditorParameterDefinition(String name) {
        super(name);
        if (!isValidName(name)) {
            throw new IllegalArgumentException("Invalid Name - " + name);
        }
    }

    private static void checkValidJson(String json, String errorMessage) {
        try {
            JSONSerializer.toJSON(json);
        } catch (JSONException e) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private static boolean isValidName(String name) {
        return OK_NAME.matcher(name).matches();
    }

    @DataBoundSetter
    public void setSchema(String schema) {
        checkValidJson(schema, "schema must be valid json");
        this.schema = schema;
    }

    @DataBoundSetter
    public void setStartval(String startval) {
        startval = Util.fixEmptyAndTrim(startval);
        if (startval != null) {
            checkValidJson(startval, "startval must be valid json or empty.");
        }
        this.startval = startval;
    }

    @DataBoundSetter
    public void setStartvalUrl(String startvalUrl) {
        this.startvalUrl = Util.fixEmptyAndTrim(startvalUrl);
    }

    @DataBoundSetter
    public void setOptions(String options) {
        checkValidJson(options, "options must be valid json");
        this.options = options;
    }

    @Restricted(DoNotUse.class) // invoked from index.jelly
    public String getMergedOptions() {
        Map<String, Object> optionMap = new HashMap<>(JsonUtil.toMap(options));
        String resolved = resolveStartval();
        if (resolved != null) {
            optionMap.put("startval", JsonUtil.toObject(resolved));
        }
        optionMap.put("schema", JsonUtil.toMap(schema));
        return JsonUtil.toJson(optionMap);
    }

    private String resolveStartval() {
        if (startvalUrl == null) {
            return startval;
        }
        try {
            var client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofMillis(URL_TIMEOUT))
                    .build();
            var request = HttpRequest.newBuilder(URI.create(startvalUrl))
                    .timeout(Duration.ofMillis(URL_TIMEOUT))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new IllegalStateException("HTTP " + response.statusCode());
            }
            JSONSerializer.toJSON(response.body()); // validate parseable
            return response.body();
        } catch (Exception e) {
            LOGGER.warning("startvalUrl fetch failed, falling back to static startval: " + e.getMessage());
            return startval;
        }
    }

    @Override
    public JsonEditorParameterDefinition copyWithDefaultValue(ParameterValue defaultValue) {
        if (defaultValue instanceof JsonEditorParameterValue) {
            JsonEditorParameterDefinition def = new JsonEditorParameterDefinition(getName());
            def.setDescription(getDescription());
            def.setStartval(((JsonEditorParameterValue) defaultValue).getJson());
            def.setStartvalUrl(startvalUrl);
            return def;
        } else {
            return this;
        }
    }

    @Override
    public JsonEditorParameterValue createValue(StaplerRequest2 request, JSONObject jo) {
        JsonEditorParameterValue value = request.bindJSON(JsonEditorParameterValue.class, jo);
        value.setDescription(getDescription());
        return value;
    }

    public JsonEditorParameterValue createValue(String json) {
        return new JsonEditorParameterValue(getName(), json, getDescription());
    }

    @Override
    public JsonEditorParameterValue getDefaultParameterValue() {
        String resolved = resolveStartval();
        if (resolved == null) {
            return null;
        }
        return new JsonEditorParameterValue(getName(), resolved, getDescription());
    }

    @Extension
    @Symbol({"jsonEditor"})
    public static class DescriptorImpl extends ParameterDescriptor implements DescriptorChecks {

        private static FormValidation isValidJson(String options, String errorMessage) {
            try {
                checkValidJson(options, errorMessage);
                return FormValidation.ok();
            } catch (IllegalArgumentException e) {
                return FormValidation.error(errorMessage);
            }
        }

        @Override
        @POST
        public FormValidation doCheckName(@QueryParameter String name) {
            Jenkins.get().checkPermission(Jenkins.READ);
            return isValidName(name)
                    ? FormValidation.ok()
                    : FormValidation.error("Name should match regular expression [A-Za-z][\\w-]{0,63}");
        }

        @Override
        @POST
        public FormValidation doCheckOptions(@QueryParameter String options) {
            Jenkins.get().checkPermission(Jenkins.READ);
            return isValidJson(options, "options must be valid json");
        }

        @Override
        @POST
        public FormValidation doCheckSchema(@QueryParameter String schema) {
            Jenkins.get().checkPermission(Jenkins.READ);
            return isValidJson(schema, "schema must be valid json");
        }

        @Override
        @POST
        public FormValidation doCheckStartval(@QueryParameter String startval) {
            Jenkins.get().checkPermission(Jenkins.READ);
            if (Util.fixEmptyAndTrim(startval) == null) {
                return FormValidation.ok();
            }
            return isValidJson(startval, "startval must be valid json or empty");
        }

        @Override
        @POST
        public FormValidation doCheckStartvalUrl(@QueryParameter String startvalUrl) {
            Jenkins.get().checkPermission(Jenkins.READ);
            if (Util.fixEmptyAndTrim(startvalUrl) == null) {
                return FormValidation.ok();
            }
            try {
                URI.create(startvalUrl.trim()).toURL();
                return FormValidation.ok();
            } catch (Exception e) {
                return FormValidation.error("startvalUrl must be a valid URL or empty");
            }
        }

        @Override
        @NonNull
        public String getDisplayName() {
            return "Json Editor Parameter Definition";
        }
    }
}
