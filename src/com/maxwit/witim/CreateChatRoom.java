package com.maxwit.witim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.xdata.Form;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

public class CreateChatRoom extends Activity {
	private EditText roomNameEditText;
	private EditText roomDescribeEditText;
	private EditText roomPasswordEditText;
	private Spinner maxUserSpinner;
	private CheckBox checkBox;
	private SpinnerAdapter adapter;

	private String roomName;
	private String roomDescribe;
	private String roomPassword;
	private List<String> maxUserList;
	private String conferenceName;
	private XMPPConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_room);
		findViews();
		List<String> list = new ArrayList<String>();
		list.add("10");
		list.add("20");
		list.add("30");
		list.add("50");
		list.add("0");
		adapter = new ArrayAdapter<String>(CreateChatRoom.this,
				android.R.layout.simple_spinner_item, list);
		maxUserSpinner.setAdapter(adapter);

		connection = SingletonConnection.getInstance();
	}

	private void findViews() {
		roomNameEditText = (EditText) findViewById(R.id.editText1);
		roomDescribeEditText = (EditText) findViewById(R.id.editText2);
		roomPasswordEditText = (EditText) findViewById(R.id.editText3);
		maxUserSpinner = (Spinner) findViewById(R.id.spinner1);
		checkBox = (CheckBox) findViewById(R.id.persistent);
	}

	private void getViewsDate() {
		roomDescribe = roomDescribeEditText.getText().toString();
		roomName = roomNameEditText.getText().toString();
		roomPassword = roomPasswordEditText.getText().toString();
		maxUserList = new ArrayList<String>();
		maxUserList.add(maxUserSpinner.getSelectedItem().toString());
	}

	@SuppressLint("ShowToast")
	private boolean isExsit() {
		Collection<HostedRoom> hostedRoom;
		try {
			hostedRoom = MultiUserChat.getHostedRooms(connection,
					connection.getServiceName());
			for (HostedRoom room : hostedRoom) {
				for (HostedRoom j : MultiUserChat.getHostedRooms(connection,
						room.getJid())) {
					System.out.println("------>" + j.getName());
					if (j.getName().equals(roomName)) {
						Toast.makeText(CreateChatRoom.this, "房间名已存在...", 1)
								.show();
						return false;
					}
				}
			}

		} catch (NoResponseException e1) {
			e1.printStackTrace();
		} catch (XMPPErrorException e1) {
			e1.printStackTrace();
		} catch (NotConnectedException e1) {
			e1.printStackTrace();
		}
		return true;
	}

	@SuppressLint("ShowToast")
	public void createRoom(View view) {
		getViewsDate();
		conferenceName = roomName + "@conference."
				+ connection.getServiceName();
		// get exist Room

		if (roomName.equals("")) {
			Toast.makeText(CreateChatRoom.this, "房间名不能为空...", 1).show();
		} else if (isExsit()) {
			MultiUserChat muc = new MultiUserChat(connection, conferenceName);
			try {
				muc.create(roomName);
				Form submitForm = muc.getConfigurationForm().createAnswerForm();
				submitForm.setAnswer("muc#roomconfig_maxusers", maxUserList);
				submitForm.setAnswer("muc#roomconfig_roomdesc", roomDescribe);
				submitForm.setAnswer("muc#roomconfig_allowinvites", true);
				submitForm.setAnswer("muc#roomconfig_changesubject", true);
				if (!roomPassword.equals("")) {
					submitForm.setAnswer(
							"muc#roomconfig_passwordprotectedroom", true);
					submitForm.setAnswer("muc#roomconfig_roomsecret",
							roomPassword);
				}
				if (checkBox.isChecked()) {
					submitForm.setAnswer("muc#roomconfig_persistentroom", true);
				}
				muc.sendConfigurationForm(submitForm);
			} catch (NoResponseException e) {
				e.printStackTrace();
			} catch (XMPPErrorException e) {
				e.printStackTrace();
			} catch (SmackException e) {
				e.printStackTrace();
			}

			Intent intent = new Intent(CreateChatRoom.this,
					MutliChatActivity.class);
			intent.putExtra("conferenceName", conferenceName);
			startActivity(intent);
		}

	}

	public void cancerCreate(View view) {
		System.exit(0);
	}
}
