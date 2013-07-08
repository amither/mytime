package com.example.mytime;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.widget.EditText;

public class NewTaskDialog extends DialogFragment {
	
	public interface NewTaskDialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);
		public void onDialogNegativeClick(DialogFragment dialog);
	}
	
	private NewTaskDialogListener mListener;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mListener = (NewTaskDialogListener)activity;
		} catch(ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement NewTaskDialogListener");
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState){
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(getActivity().getLayoutInflater().inflate(R.layout.newtask_dialog, null))
			   .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					/*Bundle bundle = new Bundle();
					EditText editText= ((EditText)(getDialog().findViewById(R.id.newtask_title)));
					String title = editText.getText().toString();
					Log.d("newTaskDialog", title);
					bundle.putString("title", 
								     title);*/
					mListener.onDialogPositiveClick(NewTaskDialog.this);
				}
			   })
			  .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					mListener.onDialogNegativeClick(NewTaskDialog.this);
				}
			   });
		return builder.create();
	}
}
