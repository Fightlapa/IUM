package packages.products;

import android.text.TextUtils;

import com.google.android.gms.common.api.ApiException;

import org.json.JSONException;
import org.json.JSONObject;
import java.net.CookieHandler;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import static packages.products.MainActivity.productDatabase;

public class BackEndRequestMaker {

    public static String sessionToken = null;
    public static CookieManager cookieManager = new CookieManager();
    public static boolean isOnline = true;

    public static class Response {
        String body = "";
        int code = -1;
    }

    public static boolean isOnline()
    {
        return isOnline;
    }

    public static void changeOnlineStatus()
    {
        isOnline ^= true;
        if (isOnline)
        {
            sendPendingRequests();
        }
    }

    public static void sendPendingRequests() {
        for (Request pendingRequest : ProductRepository.getAllRequests())
        {
            try
            {
                if (pendingRequest.method.equals("POST"))
                {
                    Response response = makeCall(pendingRequest.url, pendingRequest.method, pendingRequest.jsonString);
                    Product product = ProductRepository.getByLocalID(pendingRequest.localProductId);
                    product.serverProductId = Integer.parseInt(response.body);
                    ProductRepository.updateLocalProduct(product);
                }
                else if (pendingRequest.method.equals("PUT"))
                {
                    int serverProductId;
                    if (pendingRequest.serverProductId == -1)
                        serverProductId = ProductRepository.getByLocalID(pendingRequest.localProductId).serverProductId;
                    else
                        serverProductId = pendingRequest.serverProductId;
                    makeCall(pendingRequest.url + serverProductId, pendingRequest.method, pendingRequest.jsonString);
                }
                if (pendingRequest.method.equals("DELETE") && pendingRequest.serverProductId != -1)
                {
                    makeCall(pendingRequest.url + pendingRequest.serverProductId, pendingRequest.method, pendingRequest.jsonString);
                }
                ProductRepository.deleteRequest(pendingRequest);
            }
            catch(Exception e)
            {

            }

        }
    }

    public static String getOnlineStatusString()
    {
        String returnString;
        if (isOnline)
        {
            returnString = "Online";
        }
        else
        {
            returnString = "Offline";
        }
        return returnString;
    }


    public static class CallMaker implements Runnable {
        private volatile int responseCode;
        private volatile String returnBody = "";

        private String urlString;
        private String method;
        private String jsonString;



        CallMaker(final String urlString, final String method, final String jsonString)
        {
            this.urlString = urlString;
            this.method = method;
            this.jsonString = jsonString;
        }



        @Override
        public void run() {
            try  {
                CookieHandler.setDefault(cookieManager);
                URL url = new URL(urlString);
                HttpURLConnection con = (HttpURLConnection)url.openConnection();
                con.setRequestMethod(method);
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Accept","application/json");
                con.setDoInput(true);
                if (method.equals("POST") || method.equals("PUT"))
                {
                    con.setDoOutput(true);
                    DataOutputStream out = new DataOutputStream(con.getOutputStream());
                    out.write(jsonString.getBytes());
                    out.flush();
                    out.close();
                }

                responseCode = con.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();
                    returnBody = response.toString();
                }
                else
                {
                    returnBody = "";
                }

                con.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public int getResponseCode()
        {
            return responseCode;
        }

        public String getReturnBody()
        {
            return returnBody;
        }
    }



    public static Response makeCall(final String urlString, final String method, final String jsonString)
    {
        Response response = new Response();

        assert isOnline;

        CallMaker callMaker = new CallMaker(urlString, method, jsonString);
        Thread thread = new Thread(callMaker);

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        response.body = callMaker.getReturnBody();
        response.code = callMaker.getResponseCode();

        return response;
    }
    public static void saveCall(final String urlString, final String method, final JSONObject jsonObject, long localProductId, long serverProductId)
    {
        Request callData = new Request();
        String uniqueID = UUID.randomUUID().toString();
        callData.guid = uniqueID;
        if (!method.equals("GET"))
        {
            try {
                jsonObject.put("guid", callData.guid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (jsonObject != null)
        {
            callData.jsonString = jsonObject.toString();
        }
        callData.method = method;
        callData.url = urlString;
        callData.localProductId = (int) localProductId;
        callData.serverProductId = (int) serverProductId;
        productDatabase.RequestDao().insert(callData);
    }

}
