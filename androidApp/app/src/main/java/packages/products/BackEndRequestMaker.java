package packages.products;

import android.text.TextUtils;

import com.google.android.gms.common.api.ApiException;

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

public class BackEndRequestMaker {

    public static String sessionToken = null;
    public static CookieManager cookieManager = new CookieManager();
    public static boolean isOnline = true;

    public static Queue<CallData> requestList = new LinkedList<CallData>();

    public static class CallData {
        public String urlString;
        public String method;
        public JSONObject jsonObject;
        public Integer localProductId = -1;
    }

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
            HashMap<Integer, Integer> localRemoteIdMapping = new HashMap<>() ;
            while (!requestList.isEmpty())
            {
                CallData pendingRequest = requestList.remove();

                if (pendingRequest.method == "POST")
                {
                    Response response = makeCall(pendingRequest.urlString, pendingRequest.method, pendingRequest.jsonObject);
                    localRemoteIdMapping.put(new Integer((int) pendingRequest.localProductId), Integer.parseInt(response.body));
                }
                else if (pendingRequest.method == "PUT" || pendingRequest.method == "DELETE")
                {
                    Integer newID = localRemoteIdMapping.get(pendingRequest.localProductId);
                    makeCall(pendingRequest.urlString + newID, pendingRequest.method, pendingRequest.jsonObject);
                }

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
                if (method == "POST" || method == "PUT")
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



    public static Response makeCall(final String urlString, final String method, final JSONObject jsonObject)
    {
        Response response = new Response();

        assert isOnline;
        String jsonString;
        if (jsonObject != null)
        {
            jsonString = jsonObject.toString();
        }
        else
        {
            jsonString = "";
        }
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
    public static void saveCall(final String urlString, final String method, final JSONObject jsonObject, long localProductId)
    {
        CallData callData = new CallData();
        callData.jsonObject = jsonObject;
        callData.method = method;
        callData.urlString = urlString;
        callData.localProductId = Integer.valueOf((int) localProductId);
        requestList.add(callData);
    }

}
