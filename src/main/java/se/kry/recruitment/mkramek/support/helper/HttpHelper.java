package se.kry.recruitment.mkramek.support.helper;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class HttpHelper {

  public static void sendJsonResponse(final RoutingContext context, final Object content) {
    context.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(convertToResponse(content));
  }

  public static void sendJsonError(final RoutingContext context, final String message) {
    sendJsonResponse(context, new JsonObject().put("error", message));
  }

  private static String convertToResponse(final Object obj) {
    if (obj instanceof JsonObject || obj instanceof JsonArray) {
      return obj.toString();
    } else if (obj instanceof String) {
      return new JsonObject().put("message", obj).toString();
    } else {
      return JsonObject.mapFrom(obj).toString();
    }
  }
}
