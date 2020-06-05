package com.ourapp.patienttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.DateFormat;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private Button loginBtn, signUpBtn;
    private TextInputLayout emailField, passwordField;
    private LinearLayout loadingBar, mainContainer;
    private TextView hospitalName, forgotPasswordBtn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();

        // Finding views
        hospitalName = findViewById(R.id.hospital_name);
        loadingBar = findViewById(R.id.loading_bar);
        mainContainer = findViewById(R.id.mainContainer);
        loginBtn = findViewById(R.id.loginBtn);
        signUpBtn = findViewById(R.id.signupBtn);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        forgotPasswordBtn = findViewById(R.id.forgotPasswordBtn);

        // Animation
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hospitalName.animate()
                        .translationY(-260)
                        .setDuration(1500)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                super.onAnimationEnd(animation);
                                mainContainer.animate()
                                        .setDuration(1000)
                                        .alpha(1);

                            }
                        });
            }
        }, 2000);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Setting clickListeners
        loginBtn.setOnClickListener(this);
        signUpBtn.setOnClickListener(this);
        forgotPasswordBtn.setOnClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null){
            startActivity(new Intent(MainActivity.this, HomeActivity.class));
            Snackbar.make(loginBtn, "Successfully logged in.", Snackbar.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Not logged.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.loginBtn:
                checkValidation();
                break;
            case R.id.signupBtn:
                startActivity(new Intent(this, SignUpActivity.class));
                break;
            case R.id.forgotPasswordBtn:
                MaterialAlertDialogBuilder dialog = new MaterialAlertDialogBuilder(MainActivity.this);

                final TextInputEditText input = new TextInputEditText(MainActivity.this);
                input.setInputType(InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                input.setLayoutParams(lp);
                dialog.setView(input);
                dialog.setTitle("Password reset");
                dialog.setMessage("Enter your email, we'll send you a recovery link");

                dialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Check patter for email id
                        Pattern p = Pattern.compile(Utils.regEx);
                        Matcher m = p.matcher((input.getText().toString()));

                        if (m.find()){
                            mAuth.sendPasswordResetEmail(input.getText().toString())
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Snackbar.make(signUpBtn, "Email sent.", Snackbar.LENGTH_LONG).show();
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(signUpBtn, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
                                }
                            });
                        } else {
                            Snackbar.make(signUpBtn, "Your Email is Invalid.", Snackbar.LENGTH_LONG).show();
                        }
                    }
                });

                dialog.setNegativeButton("Cancel", null);
                dialog.show();

                break;
        }
    }


    // Check Validation before login
    private void checkValidation() {
        // Get email id and password
        final String getEmailId = emailField.getEditText().getText().toString();
        final String getPassword = passwordField.getEditText().getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(getEmailId);

        // Check for both field is empty or not
        if (getEmailId.equals("") || getEmailId.length() == 0 || getPassword.equals("") || getPassword.length() == 0) {
            Snackbar.make(loginBtn, "Please enter both credentials.", Snackbar.LENGTH_LONG).show();
        }
        // Check if email id is valid or not
        else if (!m.find())
            Snackbar.make(loginBtn, "Your Email is Invalid.", Snackbar.LENGTH_LONG).show();
            // Else do login and do your stuff
        else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
            loadingBar.setVisibility(View.VISIBLE);

            db.collection("users")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                Boolean inactive = true;
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    if (document.getData().get("email").toString().equals(getEmailId) && Boolean.parseBoolean(document.getData().get("userActive").toString())){
                                        inactive = false;
                                    }
                                }

                                if (inactive){
                                    loadingBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                    Snackbar.make(loginBtn, "Account is not yet active, please wait for activation.", Snackbar.LENGTH_LONG).show();
                                } else {
                                    mAuth.signInWithEmailAndPassword(getEmailId, getPassword)
                                            .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                                    if (task.isSuccessful()) {
                                                        // Sign in success, update UI with the signed-in user's information
                                                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                                                    } else {
                                                        // If sign in fails, display a message to the user.
                                                        loadingBar.setVisibility(View.INVISIBLE);
                                                        Snackbar.make(loginBtn, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                            } else {
                                //Log.w(TAG, "Error getting documents.", task.getException());
                            }
                        }
                    });
        }

    }
}
