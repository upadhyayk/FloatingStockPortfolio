package edu.temple.floatingstockportfolio;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

public class DetailsFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private String stockSelected;
    TextView textViewName;
    TextView textViewStockPrice;
    ImageView graphImageView;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(String param1) {
        DetailsFragment fragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            stockSelected = getArguments().getString(ARG_PARAM1);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_details, container, false);
        String fileName = "StockFile.txt";
        String jsonString = "";
        String name = "";
        String lastPrice = "";
        String graphUrl = "";
        FileInputStream fileInputStream = null;

        //get info from the file for the current jsonStock
        try{
            fileInputStream = getActivity().openFileInput(fileName);
            int c;
            while((c = fileInputStream.read()) != -1){
                jsonString += Character.toString((char) c);
            }

        }catch (Exception e){
            e.printStackTrace();
        }

        Log.d(fileName, jsonString);

        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
            jsonArray = null;
        }

        for(int i = 0; i < jsonArray.length(); i++){
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String saveStockSymbol = jsonObject.getString("Symbol");
                if(stockSelected.equals(saveStockSymbol)){
                    name = jsonObject.getString("Name");
                    Log.d("Name of Stock Selected", name);
                    lastPrice = jsonObject.getString("LastPrice");
                    Log.d("LastPrice of Stock", lastPrice);
                    graphUrl = jsonObject.getString("GraphUrl");

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        //set info to textView
        textViewName = (TextView) view.findViewById(R.id.companyName);
        textViewStockPrice = (TextView) view.findViewById(R.id.companyCurrentStockPrice);
        textViewName.setText(name);
        textViewStockPrice.setText("$" + lastPrice);
        graphImageView = (ImageView) view.findViewById(R.id.imageViewGraph);
        Picasso.with(getActivity()).load(graphUrl).into(graphImageView);

        return view;
    }
}
