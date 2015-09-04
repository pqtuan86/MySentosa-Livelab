package com.mysentosa.android.sg;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.widget.TextView;

public class ImpNotesActivity extends Activity {
	
	public static String IMPORTANT_NOTES = "notes";
	
	TextView mTxtNotes;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_imp_notes);
		
		mTxtNotes = (TextView) findViewById(R.id.tv_notes);
		mTxtNotes.setMovementMethod(new ScrollingMovementMethod());
		
		mTxtNotes.setText(getIntent().getExtras().getString(IMPORTANT_NOTES));
	}

}
