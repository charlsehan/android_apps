package com.cloudminds.numberlocation;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "NumberLocation";

    private TextView displayText;
    private EditText numberEdit;
    private Button lookupButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayText = (TextView)findViewById(R.id.display_text);
        numberEdit = (EditText)findViewById(R.id.number);
        lookupButton = (Button)findViewById(R.id.lookup);
        lookupButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        displayText.setText(null);

        String number = numberEdit.getText().toString();

        if (number == null) {
            return;
        }
        if (number.length() < 3) {
            return;
        }

        try {
            ContentResolver resolver = getContentResolver();
            Uri uri = Uri.parse("content://number_location/lookup/" + number);
            Cursor cursor = resolver.query(uri, null, null, null, null);

            if (cursor == null) {
                return;
            }

            if (cursor.getCount() == 0) {
                return;
            }
            cursor.moveToFirst();
            String location = cursor.getString(0);

            displayText.setText(location);
        } catch (Exception e) {
            Log.e(TAG, "ERROR when lookup number:", e);
        }
    }
}
