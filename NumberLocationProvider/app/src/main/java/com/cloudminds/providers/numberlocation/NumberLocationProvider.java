package com.cloudminds.providers.numberlocation;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import java.util.List;

public class NumberLocationProvider extends ContentProvider {
    private static final String TAG = "NumberLocationProvider";

    private static final int LOCATION_LOOKUP = 1;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(NumberLocation.AUTHORITY, "lookup/*", LOCATION_LOOKUP);
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        //private final Context mContext;

        DatabaseHelper(Context context) {
            super(context, DatabaseUtils.DATABASE_NAME, null, DatabaseUtils.DATABASE_VERSION);
            //mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "DatabaseHelper onCreate is called.");
            //nothing to do
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading from version " + oldVersion + " to " + newVersion
                    + ", data will be lost!");
        }
    }

    private DatabaseHelper mDbHelper;

    @Override
    public boolean onCreate() {
        Log.d(TAG, "Creating NumberLocationProvider");
        DatabaseUtils.deployDatabase(getContext());
        mDbHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATION_LOOKUP: {
                List<String> pathSegments = uri.getPathSegments();
                String number = pathSegments.size() > 1 ? pathSegments.get(1) : null;

                int numberType = DatabaseUtils.getNumberType(number);
                if (numberType == DatabaseUtils.NUMBER_TYPE_UNKNOWN) {
                    Log.d(TAG, "Unknow number type, number: " + number);
                    return null;
                }

                String tableName = DatabaseUtils.getLookupTable(numberType, number);
                if (tableName == null) {
                    Log.d(TAG, "Unknow number type, number=" + number);
                    return null;
                }
                qb.setTables(tableName);
                qb.setProjectionMap(DatabaseUtils.getProjectionMap(numberType));
                qb.setStrict(true);

                String trimmedNumber = DatabaseUtils.trimNumber(numberType, number);

                qb.appendWhere(DatabaseUtils.getLookupNumberField(numberType));
                qb.appendWhere("=substr(");
                qb.appendWhereEscapeString(trimmedNumber);
                qb.appendWhere(", 1, length(");
                qb.appendWhere(DatabaseUtils.getLookupNumberField(numberType) + "))");

                if (projection == null) {
                    projection = DatabaseUtils.getLookupProjection(numberType);
                }

                sortOrder = DatabaseUtils.getLookupNumberField(numberType) + " DESC";
                break;
            }

            default:
                throw new IllegalArgumentException("Unknown URL " + uri);
        }

        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        final Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        return c;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Modify this database is not allowed");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Modify this database is not allowed");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Modify this database is not allowed");
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case LOCATION_LOOKUP:
                return NumberLocation.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
