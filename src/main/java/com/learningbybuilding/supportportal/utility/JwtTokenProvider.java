package com.learningbybuilding.supportportal.utility;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.learningbybuilding.supportportal.constant.SecurityConstant;
import com.learningbybuilding.supportportal.domain.UserPrincipal;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.learningbybuilding.supportportal.constant.SecurityConstant.*;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    public String generateToken(UserPrincipal userPrincipal) {
        Algorithm algorithm = Algorithm.HMAC512(secret.getBytes());

        String[] authorities = getAuthorities(userPrincipal.getAuthorities());

        return JWT.create()
                .withAudience(GET_ARRAYS_ADMINISTRATION)
                .withIssuer(GET_ARRAYS_LLC)
                .withIssuedAt(new Date())
                .withSubject(userPrincipal.getUsername())
                .withArrayClaim(AUTHORITIES, authorities)
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .sign(algorithm);
    }

    public List<GrantedAuthority> getAuthorities(String token) {
        JWTVerifier jwtVerifier = getJwtVerifier();
        DecodedJWT decodedJWT;
        try {
            decodedJWT = jwtVerifier.verify(token);
        } catch(JWTVerificationException exception) {
            throw new JWTVerificationException(TOKEN_CANNOT_BE_VERIFIED);
        }
        String[] authorities =  decodedJWT.getClaims().get(AUTHORITIES).asArray(String.class);

        return Arrays.stream(authorities)
                .map(auth -> new SimpleGrantedAuthority(auth))
                .collect(Collectors.toList());
    }

    public Authentication getAuthentication(String userName,
                                            List<GrantedAuthority> authorities,
                                            HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(userName, null, authorities);

        //TODO - do we need this??
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authenticationToken;
    }

    public String getSubject(String token) {
        return getJwtVerifier().verify(token).getSubject();
    }

    //TODO - do we need this as verify verifies everything??
    public boolean isTokenValid(String userName, String token) {
        JWTVerifier verifier = getJwtVerifier();
        return StringUtils.isNotEmpty(userName) && !isTokenExpired(verifier, token);
    }

    private boolean isTokenExpired(JWTVerifier verifier, String token) {
        Date expiration = verifier.verify(token).getExpiresAt();
        return expiration.before(new Date());
    }

    private String[] getAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(auth -> auth.getAuthority())
                .toArray(String[]::new);
    }

    private JWTVerifier getJwtVerifier() {
        return JWT.require(Algorithm.HMAC512(secret.getBytes()))
                .withIssuer(GET_ARRAYS_LLC)
                .build();
    }
}