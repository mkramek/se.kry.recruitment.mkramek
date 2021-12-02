package se.kry.recruitment.mkramek.model;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.HashingStrategy;
import io.vertx.ext.auth.VertxContextPRNG;

public class User {
  private Long id;
  private String username;
  private String password;

  public User() {}

  public User(Long id, String username) {
    this.id = id;
    this.username = username;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (this.id == null) this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setHashedPassword(String password) {
    this.password = HashingStrategy.load().hash("pbkdf2", null, VertxContextPRNG.current().nextString(32), password);
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("id", id)
      .put("username", username);
  }
}
