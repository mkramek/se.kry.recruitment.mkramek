package se.kry.recruitment.mkramek.controller;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.apache.commons.validator.routines.UrlValidator;
import se.kry.recruitment.mkramek.model.Service;
import se.kry.recruitment.mkramek.model.User;
import se.kry.recruitment.mkramek.support.helper.HttpHelper;
import se.kry.recruitment.mkramek.support.intf.VertxController;
import se.kry.recruitment.mkramek.support.provider.AuthProvider;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class ServiceController implements VertxController<User> {
  private final MySQLPool client;
  private final AuthProvider authProvider;

  private static final Logger log = LoggerFactory.getLogger(ServiceController.class);

  public ServiceController(final MySQLPool databaseClient) {
    this.client = databaseClient;
    this.authProvider = AuthProvider.getInstance(databaseClient);
  }

  @Override
  public void getAll(final RoutingContext context) {
    JsonArray services = new JsonArray();
    authProvider.getAuthorizedUser(context)
      .compose(user -> getServicesByUserId(user.getId()))
      .onSuccess(result -> {
        result.forEach(row -> {
          Service service = new Service(
            row.getLong("id"),
            row.getString("name"),
            row.getString("url"),
            row.getLocalDateTime("created_at"),
            row.getString("last_status")
          );
          services.add(service.toJson());
        });
        HttpHelper.sendJsonResponse(context, services);
      })
      .onFailure(error -> {
        log.error(error.getMessage(), error.getCause());
        HttpHelper.sendJsonError(context, error.getMessage());
      });
  }

  @Override
  public void getOne(final RoutingContext context) {
    long id = Long.parseLong(context.request().getParam("id"));
    authProvider.getAuthorizedUser(context)
      .compose(user -> getServiceById(id, user.getId()))
      .onSuccess(result -> {
        Row row = result.iterator().next();
        HttpHelper.sendJsonResponse(context, new Service(
          row.getLong("id"),
          row.getString("name"),
          row.getString("url"),
          row.getLocalDateTime("createdAt"),
          row.getString("last_status"))
        );
      })
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  @Override
  public void create(final RoutingContext context) {
    Service newService = context.getBodyAsJson().mapTo(Service.class);
    newService.setCreatedAt(LocalDateTime.now());
    AtomicReference<User> authorizedUser = new AtomicReference<>();
    authProvider.getAuthorizedUser(context)
      .compose(user -> {
        authorizedUser.set(user);
        return validateUrl(newService.getUrl());
      })
      .compose(isUrlValid -> {
        if (isUrlValid) {
          return createService(newService, authorizedUser.get().getId());
        }
        return Future.failedFuture("Invalid URL");
      })
      .compose(result -> {
        long lastInsertedId = result.property(MySQLClient.LAST_INSERTED_ID);
        newService.setId(lastInsertedId);
        return getServiceById(lastInsertedId, authorizedUser.get().getId());
      })
      .onSuccess(result -> {
        Row row = result.iterator().next();
        Service createdService = new Service(
          row.getLong("id"),
          row.getString("name"),
          row.getString("url"),
          row.getLocalDateTime("created_at"),
          row.getString("last_status")
        );
        HttpHelper.sendJsonResponse(context, createdService.toJson());
      })
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  @Override
  public void update(final RoutingContext context) {
    Service update = context.getBodyAsJson().mapTo(Service.class);
    log.info(update.toJson());
    update.setId(Long.parseLong(context.request().getParam("id")));
    AtomicReference<User> authorizedUser = new AtomicReference<>();
    authProvider.getAuthorizedUser(context)
      .compose(user -> {
        authorizedUser.set(user);
        return getUserFromService(update.getId());
      })
      .compose(result -> {
        if (result.size() > 0) {
          Row foundUser = result.iterator().next();
          if (Objects.equals(authorizedUser.get().getId(), foundUser.getLong("user"))) {
            return validateUrl(update.getUrl());
          }
        }
        return Future.failedFuture("User unauthorized");
      })
      .compose(isUrlValid -> isUrlValid ? updateService(update) : Future.failedFuture("Invalid URL"))
      .compose(result -> getServiceById(update.getId(), authorizedUser.get().getId()))
      .onSuccess(srv -> {
        final Service service;
        if (srv.size() > 0) {
          Row first = srv.iterator().next();
          service = new Service(
            first.getLong("id"),
            first.getString("name"),
            first.getString("url"),
            first.getLocalDateTime("created_at"),
            first.getString("last_status")
          );
          HttpHelper.sendJsonResponse(context, service.toJson());
        } else {
          HttpHelper.sendJsonError(context, "Unexpected error: service is empty");
        }
      })
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  @Override
  public void delete(final RoutingContext context) {
    long toDelete = Long.parseLong(context.request().getParam("id"));
    AtomicReference<User> authorizedUser = new AtomicReference<>();
    authProvider.getAuthorizedUser(context)
      .compose(user -> {
        authorizedUser.set(user);
        return getUserFromService(toDelete);
      })
      .compose(result -> {
        if (result.size() > 0) {
          Row foundUser = result.iterator().next();
          if (Objects.equals(authorizedUser.get().getId(), foundUser.getLong("user"))) {
            return deleteService(toDelete);
          }
          return Future.failedFuture("User unauthorized");
        }
        return Future.failedFuture("User unauthorized");
      })
      .onSuccess(effect -> HttpHelper.sendJsonResponse(context, new JsonObject().put("message", "Service with ID of " + toDelete + " has been deleted.")))
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  private Future<RowSet<Row>> createService(final Service newService, long userId) {
    return client
      .preparedQuery("INSERT INTO `services` (name, url, user, created_at) VALUES (?, ?, ?, ?)")
      .execute(Tuple.of(newService.getName(), newService.getUrl(), userId, newService.getCreatedAt()));
  }

  private Future<RowSet<Row>> deleteService(final long serviceId) {
    return client
      .preparedQuery("DELETE FROM `services` WHERE id=?")
      .execute(Tuple.of(serviceId));
  }

  private Future<RowSet<Row>> getUserFromService(final long serviceId) {
    return client.preparedQuery("SELECT user FROM services WHERE id=?")
      .execute(Tuple.of(serviceId));
  }

  private Future<RowSet<Row>> updateService(final Service update) {
    return client
      .preparedQuery("UPDATE `services` set name=?, url=?, last_status=? WHERE id=?")
      .execute(Tuple.of(update.getName(), update.getUrl(), update.getLastStatus(), update.getId()));
  }

  private Future<RowSet<Row>> getServiceById(final long serviceId, final long userId) {
    return client.preparedQuery("SELECT id, name, url, created_at, last_status FROM services WHERE id=? AND user=?")
      .execute(Tuple.of(serviceId, userId));
  }

  private Future<RowSet<Row>> getServicesByUserId(final long userId) {
    return client.preparedQuery("SELECT id, name, url, created_at, last_status FROM services WHERE user=?")
      .execute(Tuple.of(userId));
  }

  private Future<Boolean> validateUrl(final String url) {
    final String[] schemes = { "http", "https" };
    UrlValidator validator = new UrlValidator(schemes, UrlValidator.ALLOW_LOCAL_URLS);
    Promise<Boolean> validateUrl = Promise.promise();
    if (validator.isValid(url)) {
      validateUrl.complete(true);
    } else {
      validateUrl.fail("Invalid URL");
    }
    return validateUrl.future();
  }
}
