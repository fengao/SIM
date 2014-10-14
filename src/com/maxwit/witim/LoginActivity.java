package com.maxwit.witim;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.harmony.javax.security.sasl.SaslException;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class LoginActivity extends Activity {

	private EditText username;
	private EditText password;
	private EditText ipAdress;
	private Button enter;

	private CheckBox remember;
	private CheckBox autoSign;
	private CheckBox invisible;

	private LoginService service;
	Map<String, ?> map = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		findViews();

		if (getIntent() != null) {
			storageData();
			autoSign.setChecked(false);
		} else {
			storageData();
			autoSign.setChecked((Boolean) map.get("isauto"));
		}

		if (autoSign.isChecked()) {
			onClickLogin(enter);
		}

		if (invisible.isChecked()) {

		}
	}

	private void findViews() {
		username = (EditText) this.findViewById(R.id.username);
		password = (EditText) this.findViewById(R.id.password);
		ipAdress = (EditText) this.findViewById(R.id.ip);

		remember = (CheckBox) this.findViewById(R.id.remember);
		autoSign = (CheckBox) this.findViewById(R.id.auto_sign);
		invisible = (CheckBox) this.findViewById(R.id.invisible);

		enter = (Button) this.findViewById(R.id.enter);
	}

	private void storageData() {
		service = new LoginService(this);
		map = service.getSharePreference("login");
		if (map != null && !map.isEmpty()) {
			remember.setChecked((Boolean) map.get("isremember"));
			if (remember.isChecked()) {
				username.setText(map.get("username").toString());
				password.setText(map.get("password").toString());
				ipAdress.setText(map.get("ipAdress").toString());
			}
			invisible.setChecked((Boolean) map.get("isinvisible"));
		}
	}

	public boolean getInfomations() {
		if (!username.getText().toString().equals("")) {
			SingletonConnection.username = username.getText().toString();
		} else {
			Toast.makeText(this, "account is empty", Toast.LENGTH_SHORT).show();
			return false;
		}

		if (!password.getText().toString().equals("")) {
			SingletonConnection.password = password.getText().toString();
		} else {
			Toast.makeText(this, "password is empty", Toast.LENGTH_SHORT)
					.show();
			return false;
		}

		SingletonConnection.server = ipAdress.getText().toString();
		return true;
	}

	public void onClickLogin(View v) {
		if (getInfomations()) {
			System.out.println("username:" + SingletonConnection.username + "password" + SingletonConnection.password);
			Map<String, Object> map = new HashMap<String, Object>();
			if (remember.isChecked()) {
				map.put("username", SingletonConnection.username);
				map.put("password", SingletonConnection.password);
				map.put("ipAdress", SingletonConnection.server);
			} else {
				map.put("username", "");
				map.put("password", "");
				map.put("ipAdress", "");
			}
			map.put("isauto", autoSign.isChecked());
			map.put("isremember", remember.isChecked());
			map.put("isinvisible", invisible.isChecked());
			service.saveSharePreference("login", map);

			new Thread() {
				@Override
				public void run() {
					try {
						XMPPConnection connection = SingletonConnection
								.getInstance();
						connection.connect();
						connection.login(SingletonConnection.username,
								SingletonConnection.password);
						connection.sendPacket(new Presence(Presence.Type.unavailable));
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (SaslException e) {
						e.printStackTrace();
					} catch (XMPPException e) {
						e.printStackTrace();
					} catch (SmackException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					if (SingletonConnection.getInstance().isAuthenticated()) {
						Intent intent = new Intent(LoginActivity.this,
								OfflineMSG.class);
						startActivity(intent);
						finish();
					} else {
						myHandler.sendMessage(myHandler.obtainMessage());
					}
				}
			}.start();
		}
	}

	Handler myHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(LoginActivity.this,
					"wrong account or wrong password, please try again",
					Toast.LENGTH_SHORT).show();
		}
	};

	public void onRegister(View v) {
		Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
		startActivity(intent);
	}
}