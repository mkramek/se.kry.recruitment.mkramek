package se.kry.recruitment.mkramek.support.provider;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.JWTOptions;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.authentication.AuthenticationProvider;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.auth.sqlclient.SqlAuthentication;
import io.vertx.ext.auth.sqlclient.SqlAuthenticationOptions;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import se.kry.recruitment.mkramek.model.User;
import se.kry.recruitment.mkramek.support.helper.AuthHelper;

import java.util.Optional;

public class AuthProvider {
  private final JWTAuth jwtAuthProvider;
  private final AuthenticationProvider sqlAuthProvider;
  private final MySQLPool client;

  private static AuthProvider INSTANCE;

  private AuthProvider(MySQLPool client) {
    KeyStoreOptions keyStoreOpts = new KeyStoreOptions().setPath("src/main/resources/keystore.jceks").setPassword("secret");
    JWTAuthOptions jwtOpts = new JWTAuthOptions().setKeyStore(keyStoreOpts);
    SqlAuthenticationOptions sqlOpts = new SqlAuthenticationOptions();
    this.jwtAuthProvider = JWTAuth.create(Vertx.currentContext().owner(), jwtOpts);
    this.sqlAuthProvider = SqlAuthentication.create(client, sqlOpts);
    this.client = client;
  }

  public static AuthProvider getInstance(MySQLPool client) {
    if (INSTANCE == null) {
      INSTANCE = new AuthProvider(client);
    }
    return INSTANCE;
  }

  public Future<String> authenticateWithJWT(final RoutingContext context) {
    Promise<String> authenticateWithJWT = Promise.promise();
    Optional<String> authToken = AuthHelper.getTokenFromHeader(context.request());
    authToken.ifPresentOrElse(
      token -> {
        if ("null".equals(token)) {
          authenticateWithJWT.fail("Empty token");
        } else {
          jwtAuthProvider.authenticate(new JsonObject().put("token", token))
            .onSuccess(jwtUser -> authenticateWithJWT.complete(jwtUser.principal().getString("sub")))
            .onFailure(error -> authenticateWithJWT.fail(error.getMessage()));
        }
      },
      () -> authenticateWithJWT.fail("Empty token"));
    return authenticateWithJWT.future();
  }

  public Future<String> authenticateWithSQL(final RoutingContext context) {
    return authenticateClient(context.getBodyAsJson())
      .compose(userAuthenticated -> {
        if (userAuthenticated) {
          return generateTokenForUser(context.getBodyAsJson());
        } else {
          return Future.failedFuture("Authentication failed, possibly wrong password.");
        }
      });
  }

  private Future<Boolean> authenticateClient(final JsonObject jsonAuthData) {
    Promise<Boolean> clientAuthenticated = Promise.promise();
    if (jsonAuthData == null) {
      clientAuthenticated.fail("No data provided.");
    } else {
      sqlAuthProvider.authenticate(jsonAuthData)
        .onSuccess(user -> clientAuthenticated.complete(true))
        .onFailure(error -> clientAuthenticated.complete(false));
      }
    return clientAuthenticated.future();
  }

  private Future<String> generateTokenForUser(final JsonObject jsonAuthData) {
    Promise<String> generateToken = Promise.promise();
    String authToken = jwtAuthProvider.generateToken(new JsonObject().put("sub", jsonAuthData.getString("username")), new JWTOptions().setAlgorithm("ES512"));
    if (authToken == null || authToken.isBlank()) {
      generateToken.fail("Received null or empty token");
    } else {
      generateToken.complete(authToken);
    }
    return generateToken.future();
  }

  public Future<User> getAuthorizedUser(final RoutingContext context) {
    Promise<User> authorizeUser = Promise.promise();
    authenticateWithJWT(context)
      .onSuccess(user -> client.preparedQuery("SELECT id, username FROM users WHERE username=? LIMIT 1")
        .execute(Tuple.of(user))
        .onSuccess(result -> {
          if (result.size() > 0) {
            Row userData = result.iterator().next();
            User authedUser = new User(userData.getLong("id"), userData.getString("username"));
            authorizeUser.complete(authedUser);
          } else {
            authorizeUser.fail("Authorization error: user not found");
          }
        })
        .onFailure(error -> {
          authorizeUser.fail("Authorization error: query failed");
        }))
      .onFailure(error -> authorizeUser.fail("Authorization error: access denied"));
    return authorizeUser.future();
  }
}
