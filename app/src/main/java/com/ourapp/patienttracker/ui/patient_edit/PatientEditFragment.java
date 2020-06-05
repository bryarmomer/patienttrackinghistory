package com.ourapp.patienttracker.ui.patient_edit;

import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.ourapp.patienttracker.R;
import com.ourapp.patienttracker.Utils;
import com.tsongkha.spinnerdatepicker.DatePicker;
import com.tsongkha.spinnerdatepicker.DatePickerDialog;
import com.tsongkha.spinnerdatepicker.SpinnerDatePickerDialogBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.ContentValues.TAG;


public class PatientEditFragment extends Fragment implements DatePickerDialog.OnDateSetListener {
    private Button saveBtn, pickData, pickTime;
    private String patientId;
    private int activeDialog = 0; // 0 -> birthday / 1-> admission / 2-> appointment
    FirebaseFirestore db;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private TextInputLayout firstNameField, lastNameField, birthDateFieldLayout, admissionDateFieldLayout, emergencyPhoneField, phoneField, emailField, addressField, bloodPressureField, hereditaryYesField, currentMedicationField, oldSurgeriesField, brokenBonesField, patientNoteField;
    private String firstName, lastName, emergencyPhone, admissionDate, birthDate, phone, email, address, bloodGroup, bloodPressure, diabetesType, hereditaryYes, allergies, hereditary, currentMedication, oldSurgeries, brokenBones, patientNote;
    private String appointmentDate, appointmentTime;
    private TextInputEditText birthDateField, admissionDateField;

    AutoCompleteTextView bloodGroupField,diabetesTypeField,allergiesField,hereditaryField;

