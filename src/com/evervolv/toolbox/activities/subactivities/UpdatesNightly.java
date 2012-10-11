package com.evervolv.toolbox.activities.subactivities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.evervolv.toolbox.utils.XmlHandler;

public class UpdatesNightly extends SettingsFragment implements OnPreferenceChangeListener {

    private static final String TAG = "EVToolbox";
    
    private static final String SCHEDULE_PREF = "pref_updates_nightly_schedule";
    
    private PreferenceScreen mPrefSet;
    private ListPreference mCheckUpdates;
    private String mNightlyUrl;
    private ArrayList<String> mNightlyLocations;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.updates_nightlies);

        mPrefSet = getPreferenceScreen();
        setHasOptionsMenu(true);

        mCheckUpdates = (ListPreference) mPrefSet.findPreference(SCHEDULE_PREF);
        mCheckUpdates.setSummary(mCheckUpdates.getEntry());
        mCheckUpdates.setOnPreferenceChangeListener(this);
        
        Resources res = getResources();
        mNightlyUrl = res.getString(R.string.update_url)
                + "nightlies/";
                //+ res.getString(R.string.device_codename) + "/nightlies/index";
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
                checkForUpdates();
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

    
    private void checkForUpdates() {
        Log.d(TAG, "Checking For Updates!");
        //Download the index that will tell use each nightly folder in use.
        new DownloadIndex().execute(mNightlyUrl + "index");
    }

    private class DownloadIndex extends AsyncTask<String, String, ArrayList<File>> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute!");
            super.onPreExecute();
        }
        @Override
        protected ArrayList<File> doInBackground(String... aurl) {
            Log.d(TAG, "doInBackground!");
            int count;
            ArrayList<String> locations = new ArrayList<String>();
            ArrayList<File> xmlManifests = new ArrayList<File>();
            try {
                URL url = new URL(aurl[0]);
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(url.openStream(), "UTF-8"));
                for (;;) {
                    String line = in.readLine();
                    Log.d(TAG, "Line: " + line);
                    if (line == null) break;
                    locations.add(line);
                }
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            for (String manifest : locations) {
                Log.d(TAG, "Found: [ " + locations.size() + " ] manifests.");
                Log.d(TAG, "Manifest: " + mNightlyUrl + manifest + "/manifest.xml");
                try {
                    URL url = new URL(mNightlyUrl + manifest + "/manifest.xml");
                    String path = getContext().getCacheDir() + "/manifest-"
                            + manifest + ".xml";
                    InputStream input = new BufferedInputStream(url.openStream());
                    
                    OutputStream output = new FileOutputStream(path);

                    byte data[] = new byte[1024];

                    long total = 0;

                    while ((count = input.read(data)) != -1) {
                        output.write(data, 0, count);
                    }

                    output.flush();
                    output.close();
                    input.close();
                    xmlManifests.add(new File(path));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return xmlManifests;
        }
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);
            Log.d(TAG, "Progess: " + progress[0]);
        }
        @Override
        protected void onPostExecute(ArrayList<File> manifests) {
            super.onPostExecute(manifests);
            try {
                SAXParserFactory spf = SAXParserFactory.newInstance();
                SAXParser sp = spf.newSAXParser();
                XMLReader xr = sp.getXMLReader();
                XmlHandler myHandler = new XmlHandler();
                xr.setContentHandler(myHandler);
                for (File manifest : manifests) {
                    Log.d(TAG, "File: " + manifest.toString());
                    InputStream inputStream = new FileInputStream(manifest);
                    Reader reader = new InputStreamReader(inputStream,"UTF-8");
                    xr.parse(new InputSource(reader));
                }
                //Clear cache after we're done.
                File dir = getContext().getCacheDir();
                if (dir != null && dir.isDirectory()) { deleteDir(dir); }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
           String[] children = dir.list();
           for (int i = 0; i < children.length; i++) {
              boolean success = deleteDir(new File(dir, children[i]));
              if (!success) {
                 return false;
              }
           }
        }

        // The directory is now empty so delete it
        return dir.delete();
     }

    
    private class DownloadFile extends AsyncTask<String, String, String> {
        
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute!");
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... aurl) {
            Log.d(TAG, "doInBackground!");
            int count;
            
            try {
                
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                String fileLocation = aurl[1];
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(fileLocation);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(""+(int)((total*100)/lenghtOfFile));
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;

        }
        protected void onProgressUpdate(String... progress) {
            super.onProgressUpdate(progress);
            Log.d(TAG, "Progess: " + progress[0]);
        }

        @Override
        protected void onPostExecute(String unused) {
            super.onPostExecute(unused);
        }
    }
}
