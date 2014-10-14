package com.maxwit.witim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.SmackException.NoResponseException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException.XMPPErrorException;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.muc.HostedRoom;
import org.jivesoftware.smackx.muc.MultiUserChat;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;
import android.widget.Toast;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {
	private final static String[] mTabs = { "Session", "Contacts", "Meettings",
			"Me" };

	static List<String> sessionsList = new ArrayList<String>();

	private Roster roster;
	
	private static String username;
	
	static ArrayList<String> group = new ArrayList<String>(); // group list
	static List<List<String>> child = new ArrayList<List<String>>(); // child
	static List<String> list = new ArrayList<String>();

	static List<String> meetingsList = new ArrayList<String>();

	static List<String> meList = new ArrayList<String>();

	static XMPPConnection connection;

	private DBHelper dbHelper;

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v13.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		if (connection == null)
			connection = SingletonConnection.getInstance();

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
					}
				});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mTabs.length; i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mTabs[i])
					.setTabListener(new TabListener() {
						@Override
						public void onTabUnselected(Tab tab,
								FragmentTransaction ft) {

						}

						@Override
						public void onTabSelected(Tab tab,
								FragmentTransaction ft) {
							mViewPager.setCurrentItem(tab.getPosition());
						}

						@Override
						public void onTabReselected(Tab tab,
								FragmentTransaction ft) {

						}
					}));
		}

		createMeMenu();
		new ContactsThread().start();
		new MeetingsThread().start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(position + 1);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return mTabs.length;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return null;
		}
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_NUMBER = "section_number";

		/**
		 * Returns a new instance of this fragment for the given section number.
		 */
		public static PlaceholderFragment newInstance(int sectionNumber) {
			PlaceholderFragment fragment = new PlaceholderFragment();
			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_NUMBER, sectionNumber);
			fragment.setArguments(args);
			return fragment;
		}

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = null;
			switch (getArguments().getInt(ARG_SECTION_NUMBER)) {
			case 1:
				rootView = inflater.inflate(R.layout.session, container, false);
				ArrayAdapter<String> sessionsAdapter = new ArrayAdapter<String>(
						this.getActivity(),
						android.R.layout.simple_list_item_1, sessionsList);

				ListView session = (ListView) rootView
						.findViewById(R.id.session);
				session.setAdapter(sessionsAdapter);
				session.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						String name = sessionsList.get(position);
						String[] names = name.split("\\.");
						if (names[names.length - 1].equals(connection
								.getServiceName())) {
							Intent intent = new Intent(getActivity(),
									MutliChatActivity.class);
							intent.putExtra("conferenceName", name);
							startActivity(intent);
						} else {
							Intent intent = new Intent(getActivity(),
									SingleChatActivity.class);
							intent.putExtra("window", name);
							startActivity(intent);
						}
					}
				});
				break;

			case 2:
				rootView = inflater
						.inflate(R.layout.contacts, container, false);
				ExpandableListView contacts = (ExpandableListView) rootView
						.findViewById(R.id.contacts);

				contacts.setOnChildClickListener(new OnChildClickListener() {
					@Override
					public boolean onChildClick(ExpandableListView parent,
							View v, int groupPosition, int childPosition,
							long id) {
						Intent intent = new Intent(getActivity(),
								SingleChatActivity.class);
						String name = child.get(groupPosition).get(
								childPosition);
						intent.putExtra("window", name);
						if (!sessionsList.contains(name)) {
							sessionsList.add(name);
						}
						startActivity(intent);
//						username = child.get(groupPosition).get(
//								childPosition);
//						showPopup(parent.getChildAt(childPosition));
						return false;
					}
				});
				contacts.setAdapter(new MainActivity().new ContactsAdapter(this
						.getActivity()));
				break;

			case 3:
				rootView = inflater.inflate(R.layout.meettings, container,
						false);
				ArrayAdapter<String> meettingsAdapter = new ArrayAdapter<String>(
						this.getActivity(),
						android.R.layout.simple_list_item_1, meetingsList);
				ListView meettings = (ListView) rootView
						.findViewById(R.id.meettings);
				meettings.setAdapter(meettingsAdapter);
				meettings.setOnItemClickListener(new OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent intent = new Intent(getActivity(),
								MutliChatActivity.class);
						String name = meetingsList.get(position);
						intent.putExtra("conferenceName", name);
						if (!sessionsList.contains(name)) {
							sessionsList.add(name);
						}
						startActivity(intent);
					}
				});
				break;

			case 4:
				rootView = inflater.inflate(R.layout.me, container, false);
				ListView me = (ListView) rootView.findViewById(R.id.me);
				ArrayAdapter<String> meAdapter = new ArrayAdapter<String>(
						this.getActivity(),
						android.R.layout.simple_list_item_1, meList);
				me.setAdapter(meAdapter);
				me.setOnItemClickListener(new OnItemClickListener() {
					@SuppressLint("InflateParams")
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						switch (position) {
						case 0:
							// TODO Auto-generated method stub
							break;
						case 1:
							Intent intent = new Intent(getActivity(),
									LoginActivity.class);
							startActivity(intent);
//							getActivity().finish();
							break;
						case 2:
							// TODO Auto-generated method stub
							break;
						case 3:
							AlertDialog.Builder builder = new AlertDialog.Builder(
									getActivity());
							builder.setTitle("About");
							view = LayoutInflater.from(getActivity()).inflate(
									R.layout.about, null);
							builder.setView(view);
							builder.setPositiveButton("ok",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									});
							builder.show();
							break;
						}
					}
				});
				break;
			}
			return rootView;
		}
	}

	private void setUsers() {
		List<RosterEntry> entries = null;
		String sql = "";
		String groupName = "";
		group = new ArrayList<String>();
		for (int i = 0; i < getGroups(roster).size(); i++) {
			groupName = getGroups(roster).get(i).getName();
			group.add(groupName);
			entries = getEntriesByGroup(roster, groupName);
			list = new ArrayList<String>();
			for (RosterEntry r : entries) {
				list.add(r.getName());
				sql = "create table "
						+ r.getName()
						+ "(pid integer primary key autoincrement,name varchar(64),record varchar(64))";
				dbHelper = new DBHelper(this, sql);
				dbHelper.getReadableDatabase();
			}
			child.add(list);
		}
	}

	private List<RosterGroup> getGroups(Roster roster) {
		List<RosterGroup> groupsList = new ArrayList<RosterGroup>();
		Collection<RosterGroup> rosterGroup = roster.getGroups();
		Iterator<RosterGroup> i = rosterGroup.iterator();
		while (i.hasNext())
			groupsList.add(i.next());
		return groupsList;
	}

	private List<RosterEntry> getEntriesByGroup(Roster roster, String groupName) {
		List<RosterEntry> EntriesList = new ArrayList<RosterEntry>();
		RosterGroup rosterGroup = roster.getGroup(groupName);
		Collection<RosterEntry> rosterEntry = rosterGroup.getEntries();
		Iterator<RosterEntry> i = rosterEntry.iterator();
		while (i.hasNext())
			EntriesList.add(i.next());
		return EntriesList;
	}

	class ContactsAdapter extends BaseExpandableListAdapter {

		private Context context;

		public ContactsAdapter(Context context) {
			this.context = context;
		}

		@Override
		public int getGroupCount() {
			return group.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return child.get(groupPosition).size();
		}

		@Override
		public Object getGroup(int groupPosition) {
			return group.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return child.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			TextView textView = null;
			if (convertView == null) {
				textView = new TextView(context);
			} else {
				textView = (TextView) convertView;
			}
			textView.setText(group.get(groupPosition));
			textView.setTextSize(30);
			textView.setPadding(36, 10, 0, 10);
			return textView;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			TextView textView = null;
			if (convertView == null) {
				textView = new TextView(context);
			} else {
				textView = (TextView) convertView;
			}
			textView.setText(child.get(groupPosition).get(childPosition));
			textView.setTextSize(20);
			textView.setPadding(72, 10, 0, 10);
			return textView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	class ContactsThread extends Thread {
		@Override
		public void run() {
			roster = connection.getRoster();

			roster.addRosterListener(new RosterListener() {
				@Override
				public void entriesAdded(Collection<String> addresses) {

				}

				@Override
				public void entriesDeleted(Collection<String> addresses) {

				}

				@Override
				public void entriesUpdated(Collection<String> addresses) {

				}

				@Override
				public void presenceChanged(Presence presence) {

				}
			});
			setUsers();
		}
	}

	class MeetingsThread extends Thread {
		@Override
		public void run() {
			String sql = "";
			try {
				Collection<HostedRoom> rooms;
				rooms = MultiUserChat.getHostedRooms(connection, "conference."
						+ connection.getServiceName());
				for (HostedRoom room : rooms) {
					if (!meetingsList.contains(room.getJid()))
						meetingsList.add(room.getJid());
					sql = "create table "
							+ room.getName()
							+ "qun(pid integer primary key autoincrement,name varchar(64),record varchar(64))";
					dbHelper = new DBHelper(MainActivity.this, sql);
					dbHelper.getReadableDatabase();
				}
			} catch (NoResponseException e) {
				e.printStackTrace();
			} catch (XMPPErrorException e) {
				e.printStackTrace();
			} catch (NotConnectedException e) {
				e.printStackTrace();
			}
		}
	}

	private void createMeMenu() {
		meList = new ArrayList<String>();
		meList.add("games");
		meList.add("check out");
		meList.add("Setting");
		meList.add("About");
	}

	/**
	 * setting.xml change password
	 * 
	 * @param connection
	 * @param pwd
	 * @return
	 */
	public static boolean changePassword(XMPPConnection connection, String pwd) {
		try {
			AccountManager.getInstance(connection).changePassword(pwd);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/**
	 * 弹出sonmenu菜单
	 * @param view
	 */
	
	public void showPopup(View view) {
		PopupMenu popupMenu = new PopupMenu(this, view);
		popupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {

			@SuppressLint("ShowToast")
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.chat:
					Intent intent = new Intent(MainActivity.this, SingleChatActivity.class);
					intent.putExtra("window", username);
					startActivity(intent);
					break;
				case R.id.findInformation:
					Toast.makeText(MainActivity.this, "我是小丽啊", 1).show();
					break;
				case R.id.removedFriend:
					Toast.makeText(MainActivity.this, "我还会回来的！！！", 1).show();
					break;	
				default:
					break;
				}
				return false;
			}
		});
		
		MenuInflater inflater = popupMenu.getMenuInflater();
		inflater.inflate(R.menu.main, popupMenu.getMenu());
//		popupMenu.getMenuInflater().inflate(R.menu.main, popupMenu.getMenu());
		popupMenu.show();
	}
	
	/**
	 * 点击按钮弹出菜单
	 * @param view
	 */
	public void popMenu(View view) {
		PopupMenu FpopupMenu = new PopupMenu(this, view);
		FpopupMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@SuppressLint("ShowToast")
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.addfriend:
					Intent intent1 = new Intent(MainActivity.this, AddFriend.class);
					intent1.putStringArrayListExtra("group", group);
					startActivity(intent1);
					Toast.makeText(MainActivity.this, "添加好友", 1).show();
					break;
				case R.id.foundmeettingroom:
					Intent intent2 = new Intent(MainActivity.this, CreateChatRoom.class);
					startActivity(intent2);
					Toast.makeText(MainActivity.this, "添加房间", 1).show();
					break;
				default:
					break;
				}
				return false;
			}
		});
		MenuInflater inflater = FpopupMenu.getMenuInflater();
		inflater.inflate(R.menu.menu, FpopupMenu.getMenu());
		FpopupMenu.show();
	}
	
	/**
	 * change user presence
	 * 
	 * @param code
	 * @throws NotConnectedException
	 */
	public void setPresence(int code) throws NotConnectedException {
		if (connection == null)
			return;
		Presence presence;
		switch (code) {
		case 0:
			presence = new Presence(Presence.Type.available);
			connection.sendPacket(presence);
			Log.v("state", "设置在线");
			break;
		case 1:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.chat);
			connection.sendPacket(presence);
			Log.v("state", "设置Q我吧");
			System.out.println(presence.toXML());
			break;
		case 2:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.dnd);
			connection.sendPacket(presence);
			Log.v("state", "设置忙碌");
			System.out.println(presence.toXML());
			break;
		case 3:
			presence = new Presence(Presence.Type.available);
			presence.setMode(Presence.Mode.away);
			connection.sendPacket(presence);
			Log.v("state", "设置离开");
			System.out.println(presence.toXML());
			break;
		case 4:
			Roster roster = connection.getRoster();
			Collection<RosterEntry> entries = roster.getEntries();
			for (RosterEntry entry : entries) {
				presence = new Presence(Presence.Type.unavailable);
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				presence.setFrom(connection.getUser());
				presence.setTo(entry.getUser());
				connection.sendPacket(presence);
				System.out.println(presence.toXML());
			}
			// 向同一用户的其他客户端发送隐身状态
			presence = new Presence(Presence.Type.unavailable);
			presence.setPacketID(Packet.ID_NOT_AVAILABLE);
			presence.setFrom(connection.getUser());
			presence.setTo(StringUtils.parseBareAddress(connection.getUser()));
			connection.sendPacket(presence);
			Log.v("state", "设置隐身");
			break;
		case 5:
			presence = new Presence(Presence.Type.unavailable);
			connection.sendPacket(presence);
			Log.v("state", "设置离线");
			break;
		default:
			break;
		}
	}

	/**
	 * delete current user
	 * 
	 * @param connection
	 * @return
	 */
	public static boolean deleteAccount(XMPPConnection connection) {
		try {
			AccountManager.getInstance(connection).deleteAccount();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}