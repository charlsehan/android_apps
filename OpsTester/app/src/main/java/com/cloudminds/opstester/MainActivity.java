package com.cloudminds.opstester;

import android.Manifest;
import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button contactsRead;
    Button contactsWrite;
    Button callLogRead;
    Button callLogWrite;
    Button smsRead;
    Button smsWrite;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactsRead = (Button) findViewById(R.id.contacts_read);
        contactsRead.setOnClickListener(this);
        contactsWrite = (Button) findViewById(R.id.contacts_write);
        contactsWrite.setOnClickListener(this);
        callLogRead = (Button) findViewById(R.id.calllog_read);
        callLogRead.setOnClickListener(this);
        callLogWrite = (Button) findViewById(R.id.calllog_write);
        callLogWrite.setOnClickListener(this);
        smsRead = (Button) findViewById(R.id.sms_read);
        smsRead.setOnClickListener(this);
        smsWrite = (Button) findViewById(R.id.sms_write);
        smsWrite.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contacts_read: {
                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null) {
                    cursor.close();
                }
                break;
            }
            case R.id.contacts_write: {
                Uri uri = ContactsContract.Contacts.CONTENT_URI;
                ContentResolver resolver = getContentResolver();
                String where = ContactsContract.Contacts._ID + "=1024";
                resolver.delete(uri, where, null);
                break;
            }
            case R.id.calllog_read: {
                Uri uri = CallLog.Calls.CONTENT_URI;
                ContentResolver resolver = getContentResolver();
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                Cursor cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null) {
                    cursor.close();
                }
                break;
            }
            case R.id.calllog_write: {
                Uri uri = CallLog.Calls.CONTENT_URI;
                ContentResolver resolver = getContentResolver();
                String where = CallLog.Calls._ID + "=1024";
                resolver.delete(uri, where, null);
                break;
            }
            case R.id.sms_read: {
                Uri uri = Telephony.Sms.CONTENT_URI;
                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(uri, null, null, null, null);
                if (cursor != null) {
                    cursor.close();
                }
                break;
            }
            case R.id.sms_write: {
                Uri uri = Telephony.Sms.CONTENT_URI;
                ContentResolver resolver = getContentResolver();
                String where = Telephony.Sms._ID + "=1024";
                resolver.delete(uri, where, null);
                break;
            }
            default:
                break;
        }
    }
}
