package me.rsanchez.mysunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.ArrayList;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        ArrayList<String> forecastList = new ArrayList<String>();

        forecastList.add("Hoy - soleado - 32/27");
        forecastList.add("Mañana - soleado - 33/29");
        forecastList.add("Martes - nublado - 30/28");
        forecastList.add("Mier - nublado - 28/23");
        forecastList.add("Jueves - soleado - 32/29");
        forecastList.add("Viernes - soleado - 34/29");

        mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast, R.id.list_item_forecast_textview, forecastList);

        //FrameLayout container = (FrameLayout) getActivity().findViewById(R.id.container);
        ListView listView = (ListView) getActivity().findViewById(R.id.listview_forecast);

        listView.setAdapter(mForecastAdapter);



        return inflater.inflate(R.layout.fragment_main, container, false);
    }
}
