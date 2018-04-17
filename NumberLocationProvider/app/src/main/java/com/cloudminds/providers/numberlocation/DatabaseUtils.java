package com.cloudminds.providers.numberlocation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseUtils {
    private static final String TAG = "NumberLocationProvider";

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "numberlocation.db";

    public static final String PREF_DATABASE_DEPLOYED = "pref_database_deployed";

    public static final int NUMBER_TYPE_MOBILE = 0;
    public static final int NUMBER_TYPE_FIXED_PHONE = 1;
    public static final int NUMBER_TYPE_SERVICE = 2;
    public static final int NUMBER_TYPE_FOREIGN = 3;
    public static final int NUMBER_TYPE_UNKNOWN = 4;

    private static final ArrayList<String> sMobileSections;
    private static final HashMap<String, String> sMobileTableMap;

    static {
        sMobileSections = new ArrayList<String>();
        sMobileSections.add("130");
        sMobileSections.add("131");
        sMobileSections.add("132");
        sMobileSections.add("133");
        sMobileSections.add("134");
        sMobileSections.add("135");
        sMobileSections.add("136");
        sMobileSections.add("137");
        sMobileSections.add("138");
        sMobileSections.add("139");

        sMobileSections.add("145");
        sMobileSections.add("147");

        sMobileSections.add("150");
        sMobileSections.add("151");
        sMobileSections.add("152");
        sMobileSections.add("153");
        sMobileSections.add("155");
        sMobileSections.add("156");
        sMobileSections.add("157");
        sMobileSections.add("158");
        sMobileSections.add("159");

        sMobileSections.add("170");
        sMobileSections.add("176");
        sMobileSections.add("177");
        sMobileSections.add("178");

        sMobileSections.add("180");
        sMobileSections.add("181");
        sMobileSections.add("182");
        sMobileSections.add("183");
        sMobileSections.add("184");
        sMobileSections.add("185");
        sMobileSections.add("186");
        sMobileSections.add("187");
        sMobileSections.add("188");
        sMobileSections.add("189");

        sMobileTableMap = new HashMap<String, String>();
        for (String section : sMobileSections) {
            sMobileTableMap.put(section + "0", "a" + section);
            sMobileTableMap.put(section + "1", "a" + section);
            sMobileTableMap.put(section + "2", "a" + section);
            sMobileTableMap.put(section + "3", "a" + section);
            sMobileTableMap.put(section + "4", "a" + section);
            sMobileTableMap.put(section + "5", "b" + section);
            sMobileTableMap.put(section + "6", "b" + section);
            sMobileTableMap.put(section + "7", "b" + section);
            sMobileTableMap.put(section + "8", "b" + section);
            sMobileTableMap.put(section + "9", "b" + section);
        }
    }

    public static class TableMobile {

        public static final String PREFIX_NUMBER = "prefixnumber";
        public static final String ADDRESS = "address";

        public static final HashMap<String, String> PROJECTION_MAP;
        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(PREFIX_NUMBER, PREFIX_NUMBER);
            PROJECTION_MAP.put(ADDRESS, ADDRESS);
        }

        public static final String[] LOOKUP_PROJECTION = new String[] {
                ADDRESS
        };
    }

    public static class TableFixedPhone {
        public static final String TABLE_NAME = "areacode";

        public static final String CODE = "code";
        public static final String CITY = "city";

        public static final HashMap<String, String> PROJECTION_MAP;
        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(CODE, CODE);
            PROJECTION_MAP.put(CITY, CITY);
        }

        public static final String[] LOOKUP_PROJECTION = new String[] {
                CITY
        };
    }

    public static class TableForeign {
        public static final String TABLE_NAME = "areacode_abroad";

        public static final String CODE = "code";
        public static final String CITY = "city";

        public static final HashMap<String, String> PROJECTION_MAP;
        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(CODE, CODE);
            PROJECTION_MAP.put(CITY, CITY);
        }

        public static final String[] LOOKUP_PROJECTION = new String[] {
                CITY
        };
    }

    public static class TableServiceNumber {
        public static final String TABLE_NAME = "servicenumber";

        public static final String NUMBER = "number";
        public static final String ADDRESS = "address";

        public static final HashMap<String, String> PROJECTION_MAP;
        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put(NUMBER, NUMBER);
            PROJECTION_MAP.put(ADDRESS, ADDRESS);
        }

        public static final String[] LOOKUP_PROJECTION = new String[] {
                ADDRESS
        };
    }

    public static int getNumberType(String number) {
        if (number.startsWith("+") || number.startsWith("00")) {
            return NUMBER_TYPE_FOREIGN;
        } else if (number.startsWith("0")) {
            return NUMBER_TYPE_FIXED_PHONE;
        } else if (number.length() >= 7 && number.startsWith("1")) {
            return NUMBER_TYPE_MOBILE;
        } else if (number.length() >= 5 && number.length() <= 6) {
            return NUMBER_TYPE_SERVICE;
        }
        return NUMBER_TYPE_UNKNOWN;
    }

    public static String trimNumber(int numberType, String number) {
        String trimmedNumber;
        switch (numberType) {
            case NUMBER_TYPE_FOREIGN:
                if (number.startsWith("+")) {
                    trimmedNumber = number.substring(1);
                } else if (number.startsWith("00")) {
                    trimmedNumber = number.substring(2);
                } else {
                    throw new InvalidParameterException("Invalid foreign number: " + number);
                }
                break;
            case NUMBER_TYPE_FIXED_PHONE:
            case NUMBER_TYPE_MOBILE:
            case NUMBER_TYPE_SERVICE:
            default:
                trimmedNumber = number;
                break;
        }
        return trimmedNumber;
    }

    private static String getMobileTable(String number) {
        String prefix = number.substring(0, 4);
        return sMobileTableMap.get(prefix);
    }

    public static String getLookupTable(int numberType, String number) {
        String tableName = null;
        switch (numberType) {
            case NUMBER_TYPE_FOREIGN:
                tableName = TableForeign.TABLE_NAME;
                break;
            case NUMBER_TYPE_FIXED_PHONE:
                tableName = TableFixedPhone.TABLE_NAME;
                break;
            case NUMBER_TYPE_MOBILE:
                tableName = getMobileTable(number);
                break;
            case NUMBER_TYPE_SERVICE:
                tableName = TableServiceNumber.TABLE_NAME;
                break;
            default:
                break;
        }
        return tableName;
    }

    public static HashMap<String, String> getProjectionMap(int numberType) {
        HashMap<String, String> projectionMap = null;
        switch (numberType) {
            case NUMBER_TYPE_FOREIGN:
                projectionMap = TableForeign.PROJECTION_MAP;
                break;
            case NUMBER_TYPE_FIXED_PHONE:
                projectionMap = TableFixedPhone.PROJECTION_MAP;
                break;
            case NUMBER_TYPE_MOBILE:
                projectionMap = TableMobile.PROJECTION_MAP;
                break;
            case NUMBER_TYPE_SERVICE:
                projectionMap = TableServiceNumber.PROJECTION_MAP;
                break;
            default:
                break;
        }
        return projectionMap;
    }

    public static String[] getLookupProjection(int numberType) {
        String[] projection = null;
        switch (numberType) {
            case NUMBER_TYPE_FOREIGN:
                projection = TableForeign.LOOKUP_PROJECTION;
                break;
            case NUMBER_TYPE_FIXED_PHONE:
                projection = TableFixedPhone.LOOKUP_PROJECTION;
                break;
            case NUMBER_TYPE_MOBILE:
                projection = TableMobile.LOOKUP_PROJECTION;
                break;
            case NUMBER_TYPE_SERVICE:
                projection = TableServiceNumber.LOOKUP_PROJECTION;
                break;
            default:
                break;
        }
        return projection;
    }

    public static String getLookupNumberField(int numberType) {
        String numberField = null;
        switch (numberType) {
            case NUMBER_TYPE_FOREIGN:
                numberField = TableForeign.CODE;
                break;
            case NUMBER_TYPE_FIXED_PHONE:
                numberField = TableFixedPhone.CODE;
                break;
            case NUMBER_TYPE_MOBILE:
                numberField = TableMobile.PREFIX_NUMBER;
                break;
            case NUMBER_TYPE_SERVICE:
                numberField = TableServiceNumber.NUMBER;
                break;
            default:
                break;
        }
        return numberField;
    }

    public static void deployDatabase(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        if (pref.getBoolean(PREF_DATABASE_DEPLOYED, false)) {
            Log.d(TAG, "database is already deployed");
            return;
        }

        File f = context.getDatabasePath(DATABASE_NAME);
        Log.d(TAG, "deployDatabase " + f.getPath());

        if (f.exists()) {
            Log.d(TAG, "database file already exist, delete it");
            if (f.delete()) {
                Log.d(TAG, "delete ok");
            } else {
                Log.d(TAG, "delete fail");
                return;
            }
        }

        File databaseDir = f.getParentFile();
        if (!databaseDir.exists()) {
            if (!databaseDir.mkdirs()) {
                Log.e(TAG, "make database dir failed !!!");
                return;
            }
        }

        AssetManager assetManager = context.getAssets();
        try {
            byte[] buffer = new byte[262144];
            InputStream is = assetManager.open(DATABASE_NAME);
            FileOutputStream os = new FileOutputStream(f, false);
            try {
                int r;
                while ((r = is.read(buffer)) > 0) {
                    os.write(buffer, 0, r);
                }
            } finally {
                os.close();
            }
            Log.d(TAG, "deploy database success");
            pref.edit().putBoolean(PREF_DATABASE_DEPLOYED, true).apply();
        } catch (IOException e) {
            Log.e(TAG, "ERROR when deployDatabase:", e);
        }
    }
}
