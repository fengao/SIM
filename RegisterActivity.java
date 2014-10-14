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

import com.example.appclient4_xmpp4.R;

public class RegisterActivity extends Activity {

	private EditText userName;
	private EditText passWord;
	private EditText passWordCfg;
	private EditText email;

	String name;
	String passwd;
	String passwdCfg;
	String mail;

	private XMPPConnection connection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.register);

		findViews();
	}

	private void findViews() {
		userName = (EditText) findViewById(R.id.reName);
		passWord = (EditText) findViewById(R.id.rePassword);
		passWordCfg = (EditText) findViewById(R.id.rePassT);
		email = (EditText) findViewById(R.id.reEmail);
	}

	public static void regist(String account, String password, String email,
			XMPPConnection con) {

		AccountManager accountManager = AccountManager.getInstance(con);
		try {
			if (email == null || email.equals(" ")) {
				accountManager.createAccount(account, password);
			}
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
		name = userName.getText().toString().trim();
		passwd = passWord.getText().toString().trim();
		passwdCfg = passWordCfg.getText().toString().trim();
		mail = email.getText().toString().trim();

		if (name == null || name.equals("") || passwd == null
				|| passwd.equals("") || passwdCfg == null
				|| passwdCfg.equals("")) {
			Toast.makeText(RegisterActivity.this, "用户名和密码不能为空", 1).show();
		} else if (!passwd.equals(passwdCfg)) {
			Toast.makeText(getBaseContext(), "密码不一致", 1).show();
		} else {
			new RegisterThread().start();
		}
	}

	public void onCancel(View view) {
		finish();
	}
}
