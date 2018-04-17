package com.cloudminds.providers.numberlocation;


import android.net.Uri;

public class NumberLocation {
    public static final String AUTHORITY = "number_location";

    /**
     * The content:// style URL for this provider
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://" + AUTHORITY);

    public static final Uri CONTENT_LOOKUP_URI =
            Uri.parse(CONTENT_URI + "/lookup");

    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/number_location";

}
