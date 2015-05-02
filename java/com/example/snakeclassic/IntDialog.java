package com.example.snakeclassic;

/**
 * Interface for DialogQuit and ActHome connection. It provides the general
 * method startNewGame() that invokes when a player wants to start a new game
 * after he loses or wins.
 * Created by Maxim on 27.04.2015.
 */
interface IntDialog {
  public void startNewGame();
  public final String title = "dialogTitle";
}
