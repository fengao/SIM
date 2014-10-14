package com.maxwit.witim;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
	private DBHelper helper;
	private SQLiteDatabase database;

	public DBManager(Context context) {
		helper = new DBHelper(context);
	}

	public boolean updateBySQL(String sql, Object[] bindArgs) {
		boolean flag = false;
		try {
			database.execSQL(sql, bindArgs);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}

	public Map<String, String> queryBySQL(String sql, String[] selectionArgs) {
		Map<String, String> map = new HashMap<String, String>();
		Cursor cursor = database.rawQuery(sql, selectionArgs);
		int cols_len = cursor.getColumnCount();
		while (cursor.moveToNext()) {
			for (int i = 0; i < cols_len; i++) {
				String cols_name = cursor.getColumnName(i);
				String cols_value = cursor.getString(cursor
						.getColumnIndex(cols_name));
				if (cols_value == null) {
					cols_value = "";
				}
				map.put(cols_name, cols_value);
			}
		}
		return map;
	}

	public void getDataBaseConn() {
		database = helper.getWritableDatabase();
	}

	public void releaseConn() {
		if (database != null) {
			database.close();
		}
	}

	public List<Map<String, String>> queryMultiMaps(String sql,
			String[] selectionArgs) {
		List<Map<String, String>> list = new ArrayList<Map<String, String>>();
		Cursor cursor = database.rawQuery(sql, selectionArgs);
		int cols_len = cursor.getColumnCount();
		while (cursor.moveToNext()) {
			Map<String, String> map = new HashMap<String, String>();
			for (int i = 0; i < cols_len; i++) {
				String cols_name = cursor.getColumnName(i);
				String cols_value = cursor.getString(cursor
						.getColumnIndex(cols_name));
				if (cols_value == null) {
					cols_value = "";
				}
				map.put(cols_name, cols_value);
			}
			list.add(map);
		}
		return list;
	}

	public <T> T querySingleCursor(String sql, String[] selectionArgs,
			Class<T> cls) {
		T t = null;
		Cursor cursor = database.rawQuery(sql, selectionArgs);
		int cols_len = cursor.getColumnCount();
		while (cursor.moveToNext()) {
			try {
				t = cls.newInstance();
				for (int i = 0; i < cols_len; i++) {
					String cols_name = cursor.getColumnName(i);
					String cols_value = cursor.getString(cursor
							.getColumnIndex(cols_name));
					if (cols_value == null) {
						cols_value = "";
					}
					Field field = cls.getDeclaredField(cols_name);
					field.setAccessible(true);
					field.set(t, cols_value);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return t;
	}

	public <T> List<T> queryMultiCursor(String sql, String[] selectionArgs,
			Class<T> cls) {
		List<T> list = new ArrayList<T>();
		Cursor cursor = database.rawQuery(sql, selectionArgs);
		int cols_len = cursor.getColumnCount();
		while (cursor.moveToNext()) {
			try {
				T t = cls.newInstance();
				for (int i = 0; i < cols_len; i++) {
					String cols_name = cursor.getColumnName(i);
					String cols_value = cursor.getString(cursor
							.getColumnIndex(cols_name));
					if (cols_value == null) {
						cols_value = "";
					}
					Field field = cls.getDeclaredField(cols_name);
					field.setAccessible(true);
					field.set(t, cols_value);
					list.add(t);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	/**
	 * 
	 * @param table
	 * @param nullColumnHack
	 * @param values
	 * @return
	 */
	public boolean insert(String table, String nullColumnHack,
			ContentValues values) {
		boolean flag = false;
		// insert into tableName(a,b,c) values(?,?,?);
		long id = database.insert(table, nullColumnHack, values);
		flag = (id > 0 ? true : false);
		return flag;
	}

	/**
	 * update tableName set name = ? ,address = ?, age = ? where pid = ?
	 * 
	 * @param table
	 * @param values
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public boolean update(String table, ContentValues values,
			String whereClause, String[] whereArgs) {
		boolean flag = false;
		int count = database.update(table, values, whereClause, whereArgs);
		flag = (count > 0 ? true : false);
		return flag;
	}

	/**
	 * delete from tableName where pid = ?
	 * 
	 * @param table
	 * @param whereClause
	 * @param whereArgs
	 * @return
	 */
	public boolean delete(String table, String whereClause, String[] whereArgs) {
		boolean flag = false;
		int count = database.delete(table, whereClause, whereArgs);
		flag = (count > 0 ? true : false);
		return flag;
	}

	/**
	 * select [distinct][columnName]... from tableName
	 * [where][selection][selectionArgs] [groupBy][having][order by][limit];
	 * 
	 * @param distinct
	 * @param table
	 * @param columns
	 * @param selection
	 * @param selectionArgs
	 * @param groupBy
	 * @param having
	 * @param orderBy
	 * @param limit
	 * @return
	 */
	public Cursor query(String table, String[] columns, String selection,
			String[] selectionArgs, String groupBy, String having,
			String orderBy, String limit) {
		Cursor cursor = null;
		cursor = database.query(table, columns, selection, selectionArgs,
				groupBy, having, orderBy, limit);
		return cursor;
	}

	/**
	 * @param sql
	 * @param selectionArgs
	 * @return
	 */
	public Cursor queryMultiCursor(String sql, String[] selectionArgs) {
		Cursor cursor = database.rawQuery(sql, selectionArgs);
		return cursor;
	}

}