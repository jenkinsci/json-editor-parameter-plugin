import hudson.util.FormValidation;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;

/**
 * Interface for validating JSON editor parameter fields in Jenkins.
 * Methods are explicitly defined for use in Jelly UI validation.
 */
public interface DescriptorChecks {
    
    /**
     * Validates the provided name parameter.
     *
     * @param name the name of the parameter
     * @return FormValidation result
     */
    FormValidation doCheckName(@QueryParameter @Nonnull String name);

    /**
     * Validates the provided JSON options.
     *
     * @param options the JSON options string
     * @return FormValidation result
     */
    FormValidation doCheckOptions(@QueryParameter @Nonnull String options);

    /**
     * Validates the JSON schema.
     *
     * @param schema the JSON schema string
     * @return FormValidation result
     */
    FormValidation doCheckSchema(@QueryParameter @Nonnull String schema);

    /**
     * Validates the start value parameter.
     *
     * @param startval the start value
     * @return FormValidation result
     */
    FormValidation doCheckStartval(@QueryParameter @Nonnull String startval);
}
