package se.kry.recruitment.mkramek.router;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.mysqlclient.MySQLPool;
import se.kry.recruitment.mkramek.controller.*;
import se.kry.recruitment.mkramek.support.intf.AuthnHandler;

public class RoutingProvider {
  private static RoutingProvider INSTANCE;
  private final Router router;

  private final UserController userController;
  private final ServiceController serviceController;
  private final AuthController authController;
  private final DefaultController defaultController;
  private final MockController mockController;

  private RoutingProvider(Vertx vertx, MySQLPool client) {
    this.router = Router.router(vertx);
    this.userController = new UserController(client);
    this.serviceController = new ServiceController(client);
    this.authController = new AuthController(client);
    this.defaultController = new DefaultController();
    this.mockController = new MockController();
    this.setupRoutes(this.router);
  }

  public static RoutingProvider getInstanceWithDatabase(MySQLPool client) {
    if (INSTANCE == null) {
      Vertx vertx = Vertx.currentContext().owner();
      INSTANCE = new RoutingProvider(vertx, client);
    }
    return INSTANCE;
  }

  private void setupRoutes(Router router) {
    BodyHandler bodyHandler = BodyHandler.create();
    StaticHandler staticHandler = StaticHandler.create();
    AuthnHandler authnHandler = AuthnHandler.create();

    router.route("/api/v1*").handler(bodyHandler);
    router.get("/api/v1").handler(defaultController::getApiPath);

    router.get("/api/v1/mock/:http").handler(mockController::getHttpCode);

    router.get("/api/v1/auth/verify").handler(authnHandler).handler(authController::verifyToken);
    router.post("/api/v1/auth/login").handler(authController::login);
    router.get("/api/v1/auth/info").handler(authController::getUserInfo);

    router.route("/api/v1/user*").handler(authnHandler);
    router.route("/api/v1/service*").handler(authnHandler);

    router.get("/api/v1/user").handler(userController::getAll);
    router.get("/api/v1/user/:id").handler(userController::getOne);
    router.post("/api/v1/user").handler(userController::create);
    router.put("/api/v1/user/:id").handler(userController::update);
    router.delete("/api/v1/user/:id").handler(userController::delete);

    router.get("/api/v1/service").handler(serviceController::getAll);
    router.get("/api/v1/service/:id").handler(serviceController::getOne);
    router.post("/api/v1/service").handler(serviceController::create);
    router.put("/api/v1/service/:id").handler(serviceController::update);
    router.delete("/api/v1/service/:id").handler(serviceController::delete);

    router.route().handler(staticHandler);
    router.route().handler(this::reroute);
  }

  private void reroute(RoutingContext context) {
    if (!"/".equals(context.normalizedPath())) {
      context.reroute("/");
    } else {
      context.next();
    }
  }

  public Router getRouter() {
    return router;
  }
}
