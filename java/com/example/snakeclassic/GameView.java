package com.example.snakeclassic;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Game view in which everything will take place
 * Created by Maxim on 10.04.2015.
 */
public class GameView extends SurfaceView
                      implements SurfaceHolder.Callback {
  private final GameThread thread;
//  private InterField.Snake snake;

  public GameView(Context context, AttributeSet attrs) {
    super(context, attrs);
    getHolder().addCallback(this);

    // create thread only: it's started in surfaceCreated()
    thread = new GameThread(getHolder(), context);

//    // get pointer to the snake
//    snake = thread.getSnake();
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width,
                             int height) {  }

  /**
   * Callback invoked when the Surface has been created and is ready to be used
   */
  @Override
  public void surfaceCreated(SurfaceHolder holder) {
    if(thread.isInBackground()) {
      thread.setInBackground(false);
    }
    else {
      thread.setGameFieldSizes(getMeasuredWidth(), getMeasuredHeight());
      thread.setRunning(true);
      thread.start();
    }
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder) {
    thread.pause();
    thread.setInBackground(true);
  }

  /**
   * Fetches the animation thread corresponding to this GameView
   * @return the animation thread
   */
  public GameThread getThread() {
    return thread;
  }

//  public void changeDir(int key) {
//    snake = thread.getSnake();
//    snake.changeDir(key);
//  }

  /**
   * Standard window-focus override. Notice focus lost so we can pause on focus
   * lost. e.g. user switches to take a call
   */
  @Override
  public void onWindowFocusChanged(boolean hasWindowFocus) {
    if(!hasWindowFocus) thread.pause();
  }
}