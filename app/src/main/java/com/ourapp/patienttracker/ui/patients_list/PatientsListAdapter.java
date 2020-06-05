package com.ourapp.patienttracker.ui.patients_list;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.ourapp.patienttracker.R;
import com.ourapp.patienttracker.ui.patient_edit.PatientEditFragment;

import java.util.ArrayList;

public class PatientsListAdapter extends RecyclerView.Adapter<PatientsListAdapter.SwipeViewHolder> {

    private Context context;
    private CallBack callBack;
    private ArrayList<Patient> patients;
    FirebaseFirestore db;
    FirebaseUser user;
    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    public PatientsListAdapter(Context context, CallBack callBack, ArrayList<Patient> patients) {
        this.context = context;
        this.patients = patients;
        this.callBack = callBack;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_patient_swipe, viewGroup, false);
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

    public interface CallBack{
        void startEditFragment(String id);
    }

    class SwipeViewHolder extends RecyclerView.ViewHolder {

        private TextView textView, doneLayout, editLayout;
        private SwipeRevealLayout swipelayout;

        SwipeViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textView);
            swipelayout = itemView.findViewById(R.id.swipelayout);
            doneLayout = itemView.findViewById(R.id.doneLayout);
            editLayout = itemView.findViewById(R.id.editLayout);
        }

        void bindData(final Patient patient) {
            doneLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String patientId = patients.get(getAdapterPosition()).getId();
                    db.collection("users")
                            .document(user.getUid())
                            .collection("patients")
                            .document(patientId)
                            .update("curedPatient", true);

                    patients.remove(getAdapterPosition());
                    notifyItemRemoved(getAdapterPosition());
                }
            });

            textView.setText(patient.getName());

            editLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Edit fragment
                    callBack.startEditFragment(patients.get(getAdapterPosition()).getId());
                    //Toast.makeText(context, patients.get(getAdapterPosition()).getId(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}