package edu.temple.floatingstockportfolio;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements NavigationFragment.OnFragmentInteractionListener {
    FragmentManager fragmentManager = getSupportFragmentManager();
    FragmentTransaction fragmentTransaction;
    String stockName;
    Button button;
    EditText editText;
    TextView textView;
    String itemSelected;
    NavigationFragment navigationFragment;
    DetailsFragment detailsFragment;
    public static TextView data;
    ArrayList<String> stocks = new ArrayList<>();

    MyService myService = new MyService();
    boolean isBound = false;
    boolean twoPanes = false;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textViewMessage);
        button = (Button) findViewById(R.id.goButton);
        data = (TextView) findViewById(R.id.textViewMessage);

        navigationFragment = NavigationFragment.newInstance(stocks);
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragmentContainer, navigationFragment);
        fragmentTransaction.commit();

        String fileName = "StockFile.txt";
        File file = new File(getFilesDir(), fileName);
        try{
            file.createNewFile();
        }catch (Exception e){
            e.printStackTrace();
        }

        Intent intent = new Intent(this,MyIntentService.class);
        startService(intent);

        twoPanes = (findViewById(R.id.frameLayout) != null);


        //hide and show the text to add more stocks
        ImageButton imageButton = (ImageButton) findViewById(R.id.imageButton);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getVisibility() == View.VISIBLE) {
                    editText.setVisibility(View.INVISIBLE);
                } else {
                    editText.setVisibility(View.VISIBLE);
                }

                if (button.getVisibility() == View.VISIBLE) {
                    button.setVisibility(View.INVISIBLE);
                } else {
                    button.setVisibility(View.VISIBLE);
                }
            }
        });

        //add stock to arraylist and make new instance of fragment with updated arraylist
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stockName = editText.getText().toString();
                if(stocks.contains(stockName)){
                    Toast.makeText(getApplicationContext(), "Stock already exists", Toast.LENGTH_SHORT).show();
                }else {
                    stocks.add(stockName);
                    getStockInfo(stockName);
                }
                if (!stocks.isEmpty()) {
                    if (textView.getVisibility() == View.VISIBLE) {
                        textView.setVisibility(View.INVISIBLE);
                    }
                }
                navigationFragment = NavigationFragment.newInstance(stocks);
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.fragmentContainer, navigationFragment).commit();
            }
        });
    }


    //connect service
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.LocalBinder binder = (MyService.LocalBinder) iBinder;
            myService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isBound = false;
        }
    };

    //send the item selected to the details fragment
    @Override
    public void onFragmentInteraction(String string) {
        itemSelected = string;
        detailsFragment = DetailsFragment.newInstance(itemSelected);
        if(twoPanes){
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.frameLayout, detailsFragment).commit();
        }else{
            fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.replace(R.id.fragmentContainer, detailsFragment).commit();
        }
    }


    public void getStockInfo(final String symbol) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                String fileName = "StockFile.txt";
                String jsonString = "";
                boolean exists = false;

                try {
                    try {
                        //get saved input from file to add to
                        FileInputStream fileInputStream = getApplicationContext().openFileInput(fileName);
                        int c;
                        while ((c = fileInputStream.read()) != -1) {
                            jsonString += Character.toString((char) c);
                        }
                    }catch (Exception e) {
                        e.printStackTrace();
                    }

                    Log.d("stockFile.txt", jsonString);
                    JSONArray jsonArray;
                    if (jsonString.length() > 0) {
                        jsonArray = new JSONArray(jsonString);
                    } else {
                        jsonArray = new JSONArray();
                    }

                    // Check if the stock already exists
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String savedStockSymbol = jsonObject.getString("Symbol");
                        if (symbol.equals(savedStockSymbol)) {
                            Toast.makeText(getApplicationContext(), "The Stock already exists", Toast.LENGTH_SHORT).show();
                            exists = true;
                            return;
                        }
                    }

                    //get info from url and save into a string and close
                    URL url = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + symbol);
                    Log.d("symbol added", symbol);
                  //  HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                  //  InputStream inputStream = httpURLConnection.getInputStream();

                    InputStream inputStream = url.openStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

                    StringBuilder stringBuilder = new StringBuilder();
                    String line = "";
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }

                    inputStream.close();
                    bufferedReader.close();

                    //put info into jsonObject
                    JSONObject stockObject = new JSONObject(stringBuilder.toString());
                    Log.d("Saved stock data", stringBuilder.toString());

                    //put info into jsonArray
                    JSONObject fileInputForArray = new JSONObject();
                    fileInputForArray.put("Symbol", stockObject.get("Symbol"));
                    fileInputForArray.put("Name", stockObject.get("Name"));
                    fileInputForArray.put("LastPrice", stockObject.get("LastPrice"));
                    String GraphUrl = "https://finance.google.com/finance/getchart?p=5d&q=" + symbol;
                    fileInputForArray.put("GraphUrl", GraphUrl);
                    jsonArray.put(fileInputForArray);


                    //itterate through jsonArray
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject currentObject = jsonArray.getJSONObject(i);
                        Iterator key = currentObject.keys();
                        while (key.hasNext()) {
                            String currentKey = key.next().toString();
                            Log.d("Key", currentKey);
                            Log.d("Value", currentObject.getString(currentKey));
                        }
                        Log.d("Blank Line", "----------------------");
                    }


                    //save JsonArray to file
                    FileOutputStream outputStream = getApplicationContext().openFileOutput(fileName, MODE_PRIVATE);
                    outputStream.write(jsonArray.toString().getBytes());
                    outputStream.close();


                 //   httpURLConnection.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
