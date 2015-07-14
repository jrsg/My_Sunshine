package me.rsanchez.mysunshine;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * Created by JoseRuben on 06/07/2015.
 */

public class ForecastFragment extends Fragment {
    private ArrayAdapter<String> mForecastAdapter;
    private ListView mForecastlistView;
    private ShareActionProvider mShareActionProvider;

    public final String DETAIL_INDEX = "detail_index";

    public ForecastFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        mForecastAdapter = new ArrayAdapter<String>(getActivity(), R.layout.list_item_forecast, R.id.list_item_forecast_textview, new ArrayList<String>());
        mForecastlistView = (ListView) rootView.findViewById(R.id.listview_forecast);

        mForecastlistView.setAdapter(mForecastAdapter);

        mForecastlistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String data = mForecastAdapter.getItem(position);


                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_TEXT, data);
                startActivity(intent);
            }
        });

        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.forecastfragment, menu);

    }

    @Override
    public void onStart() {
        super.onStart();
        refreshForecast();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                refreshForecast();
                return true;
            case R.id.action_show_location:
                showPreferredLocation();
                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showPreferredLocation(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String locationPref = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));

        if(locationPref != ""){
            Intent intent = new Intent(Intent.ACTION_VIEW);

            Uri uri = Uri.parse("geo:0,0?q="+locationPref);

            Log.i("URI_TEST", uri.toString());

            intent.setData(uri);
            if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        }
    }

    private void refreshForecast() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        String locationPref = sharedPref.getString(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));

        String unitPref = sharedPref.getString(getString(R.string.pref_unit_key), "");

        if(locationPref.length() == 5) {
            new FetchWeatherTask(getActivity(), mForecastAdapter).execute(locationPref, unitPref);
        }else{
            Toast.makeText(getActivity(),"CP incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    /*public class FetchWeatherTask extends AsyncTask<String, Void, String[]>{
        private final String TAG = FetchWeatherTask.class.getSimpleName();

        @Override
        protected void onPostExecute(String[] strings) {
            super.onPostExecute(strings);

            if(strings != null){
                mForecastAdapter.clear();
                for(String row : strings){
                    mForecastAdapter.add(row);
                }
            }
            mForecastAdapter.notifyDataSetChanged();

        }

        @Override
        protected String[] doInBackground(String... params) {
            String cp = params[0];
            String unit = params[1];

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are available at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                Uri uri = new Uri.Builder()
                        .scheme("http")
                        .authority("api.openweathermap.org")
                        .appendPath("data")
                        .appendPath("2.5")
                        .appendPath("forecast")
                        .appendQueryParameter("q", cp)
                        .appendQueryParameter("mode", "json")
                        .appendQueryParameter("units", unit)
                        .appendQueryParameter("cnt", "7").build();

                //Log.i(TAG, uri.toString());

                URL url = new URL(uri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }



            //Log.i(TAG, forecastJsonStr);

            if(forecastJsonStr != null){
                WeatherDataParser myParser = new WeatherDataParser();
                try {
                    return myParser.getMaxTempetureForDay(forecastJsonStr, 7);
                }catch (final JSONException e){
                    Log.e("myParser", "Error parsing json data", e);
                }

            }


            return null;
        }
    }*/
}

