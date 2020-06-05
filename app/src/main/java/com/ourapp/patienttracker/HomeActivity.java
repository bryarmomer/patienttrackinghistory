package com.ourapp.patienttracker;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class HomeActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private LinearLayout welcomeMessage, loadingBar;
    private NavigationView navigationView;
    private View app_bar_home;
    FirebaseFirestore db;
    Toolbar toolbar;
    DrawerLayout drawer;
    NavController navController;
    private FirebaseAuth mAuth;
    DocumentReference docRef;
    FirebaseUser user;
    TextView welcomeMessageDoctorName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Change status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.blue));
        }

        db = FirebaseFirestore.getInstance();
        setContentView(R.layout.activity_home);

        navigationView = findViewById(R.id.nav_view);
        app_bar_home = findViewById(R.id.app_bar_home);
        welcomeMessage = findViewById(R.id.welcomeMessage);
        toolbar = findViewById(R.id.toolbar);
        drawer = findViewById(R.id.drawer_layout);
        loadingBar = findViewById(R.id.loading_bar);
        welcomeMessageDoctorName = findViewById(R.id.welcomeMessageDoctorName);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        docRef = db.collection("users").document(user.getUid());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        loadingBar.setVisibility(View.GONE);
                        welcomeMessage.setVisibility(View.VISIBLE);

                        String fullDoctorName = "Dr. "+document.getData().get("firstName").toString() + " "+ document.getData().get("lastName").toString();
                        welcomeMessageDoctorName.setText(fullDoctorName);

                        View headerView = navigationView.getHeaderView(0);
                        TextView navUsername = headerView.findViewById(R.id.doctorName);
                        navUsername.setText(fullDoctorName);
                        ImageView navImageView = headerView.findViewById(R.id.doctorImageView);
                        if (!(document.getData().get("imgUrl").toString().equals("") || document.getData().get("imgUrl").toString().length() == 0)){
                            Picasso
                                    .get()
                                    .load(document.getData().get("imgUrl").toString())
                                    .resize(120, 120)
                                    .centerCrop()
                                    .into(navImageView);
                        }

                        welcomeMessage.animate()
                                .setDuration(3000)
                                .alpha(0)
                                .setListener(new AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        super.onAnimationEnd(animation);
                                        welcomeMessage.setVisibility(View.GONE);
                                        navigationView.setVisibility(View.VISIBLE);
                                        app_bar_home.setVisibility(View.VISIBLE);
                                    }
                                });
                    } else {
                        //Log.d(TAG, "No such document");
                    }
                } else {
                    //Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });

        setSupportActionBar(toolbar);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_patients_list, R.id.nav_cured_patients, R.id.nav_feedback)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.logoutBtn){
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
