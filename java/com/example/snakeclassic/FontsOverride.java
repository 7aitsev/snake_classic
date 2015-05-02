package com.example.snakeclassic;

import java.lang.reflect.Field;

import android.content.Context;
import android.graphics.Typeface;

/**
 * Created by Maxim on 08.04.2015.
 */

public final class FontsOverride {

  public static void setDefaultFont(Context context,
                                    String staticTypefaceFieldName,
                                    String fontAssetName) {
    final Typeface regular = Typeface.createFromAsset(context.getAssets(),
        fontAssetName);
    replaceFont(staticTypefaceFieldName, regular);
  }

  private static void replaceFont(String staticTypefaceFieldName,
                                    final Typeface newTypeface) {
    try {
      final Field staticField = Typeface.class
          .getDeclaredField(staticTypefaceFieldName);
      staticField.setAccessible(true);
      try {
        staticField.set(null, newTypeface);
      } catch (IllegalAccessException e) {
        e.printStackTrace();
      }
    } catch (NoSuchFieldException e) {
      e.printStackTrace();
    }
  }
}

/* This solution was provided by weston (stackoverflow.com/users/360211/weston).
 * For more details: stackoverflow.com/question/2711858
 */