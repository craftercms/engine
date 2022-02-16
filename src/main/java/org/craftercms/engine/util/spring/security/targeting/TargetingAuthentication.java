/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.craftercms.engine.util.spring.security.targeting;

import org.craftercms.profile.api.Profile;
import org.craftercms.security.authentication.Authentication;

/**
 * Implementation of {@link org.craftercms.security.authentication.Authentication} used for standalone authentication.
 *
 * @author avasquez
 */
public class TargetingAuthentication implements Authentication {

    protected Profile profile;

    public TargetingAuthentication(Profile profile) {
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
