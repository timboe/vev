package timboe.destructor.enums;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import timboe.destructor.Param;

/**
 * Created by Tim on 01/01/2018.
 */

public enum Particle {
  kH,
  kW,
  kZ,
  kE,
  kM,
  kQ;

  private static final List<Particle> values = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int size = values.size();
  private static final Random R = new Random();

  public static Particle random() {
    return values.get(R.nextInt(size));
  }

  public String getString() {
    switch (this) {
      case kH: return "H";
      case kW: return "W";
      case kZ: return "Z";
      case kE: return "e";
      case kM: return "μ";
      case kQ: return "q";
      default: return "?";
    }
  }

  public static Particle getParticleFromColour(Colour c) {
    switch (c) {
      case kBLACK: return kH;
      case kRED_DARK: return kW;
      case kGREEN_DARK: return kZ;
      case kRED: return kE;
      case kGREEN: return kM;
      case kBLUE: return kQ;
      default: return null;
    }
  }

  public Color getHighlightColour() {
    switch (this) {
      case kH: return Param.PARTICLE_H;
      case kW: return Param.PARTICLE_W;
      case kZ: return Param.PARTICLE_Z;
      case kE: return Param.PARTICLE_E;
      case kM: return Param.PARTICLE_M;
      case kQ: return Param.PARTICLE_Q;
      default:
        Gdx.app.error("getHighlightColour","Unknown particle " + getString());
        return null;
    }
  }

  public int getStandingOrderOffset() {
    switch (this) {
      case kH: return 0;
      case kW: return 2;
      case kZ: return 4;
      case kE: return 6;
      case kM: return 8;
      case kQ: return 10;
      default: return 0;
    }
  }

  public Colour getColourFromParticle() {
    switch (this) {
      case kH: return Colour.kBLACK;
      case kW: return Colour.kRED_DARK;
      case kZ: return Colour.kGREEN_DARK;
      case kE: return Colour.kRED;
      case kM: return Colour.kGREEN;
      case kQ: return Colour.kBLUE;
      default: return null;
    }
  }

  // TODO tweak
  public int getCreateEnergy() {
    switch (this) {
      case kH: return 25;
      case kW: return 25;
      case kZ: return 25;
      case kE: return 25;
      case kM: return 25;
      case kQ: return 25;
      default: return 0;
    }
  }

  public static String getStringFromColour(Colour c) {
    return getParticleFromColour(c).getString();
  }

}
