package se.kry.recruitment.mkramek.controller;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import se.kry.recruitment.mkramek.support.helper.HttpHelper;

public class MockController {
  public MockController() {}

  public void getHttpCode(final RoutingContext context) {
    final String code = context.request().getParam("http");
    if ("timeout".equals(code)) {
      Vertx vertx = Vertx.currentContext().owner();
      vertx.setTimer(8000, then -> HttpHelper.sendJsonResponse(context, new JsonObject().put("message", "This response purposely exceeds time set in client")));
    } else {
      final int httpCode = Integer.parseInt(code);
      context.response().setStatusCode(httpCode).end(new JsonObject().put("http_code", httpCode).toString());
    }
  }
}
