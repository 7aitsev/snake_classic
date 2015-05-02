package com.example.snakeclassic;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * This id a SnakeGame activity that houses a single GameView
 * Created by Maxim on 08.04.2015.
 */
public class ActSnakeGame extends Activity
                          implements IntDialog {
  /** a handle to the the View in which the game is running */
  private GameView mGameView;
  /** a handle to the thread that's actually running the animation */
  private GameThread mGameThread;
  /** a handle to the snake which has created in the GameThread */
  private InterField.Snake snake;
  /**
   * public constants that define the ultimate goal of messages which are sent
   * to a Handler
   */
  public static final byte TOUCH_INFO = 1,
                           TOUCH_DIALOG = 2,
                           TOUCH_BUTTON_PAUSE = 3;

  private Button but_pause;
  private final String KEY_PAUSE = "bPause";

  /*
   * To serve an Easter Egg
   */
  private static byte numOfClicks = 0;
  private static long prevTime = 0L;

  /**
   * Invoked during when the Activity is created
   *
   * @param savedInstanceState a Bundle containing state saved from a
   *        previous execution, or null if this is a new execution
   */
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.play);

    Handler mHandler = new Handler(new Handler.Callback() {
      @Override
      public boolean handleMessage(Message msg) {
        switch(msg.what) {
          case TOUCH_INFO : {
            TextView txtTime = (TextView) findViewById(R.id.time);
            txtTime.setText(msg.getData().getString("time"));
            TextView txtScore = (TextView) findViewById(R.id.score);
            txtScore.setText(msg.getData().getString("score"));
            break;
          }
          case TOUCH_DIALOG: {
            Bundle b = new Bundle();
            String dialogText;
            if(msg.arg1 == GameThread.STATE_LOSE)
              dialogText = getString(R.string.txt_game_over);
            else
              dialogText = getString(R.string.txt_you_win);
            b.putString(IntDialog.title, dialogText);
            DialogState dialogLose = new DialogState();
            dialogLose.setArguments(b);
            dialogLose.show(getFragmentManager(), "empty string\0");
            but_pause.setVisibility(View.INVISIBLE);
            break;
          }
          case TOUCH_BUTTON_PAUSE: {
            if(msg.arg1 == GameThread.STATE_PAUSE)
              but_pause.setBackgroundResource(R.drawable.but_resume);
            else
              but_pause.setBackgroundResource(R.drawable.but_pause);
            break;
          }
        }
        return true;
      }
    });

    // get handles to the GameView form XML, and its GameThread
    mGameView = (GameView) findViewById(R.id.game_field);
    // set SurfaceView on top to make it transparent
    mGameView.setZOrderOnTop(true);
    mGameView.getHolder().setFormat(PixelFormat.TRANSPARENT);
    // get pointer to the game thread
    mGameThread = mGameView.getThread();
    // add a handler
    mGameThread.setHandler(mHandler);

    if(savedInstanceState == null) {
      // we were just launched: set up a new game
      mGameThread.setState(GameThread.STATE_READY);
    }
    else {
      // we are being restored: resume a previous game
      mGameThread.restoreState(savedInstanceState);
    }

    if(mGameThread.getGameState() == GameThread.STATE_READY) {
      findViewById(R.id.layout_start_screen).setVisibility(View.VISIBLE);
    }

//    final Button but_left = (Button) findViewById(R.id.left);
//    but_left.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        mGameView.changeDir(-1);
//      }
//    });
//    final Button but_right = (Button) findViewById(R.id.right);
//    but_right.setOnClickListener(new View.OnClickListener() {
//      @Override
//      public void onClick(View v) {
//        mGameView.changeDir(1);
//      }
//    });

    but_pause = (Button) findViewById(R.id.but_pause);
    but_pause.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if(GameThread.STATE_RUNNING == mGameThread.getGameState()) {
          mGameThread.pause();
        }
        else if(GameThread.STATE_PAUSE == mGameThread.getGameState()) {
          mGameThread.unpause();
        }

        long now = System.currentTimeMillis();
        if(numOfClicks == 4) {
          mGameThread.runEasterEgg();
          mGameThread.unpause();
          numOfClicks = 0;
        } else if(now - prevTime < 500)
          ++numOfClicks;
        prevTime = now;
      }
    });
    if(savedInstanceState == null) {
      but_pause.setVisibility(View.INVISIBLE);
    }
    else {
      if(View.VISIBLE == savedInstanceState.getInt(KEY_PAUSE))
        but_pause.setVisibility(View.VISIBLE);
      else
        but_pause.setVisibility(View.INVISIBLE);
      but_pause.setBackgroundResource(R.drawable.but_resume);
    }

    final Button but_start = (Button) findViewById(R.id.but_start);
    but_start.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        findViewById(R.id.layout_start_screen).setVisibility(View.INVISIBLE);
        but_pause.setVisibility(View.VISIBLE);
        but_pause.setBackgroundResource(R.drawable.but_pause);
        mGameThread.doStart();
      }
    });
    Log.w("ActSnakeGame", "onCreate");

    // set the swipe listener
    snake = mGameThread.getSnake();
    mGameView.setOnTouchListener(new OnSwipeTouchListener(this) {
      public void onSwipeTop() {
        snake.changeDir(InterField.Snake.up);
      }
      public void onSwipeLeft() {
        snake.changeDir(InterField.Snake.left);
      }
      public void onSwipeBottom() {
        snake.changeDir(InterField.Snake.down);
      }
      public void onSwipeRight() {
        snake.changeDir(InterField.Snake.right);
      }
    });
  }

  /**
   * Invoked when the Activity loses user focus
   */
  @Override
  protected void onPause() {
    mGameThread.pause();
    super.onPause();
    Log.w("ActSnakeGame", "OnPause");
  }

  /**
  * Notification that something is about to happen to give the Activity a
  * chance to save state.
  * @param outState a Bundle into which this Activity should save its state
  */
  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    mGameThread.saveState(outState);
    outState.putInt(KEY_PAUSE, but_pause.getVisibility());
    Log.w("ActSnakeGame", "onSaveInstanceState");
  }

  /**
   * if an user has chosen "Play again" button, start new game
   */
  @Override
  public void startNewGame() {
    mGameThread.resetGame();
    snake = mGameThread.getSnake();
    findViewById(R.id.layout_start_screen).setVisibility(View.VISIBLE);
    but_pause.setVisibility(View.INVISIBLE);
  }

  /**
   * override this method to show the dialog for exit confirmation
   */
  @Override
  public void onBackPressed() {
    DialogQuit dialogQuit = new DialogQuit();
    dialogQuit.show(getFragmentManager(), "empty string\0");
  }
}