package co.flyver.flyverrc;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Created by flyver on 10/15/14.
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
