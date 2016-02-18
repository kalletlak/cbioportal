/*
 * Copyright (c) 2015 Memorial Sloan-Kettering Cancer Center.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF MERCHANTABILITY OR FITNESS
 * FOR A PARTICULAR PURPOSE. The software and documentation provided hereunder
 * is on an "as is" basis, and Memorial Sloan-Kettering Cancer Center has no
 * obligations to provide maintenance, support, updates, enhancements or
 * modifications. In no event shall Memorial Sloan-Kettering Cancer Center be
 * liable to any party for direct, indirect, special, incidental or
 * consequential damages, including lost profits, arising out of the use of this
 * software and its documentation, even if Memorial Sloan-Kettering Cancer
 * Center has been advised of the possibility of such damage.
 */

/*
 * This file is part of cBioPortal.
 *
 * cBioPortal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.mskcc.cbio.portal.authentication.guestuser;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mskcc.cbio.portal.authentication.PortalUserDetails;
import org.mskcc.cbio.portal.dao.PortalUserDAO;
import org.mskcc.cbio.portal.model.User;
import org.mskcc.cbio.portal.model.UserAuthorities;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 
 * Custom Authentication provider to authenticate guest user
 * 
 * @author kalletlak
 *
 */
public class GuestUserAuthenticationProvider implements AuthenticationProvider {

    private static final Log log = LogFactory.getLog(GuestUserAuthenticationProvider.class);

    private final PortalUserDAO portalUserDAO;

    /**
     * Constructor.
     *
     * Takes a ref to PortalUserDAO used to authenticate registered
     * users in the database.
     *
     * @param portalUserDAO PortalUserDAO
     */
    public GuestUserAuthenticationProvider(PortalUserDAO portalUserDAO) {
    	super();
        this.portalUserDAO = portalUserDAO;
    }
    // API

    @Override
    public Authentication authenticate(final Authentication authentication) throws AuthenticationException {
    	PortalUserDetails toReturn = null;
    	 List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        String name = authentication.getName();
        if (log.isDebugEnabled()) {
            log.debug("username : " + name);
        }
        final String password = authentication.getCredentials().toString();
        if (log.isDebugEnabled()) {
            log.debug("password : " + password);
        }
        if (name.equals("") && password.equals("")) {
        	name = "guest_user";
        	try {
                if (log.isDebugEnabled()) {
                    log.debug("loadUserDetails(), attempting to fetch portal user, email: " + name);
                }
                User user = portalUserDAO.getPortalUser(name);
                if (user != null && user.isEnabled()) {
                    if (log.isDebugEnabled()) {
                        log.debug("loadUserDetails(), attempting to fetch portal user authorities, email: " + name);
                    }
                    UserAuthorities authorities = portalUserDAO.getPortalUserAuthorities(name);
                    if (authorities != null) {
                        grantedAuthorities =
                            AuthorityUtils.createAuthorityList(authorities.getAuthorities().toArray(new String[authorities.getAuthorities().size()]));
                        toReturn = new PortalUserDetails(name, grantedAuthorities);
                        toReturn.setEmail(name);
                        toReturn.setName(name);
                    }
                }
    		}
    		catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage());
                }
                else {
                    e.printStackTrace();
                }
    		}
            final UserDetails principal = toReturn;
            final Authentication auth = new UsernamePasswordAuthenticationToken(principal, password, grantedAuthorities);
            return auth;
        } else {
            return null;
        }
    }

    @Override
    public boolean supports(final Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }

}
