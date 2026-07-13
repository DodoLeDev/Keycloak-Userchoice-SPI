package com.geomatys.keycloak.userchoice;

import java.util.ArrayList;
import java.util.List;
import org.keycloak.models.ClientSessionContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.ProtocolMapperModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.protocol.oidc.mappers.AbstractOIDCProtocolMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAccessTokenMapper;
import org.keycloak.protocol.oidc.mappers.OIDCAttributeMapperHelper;
import org.keycloak.protocol.oidc.mappers.OIDCIDTokenMapper;
import org.keycloak.protocol.oidc.mappers.UserInfoTokenMapper;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.representations.IDToken;

/**
 * Reads the answer chosen through {@link UserChoiceAuthenticator} and the claim name
 * configured on that authenticator step (both stashed as user-session notes) and injects
 * them into the OIDC tokens as a dynamically-named claim. Unlike most mappers, the claim
 * name is not configured here: it lives on the "User Choice" authenticator config so it
 * only needs to be defined in one place.
 */
public class UserChoiceProtocolMapper extends AbstractOIDCProtocolMapper
        implements OIDCAccessTokenMapper, OIDCIDTokenMapper, UserInfoTokenMapper {

    public static final String PROVIDER_ID = "user-choice-claim-mapper";

    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = new ArrayList<>();

    static {
        OIDCAttributeMapperHelper.addIncludeInTokensConfig(CONFIG_PROPERTIES, UserChoiceProtocolMapper.class);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public String getDisplayType() {
        return "User Choice Claim";
    }

    @Override
    public String getDisplayCategory() {
        return TOKEN_MAPPER_CATEGORY;
    }

    @Override
    public String getHelpText() {
        return "Adds the user's answer, chosen through the 'User Choice' authenticator step, "
                + "to the token under the claim name configured on that authenticator.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    protected void setClaim(IDToken token, ProtocolMapperModel mappingModel, UserSessionModel userSession,
            KeycloakSession keycloakSession, ClientSessionContext clientSessionCtx) {
        String claimName = userSession.getNote(UserChoiceAuthenticator.NOTE_CLAIM_NAME);
        String answer = userSession.getNote(UserChoiceAuthenticator.NOTE_ANSWER);
        if (claimName != null && !claimName.isBlank() && answer != null) {
            token.getOtherClaims().put(claimName, answer);
        }
    }
}
