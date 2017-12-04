package timboe.destructor.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import timboe.destructor.Param;
import timboe.destructor.enums.Colour;

import java.util.HashMap;
import java.util.Map;

public class Textures {

  private Map<String, TextureRegion> flippedMap = new HashMap<String, TextureRegion>();
  private Map<String, TextureRegion> retexturedMap = new HashMap<String, TextureRegion>();

  private TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("sprites.txt"));
  private TextureAtlas ui = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
  private static Textures ourInstance;

  public static Textures getInstance() {
    return ourInstance;
  }

  public static void create() {
    ourInstance = new Textures();
  }

  public void dispose() {
    flippedMap.clear();
    retexturedMap.clear();
    atlas.dispose();
    ui.dispose();
    ourInstance = null;
  }

  private Textures() {
    loadMultiColouredBall();
  }

  public TextureAtlas getAtlas() {
    return atlas;
  }

  public TextureAtlas getUIAtlas() {
    return ui;
  }

  public TextureRegion getTexture(String name, boolean flipped) {
    TextureRegion r = atlas.findRegion(name);
    if (r == null) r = retexturedMap.get(name);
    if (flipped) {
      if (!flippedMap.containsKey(name)) {
        TextureRegion newRegion = new TextureRegion(r);
        newRegion.flip(true, false);
        flippedMap.put(name, newRegion);
      }
      return flippedMap.get(name);
    }
    return r;
  }

  private void loadMultiColouredBall() {
    final int redHighlight[] = {220, 138, 92}; //= new Color(220/255f, 138/255f, 92/255f, 1f);
    final int redTexture[] = {206, 101, 80};
    final int redMain[] = {176, 78, 80};
    final int redShadow[] = {136, 57, 80};
    final int redDark[] = {72, 43, 81};
    final int redBlack[] = {39, 32, 49};
//
//    final int redHighlight[] = {220, 138, 92}; //= new Color(220/255f, 138/255f, 92/255f, 1f);
//    final int redTexture = new Color(206/255f, 101/255f, 80/255f, 1f);
//    final int redMain = new Color(176/255f, 78/255f, 80/255f, 1f);
//    final int Color redShadow = new Color(136/255f, 57/255f, 80/255f, 1f);
//    final int Color redDark = new Color(72/255f, 43/255f, 81/255f, 1f);
//    Color redBlack = new Color(39/255f, 32/255f, 49/255f, 1f);

    TextureData data = atlas.getTextures().first().getTextureData();
    data.prepare();
    Pixmap fullMap = data.consumePixmap();

    for (Colour c : Colour.values()) {
      for (int i = 0; i < Param.N_BALLS; ++i) {
        TextureRegion r = new TextureRegion(atlas.findRegion("ball_r_" + i));

        Pixmap pixmap = new Pixmap(r.getRegionWidth(), r.getRegionHeight(), Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(fullMap, 0, 0, r.getRegionX(), r.getRegionY(), r.getRegionWidth(), r.getRegionHeight());

        Color changeHighlight = new Color(147/255f, 178/255f, 155/255f, 1f);
        Color changeTexture = new Color(101/255f, 143/255f, 135/255f, 1f);
        Color changeMain = new Color(58/255f, 91/255f, 106/255f, 1f);
        Color changeShadow = new Color(45/255f, 59/255f, 89/255f, 1f);
        Color changeDark = new Color(50/255f, 43/255f, 81/255f, 1f);
        Color changeBlack = new Color(39/255f, 32/255f, 49/255f,1f);
        colourReplace(pixmap, redHighlight, changeHighlight);
        colourReplace(pixmap, redTexture, changeTexture);
        colourReplace(pixmap, redMain, changeMain);
        colourReplace(pixmap, redShadow, changeShadow);
        colourReplace(pixmap, redDark, changeDark);
        colourReplace(pixmap, redBlack, changeBlack);

        Texture newTex = new Texture(pixmap);
        TextureRegion newTexRegion = new TextureRegion(newTex);
        pixmap.dispose();

        retexturedMap.put("ball_" + c.getString() + "_" + i, newTexRegion);
      }
    }
    fullMap.dispose();
    data.disposePixmap();
  }

  private int[] getColorFromHex(Color c) {
    int colourInt[] = {(int)(255 * c.r), (int)(255 * c.g), (int)(255 * c.b)};
    return colourInt;
  }

  private void colourReplace(Pixmap pixmap, int from[], Color to) {
    pixmap.setColor(to);
    for (int y = 0; y < pixmap.getHeight(); y++) {
      for (int x = 0; x < pixmap.getWidth(); x++) {
        Color color = new Color();
        Color.rgba8888ToColor(color, pixmap.getPixel(x, y));
        int colorInt[] = getColorFromHex(color);
        if (from[0] == colorInt[0] && from[1] == colorInt[1] && from[2] == colorInt[2]) {
          pixmap.fillRectangle(x, y, 1, 1);
        }
      }
    }
  }

}

