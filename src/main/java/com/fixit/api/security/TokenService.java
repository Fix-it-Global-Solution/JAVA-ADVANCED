package com.fixit.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fixit.api.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Service
public class TokenService {

    private static final String ISSUER = "fixit-api";
    private static final ZoneOffset ZONE_OFFSET_BRASILIA = ZoneOffset.of("-03:00");

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.expiration-hours:8}")
    private long expirationHours;

    public String gerarToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer(ISSUER)
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId())
                    .withClaim("nome", usuario.getNome())
                    .withClaim("perfil", usuario.getPerfil().name())
                    .withIssuedAt(Instant.now())
                    .withExpiresAt(calcularDataExpiracao())
                    .sign(algorithm);
        } catch (JWTCreationException ex) {
            throw new IllegalStateException("Erro ao gerar token JWT para o usuário.", ex);
        }
    }

    public String validarToken(String tokenJWT) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer(ISSUER)
                    .build()
                    .verify(tokenJWT)
                    .getSubject();
        } catch (JWTVerificationException ex) {
            return null;
        }
    }

    public Instant calcularDataExpiracao() {
        return LocalDateTime.now()
                .plusHours(expirationHours)
                .toInstant(ZONE_OFFSET_BRASILIA);
    }

}

