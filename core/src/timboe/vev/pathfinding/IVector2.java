package timboe.vev.pathfinding;

import com.badlogic.gdx.Gdx;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.awt.font.GlyphJustificationInfo;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;
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
    return json;
  }

  public JSONObject serialiseTile() throws JSONException {
    JSONObject json = serialise();
    JSONObject n = new JSONObject();
    int count = 0;
    if (pathFindNeighbours != null) {
      for (IVector2 v : pathFindNeighbours) {
        JSONObject sub = new JSONObject();
        sub.put("x", v.x);
        sub.put("y", v.y);
        n.put(Integer.toString(count), sub);
        ++count;
      }
    }
    json.put("n", n);
    return json;
  }

  public IVector2(JSONObject json) throws JSONException {
   this.x = json.getInt("x");
   this.y = json.getInt("y");
   if (json.has("n")) {
     pathFindNeighbours = new HashSet<IVector2>();
     JSONObject n = json.getJSONObject("n");
     Iterator it = n.keys();
     while (it.hasNext()) {
       JSONObject sub = n.getJSONObject((String) it.next());
       pathFindNeighbours.add( new IVector2( sub ) );
     }
   }
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
    if (pathFindNeighbours == null) {
      Gdx.app.error("Null Err","No neighbours! I am:" + this);
    }
    return pathFindNeighbours;
  }
}
