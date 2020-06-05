package com.ourapp.patienttracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    LinearLayout  signup3, loadingBar;
    ScrollView signup1, signup2;
    Button continue1, continue2, finish;
    RadioButton agree, disagree;
    TextInputLayout firstNameField, lastNameField, ageField, degreeField, phoneField, emailField, passwordField, addressField, experienceField;
    String[] bloodGroups;
    FirebaseFirestore db;
    String firstName, lastName, age, degree, phone, email, password, address, experience;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        signup1 = findViewById(R.id.signup1);
        signup2 = findViewById(R.id.signup2);
        signup3 = findViewById(R.id.signup3);
        loadingBar = findViewById(R.id.loading_bar);
        continue1 = findViewById(R.id.continue1);
        continue2 = findViewById(R.id.continue2);
        finish = findViewById(R.id.finish);
        agree = findViewById(R.id.agree);
        disagree = findViewById(R.id.disagree);
        firstNameField = findViewById(R.id.firstNameField);
        lastNameField = findViewById(R.id.lastNameField);
        ageField = findViewById(R.id.ageField);
        degreeField = findViewById(R.id.degreeField);
        phoneField = findViewById(R.id.phoneField);
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        addressField = findViewById(R.id.addressField);
        experienceField = findViewById(R.id.experienceField);

        bloodGroups = new String[] {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        R.layout.dropdown_menu_popup_item,
                        bloodGroups);

        AutoCompleteTextView editTextFilledExposedDropdown = findViewById(R.id.bloodGroupField);
        editTextFilledExposedDropdown.setAdapter(adapter);

        continue1.setOnClickListener(this);
        continue2.setOnClickListener(this);
        finish.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.continue1:
                if (!agree.isChecked()){
                    Snackbar.make(continue1, "You must agree to continue.", Snackbar.LENGTH_LONG).show();
                } else {
                    signup1.animate()
                            .setDuration(500)
                            .alpha(0)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    signup1.setVisibility(View.GONE);
                                    signup2.setAlpha(0);
                                    signup2.setVisibility(View.VISIBLE);
                                    signup2.animate()
                                            .setDuration(500)
                                            .alpha(1);
                                }
                            });
                }
                break;
            case R.id.continue2:
                if (checkValidation()){
                    signup2.animate()
                            .setDuration(500)
                            .alpha(0)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    signup2.setVisibility(View.GONE);
                                    signup3.setAlpha(0);
                                    signup3.setVisibility(View.VISIBLE);
                                    signup3.animate()
                                            .setDuration(500)
                                            .alpha(1);
                                }
                            });
                }
                break;
            case R.id.finish:
                experience = experienceField.getEditText().getText().toString();
                if (experience.equals("") || experience.length() == 0){
                    Snackbar.make(continue2, "Experience is required.", Snackbar.LENGTH_LONG).show();
                } else {
                    signUp();
                }
        }
    }

    boolean checkValidation(){
        // Get inputs
        firstName = firstNameField.getEditText().getText().toString();
        lastName = lastNameField.getEditText().getText().toString();
        age = ageField.getEditText().getText().toString();
        degree = degreeField.getEditText().getText().toString();
        phone = phoneField.getEditText().getText().toString();
        email = emailField.getEditText().getText().toString();
        password = passwordField.getEditText().getText().toString();
        address = addressField.getEditText().getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(email);

        // Check for all fields are empty or not
        if (
                firstName.equals("") || firstName.length() == 0 ||
                lastName.equals("") || lastName.length() == 0 ||
                age.equals("") || age.length() == 0 ||
                degree.equals("") || degree.length() == 0 ||
                phone.equals("") || phone.length() == 0 ||
                email.equals("") || email.length() == 0 ||
                address.equals("") || address.length() == 0
        ) {
            Snackbar.make(continue2, "Please enter all credentials.", Snackbar.LENGTH_LONG).show();
            return false;
        }

        if (!m.find()) {
            Snackbar.make(continue2, "Your Email is Invalid.", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    void signUp(){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        loadingBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "Singed -> Database", Toast.LENGTH_SHORT).show();
                            // Create a new user with a first and last name
                            Map<String, Object> user = new HashMap<>();
                            user.put("firstName", firstName);
                            user.put("lastName", lastName);
                            user.put("age", age);
                            user.put("degree", degree);
                            user.put("phone", phone);
                            user.put("email", email);
                            user.put("address", address);
                            user.put("experience", experience);
                            user.put("imgUrl", "");
                            user.put("userActive", false);
                            user.put("userAdmin", false);

                            // Add a new document with a generated ID
                            db.collection("users")
                                    .document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                    .set(user)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                            new MaterialAlertDialogBuilder(SignUpActivity.this)
                                                    .setTitle("Success")
                                                    .setMessage("Your information has been saved for later review. You'll get an email once you've been accepted!")
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            FirebaseAuth.getInstance().signOut();
                                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                        }
                                                    })
                                                    .show();
                                            // Log.d(TAG, "DocumentSnapshot added with ID: " + documentReference.getId());
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                            new MaterialAlertDialogBuilder(SignUpActivity.this)
                                                    .setTitle("Title")
                                                    .setMessage(e.getMessage())
                                                    .setCancelable(false)
                                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            startActivity(new Intent(SignUpActivity.this, MainActivity.class));
                                                        }
                                                    })
                                                    .show();
                                            // Log.w(TAG, "Error adding document", e);
                                        }
                                    });
                        } else {
                            // If sign in fails, display a message to the user.
                            loadingBar.setVisibility(View.GONE);
                            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            Snackbar.make(continue1, task.getException().getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }
}
