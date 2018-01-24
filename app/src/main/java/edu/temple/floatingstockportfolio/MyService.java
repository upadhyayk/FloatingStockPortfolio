package edu.temple.floatingstockportfolio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
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
import java.nio.Buffer;
import java.util.Iterator;

public class MyService extends Service {

    private final IBinder iBinder = new LocalBinder();
    String response;

    public class LocalBinder extends Binder{
        MyService getService(){
            return MyService.this;
        }
    }

    public MyService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
       return iBinder;
    }

    //get stock info from online and save to file
    public void getStockInformation(final String symbol){
        new Thread(new Runnable() {
            @Override
            public void run() {
                    try {

                        String fileName = "stockFile.txt";
                        String jsonString = "";

                        //get saved input from file to add to
                        FileInputStream fileInputStream = getApplicationContext().openFileInput(fileName);
                        int c;
                        while((c = fileInputStream.read()) != -1) {
                            jsonString += Character.toString((char) c);
                        }

                        Log.d("stockFile.txt", jsonString);
                        JSONArray jsonArray;
                        if(jsonString.length() > 0) {
                            jsonArray = new JSONArray(jsonString);
                        } else {
                            jsonArray = new JSONArray();
                        }

                        // Check if the stock already exists
                        for(int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String savedStockSymbol = jsonObject.getString("stockSymbol");
                            if(symbol.equals(savedStockSymbol)) {
                                Toast.makeText(getApplicationContext(), "The Stock already exists", Toast.LENGTH_SHORT);
                                return;
                            }
                        }

                        //get info from url and save into a string and close
                        URL url = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + symbol);
                        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                        InputStream inputStream = httpURLConnection.getInputStream();
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


                        /*
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
                        */

                        //save JsonArray to file
                        FileOutputStream outputStream;
                        try {
                            outputStream = getApplicationContext().openFileOutput(fileName, MODE_PRIVATE);
                            outputStream.write(jsonArray.toString().getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        httpURLConnection.disconnect();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
        }).start();
    }
}
