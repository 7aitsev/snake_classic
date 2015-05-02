package com.example.snakeclassic;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Message;
import android.view.SurfaceHolder;
import android.os.Handler;

import java.util.concurrent.TimeUnit;

/**
 * Game's thread that updates depend on periodicity snake's and fruit's state
 * and than draws everything on the GameView using Canvas
 * Created by Maxim on 10.04.2015.
 */
class GameThread extends Thread {
  private InterField field;
  private InterField.Snake snake;
  private InterField.Fruit fruit;

  /*
   * State-tracking constants
   */
  public static final byte STATE_LOSE = 1,
                           STATE_PAUSE = 2,
                           STATE_READY = 3,
                           STATE_RUNNING = 4,
                           STATE_WIN = 5;
  /*
   * Other constants
   */
  private final String KEY_GAME_TIME = "gameTime",
                       KEY_GAME_START_TIME = "gameStartTime",
                       KEY_PREV_TIME = "prevTime",
                       KEY_HEIGHT = "height",
                       KEY_WIDTH = "width",
                       KEY_SCALE = "scale",
                       KEY_MODE = "mode",
                       KEY_FIELD = "field",
                       KEY_GOD_MODE = "godMode",
                       KEY_PERIODICITY = "periodicity";

  private final SurfaceHolder surfaceHolder;
  private final Bitmap wall_pic,
                       body_pic,
                       fruit_pic;

  /**
   * Message handler used by thread to interact with TextView(s) or to show
   * dialogs
   */
  private Handler handler;

  /** Indicate whether the surface has been created and is ready to draw */
  private boolean running,
                  inBackground;
  private final Object runLock = new Object();

  /** The state of the game. One of READY, RUNNING, PAUSE, LOSE, OR WIN */
  private byte mode;
  private boolean STATE_GOD_MODE = false;

  private final int N = 12, M = 17;
  private float W, H, SCALE;

  private long prevTime, gameStartTime, gameTime,
               periodicity = 250L;

  public GameThread(SurfaceHolder surfaceHolder, Context context) {
    this.surfaceHolder = surfaceHolder;
    Resources resources = context.getResources();

    wall_pic = BitmapFactory.decodeResource(resources, R.drawable.wall);
    body_pic = BitmapFactory.decodeResource(resources, R.drawable.snake);
    fruit_pic = BitmapFactory.decodeResource(resources, R.drawable.fruit);

    field = new InterField(N, M);
    snake = field.getSnake();
    fruit = field.getFruit();
  }

  void setHandler(Handler h) {
    handler = h;
  }

  private long getMills(long t) {
    return TimeUnit.MILLISECONDS.convert(t, TimeUnit.NANOSECONDS);
  }

  public InterField.Snake getSnake() {
    return snake;
  }

  public void doStart() {
    synchronized (surfaceHolder) {
      gameStartTime = gameTime = prevTime = getMills(System.nanoTime());
      setState(STATE_RUNNING);
      fruit.newFruit();
    }
  }

  public void resetGame() {
    field = new InterField(N, M);
    snake = field.getSnake();
    fruit = field.getFruit();
    gameTime = 0L;

    showScoreAndTime();
    setState(STATE_READY);
  }

  /**
   * Used to signal the thread whether it should be running or not.
   * Passing true allows the thread tor run; passing false will shut it down
   * if it's already running. Calling start() after this was most recently
   * called with false will result in an immediate shutdown.
   * @param b true to run, false to shut down
   */
  public void setRunning(boolean b) {
    synchronized (runLock) {
      running = b;
    }
  }

  public void setState(byte mode) {
    synchronized (surfaceHolder) {
      this.mode = mode;
    }
  }

  public byte getGameState() {
    synchronized (surfaceHolder) {
      return mode;
    }
  }

  public Bundle saveState(Bundle map) {
    synchronized (surfaceHolder) {
      if(map != null) {
        map.putLong(KEY_GAME_TIME, gameTime);
        map.putLong(KEY_GAME_START_TIME, gameStartTime);
        map.putLong(KEY_PREV_TIME, prevTime);
        map.putFloat(KEY_HEIGHT, H);
        map.putFloat(KEY_WIDTH, W);
        map.putFloat(KEY_SCALE, SCALE);
        map.putByte(KEY_MODE, mode);
        map.putParcelable(KEY_FIELD, field);
        map.putBoolean(KEY_GOD_MODE, STATE_GOD_MODE);
        map.putLong(KEY_PERIODICITY, periodicity);
      }
    }
    return map;
  }

  public synchronized void restoreState(Bundle savesState) {
    synchronized (surfaceHolder) {
      setState(STATE_PAUSE);
      // restore data from Bundle
      gameTime = savesState.getLong(KEY_GAME_TIME);
      gameStartTime = savesState.getLong(KEY_GAME_START_TIME);
      prevTime = savesState.getLong(KEY_PREV_TIME);
      H = savesState.getFloat(KEY_HEIGHT);
      W = savesState.getFloat(KEY_WIDTH);
      SCALE = savesState.getFloat(KEY_SCALE);
      mode = savesState.getByte(KEY_MODE);
      field = savesState.getParcelable(KEY_FIELD);
      snake = field.getSnake();
      fruit = field.getFruit();
      STATE_GOD_MODE = savesState.getBoolean(KEY_GOD_MODE);
      periodicity = savesState.getLong(KEY_PERIODICITY);

      // initialize score and time on the game's activity
      showScoreAndTime();
    }
  }

  public void setGameFieldSizes(int w, int h) {
    SCALE = (float)(w-1)/(float)N;
    while((H = SCALE*M) > h) {
      SCALE-=0.1;
    }
    W=SCALE*N;
  }

