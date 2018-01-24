package edu.temple.floatingstockportfolio;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;


public class MyIntentService extends IntentService {

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        while(true) {
            try{
                // String that will hold the saves stocks
                String jsonString = "";
                String fileName = "StockFile.txt";
                FileInputStream fileInputStream = getApplicationContext().openFileInput(fileName);
                int c;
                while((c = fileInputStream.read()) != -1) {
                    jsonString += Character.toString((char) c);
                }

                fileInputStream.close();
                // Create a JSON Array of the existing stocks
                JSONArray jsonArray;
                if(jsonString.length() > 0) {
                    jsonArray = new JSONArray(jsonString);

                    String jsonArrayLength = Integer.toString(jsonArray.length());
                    Log.d("inServiceJSONArrayLen", jsonArrayLength);

                    // Loop through the existing stocks and check each one for if the last price changed
                    for(int i = 0; i < jsonArray.length(); i++) {
                        // Retrieve JSON Object of the stock at position i
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String stockSymbol = jsonObject.getString("Symbol");
                        String lastPrice = jsonObject.getString("LastPrice");

                        Log.d("inServiceLastPrice", lastPrice);

                        // Get the new stock details
                        URL stockURL = new URL("http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=" + stockSymbol);

                        InputStream inputStream = stockURL.openStream();

                        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        String response = "";
                        String temporaryResponse;

                        temporaryResponse = bufferedReader.readLine();
                        while(temporaryResponse != null) {
                            response += temporaryResponse;
                            temporaryResponse = bufferedReader.readLine();
                        }

                        // Close connection with InputStreamReader and InputStream
                        inputStream.close();
                        inputStreamReader.close();
                        bufferedReader.close();



                        JSONObject newStockObject = new JSONObject(response);
                        // Check if the new lastPrice is the same as the saved one.
                        // If not the same, the update it and save it to the JSON Array
                        // and update the saved file.

                        if(!newStockObject.getString("LastPrice").equals(lastPrice)) {
                            // FOR TESTING UNCOMMENT THE LINE BELOW AND COMMENT THE LINE ABOVE
                            //if(!"123".equals(lastPrice)) {
                            Log.d("updatingStock", "true");

                            lastPrice = newStockObject.getString("LastPrice");
                            // FOR TESTING UNCOMMENT THE LINE BELOW AND COMMENT THE LINE ABOVE
                            //lastPrice = "123";
                            jsonObject.put("LastPrice", lastPrice);
                            jsonArray.remove(i);
                            jsonArray.put(jsonObject);

                            FileOutputStream fileOutputStream = getApplicationContext().openFileOutput("StockFile.txt", MODE_PRIVATE);

                            fileOutputStream.write(jsonArray.toString().getBytes());

                            fileOutputStream.close();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Sleep for 60 seconds
            SystemClock.sleep(60000);
        }
    }

}

