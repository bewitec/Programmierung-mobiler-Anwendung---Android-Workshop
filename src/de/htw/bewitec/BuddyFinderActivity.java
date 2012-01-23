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
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class BuddyFinderActivity extends Activity {
    private ListView contactsListView;
	
	/**
	 * Tag for Android logging mechanism
	 */
	public static final String TAG = "ContactManager";


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.v(TAG, "Activity State: onCreate()");
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.main);
                
        contactsListView = (ListView) findViewById(R.id.contactsListView);
        contactsListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
				TextView addressTextView = (TextView) view.findViewById(R.id.contactEntryAddress);
				String address = addressTextView.getText().toString();
				launchMapsActivity(address);
			}
		});

        populateContactList();
    }
    
    private void launchMapsActivity(String address) {
    	Log.v(TAG, "Activity State: launchMapsActivity()");
    	Intent mapsIntent = new Intent(Intent.ACTION_VIEW);
    	Uri mapsUri = Uri.parse("geo:0,0?q=" + address);
    	mapsIntent.setData(mapsUri);
    	this.startActivity(mapsIntent);
    }


	private void populateContactList() {
		Log.v(TAG, "Activity State: populateContactList()");
        Cursor cursor = getContacts();
        String[] fields = new String[] {
                ContactsContract.Data.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
        };
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.contact_entry, cursor,
                fields, new int[] {R.id.contactEntryText, R.id.contactEntryAddress});
        contactsListView.setAdapter(adapter);
	}
	
	private Cursor getContacts() {
		Log.v(TAG, "Activity State: getContacts()");
		Uri uri = ContactsContract.Data.CONTENT_URI;
		String[] projection = new String[] {
				ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME,
				ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
		};
		String whereClause = ContactsContract.Data.MIMETYPE + " = ?"; 
		String[] whereParams = new String[] {
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
		};
		
		return managedQuery(uri, projection, whereClause, whereParams, null);
	}
	
}