package se.kry.recruitment.mkramek.support.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.KeyStoreOptions;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTAuthOptions;
import io.vertx.ext.web.RoutingContext;
import se.kry.recruitment.mkramek.support.helper.AuthHelper;
import se.kry.recruitment.mkramek.support.helper.HttpHelper;
import se.kry.recruitment.mkramek.support.intf.AuthnHandler;

import java.util.Map;
import java.util.Optional;

public class AuthnHandlerImpl implements AuthnHandler {
  private static final Logger log = LoggerFactory.getLogger(AuthnHandlerImpl.class);
  private final Map<String, HttpMethod> excludedUrls = Map.of(
    "/api/v1/user", HttpMethod.POST,
    "/api/v1/auth/login", HttpMethod.POST
  );

  public AuthnHandlerImpl() {}

  @Override
  public void handle(final RoutingContext context) {
    context.request().pause();
    auth(context).onSuccess(event -> {
      context.request().resume();
      context.next();
    }).onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  public Future<JsonObject> auth(final RoutingContext context) {
    Promise<JsonObject> authorize = Promise.promise();
    Optional<String> authToken = AuthHelper.getTokenFromHeader(context.request());
    if (isPathExcluded(context.request())) {
      authorize.complete();
    } else if (authToken.isPresent()) {
      JWTAuthOptions jwtOpts = new JWTAuthOptions()
        .setKeyStore(new KeyStoreOptions()
          .setPath("src/main/resources/keystore.jceks")
          .setPassword("secret")
        );
      JWTAuth jwtAuth = JWTAuth.create(Vertx.currentContext().owner(), jwtOpts);
      jwtAuth.authenticate(new JsonObject().put("token", authToken.get()))
        .onSuccess(user -> authorize.complete(user.principal()))
        .onFailure(error -> authorize.fail(error.getCause()));
    } else {
      authorize.fail("No token provided");
    }
    return authorize.future();
  }

  private boolean isPathExcluded(final HttpServerRequest request) {
    boolean isPathExcluded = false;
    for (Map.Entry<String, HttpMethod> url : excludedUrls.entrySet()) {
      isPathExcluded = request.method().equals(url.getValue()) && request.uri().contains(url.getKey());
      if (isPathExcluded) break;
    }
    return isPathExcluded;
  }
}
