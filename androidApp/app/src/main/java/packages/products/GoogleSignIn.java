package packages.products;

import android.content.Intent;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static android.view.View.GONE;
import static packages.products.BackEndRequestMaker.cookieManager;
import static packages.products.BackEndRequestMaker.makeCall;
import static packages.products.BackEndRequestMaker.sessionToken;

public class GoogleSignIn {
    static final String COOKIES_HEADER = "Set-Cookie";
    public static int RC_SIGN_IN = 100;
    public static GoogleSignInAccount googleAccount;
    public static LoggedUser loggedUser = LoggedUser.None;

    public enum LoggedUser{
        None,
        Maintainer,
        Manager
    };

    public void OnIntentStart(int requestCode, Intent data)
    {
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = com.google.android.gms.auth.api.signin.GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            BackEndRequestMaker.Response result = makeCall("http://10.0.2.2:5000/products", "GET", "");
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(final Task<GoogleSignInAccount> completedTask) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try  {
                    googleAccount = completedTask.getResult(ApiException.class);
                    String idToken = googleAccount.getIdToken();
                    URL url = new URL("http://10.0.2.2:5000/logingoogle");
                    HttpURLConnection con = (HttpURLConnection)url.openConnection();
                    con.setRequestMethod("POST");
                    con.setDoOutput(true);
                    //con.setRequestProperty("Cookie", "name=value");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Accept","application/json");
                    con.setDoOutput(true);
                    con.setDoInput(true);
                    con.connect();

                    String jsonString = new JSONObject()
                            .put("token", idToken).toString();


                    DataOutputStream out = new DataOutputStream(con.getOutputStream());
                    out.write(jsonString.getBytes());
                    out.flush();
                    out.close();
                    int responseCode = con.getResponseCode();

                    Map<String, List<String>> headerFields = con.getHeaderFields();
                    List<String> cookiesHeader = headerFields.get(COOKIES_HEADER);

                    if (cookiesHeader != null) {
                        for (String cookie : cookiesHeader) {
                            cookieManager.getCookieStore().add(null, HttpCookie.parse(cookie).get(0));
                        }
                    }

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader br=new BufferedReader(new InputStreamReader(con.getInputStream()));
                        line=br.readLine();
                        if (line.equals("\"Maintainer\""))
                        {
                            loggedUser = LoggedUser.Maintainer;
                        }
                        else
                        {
                            loggedUser = LoggedUser.Manager;
                        }
                    }


                    con.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }});

        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        // Signed in successfully, show authenticated UI.
        //updateUI(account);

    }
}
