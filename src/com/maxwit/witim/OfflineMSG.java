package com.maxwit.witim;

import java.util.ArrayList;
import java.util.List;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketExtensionFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.disco.ServiceDiscoveryManager;
import org.jivesoftware.smackx.disco.packet.DiscoverInfo;
import org.jivesoftware.smackx.offline.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.xdata.Form;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class OfflineMSG extends Activity {

	private Message message = null;
	private List<String> messages = null;
	private ListView listviewMSG;
	private ArrayAdapter<List<String>> adapter;

	@SuppressLint("ShowToast")
	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.offlinemessage);

		listviewMSG = (ListView) findViewById(R.id.listViewOfflineMessage);
		adapter = new ArrayAdapter<List<String>>(this,
				android.R.layout.simple_list_item_1);

		OfflineMessageManager offlineMessageManager = new OfflineMessageManager(
				SingletonConnection.getInstance());
		try {
			offlineMessageManager.getMessageCount();
			offlineMessageManager.getMessages();
			offlineMessageManager.deleteMessages();
		} catch (NoResponseException | XMPPErrorException
				| NotConnectedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (messages == null || messages.equals("")) {
			System.out.println("--OfflineMessages->" + messages);
			Toast.makeText(OfflineMSG.this, "没有离线消息...", 1).show();
		} else {
			adapter.addAll(messages);
			listviewMSG.setAdapter(adapter);
		}
	}

	public class OfflineMessageManager {
		private final static String namespace = "http://jabber.org/protocol/offline";

		private PacketFilter packetFilter;
		private XMPPConnection connection;
		protected Packet packet;

		public OfflineMessageManager(XMPPConnection connection) {
			this.connection = SingletonConnection.getInstance();
			packetFilter = new AndFilter(new PacketExtensionFilter("offline",
					namespace), new PacketTypeFilter(Message.class));
		}

		public int getMessageCount() throws NoResponseException,
				XMPPErrorException, NotConnectedException {
			DiscoverInfo info = ServiceDiscoveryManager.getInstanceFor(
					connection).discoverInfo(null, namespace);
			Form extendedInfo = Form.getFormFrom(info);
			if (extendedInfo != null) {
				String value = extendedInfo.getField("number_of_messages")
						.getValues().get(0);
				System.out.println("--OfflineMessage->" + value);
				return Integer.parseInt(value);
			}
			return 0;
		}

		public List<String> getMessages() throws NoResponseException,
				XMPPErrorException, NotConnectedException {
			messages = new ArrayList<String>();
			OfflineMessageRequest request = new OfflineMessageRequest();
			request.setFetch(true);

			PacketCollector messageCollector = connection
					.createPacketCollector(packetFilter);
			try {
				connection.createPacketCollectorAndSend(request)
						.nextResultOrThrow();
				// Collect the received offline messages
				message = (Message) messageCollector.nextResult();
				int i = 1;
				while (message != null) {
					messages.add("[" + i + "]Received from:"
							+ message.getFrom().split("/")[0] + ":"
							+ message.getBody() + "       ");
					message = (Message) messageCollector.nextResult();
					i++;
				}
			} finally {
				// Stop queuing offline messages
				messageCollector.cancel();
			}
			return messages;
		}

		public void deleteMessages() throws NoResponseException,
				XMPPErrorException, NotConnectedException {
			OfflineMessageRequest request = new OfflineMessageRequest();
			request.setPurge(true);
			connection.createPacketCollectorAndSend(request)
					.nextResultOrThrow();
			// 设置成在线状态
			Presence presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);
		}
	}

	public void logOff() throws NotConnectedException {
		SingletonConnection.getInstance().disconnect();
	}

	public void backMenu(View view) {
		Intent intent = new Intent(OfflineMSG.this, MainActivity.class);
		startActivity(intent);
	}
}