    public PatientEditFragment(String id) {
        // Required empty public constructor
        this.patientId = id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView =  inflater.inflate(R.layout.fragment_patient_edit, container, false);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();

        firstNameField = rootView.findViewById(R.id.firstNameField);
        lastNameField = rootView.findViewById(R.id.lastNameField);
        emergencyPhoneField = rootView.findViewById(R.id.emergencyPhoneField);
        phoneField = rootView.findViewById(R.id.phoneField);
        emailField = rootView.findViewById(R.id.emailField);
        addressField = rootView.findViewById(R.id.addressField);
        bloodPressureField = rootView.findViewById(R.id.bloodPressureField);
        hereditaryYesField = rootView.findViewById(R.id.hereditaryYesField);
        currentMedicationField = rootView.findViewById(R.id.currentMedicationField);
        oldSurgeriesField = rootView.findViewById(R.id.oldSurgeriesField);
        brokenBonesField = rootView.findViewById(R.id.brokenBonesField);
        patientNoteField = rootView.findViewById(R.id.patientNoteField);

        birthDateFieldLayout = rootView.findViewById(R.id.birthDateFieldLayout);
        admissionDateFieldLayout = rootView.findViewById(R.id.admissionDateFieldLayout);

        birthDateField = rootView.findViewById(R.id.birthDateField);
        birthDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeDialog = 0;
                showDate(1980, 0, 1, R.style.DatePickerSpinner);
            }
        });
        admissionDateField = rootView.findViewById(R.id.admissionDateField);
        admissionDateField.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeDialog = 1;
                showDate(1980, 0, 1, R.style.DatePickerSpinner);
            }
        });




        bloodGroupField = rootView.findViewById(R.id.bloodGroupField);
        final String[] bloodGroups = new String[] {"O-", "O+", "A-", "A+", "B-", "B+", "AB-", "AB+"};
        final ArrayAdapter<String> bloodGroupsAdapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.dropdown_menu_popup_item,
                        bloodGroups);
        bloodGroupField.setAdapter(bloodGroupsAdapter);
        bloodGroupField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                bloodGroup = bloodGroupsAdapter.getItem(position);
            }
        });

        diabetesTypeField = rootView.findViewById(R.id.diabetesTypeField);
        final String[] diabetesTypes = new String[] {"Type 1", "Type 2", "None"};
        final ArrayAdapter<String> diabetesTypesAdapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.dropdown_menu_popup_item,
                        diabetesTypes);
        diabetesTypeField.setAdapter(diabetesTypesAdapter);
        diabetesTypeField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                diabetesType = diabetesTypesAdapter.getItem(position);
            }
        });

        allergiesField = rootView.findViewById(R.id.allergiesField);
        final String[] yesNoList = new String[] {"Yes", "No"};
        final ArrayAdapter<String> allergiesAdapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.dropdown_menu_popup_item,
                        yesNoList);
        allergiesField.setAdapter(allergiesAdapter);
        allergiesField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                allergies = allergiesAdapter.getItem(position);
            }
        });

        hereditaryField = rootView.findViewById(R.id.hereditaryField);
        final ArrayAdapter<String> hereditaryAdapter =
                new ArrayAdapter<>(
                        getActivity(),
                        R.layout.dropdown_menu_popup_item,
                        yesNoList);
        hereditaryField.setAdapter(hereditaryAdapter);
        hereditaryField.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                hereditary = hereditaryAdapter.getItem(position);
            }
        });


        pickData = rootView.findViewById(R.id.pickDate);
        pickData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeDialog = 2;
                showDate(1980, 0, 1, R.style.DatePickerSpinner);
            }
        });

        pickTime = rootView.findViewById(R.id.pickTime);
        pickTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog picker = new TimePickerDialog(getContext(),android.R.style.Theme_Holo_Light_Dialog_NoActionBar,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                String time = sHour + ":" + sMinute;
                                appointmentTime = time;
                                pickTime.setText(time);
                            }
                        }, 2, 3, false);
                picker.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                picker.show();
            }
        });


        db.collection("users").document(currentUser.getUid()).collection("patients")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.getId().equals(patientId)){
                                    loadData(document);
                                }
                            }
                        } else {
                        }
                    }
                });

        saveBtn = rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create a new user with a first and last name

                if (checkValidation()){

                    Map<String, Object> patient = new HashMap<>();
                    patient.put("firstName", firstName);
                    patient.put("lastName", lastName);
                    patient.put("birthDate", birthDate);
                    patient.put("emergencyPhone", emergencyPhone);
                    patient.put("admissionDate", admissionDate);
                    patient.put("phone", phone);
                    patient.put("email", email);
                    patient.put("address", address);
                    patient.put("bloodGroup", bloodGroup);
                    patient.put("bloodPressure", bloodPressure);
                    patient.put("diabetesType", diabetesType);
                    patient.put("allergies", allergies);
                    patient.put("hereditary", hereditary);
                    patient.put("currentMedication", currentMedication);
                    patient.put("oldSurgeries", oldSurgeries);
                    patient.put("brokenBones", brokenBones);
                    patient.put("patientNote", patientNote);
                    patient.put("hereditaryYes", hereditaryYes);
                    patient.put("appointmentDate", appointmentDate);
                    patient.put("appointmentTime", appointmentTime);

                    // Patient status, this is responsible for cured(1)/not(0)
                    patient.put("curedPatient", 0);

                    db.collection("users")
                            .document(currentUser.getUid())
                            .collection("patients")
                            .document(patientId)
                            .delete();

                    // Add a new document with a generated ID
                    db.collection("users").document(currentUser.getUid()).collection("patients")
                            .add(patient)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Snackbar.make(saveBtn, "Patient updated.", Snackbar.LENGTH_LONG).show();

                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        public void run() {
                                            // wait
                                        }
                                    }, 2000);   //5 seconds

                                    getActivity().onBackPressed();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(saveBtn, e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

        return rootView;
    }

    private void loadData(QueryDocumentSnapshot patient){
        firstNameField.getEditText().setText(patient.getData().get("firstName").toString());
        lastNameField.getEditText().setText(patient.getData().get("lastName").toString());
        birthDateField.setText(patient.getData().get("birthDate").toString());
        phoneField.getEditText().setText(patient.getData().get("phone").toString());
        emailField.getEditText().setText(patient.getData().get("email").toString());
        emergencyPhoneField.getEditText().setText(patient.getData().get("emergencyPhone").toString());
        admissionDateField.setText(patient.getData().get("admissionDate").toString());
        addressField.getEditText().setText(patient.getData().get("address").toString());

        bloodGroupField.setText(patient.getData().get("bloodGroup").toString());
        bloodPressureField.getEditText().setText(patient.getData().get("bloodPressure").toString());
        diabetesTypeField.setText(patient.getData().get("diabetesType").toString());
        allergiesField.setText(Objects.requireNonNull(patient.getData().get("allergies")).toString());
        hereditaryField.setText(Objects.requireNonNull(patient.getData().get("hereditary")).toString());
        Objects.requireNonNull(hereditaryYesField.getEditText()).setText(patient.getData().get("hereditaryYes").toString());
        currentMedicationField.getEditText().setText(patient.getData().get("currentMedication").toString());

        oldSurgeriesField.getEditText().setText(patient.getData().get("oldSurgeries").toString());
        brokenBonesField.getEditText().setText(patient.getData().get("brokenBones").toString());
        patientNoteField.getEditText().setText(patient.getData().get("patientNote").toString());

        pickData.setText(patient.getData().get("appointmentDate").toString());
        pickTime.setText(patient.getData().get("appointmentTime").toString());
    }


    void showDate(int year, int monthOfYear, int dayOfMonth, int spinnerTheme) {
        new SpinnerDatePickerDialogBuilder()
                .context(getContext())
                .callback(this)
                .spinnerTheme(spinnerTheme)
                .defaultDate(year, monthOfYear, dayOfMonth)
                .build()
                .show();
    }

    boolean checkValidation(){
        // Get inputs
        firstName = firstNameField.getEditText().getText().toString();
        lastName = lastNameField.getEditText().getText().toString();
        emergencyPhone = emergencyPhoneField.getEditText().getText().toString();
        phone = phoneField.getEditText().getText().toString();
        email = emailField.getEditText().getText().toString();
        address = addressField.getEditText().getText().toString();
        bloodPressure = bloodPressureField.getEditText().getText().toString();
        currentMedication = currentMedicationField.getEditText().getText().toString();
        oldSurgeries = oldSurgeriesField.getEditText().getText().toString();
        brokenBones = brokenBonesField.getEditText().getText().toString();
        patientNote = patientNoteField.getEditText().getText().toString();
        hereditaryYes = hereditaryYesField.getEditText().getText().toString();

        birthDate = birthDateField.getText().toString();
        admissionDate = admissionDateField.getText().toString();

        // Check patter for email id
        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(email);

        // Check for all fields are empty or not
        if (
                firstName.equals("") || firstName.length() == 0 ||
                        lastName.equals("") || lastName.length() == 0 ||
                        birthDate.equals("") || birthDate.length() == 0 ||
                        emergencyPhone.equals("") || emergencyPhone.length() == 0 ||
                        admissionDate.equals("") || admissionDate.length() == 0 ||
                        phone.equals("") || phone.length() == 0 ||
                        email.equals("") || email.length() == 0 ||
                        address.equals("") || address.length() == 0
        ) {
            Snackbar.make(saveBtn, "Please enter all credentials.", Snackbar.LENGTH_LONG).show();
            return false;
        }

        if (!m.find()) {
            Snackbar.make(saveBtn, "Patient's email is Invalid.", Snackbar.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        String date = dayOfMonth+"/"+monthOfYear+"/"+year;
        switch (activeDialog){
            case 0:
                birthDateField.setText(date);
                break;
            case 1:
                admissionDateField.setText(date);
                break;
            case 2:
                appointmentDate = date;
                pickData.setText(date);
                break;
        }
    }
}
