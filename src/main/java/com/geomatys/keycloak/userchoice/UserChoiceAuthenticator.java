package com.geomatys.keycloak.userchoice;

import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.models.AuthenticatorConfigModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;

/**
 * Prompts the user to pick one answer to a customizable question. The list of valid
 * answers is read from a multivalued User Profile attribute on the authenticating user.
 * The chosen answer is stashed as a user-session note so that {@link UserChoiceProtocolMapper}
 * can later expose it as an OIDC token claim under the claim name configured here.
 */
public class UserChoiceAuthenticator implements Authenticator {

    static final Logger LOG = Logger.getLogger(UserChoiceAuthenticator.class.getName());

    static final String CONFIG_QUESTION = "question";
    static final String CONFIG_ANSWERS_ATTRIBUTE = "answersUserAttribute";
    static final String CONFIG_CLAIM_NAME = "claimName";

    static final String FORM_FIELD_ANSWER = "answer";

    /** User-session note holding the chosen answer, read by {@link UserChoiceProtocolMapper}. */
    public static final String NOTE_ANSWER = "user-choice.answer";
    /** User-session note holding the configured claim name, read by {@link UserChoiceProtocolMapper}. */
    public static final String NOTE_CLAIM_NAME = "user-choice.claim-name";

    static final UserChoiceAuthenticator SINGLETON = new UserChoiceAuthenticator();

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        Map<String, String> config = getConfig(context);
        String question = config.get(CONFIG_QUESTION);
        String answersAttribute = config.get(CONFIG_ANSWERS_ATTRIBUTE);

        List<String> answers = readAnswers(context.getUser(), answersAttribute);
        if (answers.isEmpty()) {
            LOG.warning("User Choice authenticator: user '" + context.getUser().getUsername()
                    + "' has no values for User Profile attribute '" + answersAttribute + "'");
            // Uses Keycloak's built-in error page (present in every theme's base ancestor)
            // rather than our own template, so a misconfiguration is reported cleanly even
            // if the 'user-choice' theme isn't set as the realm's login theme.
            context.failureChallenge(AuthenticationFlowError.INTERNAL_ERROR, context.form()
                    .setError("userChoiceNoAnswers")
                    .createErrorPage(Response.Status.INTERNAL_SERVER_ERROR));
            return;
        }

        context.challenge(context.form()
                .setAttribute("question", question)
                .setAttribute("answers", answers)
                .createForm("user-choice.ftl"));
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        Map<String, String> config = getConfig(context);
        String question = config.get(CONFIG_QUESTION);
        String answersAttribute = config.get(CONFIG_ANSWERS_ATTRIBUTE);
        String claimName = config.get(CONFIG_CLAIM_NAME);

        String submitted = context.getHttpRequest().getDecodedFormParameters().getFirst(FORM_FIELD_ANSWER);
        List<String> answers = readAnswers(context.getUser(), answersAttribute);

        if (submitted == null || !answers.contains(submitted)) {
            context.challenge(context.form()
                    .setError("userChoiceInvalidSelection")
                    .setAttribute("question", question)
                    .setAttribute("answers", answers)
                    .createForm("user-choice.ftl"));
            return;
        }

        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        authSession.setUserSessionNote(NOTE_ANSWER, submitted);
        authSession.setUserSessionNote(NOTE_CLAIM_NAME, claimName);
        context.success();
    }

    private static Map<String, String> getConfig(AuthenticationFlowContext context) {
        AuthenticatorConfigModel configModel = context.getAuthenticatorConfig();
        return configModel == null ? Map.of() : configModel.getConfig();
    }

    private static List<String> readAnswers(UserModel user, String answersAttribute) {
        if (answersAttribute == null || answersAttribute.isBlank()) {
            return List.of();
        }
        return user.getAttributeStream(answersAttribute).toList();
    }

    @Override
    public boolean requiresUser() {
        return true;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // The execution's AuthenticatorConfigModel isn't available in this hook, so we can't
        // know which attribute to check here. The "no answers configured" case is instead
        // handled gracefully in authenticate().
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // Nothing to provision; the answers attribute is expected to be populated ahead of time.
    }

    @Override
    public void close() {
        // Stateless singleton, nothing to release.
    }
}
