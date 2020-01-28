package timboe.vev.pathfinding;

import java.util.HashMap;
import java.util.LinkedList;

import timboe.vev.Pair;

/**
 * Created by Tim on 17/01/2018.
 */

public class PathingCache<T> {
  private HashMap<Pair<T, T>, LinkedList<T>> cache = new HashMap<Pair<T, T>, LinkedList<T>>();
  private Pair<T, T> tmpPair = new Pair<T, T>();

  public PathingCache() {
  }

  public LinkedList<T> getCacheHit(T from, T to) {
    LinkedList<T> hit = cache.get(tmpPair.set(from, to));
    if (hit == null) return null;
    return new LinkedList<T>(hit);
  }

  public void addToCache(T from, T to, LinkedList<T> path) {
    cache.put(new Pair<T, T>(from, to), path);
    return;
  }

  public void clear() {
    cache.clear();
  }


}