  private void showScoreAndTime() {
    Message msg = handler.obtainMessage(ActSnakeGame.TOUCH_INFO);
    Bundle b = new Bundle();
    String score = String.format("%d", snake.getScore());
    String time = String.format("%02d:%02d",
        TimeUnit.MINUTES.convert(gameTime, TimeUnit.MILLISECONDS),
        TimeUnit.SECONDS.convert(gameTime, TimeUnit.MILLISECONDS) % 60);
    b.putString("score", score);
    b.putString("time", time);
    msg.setData(b);
    handler.sendMessage(msg);
  }

  private void updateScoreAndTime() {
    long now = getMills(System.nanoTime());
    gameTime = now - gameStartTime;
    showScoreAndTime();
  }

  private void updatePhysics() {
    long now = getMills(System.nanoTime());

    // Do nothing if prevTime is in the future
    if(prevTime > now) return;

    long elapsedTime = now - prevTime;
    if(elapsedTime > periodicity) {
      if(STATE_GOD_MODE)
        snake.easyWin();
      else
        snake.tick();
      updateScoreAndTime();
      prevTime = now;
    }

    byte state = snake.checkState();
    if(state != 0) {
      setState(state);
      sendMsg(ActSnakeGame.TOUCH_DIALOG, state);
      if(state == STATE_WIN) {
        periodicity = 250L;
        STATE_GOD_MODE = false;
      }
      //setRunning(false); // newer do this!
    }
  }

  private void sendMsg(byte to, byte extra) {
    Message m = handler.obtainMessage(to);
    m.arg1 = (int) extra;
    handler.sendMessage(m);
  }

//  void drawField(Canvas canvas) {
//    p.setColor(resources.getColor(R.color.light_purple));
//    for(float i = 0; i <= W; i += SCALE) {
//      canvas.drawLine(i, 0, i, H, p);
//    }
//    for(float i = 0; i<=H; i+=SCALE) {
//      canvas.drawLine(0, i, W, i, p);
//    }
//  }

  private void drawFence(Canvas canvas) {
    for(float i = 0; i < W-SCALE/2; i += SCALE) {
      canvas.drawBitmap(wall_pic,null, new RectF(i, 0, i+SCALE, SCALE),null);
      canvas.drawBitmap(wall_pic,null, new RectF(i, H-SCALE, i+SCALE, H),null);
    }
    for(float i = 0; i < H-SCALE/2; i += SCALE) {
      canvas.drawBitmap(wall_pic,null, new RectF(0, i, SCALE, i+SCALE),null);
      canvas.drawBitmap(wall_pic,null, new RectF(W-SCALE, i, W, i+SCALE),null);
    }
  }

  private void drawSnake(Canvas canvas) {
    for(int i = 0; i < snake.size; ++i) {
      canvas.drawBitmap(body_pic, null, new RectF(
          (float)(snake.s[i].x)*SCALE,
          (float)(snake.s[i].y)*SCALE,
          (float)(snake.s[i].x+1)*SCALE,
          (float)(snake.s[i].y+1)*SCALE), null);
    }
  }

  private void drawFruit(Canvas canvas) {
    canvas.drawBitmap(fruit_pic, null, new RectF(
        (float)(fruit.x+0.1)*SCALE, (float)(fruit.y+0.1)*SCALE,
        (float)(fruit.x+0.9)*SCALE, (float)(fruit.y+0.9)*SCALE), null);
  }

  private void display(Canvas canvas) {
    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    //drawField(canvas);
    drawFence(canvas);
    drawSnake(canvas);
    drawFruit(canvas);
  }

  @Override
  public void run() {
    while(running) {
      synchronized (runLock) {
        while(inBackground) {
          try {
            runLock.wait();
          } catch (final InterruptedException e) {
            // Ignore error here
          }
        }
      }
      Canvas canvas = null;
      try{
        canvas = surfaceHolder.lockCanvas(null);
        if(canvas == null) continue;
        synchronized(surfaceHolder) {
          if(mode == STATE_RUNNING)
            updatePhysics();
          synchronized (runLock) {
            if(running)
              display(canvas);
          }
        }
      } finally {
          if(canvas != null) {
            surfaceHolder.unlockCanvasAndPost(canvas);
          }
      }
    }
  }

  /**
   * Pauses the physics update, animation and saves interruption time
   */
  public void pause() {
    synchronized (surfaceHolder) {
      if(mode == STATE_RUNNING) {
        prevTime = getMills(System.nanoTime());
        mode = STATE_PAUSE;
      }
    }
    sendMsg(ActSnakeGame.TOUCH_BUTTON_PAUSE, STATE_PAUSE);
  }

  /**
   * Resumes from a pause and offsets the gameStartTime according to a
   * waiting time
   */
  public void unpause() {
    synchronized (surfaceHolder) {
      // 300 is a delay after resuming
      long now = getMills(System.nanoTime()) + 300;
      prevTime = now - prevTime; // waiting time
      gameStartTime += prevTime;
      prevTime = now;
    }
    setState(STATE_RUNNING);
    sendMsg(ActSnakeGame.TOUCH_BUTTON_PAUSE, STATE_RUNNING);
  }

  public void setInBackground(boolean b) {
    synchronized (runLock) {
      inBackground = b;
      if(!b)
        runLock.notifyAll();
    }
  }

  public boolean isInBackground() {
    return inBackground;
  }

  public void runEasterEgg() {
    periodicity = 5L;
    STATE_GOD_MODE = true;
    gameStartTime = gameTime = prevTime = getMills(System.nanoTime());
    snake.prepareForEasyWin();
  }
}