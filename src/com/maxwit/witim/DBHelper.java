package com.maxwit.witim;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

	private static final String DB_NAME = "mydb.db";
	private static final int VERSION = 1;
	private String sql;

	public DBHelper(Context context){
		super(context, DB_NAME, null, VERSION);;
	}
	
	public DBHelper(Context context, String sql) {
		super(context, DB_NAME, null, VERSION);
		this.sql = sql;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		String sql = "alter table person add age integer";
		db.execSQL(sql);
	}

	@Override
	public void onOpen(SQLiteDatabase db) {
		super.onOpen(db);
//		System.out.println("--onOpen-->>");
	}
}
