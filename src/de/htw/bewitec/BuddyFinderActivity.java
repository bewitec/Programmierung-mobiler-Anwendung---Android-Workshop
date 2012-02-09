// Copyright 2011 BeWiTEC - HTW Berlin
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package de.htw.bewitec;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class BuddyFinderActivity extends Activity {
	
	/**
	 * Result code for contact picker activity
	 */
    private static final int CONTACT_REQUEST = 12345;
	
	/**
	 * Tag for Android logging mechanism
	 */
	public static final String TAG = "BuddyFinder";
	
	/**
	 * Editable TextView which contains the address to be searched
	 */
	private EditText addressTextView;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");
    	super.onCreate(savedInstanceState);
    	// This defines the layout file to use, "main.xml" in this case
    	setContentView(R.layout.main);
    	// After defining the layout, we can get all layout-elements by their id like this
    	addressTextView = (EditText) findViewById(R.id.locationEditText);
    	
    	// We define small handlers for button clicks
        findViewById(R.id.pick).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// This will be called when the "pick"-button is clicked
				launchContactsActivity();
			}
		});
		
		findViewById(R.id.find).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// This will be called when the "find" button is clicked
				// We extract the text from the text view and launch the maps activity with it
				String address = addressTextView.getText().toString();
				launchMapsActivity(address);
			}
		});
    }
    
    /**
     * This method will be called if we started an activity via "startActivityForResult".
     * When the activity that has been started returns, we will get notified here. 
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "Activity State: onActivityResult()");
    	// If our defined request code matches and the user picked a contact..
    	if(requestCode == CONTACT_REQUEST && resultCode == RESULT_OK) {
    		// Get the contacts address and put it into the textview
    		String address = getContactAddress(data.getData());
    		addressTextView.setText(address);
    	} else {
    		Log.e(TAG, "Unknown requestCode <" + requestCode + "> or result is not OK.");
    	}
    }
    
    /**
     * This will start an activity that can handle Contacts and lets the user pick one.
     */
    private void launchContactsActivity() {
    	Intent pickContactIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);
    	startActivityForResult(pickContactIntent, CONTACT_REQUEST);
    }
    
    /**
     * The picked contact is returned as an URI, the lookup key and id of
     * the contact is contained in it. We want the lookup key, which is the
     * second to last path-segment.
     * @param contactUri The lookup uri of the contact
     * @return String the lookup key for this contact
     */
    private String getContactKey(Uri contactUri) {
    	List<String> pathSegments = contactUri.getPathSegments();
    	return pathSegments.get(pathSegments.size() - 2);
    }
    
    /**
     * The address of a contact is additional data which is not contained
     * in the normal table after a simple lookup. We must define our own 
     * query for the address by using the lookup key of the picked contact.
     * @param contactUri The Contacts uri for which we want the address
     * @return String A string containing the address of the contact.
     */
    private String getContactAddress(Uri contactUri) {
    	Log.v(TAG, "Activity State: getContactAddress()");
    	// Get the lookup key of this contact
    	String lookupKey = getContactKey(contactUri);
    	String address = "";
    	// This is the address of the content provider for contact-data
    	Uri uri = ContactsContract.Data.CONTENT_URI;
    	// The table columns we want to return, the key and address
		String[] projection = new String[] {
				ContactsContract.Contacts.LOOKUP_KEY,
				ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
		};
		// The where clause is important for lookups of additional data like the address
		//	because their actual type is defined as a MIMETYPE. We also define that we want
		//	the lookup key of the returned contact to be the one we got out of the Uri.
		String whereClause = ContactsContract.Data.MIMETYPE + " = ? AND " +
				ContactsContract.Contacts.LOOKUP_KEY + " = ?"; 
		// These are the parameters of the whereClause, each is inserted in the place of a '?'
		//	in order of appearance.
		String[] whereParams = new String[] {
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE,
				lookupKey
		};
		// The actual query returns a cursor object which is used to iterate over the results
		Cursor c = getContentResolver().query(uri, projection, whereClause, whereParams, null);
		try {
			// We only expect one result and just want its address
			c.moveToFirst();
			address = c.getString(1);
		} catch(CursorIndexOutOfBoundsException cioobe) {
			// This exception is thrown when the chosen contact has no attached address
			Toast.makeText(this, "Der Kontakt hat keine eingetragene Adresse!", Toast.LENGTH_SHORT).show();
		} finally {
			// Don't forget to close all cursors
			c.close();
		}
    	return address;
    }
    
    /**
     * This will start a Maps activity, most likely Google Maps with the given
     * address. 
     * {@link http://developer.android.com/guide/appendix/g-app-intents.html}
     * @param address The street address to be searched
     */
    private void launchMapsActivity(String address) {
    	Log.v(TAG, "Activity State: launchMapsActivity()");
    	// As defined in the Google Intents list, we want the action to be VIEW..
    	Intent mapsIntent = new Intent(Intent.ACTION_VIEW);
    	// And the Uri to be viewed has the geocoordinates set to zero and the address appended
    	Uri mapsUri = Uri.parse("geo:0,0?q=" + address);
    	// Add the data and start the activity
    	mapsIntent.setData(mapsUri);
    	this.startActivity(mapsIntent);
    }
}