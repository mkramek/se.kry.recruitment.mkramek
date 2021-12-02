package se.kry.recruitment.mkramek.controller;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import se.kry.recruitment.mkramek.support.helper.HttpHelper;

public class DefaultController {
  public DefaultController() {}

  public void getApiPath(final RoutingContext context) {
    HttpHelper.sendJsonResponse(context, new JsonObject().put("version", "1.0.0-RELEASE"));
  }
}
