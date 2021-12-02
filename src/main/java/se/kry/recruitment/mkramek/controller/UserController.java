package se.kry.recruitment.mkramek.controller;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import se.kry.recruitment.mkramek.model.User;
import se.kry.recruitment.mkramek.support.helper.HttpHelper;
import se.kry.recruitment.mkramek.support.intf.VertxController;
import se.kry.recruitment.mkramek.support.provider.AuthProvider;

import java.util.concurrent.atomic.AtomicReference;

public class UserController implements VertxController<User> {
  private final MySQLPool client;
  private final AuthProvider authProvider;

  public UserController(final MySQLPool databaseClient) {
    this.client = databaseClient;
    authProvider = AuthProvider.getInstance(databaseClient);
  }

  @Override
  public void getAll(final RoutingContext context) {
    HttpHelper.sendJsonResponse(context, "Due to GDPR policy accessing data for all users is prohibited, unless having exclusive rights to access. (Also, there's no role-based authorization, so this feature is to-do in case of implementing RBA.)");
  }

  @Override
  public void getOne(final RoutingContext context) {
    final long targetUserId = Long.parseLong(context.request().getParam("id"));
    AtomicReference<User> authorizedUser = new AtomicReference<>();
    authProvider.getAuthorizedUser(context)
      .compose(user -> {
        authorizedUser.set(user);
        if (user.getId().equals(targetUserId)) {
          return getUserById(authorizedUser.get().getId());
        }
        return Future.failedFuture("Tried to read other user's data");
      })
      .onSuccess(result -> {
        Row row = result.iterator().next();
        HttpHelper.sendJsonResponse(context, new User(row.getLong("id"), row.getString("username")));
      })
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  @Override
  public void create(final RoutingContext context) {
    final JsonObject reqBody = context.getBodyAsJson();
    userExists(reqBody)
      .compose(userExists -> {
        if (!userExists) {
          return validateUsername(reqBody.getString("username"));
        }
        return Future.failedFuture("User already exists.");
      })
      .compose(isUsernameValid -> {
        if (isUsernameValid) {
          User newUser = reqBody.mapTo(User.class);
          String plainTextPassword = newUser.getPassword();
          newUser.setHashedPassword(plainTextPassword);
          return createUser(newUser);
        }
        return Future.failedFuture("Username is not valid (expected email address)");
      })
      .onSuccess(user -> HttpHelper.sendJsonResponse(context, user))
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  @Override
  public void update(RoutingContext context) {
    long userId = Long.parseLong(context.request().getParam("id"));
    User update = context.getBodyAsJson().mapTo(User.class);
    String plainTextPassword = update.getPassword();
    update.setHashedPassword(plainTextPassword);
    authProvider.getAuthorizedUser(context)
      .compose(user -> {
        if (user.getId().equals(userId)) {
          return updateUser(update, userId);
        }
        return Future.failedFuture("User is not authorized");
      })
      .compose(result -> {
        if (result.rowCount() > 0) {
          return getUserById(userId);
        }
        return Future.failedFuture("Error during user update");
      })
      .onSuccess(userResult -> {
        if (userResult.size() > 0) {
          Row row = userResult.iterator().next();
          User updatedUser = new User(row.getLong("id"), row.getString("username"));
          HttpHelper.sendJsonResponse(context, updatedUser.toJson());
        } else {
          HttpHelper.sendJsonError(context, "No user with given ID");
        }
      })
      .onFailure(error -> {
        error.printStackTrace();
        HttpHelper.sendJsonError(context, error.getLocalizedMessage());
      });
  }

  @Override
  public void delete(RoutingContext context) {
    long toDelete = Long.parseLong(context.request().getParam("id"));
    authProvider.getAuthorizedUser(context)
      .compose(user -> {
        if (user.getId().equals(toDelete)) {
          return deleteUser(toDelete);
        }
        return Future.failedFuture("User is not authorized");
      })
      .onSuccess(result -> HttpHelper.sendJsonResponse(context, new JsonObject().put("message", "User with ID of " + toDelete + " has been deleted.")))
      .onFailure(error -> HttpHelper.sendJsonError(context, error.getMessage()));
  }

  private Future<Boolean> userExists(JsonObject requestBody) {
    Promise<Boolean> userExists = Promise.promise();
    client
      .preparedQuery("SELECT username from users WHERE username LIKE ?")
      .execute(Tuple.of(requestBody.getString("username")))
      .onSuccess(result -> userExists.complete(result.size() > 0))
      .onFailure(userExists::fail);
    return userExists.future();
  }

  private Future<User> createUser(User user) {
    Promise<User> userCreation = Promise.promise();
    client
      .preparedQuery("INSERT INTO `users` (username, password) VALUES (?, ?)")
      .execute(Tuple.of(user.getUsername(), user.getPassword()), operation -> {
        if (operation.succeeded()) {
          user.setId(operation.result().property(MySQLClient.LAST_INSERTED_ID));
          userCreation.complete(user);
        } else {
          userCreation.fail(operation.cause());
        }
      });
    return userCreation.future();
  }

  private Future<RowSet<Row>> deleteUser(long userId) {
    return client
      .preparedQuery("DELETE FROM `users` WHERE id=?")
      .execute(Tuple.of(userId));
  }

  private Future<RowSet<Row>> getUserById(long userId) {
    return client.preparedQuery("SELECT id, username FROM users WHERE id=?")
      .execute(Tuple.of(userId));
  }

  private Future<Boolean> validateUsername(String username) {
    final String validEmailRegex = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
    Promise<Boolean> validateUsername = Promise.promise();
    if (username.matches(validEmailRegex)) {
      validateUsername.complete(true);
    } else {
      validateUsername.fail("Username is not valid. It should be an email address.");
    }
    return validateUsername.future();
  }

  private Future<RowSet<Row>> updateUser(User update, long userId) {
    return client
      .preparedQuery("UPDATE `users` set username=?, password=? WHERE id=?")
      .execute(Tuple.of(update.getUsername(), update.getPassword(), userId));
  }
}
