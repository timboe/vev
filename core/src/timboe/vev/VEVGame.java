package timboe.vev;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.FieldSerializer;
import com.google.gwt.thirdparty.json.JSONException;
import com.google.gwt.thirdparty.json.JSONObject;

import java.io.IOException;

import timboe.vev.entity.Entity;
import timboe.vev.manager.Camera;
import timboe.vev.manager.GameState;
import timboe.vev.manager.Sounds;
import timboe.vev.manager.Textures;
import timboe.vev.manager.UI;
import timboe.vev.manager.World;

public class VEVGame extends Game {

  @Override
  public void create () {
    Camera.create();
    Textures.create();
    GameState.create();
    Sounds.create();
    World.create();
    UI.create();

    GameState.getInstance().setGame(this);
    GameState.getInstance().setToTitleScreen();
    Sounds.getInstance().doMusic();
	}
	
  @Override
  public void dispose () {
//    try {
//      persist();
//    } catch (IOException ex) {
//      Gdx.app.error("persist IO exception",ex.getMessage());
//    } catch (ClassNotFoundException ex) {
//      Gdx.app.error("persist class exception",ex.getMessage());
//    }
    boolean isLocAvailable = Gdx.files.isLocalStorageAvailable();
    if (isLocAvailable) {
      FileHandle handle = Gdx.files.local("data/VEV_save.json");
      try {
        JSONObject json = new JSONObject();
        json.put("GameState", GameState.getInstance().serialise());
        handle.writeString(json.toString(), false);
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    UI.getInstance().dispose();
    Textures.getInstance().dispose();
    Sounds.getInstance().dispose();
    World.getInstance().dispose();
    Camera.getInstance().dispose();
    GameState.getInstance().dispose();
  }

  private void persist() throws IOException, ClassNotFoundException {
//    Kryo kryo = new Kryo();
//    kryoReg(kryo);
//
//    Output output = new Output(new FileOutputStream("VEV_save.bin"));
//    kryo.writeObject(output, World.getInstance().getTile(0,0));
//    output.close();
  }

  private void kryoReg(Kryo kryo) {

    kryo.setReferences(true);

    FieldSerializer<Entity> fsTile = new FieldSerializer<Entity>(kryo, Entity.class);
    fsTile.setCopyTransient(false);
    kryo.register(Entity.class, fsTile);

    kryo.register(Array.class, new Serializer<Array>() {
      {
        setAcceptsNull(true);
      }

      private Class genericType;

      public void setGenerics (Kryo kryo, Class[] generics) {
        if (generics != null && kryo.isFinal(generics[0])) genericType = generics[0];
        else genericType = null;
      }

      public void write (Kryo kryo, Output output, Array array) {
        int length = array.size;
        output.writeInt(length, true);
        if (length == 0) {
          genericType = null;
          return;
        }
        if (genericType != null) {
          Serializer serializer = kryo.getSerializer(genericType);
          genericType = null;
          for (Object element : array)
            kryo.writeObjectOrNull(output, element, serializer);
        } else {
          for (Object element : array)
            kryo.writeClassAndObject(output, element);
        }
      }

      public Array read (Kryo kryo, Input input, Class<Array> type) {
        Array array = new Array();
        kryo.reference(array);
        int length = input.readInt(true);
        array.ensureCapacity(length);
        if (genericType != null) {
          Class elementClass = genericType;
          Serializer serializer = kryo.getSerializer(genericType);
          genericType = null;
          for (int i = 0; i < length; i++)
            array.add(kryo.readObjectOrNull(input, elementClass, serializer));
        } else {
          for (int i = 0; i < length; i++)
            array.add(kryo.readClassAndObject(input));
        }
        return array;
      }
    });

    kryo.register(IntArray.class, new Serializer<IntArray>() {
      {
        setAcceptsNull(true);
      }

      public void write (Kryo kryo, Output output, IntArray array) {
        int length = array.size;
        output.writeInt(length, true);
        if (length == 0) return;
        for (int i = 0, n = array.size; i < n; i++)
          output.writeInt(array.get(i), true);
      }

      public IntArray read (Kryo kryo, Input input, Class<IntArray> type) {
        IntArray array = new IntArray();
        kryo.reference(array);
        int length = input.readInt(true);
        array.ensureCapacity(length);
        for (int i = 0; i < length; i++)
          array.add(input.readInt(true));
        return array;
      }
    });

    kryo.register(FloatArray.class, new Serializer<FloatArray>() {
      {
        setAcceptsNull(true);
      }

      public void write (Kryo kryo, Output output, FloatArray array) {
        int length = array.size;
        output.writeInt(length, true);
        if (length == 0) return;
        for (int i = 0, n = array.size; i < n; i++)
          output.writeFloat(array.get(i));
      }

      public FloatArray read (Kryo kryo, Input input, Class<FloatArray> type) {
        FloatArray array = new FloatArray();
        kryo.reference(array);
        int length = input.readInt(true);
        array.ensureCapacity(length);
        for (int i = 0; i < length; i++)
          array.add(input.readFloat());
        return array;
      }
    });

    kryo.register(Color.class, new Serializer<Color>() {
      public Color read (Kryo kryo, Input input, Class<Color> type) {
        Color color = new Color();
        Color.rgba8888ToColor(color, input.readInt());
        return color;
      }

      public void write (Kryo kryo, Output output, Color color) {
        output.writeInt(Color.rgba8888(color));
      }
    });
  }
}
