package se.kry.recruitment.mkramek.support.helper;

import io.vertx.core.http.HttpServerRequest;

import java.util.Optional;

public class AuthHelper {
  public static Optional<String> getTokenFromHeader(HttpServerRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && !authHeader.isBlank()) {
      String token = authHeader.replaceFirst("(.* )","");
      return Optional.of(token);
    }
    return Optional.empty();
  }
}
