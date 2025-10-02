package ca.uhn.fhir.jpa.starter.auth.service;


import graphql.util.Pair;

public interface AuthService {
    Pair<Boolean, String> isTokenValid(String token);
}
