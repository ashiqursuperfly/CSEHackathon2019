package ashiqur.goriberfitbit.db;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ashiqur.goriberfitbit.db.db_models.SessionData;

public class DBHelper extends SQLiteOpenHelper {

    // Logcat tag
    private static final String LOG = "DatabaseHelper";

    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "SqliteDb." + "GoriberFitbit";

    // Table Names
    private static final String TABLE_SESSION_DATA = "session_data";

    // column names
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_DISTANCE = "distance";
    private static final String COLUMN_LEAP_COUNT = "leap_count";

    // STEPCOUNT Table - column names
    private static final String COLUMN_COUNT = "count";

    // Table Create Statements
    // Todo table create statement
    private static final String CREATE_TABLE_SESSION_DATA = "CREATE TABLE "
            + TABLE_SESSION_DATA + "(" + COLUMN_START_TIME + " DATETIME ," +
            COLUMN_END_TIME + " DATETIME ," +
            COLUMN_DISTANCE + " REAL ," +
            COLUMN_LEAP_COUNT + " INTEGER" + ")";



    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }
    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        // creating required tables
        db.execSQL(CREATE_TABLE_SESSION_DATA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // on upgrade drop older tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION_DATA);
        // create new tables
        onCreate(db);
    }
    public long createSessionData(SessionData todo) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_START_TIME, todo.startTime);
        values.put(COLUMN_END_TIME, todo.endtime);
        values.put(COLUMN_LEAP_COUNT, todo.leaps);
        values.put(COLUMN_DISTANCE, todo.distance);

        // insert row
        long todo_id = db.insert(TABLE_SESSION_DATA, null, values);

        // assigning tags to todo
//        for (long tag_id : tag_ids) {
//            createTodoTag(todo_id, tag_id);
//        }
        return todo_id;
    }
    public List<SessionData> getAllSessions() {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_SESSION_DATA ;
        Log.e(LOG, selectQuery);
        List<SessionData> todos = new ArrayList<SessionData>();


        Log.e(LOG, selectQuery);

        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                SessionData td = new SessionData();
                td.setStartTime(c.getString(c.getColumnIndex(COLUMN_START_TIME)));
                td.setEndtime(c.getString(c.getColumnIndex(COLUMN_END_TIME)));
                td.setDistance(c.getDouble(c.getColumnIndex(COLUMN_DISTANCE)));
                td.setLeaps(c.getInt(c.getColumnIndex(COLUMN_LEAP_COUNT)));

                // adding to todo list
                todos.add(td);
            } while (c.moveToNext());
        }



        return todos;
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen())
            db.close();
    }
}

