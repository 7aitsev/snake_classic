package com.example.snakeclassic;

/*
 * Override the default font
 */
public final class App extends android.app.Application {
  @Override
  public void onCreate() {
    super.onCreate();
    FontsOverride.setDefaultFont(this, "MONOSPACE", "pressstart2p.ttf");
  }
}