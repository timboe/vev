package co.uk.timmartin.vev.manager;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import co.uk.timmartin.vev.Param;
import co.uk.timmartin.vev.Util;
import co.uk.timmartin.vev.enums.Colour;
import co.uk.timmartin.vev.enums.Particle;

public class Textures {

  private final Map<String, TextureRegion> flippedMap = new HashMap<String, TextureRegion>();
  private final Map<String, TextureRegion> retexturedMap = new HashMap<String, TextureRegion>();

  private final TextureAtlas atlas = new TextureAtlas(Gdx.files.internal("sprites.txt"));
  private final TextureAtlas ui = new TextureAtlas(Gdx.files.internal("uiskin.atlas"));
  private static Textures ourInstance;

  private final Color redHighlight = new Color(220 / 255f, 138 / 255f, 92 / 255f, 1f);
  private final Color redTexture = new Color(206 / 255f, 101 / 255f, 80 / 255f, 1f);
  private final Color redMain = new Color(176 / 255f, 78 / 255f, 80 / 255f, 1f);
  private final Color redOutline = new Color(136 / 255f, 57 / 255f, 80 / 255f, 1f);
  private final Color redDark = new Color(72 / 255f, 43 / 255f, 81 / 255f, 1f);
  private final Color redBlack = new Color(39 / 255f, 32 / 255f, 49 / 255f, 1f);

  public final EnumMap<Particle, Color> particleBaseColours = new EnumMap<Particle, Color>(Particle.class);

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

  public void updateParticleHues() {
    for (Particle p : Particle.values()) {
      final int hue = Persistence.getInstance().particleHues.get(p);
      particleBaseColours.put(p, convertHSBtoRGB(hue / 360f, Param.HSB_BASE_SATURATION / 100f, Param.HSB_BASE_BRIGHTNESS / 100f));
    }
    loadMultiColouredBall();
  }

  private Color mod(Particle p, int hueMod, int s, int b) {
    int hue = Persistence.getInstance().particleHues.get(p);
    if (hue > 720) { // Final third (bright)
      hue -= 720;
      b += 30;
      s += 30;
    } else if (hue > 360) { // Middle third (norm)
      hue -= 360;
    } else { // Lower third (muted)
      b -= 30;
      s -= 30;
    }
    hue += hueMod;
    if (hue > 360) hue -= 360;
    s = Util.clamp(s, 0, 100);
    b = Util.clamp(b, 0, 100);
    return convertHSBtoRGB(hue / 360f, s / 100f, b / 100f);
  }

  public static Color convertHSBtoRGB(float h, float s, float b) {
    int R = 0;
    int G = 0;
    int B = 0;
    if (s == 0.f) {
      R = G = B = (int) (b * 255.f + .5f);
    } else {
      float hm6h_A = (h - (float) Math.floor(h)) * 6f;
      float hm6h = hm6h_A - (float) Math.floor(hm6h_A);
      float bX1ms = b * (1f - s);
      float bX1msXhm6 = b * (1f - s * hm6h);
      float bX1msX1mhm6 = b * (1f - s * (1f - hm6h));
      switch ((int) hm6h_A) {
        case 0:
          R = (int) (b * 255f + .5f);
          G = (int) (bX1msX1mhm6 * 255f + .5f);
          B = (int) (bX1ms * 255f + .5f);
          break;
        case 1:
          R = (int) (bX1msXhm6 * 255f + .5f);
          G = (int) (b * 255f + .5f);
          B = (int) (bX1ms * 255f + .5f);
          break;
        case 2:
          R = (int) (bX1ms * 255f + .5f);
          G = (int) (b * 255f + .5f);
          B = (int) (bX1msX1mhm6 * 255f + .5f);
          break;
        case 3:
          R = (int) (bX1ms * 255f + .5f);
          G = (int) (bX1msXhm6 * 255f + .5f);
          B = (int) (b * 255f + .5f);
          break;
        case 4:
          R = (int) (bX1msX1mhm6 * 255f + .5f);
          G = (int) (bX1ms * 255f + .5f);
          B = (int) (b * 255f + .5f);
          break;
        case 5:
          R = (int) (b * 255f + .5f);
          G = (int) (bX1ms * 255f + .5f);
          B = (int) (bX1msXhm6 * 255f + .5f);
      }
    }

    return new Color(R / 255f, G / 255f, B / 255f, 1f);
  }

  private Textures() {
    updateParticleHues();
  }

  public TextureAtlas getAtlas() {
    return atlas;
  }

