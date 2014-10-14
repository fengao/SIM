package com.maxwit.witim;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class AddFriendService extends Service {
	private XMPPConnection con = null;

	public AddFriendService() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		con = SingletonConnection.getInstance();
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.i("Presence", "PresenceService-----" + (con == null));
		if (con != null && con.isAuthenticated() && con.isConnected()) {  //已经认证的情况下，才能正确收到Presence包（也就是登陆）
			final String loginuser = con.getUser().substring(0,con.getUser().lastIndexOf("@"));   //条件过滤器  过滤出Presence包
			PacketFilter filter = new AndFilter(new PacketTypeFilter(Presence.class));
			PacketListener listener = new PacketListener() {
				
				@Override
				public void processPacket(Packet packet) throws NotConnectedException {
					//Presence是Packet的子类 
					// TODO Auto-generated method stub
					Log.i("Presence", "PresenceService------" + packet.toXML());
					if (packet instanceof Presence) {
						Log.i("Presence", "" + packet.toXML());
						Presence presence = (Presence) packet;
						String from = presence.getFrom();
						String to = presence.getTo();
						if (presence.getType().equals(Presence.Type.subscribe)) {  //好友申请
							System.out.println("--subscribe->" + from + to);
						}else if (presence.getType().equals(Presence.Type.subscribed)) {  //同意添加好友  
							System.out.println("--subscribed->" + from + to);
                        } else if (presence.getType().equals(Presence.Type.unsubscribe)) { //拒绝添加好友和删除好友  
                              
                        } else if (presence.getType().equals(Presence.Type.unsubscribed)) {  //这个我没用到  
                        	
                        } else if (presence.getType().equals(Presence.Type.unavailable)) {  //好友下线   要更新好友列表，可以在这收到包后，发广播到指定页面   更新列表  
                              
                        } else {  //好友上线  
                              
                        }  
					}
				}
			};
			con.addPacketListener(listener, filter);  //注册监听
		}
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
