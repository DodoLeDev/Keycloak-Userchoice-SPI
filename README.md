### NOTE: This plugin has been vibe-coded with CLAUDE Code in order to quickly have a MVP.

**I am not a Keycloak extension developer, so I can't help you much with this project. Still, because it helped me and may help others, I still release it in the open.**

**THIS PROJECT COMES WITHOUT ANY WARRANTY. USE IT AT YOUR OWN RISK**

# Keycloak-Userchoice-SPI

A Keycloak extension providing a way to prompt users to make a choice when authenticating.

It adds a **"User Choice"** execution that can be placed in any Authentication Flow. When
reached during login, it asks the user to pick one answer to a configurable question; the
possible answers come from a multivalued User Profile attribute on that user. The chosen
answer is exposed as a claim in the OpenID Connect tokens issued to the client.

<img width="500" alt="image" src="https://github.com/user-attachments/assets/c04cd4ec-30cf-402c-bd8a-8bc973aacd57" />


## How it works

- **User Choice** (Authentication → Authenticator, `org.keycloak.authentication.Authenticator`):
  renders the question and a radio-button list built from the configured User Profile
  attribute, validates the submission server-side against that same attribute (never trusts
  the posted value), and stashes the chosen answer as a user-session note.
- **User Choice Claim** (OIDC Protocol Mapper): reads that session note — plus the claim name
  configured on the authenticator step — and injects it into the ID token / access token /
  userinfo response.

Splitting the work this way keeps the claim name defined in exactly one place (the
authenticator's config) while still going through Keycloak's normal Protocol Mapper
mechanism to reach the token.

## Configuration

Add a **User Choice** execution to an Authentication Flow and set:

| Field | Description |
|---|---|
| Question | The question displayed to the user. |
| Answers User Profile Attribute | Picked from a live dropdown of the realm's User Profile attributes; must be a multivalued attribute holding the possible answers for each user. |
| Token Claim Name | Name of the claim added to the OIDC tokens with the user's chosen answer. |

If a user has no values set for the selected attribute, the flow stops with a clean,
localized Keycloak error page (message key `userChoiceNoAnswers`) instead of a generic
internal error.

Then, in the Client Scope (or Client) that should carry the claim, add a mapper of type
**User Choice Claim** and enable "Add to ID token" / "Add to access token" / "Add to userinfo"
as needed. This only needs to be done once per client scope — it works for any question
text or claim name configured on the authenticator.

No realm theme change is required. The login template and its message bundle are shipped as
**theme resources** (`theme-resources/templates/`, `theme-resources/messages/` on the
classpath) rather than as a whole new named theme. Keycloak's `ExtendingTheme` merges
theme-resource-provided templates/messages into *whatever* login theme is already active
(`keycloak.v2` by default) as a fallback, so the realm can stay on its current theme
untouched. (An earlier version of this extension shipped a dedicated `user-choice` theme
requiring a Login Theme change — that approach still works but is unnecessary complexity
now that theme resources handle it.)

While iterating, re-run `kc.sh build` after replacing the jar (production Quarkus builds
bake `providers/` in at build time), and consider disabling theme caching to avoid stale
templates: `start-dev` mode disables it by default; in other modes pass
`--spi-theme-static-max-age=-1 --spi-theme-cache-themes=false --spi-theme-cache-templates=false`.

## Building

Requires Java 17+ and Maven.

```shell
mvn package
```

This produces `target/keycloak-userchoice-spi-1.0.0-SNAPSHOT.jar`.

## Deploying

Copy the built jar into Keycloak's `providers/` directory and rebuild:

```shell
cp target/keycloak-userchoice-spi-1.0.0-SNAPSHOT.jar $KEYCLOAK_HOME/providers/
$KEYCLOAK_HOME/bin/kc.sh build
```

Restart Keycloak, then configure the flow, mapper, and theme as described above.
