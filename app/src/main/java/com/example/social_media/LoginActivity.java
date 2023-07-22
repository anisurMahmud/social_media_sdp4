package com.example.social_media;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    GoogleSignInClient mGoogleSignInClient;

    //views
    EditText mEmailEt, mPasswordEt;
    TextView notHaveAccountTv, mRecoverPassTv;
    Button mLoginBtn;
    //progress dialog
    ProgressDialog progressDialog;

    //declare an instance of firebase
    private FirebaseAuth mAuth;
    SignInButton mGoogleLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init
        mEmailEt = findViewById(R.id.emailEt);
        mPasswordEt = findViewById(R.id.passwordEt);
        mLoginBtn = findViewById(R.id.loginBtn);
        notHaveAccountTv = findViewById(R.id.nothave_accountTv);
        mRecoverPassTv = findViewById(R.id.recoverPassTv);
        //before mAuth
        //Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);

        //in the onCreate() method, initiate the firebaseAuth instance.
        mAuth = FirebaseAuth.getInstance();
        mGoogleLoginBtn = findViewById(R.id.googleLoginBtn);

        //login button click
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //input data
                String email = mEmailEt.getText().toString();
                String passw = mPasswordEt.getText().toString();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    //invalid email pattern error
                    mEmailEt.setError("Invalid mail");
                    mEmailEt.setFocusable(true);
                } else {
                    //valid mail pattern
                    loginUser(email, passw);
                }

            }
        });
        //not have account click
        notHaveAccountTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                finish();
            }
        });

        // recover pass textview click

        mRecoverPassTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRecoverPasswordDialog();
            }
        });
        //handle google login btn click
        mGoogleLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        //init progress dialog
        progressDialog = new ProgressDialog(this);
        //progressDialog.setMessage("Logging in...");
    }

    private void showRecoverPasswordDialog() {
        //AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");

        //set layout linear layout
        LinearLayout linearLayout = new LinearLayout(this);

        //views to set in dialog
        EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 10, 10, 10);

        builder.setView(linearLayout);

        //button recovers
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input email
                String email = emailEt.getText().toString().trim();
                beginRecovery(email);

            }
        });


        //button cancel
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dismiss dialog
                dialog.dismiss();
            }
        });

        //show dialog
        builder.create().show();
    }

    private void beginRecovery(String email) {
        //show progress diagram
        progressDialog.setMessage("Sending email...");
        progressDialog.show();
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Failed...", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        //get and show proper error message
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();


                    }
                });

    }


    private void loginUser(String email, String passw) {
        //show progress diagram
        progressDialog.setMessage("Logging in...");
        progressDialog.show();
        mAuth.signInWithEmailAndPassword(email, passw)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            progressDialog.dismiss();
                            FirebaseUser user = mAuth.getCurrentUser();
                            //Toast.makeText(LoginActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginActivity.this, "Authentication failed. ", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //get error and show error message
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); //go previous activity
        return super.onSupportNavigateUp();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Result returned from launching the intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount>task= GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                //Google Sign In was successful, authenticate with firebase
                //GoogleSignInAccount account = task.getResult(ApiException.class);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWIthGoogle(account);
            } catch (ApiException e) {
                // Google Sign In Failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void firebaseAuthWIthGoogle (GoogleSignInAccount acct){

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(),null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //Sign in success, update UI with the signed-in user's information
                            FirebaseUser user=mAuth.getCurrentUser();

                            //if user is signing in first time then get and show user info from google account
                            if(task.getResult().getAdditionalUserInfo().isNewUser()){

                                //get user email and uid from auth
                                String email = user.getEmail();
                                String uid = user.getUid();
                                //when user is registered, store user info in firebase realtime database
                                //using hashmap
                                HashMap<Object, String> hashMap = new HashMap<>();
                                //put info in hashmap
                                hashMap.put("email", email);
                                hashMap.put("uid", uid);
                                hashMap.put("name", ""); //for later
                                hashMap.put("phone", ""); //for later
                                hashMap.put("image", ""); //for later
                                //firebase database instance
                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                //path to store user data named "users"
                                DatabaseReference reference = database.getReference("Users");
                                //put data within hashmap in database
                                reference.child(uid).setValue(hashMap);
                            }


                            //show user email in toast
                            Toast.makeText(LoginActivity.this, ""+user.getEmail(), Toast.LENGTH_SHORT).show();
                            //go to profile activity after logged in
                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                            finish();
                            //updateUI(user);
                        } else {
                            //If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Login Failed...", Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //get and show error message
                        Toast.makeText(LoginActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }


}