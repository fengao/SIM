package com.maxwit.witim;

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.SmackException.NotLoggedInException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;

public class AddFriend extends Activity {
	private String Username;
	private String Nickname;
	private String Groupname;
	
	private EditText editTextUsername;
	private EditText editTextNickname;
	private Spinner spinner;
	private SpinnerAdapter adapter;
	private ArrayList<String> group;
	private Roster roster;
	private XMPPConnection con = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.addfriend);
		
		editTextUsername = (EditText)findViewById(R.id.editTextusername);
		editTextNickname = (EditText)findViewById(R.id.editTextnickname);
		spinner = (Spinner)findViewById(R.id.spinnergroup);
		
		adapter = new ArrayAdapter<String>(AddFriend.this, android.R.layout.simple_spinner_item, getData());
		spinner.setAdapter(adapter);
		System.out.println("--->0");
		con = SingletonConnection.getInstance();
		roster = con.getRoster();
		
	}
	
	public AddFriend() {
		// TODO Auto-generated constructor stub
	}
	
	public ArrayList<String> recGroup() {
		Intent intent = this.getIntent();
		group = intent.getStringArrayListExtra("group");
		return group;
	}
	
	public List<String> getData() {
		List<String> list = new ArrayList<String>();
		list.addAll(recGroup());
 		return list;
	}
	
	public void addFriend(View view) throws NotLoggedInException, NoResponseException, XMPPErrorException, NotConnectedException {
		Username = editTextUsername.getText().toString() + "@" + con.getServiceName();
		Nickname = editTextNickname.getText().toString();
		Groupname = spinner.getSelectedItem().toString();
		System.out.println(Groupname);
		
		roster.createEntry(Username, Nickname, null);
		System.out.println("addFri");
		
		Intent intent = new Intent(AddFriend.this, AddFriendService.class);
		startActivity(intent);
	}
	
	public void Cancel(View view) {
		
	}

}
