package com.steeplesoft.qkm;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

@QuarkusTest
@QuarkusTestResource(MockAuthorizationServer.class)
public class SampleResourceTest {
    @Test
    public void testUserAsUser() {
        RestAssured.given()
                .contentType("application/json")
                .auth()
                .oauth2(generateJWT("user"))
                .get("/sample/user")
                .then()
                .statusCode(200);
    }

    @Test
    public void testUserAsAdmin() {
        RestAssured.given()
                .contentType("application/json")
                .auth()
                .oauth2(generateJWT("admin"))
                .get("/sample/user")
                .then()
                .statusCode(403);
    }

    @Test
    public void testAdminAsAdmin() {
        RestAssured.given()
                .contentType("application/json")
                .auth()
                .oauth2(generateJWT("admin"))
                .get("/sample/admin")
                .then()
                .statusCode(200);
    }

    @Test
    public void testAdminAsUser() {
        RestAssured.given()
                .contentType("application/json")
                .auth()
                .oauth2(generateJWT("user"))
                .get("/sample/admin")
                .then()
                .statusCode(403);
    }

    private String generateJWT(String role) {
        // Prepare JWT with claims set
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256)
                        .keyID(MockAuthorizationServer.keyPair.getKeyID())
                        .type(JOSEObjectType.JWT)
                        .build(),
                new JWTClaimsSet.Builder()
                        .subject("backend-service")
                        .issuer("https://wiremock")
                        .claim(
                                "realm_access",
                                new JWTClaimsSet.Builder()
                                        .claim("roles", Arrays.asList(role))
                                        .build()
                                        .toJSONObject()
                        )
                        .claim("scope", "openid email profile")
                        .expirationTime(new Date(new Date().getTime() + 60 * 1000))
                        .build()
        );
        // Compute the RSA signature
        try {
            signedJWT.sign(new RSASSASigner(MockAuthorizationServer.keyPair.toRSAKey()));
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
        return signedJWT.serialize();
    }
}
