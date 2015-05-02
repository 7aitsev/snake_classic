package com.example.snakeclassic;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * The dialog will called when a gamer wins or loses
 * Created by Maxim on 27.04.2015.
 */
public class DialogState extends DialogFragment {
  private boolean closeActivity = true;

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(
        Context.LAYOUT_INFLATER_SERVICE);
    View v = inflater.inflate(R.layout.dialog_game_state, null);
    ((TextView)v.findViewById(R.id.dialog_msg)).setText(getArguments()
                                               .getString(IntDialog.title));

    AlertDialog.Builder builder =
        new AlertDialog.Builder(getActivity());
    builder.setView(v);
    Button but_g_over = (Button) v.findViewById(R.id.but_game_over);
    but_g_over.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        ((IntDialog) getActivity()).startNewGame();
        closeActivity = false;
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

  @Override
  public void onCancel(DialogInterface dialog) {
    if(closeActivity)
      getActivity().finish();
    super.onCancel(dialog);
  }
}
