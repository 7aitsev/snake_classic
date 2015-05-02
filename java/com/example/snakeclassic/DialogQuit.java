package com.example.snakeclassic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

/**
 * This dialog will invoked when an user wants to quit from the game
 * Created by Maxim on 01.05.2015.
 */
public class DialogQuit extends DialogFragment {

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder =
        new AlertDialog.Builder(getActivity());
    LayoutInflater inflater = getActivity().getLayoutInflater();
    View v = inflater.inflate(R.layout.dialog_quit, null);

    builder.setView(v);

    Button but_positive = (Button) v.findViewById(R.id.but_quit_yes);
    but_positive.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().finish();
        dismiss();
      }
    });
    Button but_negative = (Button) v.findViewById(R.id.but_quit_no);
    but_negative.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        dismiss();
      }
    });
  return builder.create();
  }

  @Override
  public void onStart() {
    getDialog().setCanceledOnTouchOutside(false);
    super.onStart();
  }
}
