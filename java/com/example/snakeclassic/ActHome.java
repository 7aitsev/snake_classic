package com.example.snakeclassic;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;

/**
 * This activity houses the main menu of the game.
 * Holds following navigation buttons:
 * # Play - start new game
 * # High Scores - it has not done yet
 * # Rules - a short information for a player about how to play in the game
 * # About - describes the game and contains another appropriate information
 *
 * Created by Maxim Zaitsev on 08.04.2015.
 */
public class ActHome extends Activity {
  private ViewFlipper mFlipper;
  private final String DISPLAYED_LAYOUT = "layout";

  @Override
  public void onBackPressed() {
    if(mFlipper.getDisplayedChild() != 0)
      mFlipper.setDisplayedChild(mFlipper.indexOfChild(findViewById(
          R.id.layout_home)));
    else
      super.onBackPressed();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.home);

    // get pointer to a ViewFlipper from the layout
    mFlipper = (ViewFlipper) findViewById(R.id.layout_home_flipper);
    if(savedInstanceState != null)
      mFlipper.setDisplayedChild(savedInstanceState.getInt(DISPLAYED_LAYOUT));

    // make the link in credit text clickable and highlighted
    TextView credits = (TextView) findViewById(R.id.credits);
    credits.setMovementMethod(LinkMovementMethod.getInstance());
    credits.setLinkTextColor(getResources().getColor(R.color.light_green));

    // make background animation
    ImageView fstImgV = (ImageView) findViewById(R.id.bg_animation_fst);
    ImageView sndImgV = (ImageView) findViewById(R.id.bg_animation_snd);
    final Animation falling_down_fst = AnimationUtils.loadAnimation(this,
        R.anim.bg_animation_fst);
    final Animation falling_down_snd = AnimationUtils.loadAnimation(this,
        R.anim.bg_animation_snd);
    fstImgV.startAnimation(falling_down_fst);
    sndImgV.startAnimation(falling_down_snd);

    // set listeners
    final Button but_play = (Button) findViewById(R.id.but_play);
    but_play.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ActHome.this.startActivity(
            new Intent(ActHome.this, ActSnakeGame.class));
      }
    });
    final Button but_rules = (Button) findViewById(R.id.but_rules);
    but_rules.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mFlipper.setDisplayedChild(mFlipper.indexOfChild(findViewById(
            R.id.layer_rules)));
      }
    });
    final Button but_about  = (Button) findViewById(R.id.but_about);
    but_about.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mFlipper.setDisplayedChild(mFlipper.indexOfChild(findViewById(
            R.id.layer_about)));
      }
    });
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putInt(DISPLAYED_LAYOUT, mFlipper.getDisplayedChild());
  }
}