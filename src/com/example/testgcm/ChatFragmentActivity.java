package com.example.testgcm;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Locale;

import com.chatatainment.database.GameDatabaseOperations;
import com.chatatainment.game.TicTacToe;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class ChatFragmentActivity extends FragmentActivity implements
		ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;
	private ChatFragment chatFragment = null;
	private GameFragment gameFragment = null;
	public static final int CHAT_TAB = 0;
	public static final int GAME_TAB = 1;
	public static ActionBar globalActionBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chat_fragment);

		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		globalActionBar = actionBar;
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		
		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

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
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}
		getOverflowMenu();
		
		actionBar.setSelectedNavigationItem(getIntent().getExtras().getInt("tabToSelect"));
		
		
	}
	
	BroadcastReceiver gameMoveReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			
			/*Tab tab = getActionBar().getSelectedTab();
			if(tab.getPosition()==ChatFragmentActivity.CHAT_TAB){
				getActionBar().getTabAt(1).setText("Game *");
			}*/
		}

	};
	
	@Override
	public void onResume(){
		this.registerReceiver(gameMoveReceiver, new IntentFilter(
				"com.example.testgcm.GameMove"));
		super.onResume();
	}
	private void getOverflowMenu() {
	     try {
	        ViewConfiguration config = ViewConfiguration.get(this);
	        Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
	        if(menuKeyField != null) {
	            menuKeyField.setAccessible(true);
	            menuKeyField.setBoolean(config, false);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_chat, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.new_game:
	        Toast.makeText(this, "New Game Started", Toast.LENGTH_LONG).show();
	        startNewGame();
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void startNewGame(){
		TicTacToe ticTacToeGame =  gameFragment.getGame();
		GameDatabaseOperations.loadGameStateFromDatabase(ticTacToeGame, gameFragment.getUserNumber(), gameFragment.getGameDSForRead());
		ticTacToeGame.resetGame();
		GameDatabaseOperations.saveGameStateToDatabase(ticTacToeGame, gameFragment.getUserNumber(), gameFragment.getGameDSForWrite());
		gameFragment.updateButtonsWithGameState();
		Message msg2Send = new Message();
		msg2Send.setFrom(chatFragment.getMyNumber());
		msg2Send.setTo(chatFragment.getUserNumber());
		msg2Send.setIsMine(true);
		msg2Send.setMsg("");
		msg2Send.setTimestamp(new Date());
		msg2Send.setType(Message.Types.NEW_GAME_REQUEST);
		chatFragment.addNewMessage(msg2Send);
		chatFragment.new SendMessageTask().execute(msg2Send);
	}

	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
		/*if(tab.getPosition()==ChatFragmentActivity.GAME_TAB){
			tab.setText(mSectionsPagerAdapter.getPageTitle(tab.getPosition()));
		}*/
		
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		
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
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.
			Fragment fragment = null;
			if (position == 0) {
				fragment = new ChatFragment();
				chatFragment = (ChatFragment)fragment;
			}else{
				fragment = new GameFragment();
				gameFragment = (GameFragment)fragment;
				gameFragment.setMyState(TicTacToe.STATE_X);
			}
			return fragment;
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * A dummy fragment representing a section of the app, but that simply
	 * displays dummy text.
	 */
	public static class DummySectionFragment extends Fragment {
		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		public static final String ARG_SECTION_NUMBER = "section_number";

		public DummySectionFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_chat_dummy,
					container, false);
			TextView dummyTextView = (TextView) rootView
					.findViewById(R.id.section_label);
			dummyTextView.setText(Integer.toString(getArguments().getInt(
					ARG_SECTION_NUMBER)));
			return rootView;
		}
	}

}
