package com.example.testgcm;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;

public class MyPreferenceActivity extends PreferenceActivity implements OnPreferenceChangeListener{

	SharedPreferences preferences;
	ListPreference chatserver;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);
		PreferenceScreen prefs = getPreferenceScreen();
		chatserver = (ListPreference) prefs.findPreference("chatserver");  
		chatserver.setOnPreferenceChangeListener(this);       
	}

	@Override
	public boolean onPreferenceChange(Preference preference, Object newValue) {
		String key = preference.getKey();
		if(preference==chatserver){
			Utils.SERVER_URL = newValue.toString();
			Log.d("CHAT_APP","Sever Changed to: "+newValue);
		}
		return true;
	}
	

}
