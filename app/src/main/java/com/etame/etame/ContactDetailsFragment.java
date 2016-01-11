/*
* Copyright (C) 2011 - 2015 by Ngewi Fet <ngewif@gmail.com>
*
* Permission is hereby granted, free of charge, to any person obtaining a copy
* of this software and associated documentation files (the "Software"), to deal
* in the Software without restriction, including without limitation the rights
* to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
* copies of the Software, and to permit persons to whom the Software is
* furnished to do so, subject to the following conditions:
*
* The above copyright notice and this permission notice shall be included in
* all copies or substantial portions of the Software.
*
* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
* IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
* AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
* LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
* OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
* THE SOFTWARE.
*/

package com.etame.etame;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ContactDetailsFragment extends ListFragment {
	private TextView 					mDisplayName;
	private OnContactSelectedListener 	mContactsListener;
	private Cursor mCursor;
	private static String name;
	private  static String phoneNumber;

	private EditText timeText;
	private TextView previewText;

	private static final String TAG = "ContactDetailsFragment";
	private static final String API_KEY = "AIzaSyDSeU63H6YorDYavqbUktSaIabVlOlM-oc";
	private static final String BASE = "https://maps.googleapis.com/maps/api/staticmap";

	private static double currentLat;
	private static double currentLong;
	private static Boolean sent;


	private static String message = "Hi _name_, I will be there in _X_ minutes. Here is my location.";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_contact_detail, container, false);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Bundle args = getArguments();

		long 		personId =args.getLong(MainActivity.SELECTED_CONTACT_ID);// getIntent().getLongExtra("id", 0);
		currentLat = args.getDouble("Lat");
		currentLong = args.getDouble("Long");
		sent = false;

		Activity 	activity = getActivity();
		
		Uri phonesUri = Phone.CONTENT_URI;
		String[] projection = new String[] {
				Phone._ID, Phone.DISPLAY_NAME,
				Phone.TYPE, Phone.NUMBER, Phone.LABEL };
		String 		selection 		= Phone.CONTACT_ID + " = ?";
		String[] 	selectionArgs 	= new String[] { Long.toString(personId) };





		mCursor = activity.getContentResolver().query(phonesUri,
				projection, selection, selectionArgs, null);

		mDisplayName = (TextView) activity.findViewById(R.id.display_name);
		if (mCursor.moveToFirst()){
			mDisplayName.setText(mCursor.getString(mCursor.getColumnIndex(Phone.DISPLAY_NAME)));
		}

		name = mCursor.getString(mCursor.getColumnIndex(Phone.DISPLAY_NAME));
		phoneNumber = mCursor.getString(mCursor.getColumnIndex(Phone.NUMBER));
		//TODO :: Remove This
		Log.i("CONTACT::", name+", "+phoneNumber);

		ListAdapter adapter = new PhoneNumbersAdapter(this.getActivity(),
				R.layout.list_item_phone_number, mCursor,
				new String[] {Phone.TYPE, Phone.NUMBER }, 
				new int[] { R.id.label, R.id.phone_number });
		setListAdapter(adapter);


		Button sendButton = (Button) activity.findViewById(R.id.sendButton);
		sendButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!sent)
					sendMessage();
				else
					Toast.makeText(getActivity(), "Message Already Sent!", Toast.LENGTH_LONG).show();

				sent=true;
			}
		});



		timeText = (EditText) activity.findViewById(R.id.timeText);

		View.OnFocusChangeListener ofcListener = new MyFocusChangeListener();
		timeText.setOnFocusChangeListener(ofcListener);

        previewText = (TextView) getActivity().findViewById(R.id.previewText);
        updateMessagePreview();


	}

	public void sendMessage() {
		String sendMessage = previewText.getText().toString();

		Log.i(TAG, "sendMessage: "+sendMessage);
		Log.i(TAG, "currentLocation: " + currentLat + "," + currentLong);

		//ex: https://maps.googleapis.com/maps/api/staticmap?center=40.714728,-73.998672&zoom=14&size=400x400&key=YOUR_API_KEY
		String map_url = BASE + "?center=" + currentLat + "," + currentLong + "&size=400x400&key=" + API_KEY;


		MessageTask msgTask = new MessageTask(phoneNumber, sendMessage, getActivity());
		msgTask.execute(map_url);

		Log.i(TAG, "Finished sendMessage");



	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		try {
			mContactsListener = (OnContactSelectedListener) activity;
		} catch (ClassCastException	e) {
			throw new ClassCastException(activity.toString() + " must implement OnContactSelectedListener");
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		
		TextView 	tv 		= (TextView) v.findViewById(R.id.phone_number);
		String 		number 	= tv.getText().toString();
		String 		name	= mDisplayName.getText().toString();
		
		mContactsListener.onContactNumberSelected(number, name);
	}
		
	class PhoneNumbersAdapter extends SimpleCursorAdapter {

		public PhoneNumbersAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to, 0);
		}
		
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			super.bindView(view, context, cursor);
			
			TextView 	tx 		= (TextView) view.findViewById(R.id.label);
			int 		type 	= cursor.getInt(cursor.getColumnIndex(Phone.TYPE));
			String 		label = cursor.getString(cursor.getColumnIndex(Phone.LABEL)); 
			label = Phone.getTypeLabel(getResources(), type, label).toString();
				
			tx.setText(label);
		}
	}

	private void updateMessagePreview() {
		String sendMessage = message.replace("_name_", name);
		sendMessage = sendMessage.replace("_X_", timeText.getText());
		previewText.setText(sendMessage);

	}

	private class MyFocusChangeListener implements View.OnFocusChangeListener {

		public void onFocusChange(View v, boolean hasFocus){

			if(v.getId() == R.id.timeText && !hasFocus) {

				InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				updateMessagePreview();

			}
		}
	}
}


