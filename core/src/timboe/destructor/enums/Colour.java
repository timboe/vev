package timboe.destructor.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum Colour {
  kRED,
  kRED_DARK,
  kGREEN,
  kGREEN_DARK,
  kBLUE,
  kBLACK;

  private static final List<Colour> values = Collections.unmodifiableList(Arrays.asList(values()));
  private static final int size = values.size();
  private static final Random R = new Random();

  public static Colour random() {
    return values.get(R.nextInt(size));
  }

  public String getString() {
    switch (this) {
      case kRED: return "r";
      case kRED_DARK: return "r_dark";
      case kGREEN: return "g";
      case kGREEN_DARK: return "g_dark";
      case kBLUE: return "blue";
      default: return "b";
    }
  }
}
