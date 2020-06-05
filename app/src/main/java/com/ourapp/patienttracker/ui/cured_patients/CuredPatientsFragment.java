package com.ourapp.patienttracker.ui.cured_patients;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ourapp.patienttracker.R;
import com.ourapp.patienttracker.ui.patients_list.Patient;
import com.ourapp.patienttracker.ui.patients_list.PatientsListAdapter;

import java.util.ArrayList;
import java.util.Objects;

import static android.content.ContentValues.TAG;

public class CuredPatientsFragment extends Fragment {

    private ArrayList<Patient> patients = new ArrayList<>();
    private CuredListAdapter curedListAdapter;
    private RecyclerView recyclerView;
    private TextView noPatientsError;
    FirebaseFirestore db;
    FirebaseUser user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_cured_patients, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        recyclerView = rootView.findViewById(R.id.recyclerView);
        noPatientsError = rootView.findViewById(R.id.noPatientsError);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), LinearLayoutManager.VERTICAL));
        curedListAdapter = new CuredListAdapter(getContext(), patients);
        recyclerView.setAdapter(curedListAdapter);

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
                                if (Boolean.parseBoolean(Objects.requireNonNull(document.getData().get("curedPatient")).toString())){
                                    patients.add(patient);
                                }

                                curedListAdapter.setPatients(patients);
                            }

                            if (patients.size() == 0){
                                noPatientsError.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.nav_host_fragment, fragment);
        fragmentTransaction.addToBackStack(fragment.toString());
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        fragmentTransaction.commit();
    }
}