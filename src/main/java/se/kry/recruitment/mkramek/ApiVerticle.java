package se.kry.recruitment.mkramek;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;
import se.kry.recruitment.mkramek.router.RoutingProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicReference;

public class ApiVerticle extends AbstractVerticle {
  private static final Logger log = LoggerFactory.getLogger(ApiVerticle.class);

  static {
    ObjectMapper mapper = io.vertx.core.json.jackson.DatabindCodec.mapper();
    mapper.registerModule(new JavaTimeModule());
  }

  public static void main(String[] args) {
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ApiVerticle());
  }

  @Override
  public void start(Promise<Void> startPromise) throws Exception {
    AtomicReference<JsonObject> config = new AtomicReference<>();
    setupInitialConfig()
      .compose(initialConfig -> {
        config.set(initialConfig);
        return setupDatabase(config.get());
      })
      .compose(this::setupRouter)
      .compose(routingProvider -> initApplication(config.get().getInteger("app.port"), routingProvider.getRouter()).future())
      .onSuccess(handler -> startPromise.complete())
      .onFailure(error -> startPromise.fail(error.getCause()));
  }

  private Future<JsonObject> setupInitialConfig() {
    Promise<JsonObject> initConfig = Promise.promise();
    ConfigStoreOptions dbConfigOpts = createFileConfigStore("src/main/resources/database.properties");
    ConfigStoreOptions appConfigOpts = createFileConfigStore("src/main/resources/application.properties");
    ConfigRetrieverOptions retrieverOpts = new ConfigRetrieverOptions()
      .addStore(dbConfigOpts)
      .addStore(appConfigOpts);

    for (ConfigStoreOptions store : retrieverOpts.getStores()) {
      String testPath = store.getConfig().getString("path");
      File check = new File(testPath);
      if (!check.isFile()) {
        initConfig.fail(new FileNotFoundException("Properties file not found; tried: " + check.getAbsolutePath()));
      }
    }

    ConfigRetriever retriever = ConfigRetriever.create(vertx, retrieverOpts);
    retriever.getConfig(json -> {
      if (json.succeeded()) {
        initConfig.complete(json.result());
      } else {
        initConfig.fail(json.cause());
      }
    });
    return initConfig.future();
  }

  private Future<MySQLPool> setupDatabase(JsonObject configuration) {
    MySQLConnectOptions dbConnectOpts = new MySQLConnectOptions();
    PoolOptions dbPoolOpts = new PoolOptions();
    dbConnectOpts
      .setPort(configuration.getInteger("db.port"))
      .setHost(configuration.getString("db.host"))
      .setDatabase(configuration.getString("db.database"))
      .setUser(configuration.getString("db.user"))
      .setPassword(configuration.getString("db.password"));
    dbPoolOpts.setMaxSize(configuration.getInteger("db.max_pool_size"));
    return Future.succeededFuture(MySQLPool.pool(vertx, dbConnectOpts, dbPoolOpts));
  }

  private Future<RoutingProvider> setupRouter(MySQLPool client) {
    return Future.succeededFuture(RoutingProvider.getInstanceWithDatabase(client));
  }

  private Promise<Void> initApplication(int appPort, Router router) {
    Promise<Void> serverUp = Promise.promise();
    vertx.createHttpServer()
      .requestHandler(router)
      .listen(appPort)
      .onSuccess(server -> {
        log.info("HTTP server started on port " + appPort);
        serverUp.complete();
      })
      .onFailure(error -> serverUp.fail(error.getCause()));
    return serverUp;
  }

  private ConfigStoreOptions createFileConfigStore(String location) {
    return new ConfigStoreOptions()
      .setType("file")
      .setFormat("properties")
      .setConfig(new JsonObject().put("path", location));
  }
}
