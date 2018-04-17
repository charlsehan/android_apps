package com.cloudminds.calllogsearch;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.CallLog;
import android.text.TextUtils;

import java.util.ArrayList;

public class CallLogSearchProvider extends ContentProvider {
    public static final String TAG = "CallLogSearchProvider";

    private static final String[] SEARCH_SUGGESTIONS_COLUMNS = {
            "_id",
            SearchManager.SUGGEST_COLUMN_TEXT_1,
            SearchManager.SUGGEST_COLUMN_TEXT_2,
            SearchManager.SUGGEST_COLUMN_ICON_1,
            SearchManager.SUGGEST_COLUMN_INTENT_DATA,
    };

    public static final String[] CALLS_PROJECTION = new String[] {
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.CACHED_NAME,
    };

    public CallLogSearchProvider() {
    }

    @Override
    public boolean onCreate() {

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
        MatrixCursor suggestionCursor = new MatrixCursor(SEARCH_SUGGESTIONS_COLUMNS);

        if (TextUtils.isEmpty(selectionArgs[0])) {
            return suggestionCursor;
        }
        selection = CallLog.Calls.NUMBER + " LIKE %" + selectionArgs[0] + "% OR " +
                CallLog.Calls.CACHED_NAME + " LIKE %" + selectionArgs[0] + "%";

        ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = resolver.query(CallLog.Calls.CONTENT_URI, CALLS_PROJECTION, selection, null, sortOrder);

        try {
            while (cursor.moveToNext()) {
                String id = cursor.getString(0);
                String number = cursor.getString(1);
                String name = cursor.getString(2);
                String intentData = Uri.withAppendedPath(CallLog.Calls.CONTENT_URI, id).toString();

                ArrayList<Object> row = new ArrayList<Object>();
                row.add(id);
                row.add(number);
                row.add(name);
                row.add(String.valueOf(R.drawable.ic_phone));
                row.add(intentData);

                suggestionCursor.addRow(row);
            }
        } finally {
            cursor.close();
        }

        return suggestionCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
            String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
