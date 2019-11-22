package packages.products.ui.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import packages.products.BackEndRequestMaker;
import packages.products.MainActivity;
import packages.products.R;

import static android.view.View.GONE;
import static packages.products.GoogleSignIn.RC_SIGN_IN;

public class LoginActivity extends AppCompatActivity{

    packages.products.GoogleSignIn googleSignIn = new packages.products.GoogleSignIn();
    public GoogleSignInClient mGoogleSignInClient;

    private static String clientId = "1043665606163-fip7gfqqj0v0iqbopt8tpcrc0888ftcs.apps.googleusercontent.com";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final LoginActivity thisObj = this;
        findViewById(R.id.logOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mGoogleSignInClient.signOut().addOnCompleteListener(thisObj, new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    findViewById(R.id.sign_in_button).setEnabled(true);
                    findViewById(R.id.logOut).setEnabled(false);
                }
            });

            }
        });

        LoginActivity thisActivity = this;
        findViewById(R.id.startOffline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BackEndRequestMaker.isOnline = false;
                Intent intent = new Intent(thisActivity, MainActivity.class);
                startActivity(intent);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(clientId)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
        findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        googleSignIn.OnIntentStart(requestCode, data);
        if (googleSignIn.loggedUser != packages.products.GoogleSignIn.LoggedUser.None)
        {
            findViewById(R.id.logOut).setEnabled(true);
            findViewById(R.id.sign_in_button).setEnabled(false);
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        }
    }

}
