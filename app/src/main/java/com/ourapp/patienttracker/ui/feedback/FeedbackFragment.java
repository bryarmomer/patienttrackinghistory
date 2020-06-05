package com.ourapp.patienttracker.ui.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ourapp.patienttracker.R;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FeedbackFragment extends Fragment {

    private TextInputLayout nameField, emailField, feedbackField;
    private Button sendFeedbackBtn;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_feedback, container, false);

        nameField = rootView.findViewById(R.id.nameField);
        emailField = rootView.findViewById(R.id.emailField);
        feedbackField = rootView.findViewById(R.id.feedbackField);
        sendFeedbackBtn = rootView.findViewById(R.id.sendFeedbackBtn);

        sendFeedbackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = nameField.getEditText().getText().toString();
                String email = emailField.getEditText().getText().toString();
                String feedbackText = feedbackField.getEditText().getText().toString();

                if (name.equals("") || name.length() == 0 || email.equals("") || email.length() == 0 || feedbackText.equals("") || feedbackText.length() == 0){
                    Snackbar.make(sendFeedbackBtn, "Please enter all credentials.", Snackbar.LENGTH_LONG).show();
                } else {
                    // Save feedback in "feedback" collection
                    // Create a new user with a first and last name
                    Map<String, Object> feedback = new HashMap<>();
                    feedback.put("name", name);
                    feedback.put("email", email);
                    feedback.put("feedback", feedbackText);

                    // Add a new document with a generated ID
                    db.collection("feedback")
                            .add(feedback)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Snackbar.make(sendFeedbackBtn, "Thanks for your feedback.", Snackbar.LENGTH_LONG).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Snackbar.make(sendFeedbackBtn, Objects.requireNonNull(e.getMessage()), Snackbar.LENGTH_LONG).show();
                                }
                            });
                }
            }
        });

        return rootView;
    }
}