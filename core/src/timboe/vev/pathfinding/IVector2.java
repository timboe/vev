package timboe.vev.pathfinding;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import timboe.vev.entity.Tile;

public class IVector2 implements Comparable, Serializable, Node {
  public int x;
  public int y;
  public Set<IVector2> pathFindNeighbours = null; // Neighbours - used in pathfinding

  public JSONObject serialise() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("x", x);
    json.put("y", y);
    if (pathFindNeighbours != null) {
      JSONObject n = new JSONObject();
      Integer count = 0;
      for (IVector2 v : pathFindNeighbours) {
        JSONObject sub = new JSONObject();
        sub.put("x", v.x);
        sub.put("y", v.y);
        n.put(count.toString(), sub);
        ++count;
      }
      json.put("n", n);
    }
    return json;
  }

  public void deserialise(JSONObject json) throws JSONException {
    x = json.getInt("x");
    y = json.getInt("y");
  }

  public IVector2(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public IVector2(IVector2 v) {
    x = v.x;
    y = v.y;
  }

  public IVector2() {
    x = 0;
    y = 0;
  }

  public void set(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public void set(IVector2 v) {
    this.x = v.x;
    this.y = v.y;
  }

  public float dst(IVector2 v) {
    return (float)Math.hypot(this.x - v.x, this.y - v.y);
  }

  @Override
  public int compareTo(Object o) {
    IVector2 v = (IVector2) o;
    return (x * x + y * y) - (v.x * v.x - v.y * v.y);
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || !(o instanceof IVector2)) return false;
    IVector2 v = (IVector2) o;
    return (x == v.x && y == v.y);
  }

  @Override
  public String toString() {
    return "(" + x + "," + y + ")";
  }

  public IVector2 clone()  {
    return new IVector2(this);
  }

  @Override
  public double getHeuristic(Object goal) {
    IVector2 v = (IVector2) goal;
    return Math.hypot(x - v.x, y - v.y);
  }

  @Override
  public double getTraversalCost(Object neighbour) {
    return 1; // TODO tweak
  }

  @Override
  public Set getNeighbours() {
    return pathFindNeighbours;
  }
}
