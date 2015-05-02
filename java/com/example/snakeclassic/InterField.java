package com.example.snakeclassic;

import android.graphics.Point;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that connects Snake and Fruit class. For ease and better performance
 * Created by Maxim on 26.04.2015.
 */
public class InterField implements Parcelable {
  private final Snake snake;
  private final Fruit fruit;

  private final int n,
                    m,
                    snakeX = 3,
                    snakeY = 2;
  private int snakeLength,
              snakeSize = 3;


  private Point pivotA, pivotB, pivotC, pivotD;

  class Snake {
    public static final byte down = 1,
                             left = 2,
                               up = 3,
                            right = 4;
    int dir = down, // snake's direction
        size;       // snake's size
    Point[] s;

    /**
    * The default snake's arrangement is down from above and starts from x and y
    * coordinates down to the head of snake.
    */
    public Snake() {
      size = snakeSize;
      snakeLength = (n - 2) * (m - 2);
      s = new Point[snakeLength];
      for(int i = 0; i < snakeLength; ++i)
        s[i] = new Point();
      for(int i = 0; i < snakeSize; ++i) {
        s[i].x = snakeX;
        s[i].y = snakeY + snakeSize - 1 - i;
      }
    }

    public byte checkState() {
      if((s[0].x > n-2) || (s[0].y > m-2) || (s[0].x < 1) || (s[0].y < 1)) {
        return GameThread.STATE_LOSE;
      }

      if(size == snakeLength)
        return GameThread.STATE_WIN;

      for(int i = 2; i < size; ++i) {
        if(s[0].x == s[i].x && s[0].y == s[i].y) {
          return GameThread.STATE_LOSE;
        }
      }

      return 0;
    }

    public void tick() {
      for(int i = size - 1; i > 0; --i) {
        s[i].x=s[i-1].x;
        s[i].y=s[i-1].y;
      }

      switch (dir) {
        case  down : ++s[0].y; break;
        case  left : --s[0].x; break;
        case    up : --s[0].y; break;
        case right : ++s[0].x; break;
      }

      if(s[0].x == fruit.x && s[0].y == fruit.y) {
        ++size;
        s[size - 1].x = s[size - 2].x;
        s[size - 1].y = s[size - 2].y;
        fruit.newFruit();
      }
    }

    public void changeDir(int key) {
      if(dir % 2 != key % 2)
        dir = key;
//      dir += key;
//      if((dir == 4) || (dir == -1)) {
//        dir = 4 - Math.abs(dir);
//      }
    }

    public int getScore() {
      return 10 * (size - snakeSize);
    }

    public void prepareForEasyWin() {
      pivotA = new Point(2, 1);
      pivotB = new Point(n - 2, m - 3 - (m % 2));
      pivotC = new Point(pivotB.x, pivotB.y + 1);
      pivotD = new Point(2, m - 2);

      for(int i = 0; i < size; ++i) {
        s[i].x = 0;
        s[i].y = 0;
      }
      size = snakeSize = 1;
      s[0].x = 1;
      s[0].y = 1;
    }

    public void easyWin() {
      if(s[0].x >= pivotA.x && s[0].x <= pivotB.x &&
         s[0].y >= pivotA.y && s[0].y <= pivotB.y) {
        if((s[0].y % 2 != 0) && (s[0].x != n - 2))
          dir = right;
        else if((s[0].y % 2 == 0) && (s[0].x != 2))
          dir = left;
        else
          dir = down;
      } else if(s[0].x <= pivotC.x && s[0].x >= pivotD.x &&
          s[0].y >= pivotC.y && s[0].y <= pivotD.y) {
        if(pivotC.y == pivotD.y)
          dir = left;
        else if((s[0].x % 2 == 0) && (s[0].y != m - 2))
                dir = down;
              else if((s[0].x % 2 != 0) && (s[0].y != m-3))
                dir = up;
              else
                dir = left;
      } else if(s[0].x == 1 && s[0].y == 1)
        dir = right;
      else
      dir = up;

      tick();
    }
  }

  class Fruit {
    int x, y;

    public Fruit() {
      x = y = -1;
    }

    private void newCoordinates() {
      x = (int) (Math.random() * (n - 2)) + 1;
      y = (int) (Math.random() * (m - 2)) + 1;
    }

    void newFruit() {
      newCoordinates();
      boolean hitSnake = true;
      while(hitSnake) {
        hitSnake = false;
        for(Point p : snake.s)
          if(p.x == x && p.y == y) {
            newCoordinates();
            hitSnake = true;
            break;
          }
      }
    }
  }

  /**
   * @param N number of horizontal cells of the game field area (must be a
   *          positive)
   * @param M number of vertical cells of the game field area (must be a
   *          positive)
   */
  public InterField(int N, int M) {
    n = N; m = M;

    snake = new Snake();
    fruit = new Fruit();
  }

  public Snake getSnake() {
    return snake;
  }

  public Fruit getFruit() {
    return fruit;
  }

  // let's make it parcelable

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(n);
    dest.writeInt(m);
    // save state of Snake's
    dest.writeInt(snake.dir);
    dest.writeInt(snake.size);
    dest.writeParcelableArray(snake.s, Point.PARCELABLE_WRITE_RETURN_VALUE);
    // save state of Fruit's
    dest.writeInt(fruit.x);
    dest.writeInt(fruit.y);
  }

  public static final Parcelable.Creator<InterField> CREATOR =
                  new Parcelable.Creator<InterField>() {
    public InterField createFromParcel(Parcel in) {
      return new InterField(in);
    }
    public InterField[] newArray(int size) {
      return new InterField[size];
    }
  };

  private InterField(Parcel source) {
    n = source.readInt();
    m = source.readInt();
    snake = new Snake();
    fruit = new Fruit();
    snake.dir = source.readInt();
    snake.size = source.readInt();
    Parcelable p[] = source.readParcelableArray(Point.class.getClassLoader());
    snake.s = new Point[p.length];
    for(int i = 0; i < p.length; ++i)
      snake.s[i] = (Point) p[i];
    fruit.x = source.readInt();
    fruit.y = source.readInt();
  }
}