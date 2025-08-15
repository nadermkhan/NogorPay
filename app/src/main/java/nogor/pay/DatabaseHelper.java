package nogor.pay;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.HashMap;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sms_monitor.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SMS = "sms";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_SENDER = "sender";
    public static final String COLUMN_MESSAGE = "message";
    public static final String COLUMN_TIMESTAMP = "timestamp";

    private static final String CREATE_TABLE_SMS =
            "CREATE TABLE " + TABLE_SMS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_SENDER + " TEXT," +
                    COLUMN_MESSAGE + " TEXT," +
                    COLUMN_TIMESTAMP + " INTEGER DEFAULT (strftime('%s','now'))" +
                    ")";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        onCreate(db);
    }

    public long saveSms(String sender, String message) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SENDER, sender);
        values.put(COLUMN_MESSAGE, message);

        long id = db.insert(TABLE_SMS, null, values);
        db.close();
        return id;
    }

    public ArrayList<HashMap<String, String>> getAllSms() {
        ArrayList<HashMap<String, String>> smsList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {COLUMN_ID, COLUMN_SENDER, COLUMN_MESSAGE, COLUMN_TIMESTAMP};
        Cursor cursor = db.query(TABLE_SMS, columns, null, null, null, null,
                COLUMN_TIMESTAMP + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                HashMap<String, String> sms = new HashMap<>();
                sms.put("id", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                sms.put("sender", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SENDER)));
                sms.put("message", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
                sms.put("timestamp", cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                smsList.add(sms);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return smsList;
    }

    public void deleteSms(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SMS, COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteAllSms() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_SMS, null, null);
        db.close();
    }
}