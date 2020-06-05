package com.ourapp.patienttracker.ui.cured_patients;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ourapp.patienttracker.R;
import com.ourapp.patienttracker.ui.patients_list.Patient;

import java.util.ArrayList;

public class CuredListAdapter extends RecyclerView.Adapter<CuredListAdapter.SwipeViewHolder> {

    private Context context;
    private ArrayList<Patient> patients;
    FirebaseFirestore db;
    FirebaseUser user;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public CuredListAdapter(Context context, ArrayList<Patient> patients) {
        this.context = context;
        this.patients = patients;

        this.user = FirebaseAuth.getInstance().getCurrentUser();
        this.db = FirebaseFirestore.getInstance();
    }

    public void setPatients(ArrayList<Patient> patients) {
        this.patients = new ArrayList<>();
        this.patients = patients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SwipeViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_cured_swipe, viewGroup, false);
        return new SwipeViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull SwipeViewHolder swipeViewHolder, int i) {
        viewBinderHelper.setOpenOnlyOne(true);
        viewBinderHelper.bind(swipeViewHolder.swipelayout, String.valueOf(patients.get(i).getName()));
        viewBinderHelper.closeLayout(String.valueOf(patients.get(i).getName()));
        swipeViewHolder.bindData(patients.get(i));
    }

    @Override
    public int getItemCount() {
        return patients.size();
    }

    class SwipeViewHolder extends RecyclerView.ViewHolder {

        private TextView textView, undoneLayout, deleteLayout;
        private SwipeRevealLayout swipelayout;

        SwipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            swipelayout = itemView.findViewById(R.id.swipelayout);
            undoneLayout = itemView.findViewById(R.id.undoneLayout);
            deleteLayout = itemView.findViewById(R.id.deleteLayout);
        }

        void bindData(final Patient patient) {
            undoneLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String patientId = patients.get(getAdapterPosition()).getId();
                    db.collection("users")
                            .document(user.getUid())
                            .collection("patients")
                            .document(patientId)
                            .update("curedPatient", false);

                    patients.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });

            textView.setText(patient.getName());

            deleteLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String patientId = patients.get(getAdapterPosition()).getId();
                    db.collection("users")
                            .document(user.getUid())
                            .collection("patients")
                            .document(patientId)
                            .delete();

                    patients.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });
        }
    }
}