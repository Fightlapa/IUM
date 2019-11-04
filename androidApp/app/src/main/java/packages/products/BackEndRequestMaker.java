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

import packages.products.ui.login.LoginActivity;




public class BackEndRequestMaker {

    public static String sessionToken = null;
    public static CookieManager cookieManager = new CookieManager();

    public static class Response {
        String body;
        int code;
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
                if (method == "GET")
                {
                    con.setRequestProperty("Accept","application/json");
                    con.setDoInput(true);
                }
                else if (method == "POST" || method == "PUT")
                {
                    con.setDoOutput(true);
                }


                //con.connect();
                if (method == "GET")
                {

                }
                else if (method == "POST" || method == "PUT")
                {
                    DataOutputStream out = new DataOutputStream(con.getOutputStream());
                    out.write(jsonString.getBytes());
                    out.flush();
                    out.close();
                }



                responseCode = con.getResponseCode();


                if (responseCode == HttpURLConnection.HTTP_OK)
                {
                    if (method == "GET")
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
        CallMaker callMaker = new CallMaker(urlString, method, jsonString);
        Thread thread = new Thread(callMaker);

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Response response = new Response();

        response.body = callMaker.getReturnBody();
        response.code = callMaker.getResponseCode();
        return response;
    }
}
