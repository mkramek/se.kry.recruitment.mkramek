package se.kry.recruitment.mkramek.model;

import io.vertx.core.json.JsonObject;

import java.time.LocalDateTime;

public class Service {
  private Long id;
  private String name;
  private String url;
  private long user;
  private LocalDateTime createdAt;
  private String lastStatus;

  public Service() {}

  public Service(String name, String url, long user) {
    this.name = name;
    this.url = url;
    this.user = user;
    this.createdAt = LocalDateTime.now();
  }

  public Service(String name, String url) {
    this.name = name;
    this.url = url;
    this.createdAt = LocalDateTime.now();
  }

  public Service(Long id, String name, String url, LocalDateTime createdAt, String lastStatus) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.createdAt = createdAt;
    this.lastStatus = lastStatus;
  }

  public Service(Long id, String name, String url, long user, LocalDateTime createdAt, String lastStatus) {
    this.id = id;
    this.name = name;
    this.url = url;
    this.user = user;
    this.createdAt = createdAt;
    this.lastStatus = lastStatus;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    if (this.id == null) this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public long getUser() {
    return user;
  }

  public void setUser(long user) {
    this.user = user;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public String getLastStatus() {
    return lastStatus;
  }

  public void setLastStatus(String lastStatus) {
    this.lastStatus = lastStatus;
  }

  public JsonObject toJson() {
    return new JsonObject()
      .put("id", id)
      .put("name", name)
      .put("url", url)
      .put("createdAt", createdAt == null ? "" : createdAt.toString())
      .put("lastStatus", lastStatus);
  }
}
