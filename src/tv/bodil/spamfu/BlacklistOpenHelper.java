package tv.bodil.spamfu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BlacklistOpenHelper extends SQLiteOpenHelper {
	
	public static final String BLACKLIST_TABLE_NAME = "blacklist";
	public static final String KEY_NUMBER = "num";
	public static final String KEY_COMPANY = "company";

	private static final String DATABASE_NAME = "tv.bodil.spamfu.sqlite";
	private static final int DATABASE_VERSION = 1;
    private static final String BLACKLIST_TABLE_CREATE =
                "CREATE TABLE " + BLACKLIST_TABLE_NAME + " (" +
                KEY_NUMBER + " TEXT, " +
                KEY_COMPANY + " TEXT);";

    BlacklistOpenHelper(Context context) {
    	super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(BLACKLIST_TABLE_CREATE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

}