  public TextureAtlas getUIAtlas() {
    return ui;
  }

  public TextureRegion getTexture(String name, boolean flipped) {
    TextureRegion r = retexturedMap.get(name);
    if (r == null) r = atlas.findRegion(name);
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
    // Images in the ATLAS are red

    TextureData data = atlas.getTextures().first().getTextureData();
    data.prepare();
    Pixmap fullMap = data.consumePixmap();
    for (Particle p : Particle.values()) {
      if (p == Particle.kBlank) continue;
      for (int i = 0; i <= Param.N_BALL_SPRITES; ++i) {
        TextureRegion r;
        if (i == Param.N_BALL_SPRITES) r = new TextureRegion(atlas.findRegion("ball_r"));
        else r = new TextureRegion(atlas.findRegion("ball_r_" + i));

        Pixmap pixmap = new Pixmap(r.getRegionWidth(), r.getRegionHeight(), Pixmap.Format.RGBA8888);
        pixmap.drawPixmap(fullMap, 0, 0, r.getRegionX(), r.getRegionY(), r.getRegionWidth(), r.getRegionHeight());

        colourReplace(pixmap, redHighlight, mod(p, Param.HSB_HIGHLIGHT_HUE_MOD, Param.HSB_HIGHLIGHT_SATURATION, Param.HSB_HIGHLIGHT_BRIGHTNESS));

        colourReplace(pixmap, redTexture, mod(p, 0, Param.HSB_BASE_SATURATION, Param.HSB_BASE_BRIGHTNESS));

        colourReplace(pixmap, redOutline, mod(p, Param.HSB_OUTLINE_HUE_MOD, Param.HSB_OUTLINE_SATURATION, Param.HSB_OUTLINE_BRIGHTNESS));
        colourReplace(pixmap, redDark, mod(p, Param.HSB_SHADOW_HUE_MOD, Param.HSB_SHADOW_SATURATION, Param.HSB_SHADOW_BRIGHTNESS));

        Texture newTex = new Texture(pixmap);
        TextureRegion newTexRegion = new TextureRegion(newTex);
        pixmap.dispose();
        Colour c = p.getColourFromParticle();
        if (i == Param.N_BALL_SPRITES) retexturedMap.put("ball_" + c.getString(), newTexRegion);
        else retexturedMap.put("ball_" + c.getString() + "_" + i, newTexRegion);
      }
    }

    TextureRegion r = new TextureRegion(atlas.findRegion("ball_r"));
    Pixmap pixmap = new Pixmap(r.getRegionWidth(), r.getRegionHeight(), Pixmap.Format.RGBA8888);
    pixmap.drawPixmap(fullMap, 0, 0, r.getRegionX(), r.getRegionY(), r.getRegionWidth(), r.getRegionHeight());
    colourReplace(pixmap, redHighlight, new Color(192 / 255f, 192 / 255f, 192 / 255f, 1f));
    colourReplace(pixmap, redTexture, new Color(128 / 255f, 128 / 255f, 128 / 255f, 1f));
    colourReplace(pixmap, redOutline, new Color(64 / 255f, 64 / 255f, 64 / 255f, 1f));
    Texture newTex = new Texture(pixmap);
    TextureRegion newTexRegion = new TextureRegion(newTex);
    retexturedMap.put("ball_grey", newTexRegion);
    pixmap.dispose();

    fullMap.dispose();
    data.disposePixmap();

    if (GameState.constructed()) {
      GameState.getInstance().retextureSprites();
    }
    if (IntroState.constructed()) {
      IntroState.getInstance().retextureSprites();
    }
    if (UIIntro.constructed()) {
      UIIntro.getInstance().retextureSprites();
    }
  }

  private int[] getColorFromHex(Color c) {
    int[] colourInt = {(int) (255 * c.r), (int) (255 * c.g), (int) (255 * c.b)};
    return colourInt;
  }

  private void colourReplace(Pixmap pixmap, Color from, Color to) {
    pixmap.setColor(to);
    Color color = new Color();
    int[] fromInt = getColorFromHex(from);
    for (int y = 0; y < pixmap.getHeight(); y++) {
      for (int x = 0; x < pixmap.getWidth(); x++) {
        Color.rgba8888ToColor(color, pixmap.getPixel(x, y));
        int[] colorInt = getColorFromHex(color);
        if (fromInt[0] == colorInt[0] && fromInt[1] == colorInt[1] && fromInt[2] == colorInt[2]) {
          pixmap.fillRectangle(x, y, 1, 1);
        }
      }
    }
  }

}


