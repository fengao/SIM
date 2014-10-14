package com.maxwit.witim;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class RecordActivity extends Activity {
	ListView record;
	String name = "";
	boolean isGroup = false;
	private DBManager dbManager;
	List<String> records = new ArrayList<String>();
	ArrayAdapter<String> adapter = null;
	List<Map<String, String>> maps = null;
	int pos;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.record);

		dbManager = new DBManager(this);
		dbManager.getDataBaseConn();

		getNewIntent();

		record = (ListView) this.findViewById(R.id.record);
		if (records.isEmpty()) {
			records.add("no records");
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, records);
		} else {
			adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, records);
		}
		record.setAdapter(adapter);

		record.setOnItemLongClickListener(new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				pos = position;
				return false;
			}
		});
	};

	private void getNewIntent() {
		Intent intent = getIntent();
		isGroup = intent.getBooleanExtra("isGroup", false);
		name = intent.getStringExtra("name");
		getRecord(name);
	}

	private void getRecord(String name) {
		maps = new ArrayList<Map<String, String>>();
		String sql = "select * from " + name;
		maps = dbManager.queryMultiMaps(sql, null);
		for (Map<String, String> map : maps) {
			records.add(map.get("name") + map.get("record"));
		}
	}

	private void deleteAnyoneRecord(String s) {
		dbManager.delete("person", " pid = ? ", new String[] { s });
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.copy:
			Util.copy = records.get(pos);
			break;

		case R.id.paste:
			Toast.makeText(RecordActivity.this, "please paste at write", 1)
					.show();
			break;

		case R.id.delete:
			deleteAnyoneRecord(String.valueOf(pos));
			break;
		}
		return super.onMenuItemSelected(featureId, item);
	}
}
