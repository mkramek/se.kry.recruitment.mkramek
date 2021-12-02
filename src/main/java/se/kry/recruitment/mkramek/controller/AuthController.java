package se.kry.recruitment.mkramek.controller;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLPool;
import se.kry.recruitment.mkramek.support.helper.HttpHelper;
import se.kry.recruitment.mkramek.support.provider.AuthProvider;

public class AuthController {
  private final AuthProvider authProvider;

  public AuthController(final MySQLPool databaseClient) {
    this.authProvider = AuthProvider.getInstance(databaseClient);
  }

  public void verifyToken(final RoutingContext context) {
    authProvider.authenticateWithJWT(context)
      .onSuccess(user -> HttpHelper.sendJsonResponse(context, new JsonObject().put("token_valid", true).put("user", user)))
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  public void login(final RoutingContext context) {
    authProvider.authenticateWithSQL(context)
      .onSuccess(token -> HttpHelper.sendJsonResponse(context, new JsonObject().put("auth_token", token)))
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  public void getUserInfo(final RoutingContext context) {
    authProvider.getAuthorizedUser(context)
      .onSuccess(user -> HttpHelper.sendJsonResponse(context, new JsonObject().put("id", user.getId()).put("username", user.getUsername())))
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }
}
