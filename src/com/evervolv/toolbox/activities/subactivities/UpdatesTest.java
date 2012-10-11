package com.evervolv.toolbox.activities.subactivities;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.evervolv.toolbox.R;
import com.evervolv.toolbox.SettingsFragment;

public class UpdatesTest extends SettingsFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";
    
    private static final String TEST_PREF = "pref_updates_test_schedule";

    private PreferenceScreen mPrefSet;
    private ListPreference mCheckUpdates;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.updates_tests);

        mPrefSet = getPreferenceScreen();
        setHasOptionsMenu(true);

        mCheckUpdates = (ListPreference) mPrefSet.findPreference(TEST_PREF);
        mCheckUpdates.setSummary(mCheckUpdates.getEntry());
        mCheckUpdates.setOnPreferenceChangeListener(this);
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nightlies_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_refresh:
                //check for updates plz
                return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.valueOf((String) newValue);
        if (preference == mCheckUpdates) {
            mCheckUpdates.setSummary(mCheckUpdates.getEntries()[value]);
            //TODO:
            return true;
        }
        return false;
    }

}
