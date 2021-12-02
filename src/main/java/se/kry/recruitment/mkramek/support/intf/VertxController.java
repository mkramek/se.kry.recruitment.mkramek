package se.kry.recruitment.mkramek.support.intf;

import io.vertx.ext.web.RoutingContext;

public interface VertxController<T> {
  void getAll(RoutingContext context);
  void getOne(RoutingContext context);
  void create(RoutingContext context);
  void update(RoutingContext context);
  void delete(RoutingContext context);
}
