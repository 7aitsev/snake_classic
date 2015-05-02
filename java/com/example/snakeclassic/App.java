package com.example.snakeclassic;

/**
 * Created by Maxim on 08.04.2015.
 */

public final class App extends android.app.Application {
  @Override
  public void onCreate() {
    super.onCreate();
    FontsOverride.setDefaultFont(this, "MONOSPACE", "pressstart2p.ttf");
  }
}