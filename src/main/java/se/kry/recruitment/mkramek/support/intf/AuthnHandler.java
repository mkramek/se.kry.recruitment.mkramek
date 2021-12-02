package se.kry.recruitment.mkramek.support.intf;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import se.kry.recruitment.mkramek.support.impl.AuthnHandlerImpl;

public interface AuthnHandler extends Handler<RoutingContext> {
  @Override
  void handle(RoutingContext event);
  static AuthnHandler create() {
    return new AuthnHandlerImpl();
  }
}
