package com.maxwit.witim;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {

	private EditText username;
	private EditText password;
	private EditText password2;
	private EditText email;
	private EditText ipAdress;

	String name = "";
	String passwd = "";
	String passwd2 = "";
	String mail = "";
	String ip = "";

	private XMPPConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		findViews();
	}

	private void findViews() {
		username = (EditText) findViewById(R.id.reName);
		password = (EditText) findViewById(R.id.rePassword);
		password2 = (EditText) findViewById(R.id.rePassT);
		email = (EditText) findViewById(R.id.reEmail);
		ipAdress = (EditText) findViewById(R.id.ip);
	}

	public static void regist(String account, String password, String email,
			XMPPConnection con) {

		AccountManager accountManager = AccountManager.getInstance(con);
		try {
			accountManager.createAccount(account, password);
			Map<String, String> attributes = new HashMap<String, String>();
			attributes.put("email", email);
			accountManager.createAccount(account, password, attributes);
		} catch (NoResponseException e) {
			e.printStackTrace();
		} catch (XMPPErrorException e) {
			e.printStackTrace();
		} catch (NotConnectedException e) {
			e.printStackTrace();
		}
	}

	class RegisterThread extends Thread {
		public void run() {
			connection = SingletonConnection.getInstance();
			try {
				connection.connect();
			} catch (SmackException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (XMPPException e) {
				e.printStackTrace();
			}
			regist(name, passwd, mail, connection);
			Intent intent = new Intent(RegisterActivity.this,
					LoginActivity.class);
			startActivity(intent);
		}
	}

	public void onRegister(View view) {
		name = username.getText().toString().trim();
		passwd = password.getText().toString().trim();
		passwd2 = password2.getText().toString().trim();
		mail = email.getText().toString().trim();
		ip = ipAdress.getText().toString().trim();

		if (name.equals("")) {
			Toast.makeText(this, "name is empty", 1).show();
			username.setFocusable(true);
			username.requestFocus();
			return;
		} else if (passwd.equals("")) {
			Toast.makeText(this, "password is empty", 1).show();
			password.setFocusable(true);
			password.requestFocus();
			return;
		} else if (!passwd.equals(passwd2)) {
			Toast.makeText(this, "passwords are not same", 1).show();
			password.setText("");
			password2.setText("");
			password.setFocusable(true);
			password.requestFocus();
			return;
		} else if (mail.equals("")) {
			Toast.makeText(this, "email are not same", 1).show();
			email.setFocusable(true);
			email.requestFocus();
			return;
		} else if (ip.equals("")) {
			Toast.makeText(this, "ip are not same", 1).show();
			ipAdress.setFocusable(true);
			ipAdress.requestFocus();
			return;
		}
		SingletonConnection.server = ip;
		new RegisterThread().start();
	}

	public void onCancel(View view) {
		finish();
	}
}
