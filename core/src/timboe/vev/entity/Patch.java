package timboe.vev.entity;

import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import timboe.vev.Util;
import timboe.vev.enums.Particle;
import timboe.vev.manager.GameState;
import timboe.vev.manager.World;
import timboe.vev.pathfinding.IVector2;

/**
 * Created by Tim on 21/01/2018.
 */

public class Patch extends Entity {
  Vector<Integer> contained = new Vector<Integer>();

  public JSONObject serialise() throws JSONException {
    JSONObject json = super.serialise(false);
    JSONObject jsonContained = new JSONObject();
    int count = 0;
    for (Integer i : contained) {
      jsonContained.put(Integer.toString(count++), i);
    }
    json.put("contained", jsonContained);
    return json;
  }

  public Patch(JSONObject json) throws JSONException {
    super(json);
    JSONObject jsonContained = json.getJSONObject("contained");
    Iterator it = jsonContained.keys();
    while (it.hasNext()) {
      contained.add( jsonContained.getInt((String) it.next()) );
    }
  }

  public void addContained(Entity e) {
    contained.add(e.id);
  }

  public int remaining() {
    return contained.size();
  }

  public int removeRandom() {
    if (contained.size() == 0) {
      return 0;
    }
    int index = Util.R.nextInt( remaining() );
    int id = contained.elementAt(index);
    contained.remove(index); // From patch's shards
    Sprite t = World.getInstance().tiberiumShards.remove(id); // From global list of shards
    t.remove(); // From stage
    ++GameState.getInstance().tiberiumMined;
    return id;
  }

  public Patch(int x, int y) {
    super(x,y);
  }
}
