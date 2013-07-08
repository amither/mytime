package com.example.mytime;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteCursorDriver;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQuery;
import android.util.Log;

import com.example.mytime.R.string;

public class MytimeDatabase extends SQLiteOpenHelper {

	/** The name of the database file on the file system */
	private static final String DATABASE_NAME = "MytimeDatabase";
	/** The version of the database that this class understands. */
	private static final int DATABASE_VERSION = 1;
	private final Context mContext;

	public MytimeDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = mContext.getString(R.string.MytimeDatabase_onCreate);
		db.beginTransaction();
		try {
			db.execSQL(sql);
			db.setTransactionSuccessful();
		} catch (SQLException e) {
			Log.e("Error creating the table", e.toString());
		} finally {
			db.endTransaction();
		}
	}
	
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String sql = mContext.getString(
                R.string.MytimeDatabase_onUpgrade);
        db.beginTransaction();
        try {
            // Create tables & test data
        	db.execSQL(sql);
            db.setTransactionSuccessful();
        } catch (SQLException e) {
            Log.e("Error upgrade", e.toString());
        } finally {
            db.endTransaction();
        }

        // This is cheating.  In the real world, you'll need to add columns,
        // not rebuild from scratch
        onCreate(db);
	}

	/**
	 * the cursor for all of the taskNames
	 * 
	 */
	public static class taskNamesCursor extends SQLiteCursor {
		public static final String QUERY = "SELECT * FROM taskNames";

		/** Cursor constructor */
		taskNamesCursor(SQLiteDatabase db, SQLiteCursorDriver driver,
				String editTable, SQLiteQuery query) {
			super(db, driver, editTable, query);
		}

		/** Private factory class necessary for rawQueryWithFactory() call */
		private static class Factory implements SQLiteDatabase.CursorFactory {
			@Override
			public Cursor newCursor(SQLiteDatabase db,
					SQLiteCursorDriver driver, String editTable,
					SQLiteQuery query) {
				return new taskNamesCursor(db, driver, editTable, query);
			}
		}

		/* Accessor functions -- one per database column */
		public long getColId() {
			return getLong(getColumnIndexOrThrow("_id"));
		}

		public String getColTaskName() {
			return getString(getColumnIndexOrThrow("name"));
		}
	}

	public void addTaskName(String name) {
		ContentValues map = new ContentValues();
		map.put("name", name);
		try {
			getWritableDatabase().insert("taskNames", null, map);
		} catch (SQLException e) {
			Log.e("Error writing new taskName", e.toString());
		}
	}

	public void editTaskName(long taskName_id, String name) {
		ContentValues map = new ContentValues();
		map.put("name", name);
		String[] whereArgs = new String[] { Long.toString(taskName_id) };
		try {
			getWritableDatabase().update("taskNames", map, "_id=?", whereArgs);
		} catch (SQLException e) {
			Log.e("Error writing new taskName", e.toString());
		}
	}
	
	public void deleteTaskName(long job_id) {
        String[] whereArgs = new String[]{Long.toString(job_id)};
        try{
            getWritableDatabase().delete("taskNames", "_id=?", whereArgs);
        } catch (SQLException e) {
            Log.e("Error deleteing taskName", e.toString());
        }
    }
	
	public taskNamesCursor getTaskName() {
        String sql = taskNamesCursor.QUERY;
        SQLiteDatabase d = getReadableDatabase();
        taskNamesCursor c = (taskNamesCursor) d.rawQueryWithFactory(
            new taskNamesCursor.Factory(),
            sql,
            null,
            null);
        c.moveToFirst();
        return c;
    }
}