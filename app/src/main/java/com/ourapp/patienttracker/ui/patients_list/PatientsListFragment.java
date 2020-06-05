package com.ourapp.patienttracker.ui.patients_list;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ourapp.patienttracker.R;
import com.ourapp.patienttracker.ui.patient_add.PatientAddFragment;
import com.ourapp.patienttracker.ui.patient_edit.PatientEditFragment;

import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class PatientsListFragment extends Fragment implements PatientsListAdapter.CallBack {

    FloatingActionButton fab;
    private ArrayList<Patient> patients = new ArrayList<>();
    private PatientsListAdapter patientsListAdapter;
    private RecyclerView recyclerView;
    private TextView noPatientsError;
    FirebaseFirestore db;
    FirebaseUser user;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_patients_list, container, false);

        fab = rootView.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PatientAddFragment patientAddFragment = new PatientAddFragment();
                replaceFragment(patientAddFragment);
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        recyclerView = rootView.findViewById(R.id.recyclerView);
        noPatientsError = rootView.findViewById(R.id.noPatientsError);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        patientsListAdapter = new PatientsListAdapter(getContext(), this, patients);
        recyclerView.setAdapter(patientsListAdapter);

        createList();

        return rootView;
    }

    private void createList() {
        patients = new ArrayList<>();

        // users -> user_uid -> patients
        db.collection("users").document(user.getUid()).collection("patients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Patient patient = new Patient();
                                String patientFullName = document.getData().get("firstName") + " " + document.getData().get("lastName");
                                patient.setName(patientFullName);
                                String patientId = document.getId();
                                patient.setId(patientId);
                                if (!Boolean.parseBoolean(Objects.requireNonNull(document.getData().get("curedPatient")).toString())){
                                    patients.add(patient);
                                }

                                patientsListAdapter.setPatients(patients);
                            }

                            if (patients.size() == 0){
                                noPatientsError.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });

        //db.collection("users").document(user.getUid()).collection("patients").document()
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }

    @Override
    public void startEditFragment(String id) {
        PatientEditFragment patientEditFragment = new PatientEditFragment(id);
        replaceFragment(patientEditFragment);
    }
}