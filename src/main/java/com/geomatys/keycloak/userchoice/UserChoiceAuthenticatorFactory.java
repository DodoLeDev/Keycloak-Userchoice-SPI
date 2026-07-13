package com.geomatys.keycloak.userchoice;

import java.util.List;
import org.keycloak.Config;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.AuthenticatorFactory;
import org.keycloak.models.AuthenticationExecutionModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;

public class UserChoiceAuthenticatorFactory implements AuthenticatorFactory {

    public static final String PROVIDER_ID = "user-choice-authenticator";

    private static final AuthenticationExecutionModel.Requirement[] REQUIREMENT_CHOICES = {
            AuthenticationExecutionModel.Requirement.REQUIRED,
            AuthenticationExecutionModel.Requirement.ALTERNATIVE,
            AuthenticationExecutionModel.Requirement.DISABLED
    };

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = ProviderConfigurationBuilder.create()
            .property()
            .name(UserChoiceAuthenticator.CONFIG_QUESTION)
            .label("Question")
            .helpText("The question displayed to the user during authentication.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .property()
            .name(UserChoiceAuthenticator.CONFIG_ANSWERS_ATTRIBUTE)
            .label("Answers User Profile Attribute")
            .helpText("The multivalued User Profile attribute holding the possible answers "
                    + "for this user.")
            .type(ProviderConfigProperty.USER_PROFILE_ATTRIBUTE_LIST_TYPE)
            .add()
            .property()
            .name(UserChoiceAuthenticator.CONFIG_CLAIM_NAME)
            .label("Token Claim Name")
            .helpText("Name of the claim added to the OpenID Connect tokens, containing the "
                    + "answer chosen by the user. Requires the companion 'User Choice Claim' "
                    + "protocol mapper to be added to the relevant Client Scope.")
            .type(ProviderConfigProperty.STRING_TYPE)
            .add()
            .build();

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "User Choice";
    }

    @Override
    public String getReferenceCategory() {
        return "user-choice";
    }

    @Override
    public boolean isConfigurable() {
        return true;
    }

    @Override
    public AuthenticationExecutionModel.Requirement[] getRequirementChoices() {
        return REQUIREMENT_CHOICES;
    }

    @Override
    public boolean isUserSetupAllowed() {
        return false;
    }

    @Override
    public String getHelpText() {
        return "Prompts the user to pick an answer to a customizable question, with the "
                + "possible choices sourced from a User Profile multivalued attribute, and "
                + "exposes the chosen answer as an OpenID Connect token claim.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public Authenticator create(KeycloakSession session) {
        return UserChoiceAuthenticator.SINGLETON;
    }

    @Override
    public void init(Config.Scope config) {
        // No provider-wide configuration.
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        // Nothing to do.
    }

    @Override
    public void close() {
        // Stateless factory, nothing to release.
    }
}
