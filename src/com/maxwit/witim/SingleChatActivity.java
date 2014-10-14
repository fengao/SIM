package com.maxwit.witim;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class SingleChatActivity extends Activity {
	private ListView chatList;
	private EditText sendEdit;

	private Chat newChat;
	private ChatManager cm;
	private XMPPConnection con;
	private List<String> list;
	private ArrayAdapter<String> chat_adapter;
	private String chatWho;
	private String chatMsg;
	private String username;
	private DBManager dbManager;
	private DBHelper dbHelper;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single_chart);

		dbManager = new DBManager(this);
		dbManager.getDataBaseConn();

		findviews();
		getIntentString();

		String sql = "create table "
				+ chatWho
				+ "(pid integer primary key autoincrement,name varchar(64),record varchar(64))";
		dbHelper = new DBHelper(this, sql);
		dbHelper.getReadableDatabase();

		con = SingletonConnection.getInstance();
		cm = ChatManager.getInstanceFor(con);

		new GetMessage().start();

		list = new ArrayList<String>();
		chat_adapter = new ArrayAdapter<String>(getBaseContext(),
				android.R.layout.simple_list_item_1);

	}

	private void findviews() {
		chatList = (ListView) findViewById(R.id.recevied);
		sendEdit = (EditText) findViewById(R.id.write);
	}

	private void getIntentString() {
		Intent intent = this.getIntent();
		chatWho = intent.getStringExtra("window");
		username = SingletonConnection.username;
		chatMsg = "";
	}

	public void getRecord(View v) {
		Intent intent = new Intent(SingleChatActivity.this,
				RecordActivity.class);
		intent.putExtra("isGroup", false);
		intent.putExtra("name", chatWho);
		startActivity(intent);
	}

	// 发送按钮点击事件函数
	public void sendMessage(View view) {
		chatMsg = sendEdit.getText().toString();
		if (chatMsg.equals("") || chatMsg == null) {
			Toast.makeText(SingleChatActivity.this, "输入内容不能为空",
					Toast.LENGTH_SHORT).show();
			return;
		}
		sendEdit.setText("");
	}

	public Handler getMsgHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			super.handleMessage(msg);

			chat_adapter.addAll(list);
			chatList.setAdapter(chat_adapter);
		}
	};

	public void getMsg(String listString) {
		if (!listString.equals("")) {
			list.clear();
			list.add(listString);
		}
	}

	class GetMessage extends Thread {
		public void receive() {
			newChat = cm.createChat(chatWho + "@" + con.getServiceName(), null);
			cm.addChatListener(new ChatManagerListener() {
				@Override
				public void chatCreated(Chat chat, boolean arg1) {
					chat.addMessageListener(new MessageListener() {
						public void processMessage(Chat chat, Message message) {
							getMsg(chatWho + ": " + message.getBody());
							saveRecord(chatWho, message.getBody());
							getMsgHandler.sendMessage(getMsgHandler
									.obtainMessage());
						}
					});
				}
			});

			while (true) {
				if (!(chatMsg.equals("") || chatMsg == null)) {
					try {
						newChat.sendMessage(chatMsg);
						getMsg(username + ": " + chatMsg);
						saveRecord(username, chatMsg);
						getMsgHandler
								.sendMessage(getMsgHandler.obtainMessage());
						chatMsg = "";
					} catch (NotConnectedException e) {
						e.printStackTrace();
					} catch (XMPPException e) {
						e.printStackTrace();
					}
				}
			}
		}

		public void run() {
			receive();
		}
	}

	private void saveRecord(String name, String context) {
		ContentValues values = new ContentValues();
		values.put("name", name);
		values.put("record", context);
		dbManager.insert(chatWho, null, values);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		dbManager.releaseConn();
	}
}