package com.mysentosa.android.sg;

import java.nio.charset.Charset;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class NFCActivity extends Activity{
	private static final String TAG = "NFC";
	private NfcAdapter mNfcAdapter;
	private IntentFilter[] mNdefExchangeFilters;
	private PendingIntent mNfcPendingIntent;
//	private String[][] mTechList;
//	private MifareClassic mfc;
//	private static TextView mTextView;
	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("NFC","NFCActivity");
		setContentView(new View(this));
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
		
//		mTextView = (TextView)findViewById(R.id.textview);

		  mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
					getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		    IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
			filter.addCategory(Intent.CATEGORY_DEFAULT);
			try {
	            filter.addDataType("text/plain");
	        } catch (MalformedMimeTypeException e) {
	            throw new RuntimeException("Check your mime type.");
	        }
			
			IntentFilter filter1 = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
			IntentFilter filter2 = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
//
			mNdefExchangeFilters = new IntentFilter[] { filter, filter1, filter2 };
////			mTechList = new String[][] { new String[] { MifareClassic.class.getName() }};
//			mTechList = new String[][] { new String[] { IsoDep.class.getName() }};
			
//		if (!mNfcAdapter.isEnabled()) {
//            mTextView.setText("NFC is disabled.");
//        } else {
//            mTextView.setText("Tap NFC Tag...");
//        }
         
        handleIntent(getIntent());
		
//		Intent i = new Intent(this, DirectionsActivity.class);
//		startActivity(i);
		
	}
	
	private void handleIntent(Intent intent) {
        Toast.makeText(NFCActivity.this, "handleIntent", Toast.LENGTH_SHORT).show();
        Log.d(TAG, intent.getAction());
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            
            String type = intent.getType();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            
            

        }
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if(mNfcAdapter != null) {
			Log.d("NFC", "foregroud dispatch eanbled");
			mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
					mNdefExchangeFilters, null);
		}
	}
    
	@Override
	protected void onPause() {
		super.onPause();
		if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
		Log.d("NFC", "foregroud dispatch disabled");
		
		
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.d(TAG, "onNewIntent");
//		super.onNewIntent(intent);		
		handleIntent(intent);
		

	}
}
