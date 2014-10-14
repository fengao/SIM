package com.maxwit.test;

import android.content.ContentValues;
import android.test.AndroidTestCase;

import com.maxwit.witim.DBManager;

public class Test extends AndroidTestCase {
	public void insert2() {
		DBManager manager = new DBManager(getContext());
		manager.getDataBaseConn();
		ContentValues values = new ContentValues();
		values.put("name", "jjchen");
		values.put("record", "nice too me too");
		manager.insert("jjqun", null, values);
	}
}
