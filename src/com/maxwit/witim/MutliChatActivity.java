package com.maxwit.witim;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class MutliChatActivity extends Activity {

	private String username = SingletonConnection.username;
	private String password = SingletonConnection.password;
	private String sendString = "";
	String sql;

	private ArrayAdapter<String> conAdapter;
	private ArrayAdapter<String> userAdapter;
	private List<String> conList = new ArrayList<String>();
	private List<String> usersList = new ArrayList<String>();

	ListView show;
	ListView users;
	EditText write;

	MultiUserChat muChat;
	private DBManager dbManager;
	private DBHelper dbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mutli_chat);

		findView();
		receviedIntent();
		setAdapter();

		users.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent intent = new Intent(MutliChatActivity.this,
						SingleChatActivity.class);
				intent.putExtra("window", usersList.get(position));
				startActivity(intent);
				dbManager.releaseConn();
				finish();
			}
		});

		new MyThread().start();
	}

	private void findView() {
		show = (ListView) this.findViewById(R.id.show);
		users = (ListView) this.findViewById(R.id.users);
		write = (EditText) this.findViewById(R.id.mutliwrite);
	}

	private void receviedIntent() {
		Intent intent = this.getIntent();
		SingletonConnection.conName = intent.getStringExtra("conferenceName");
	}

	public void getRecords(View v) {
		Intent intent = new Intent(MutliChatActivity.this, RecordActivity.class);
		intent.putExtra("isGroup", true);
		intent.putExtra("name", SingletonConnection.conName.split("\\@")[0]
				+ "qun");
		startActivity(intent);
		dbManager.releaseConn();
	}

	private void setAdapter() {
		conAdapter = new ArrayAdapter<String>(MutliChatActivity.this,
				android.R.layout.simple_list_item_1);
		userAdapter = new ArrayAdapter<String>(MutliChatActivity.this,
				android.R.layout.simple_list_item_1);
	}

	public void send(View v) {
		sendString = write.getText().toString();
		if (sendString.equals("") || sendString == null) {
			Toast.makeText(MutliChatActivity.this, "输入内容不能为空",
					Toast.LENGTH_SHORT).show();
			return;
		}
		write.setText("");
	}

	class MyThread extends Thread {

		@Override
		public void run() {
			try {
				muChat = new MultiUserChat(SingletonConnection.getInstance(),
						SingletonConnection.conName);
				muChat.join(username, password);

				findMulitUser(muChat);
				myUserHandler.sendMessage(myUserHandler.obtainMessage());

				muChat.addMessageListener(new PacketListener() {
					@Override
					public void processPacket(Packet packet)
							throws NotConnectedException {
						if (packet != null) {
							org.jivesoftware.smack.packet.Message message = (org.jivesoftware.smack.packet.Message) packet;
							if (message.getBody() != null
									&& !message.getBody().equals("")) {
								String name = message.getFrom().split("\\/")[1]
										.split("//:")[0];
								getDataSource(name + ":" + message.getBody());
								// saveRecord(name, message.getBody());
								System.out.println(SingletonConnection.conName);
								myConHandler.sendMessage(myConHandler
										.obtainMessage());
							}
						}
					}
				});

				while (true) {
					if (!(sendString.equals("") || sendString == null)) {
						saveRecord(username, sendString);
						muChat.sendMessage(sendString);
						sendString = "";
					}
				}
			} catch (XMPPException e) {
				throw new IllegalStateException(e);
			} catch (SmackException e) {
				e.printStackTrace();
			}
		}
	}

	private List<String> findMulitUser(MultiUserChat muChat) {
		Iterator<String> it = muChat.getOccupants().iterator();
		// Traverse the chat room staff name
		while (it.hasNext()) {
			// Chat room members name
			String name = StringUtils.parseResource(it.next());
			usersList.add(name);
			sql = "create table "
					+ name
					+ "(pid integer primary key autoincrement,name varchar(64),record varchar(64))";
			dbHelper = new DBHelper(MutliChatActivity.this, sql);
			dbHelper.getReadableDatabase();
		}
		if (!usersList.contains(username)) {
			usersList.add(username);
			sql = "create table "
					+ username
					+ "(pid integer primary key autoincrement,name varchar(64),record varchar(64))";
			dbHelper = new DBHelper(MutliChatActivity.this, sql);
			dbHelper.getReadableDatabase();
		}
		return usersList;
	}

	Handler myConHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			conAdapter.addAll(conList);
			show.setAdapter(conAdapter);
		}
	};

	Handler myUserHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			userAdapter.addAll(usersList);
			users.setAdapter(userAdapter);
		}
	};

	public List<String> getDataSource(String s) {
		conList.clear();
		conList.add(s);
		return conList;
	}

	private void saveRecord(String name, String context) {
		dbManager = new DBManager(MutliChatActivity.this);
		dbManager.getDataBaseConn();
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("record", context);
		dbManager.insert(SingletonConnection.conName.split("\\@")[0] + "qun",
				null, values);
	}

	public List<String> getUsers(String s) {
		usersList.clear();
		usersList.add(s);
		return usersList;
	}
}
