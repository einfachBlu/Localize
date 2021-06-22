package de.blu.localize.repository;

import lombok.AccessLevel;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public abstract class MapRepository<K, V> {

  @Getter(AccessLevel.PRIVATE)
  private Map<K, V> map = new HashMap<>();

  public void put(K key, V value) {
    this.getMap().put(key, value);
  }

  public V get(K key) {
    return this.getMap().get(key);
  }

  public V getOrDefault(K key, V defaultValue) {
    return this.getMap().getOrDefault(key, defaultValue);
  }

  public void remove(K key) {
    this.getMap().remove(key);
  }

  public boolean containsKey(K key) {
    return this.getMap().containsKey(key);
  }

  public boolean containsValue(V value) {
    return this.getMap().containsValue(value);
  }

  public Map<K, V> all() {
    return this.getMap();
  }
}
