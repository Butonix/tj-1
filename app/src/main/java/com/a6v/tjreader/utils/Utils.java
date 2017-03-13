package com.a6v.tjreader.utils;

import android.os.Bundle;

public final class Utils {
  private Utils() {
    throw new AssertionError("No instances.");
  }

  public static <T extends Enum<T>> T readEnumValueFromBundle(Bundle bundle, String key, T[] values,
    T defaultValue)
  {
    return values[bundle.getInt(key, defaultValue.ordinal())];
  }
}
