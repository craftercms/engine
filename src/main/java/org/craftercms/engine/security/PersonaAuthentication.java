package org.craftercms.engine.security;

import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.Authentication;

/**
 * Implementation of {@link org.craftercms.security.authentication.Authentication} used just for Crafter Studio's
 * Personas.
 *
 * @author avasquez
 */
public class PersonaAuthentication implements Authentication {

    protected Profile profile;

    public PersonaAuthentication(Profile profile) {
        this.profile = profile;
    }

    @Override
    public String getTicket() {
        return null;
    }

    @Override
    public Profile getProfile() {
        return profile;
    }

    @Override
    public boolean isRemembered() {
        return false;
    }

}
