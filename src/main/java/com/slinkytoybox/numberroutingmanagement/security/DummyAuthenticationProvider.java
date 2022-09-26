package com.slinkytoybox.numberroutingmanagement.security;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.*;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Michael Junek (michael@juneks.com.au)
 */
@Slf4j
public class DummyAuthenticationProvider implements AuthenticationProvider {

    public DummyAuthenticationProvider() {
        super();
    }

    @Override
    public Authentication authenticate(final Authentication authentication) {
        final String logPrefix = "authenticate() - ";
        log.trace("{}Entering method", logPrefix);

        final String name = authentication.getName();
        final String password = authentication.getCredentials().toString();
        log.info("{}Authenticating user {}", logPrefix, name);
        
        log.info("{}User authenticated successfully", logPrefix);

        log.debug("{}Determining granted authorities", logPrefix);
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_DUMMY"));

        log.debug("{}Creating authentication token", logPrefix);
        UserDetails principal = new User(name, password, authorities);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, password, authorities);
        log.debug("{}Returning token: {}", logPrefix, auth);
        return auth;

    }

    @Override
    public boolean supports(Class<?> type) {
        return type.equals(UsernamePasswordAuthenticationToken.class);
    }

}
