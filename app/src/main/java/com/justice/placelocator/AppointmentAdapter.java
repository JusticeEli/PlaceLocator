package com.justice.placelocator;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import es.dmoral.toasty.Toasty;

public class AppointmentAdapter extends FirestoreRecyclerAdapter<Appointment, AppointmentAdapter.ViewHolder> {

    private Context context;

    private ItemClicked itemClicked;


    public AppointmentAdapter(Context context, @NonNull FirestoreRecyclerOptions<Appointment> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, int position, @NonNull final Appointment model) {
        holder.emailTxtView.setText(model.getEmail());

        ///////getting number of appointments for each user//////////
        getSnapshots().getSnapshot(position).getReference().collection(MainActivity.PERSONAL_APPOINTMENTS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    holder.numberOfAppointmentsTextView.setText(task.getResult().size() + "");
                } else {
                    Toasty.error(context, "Error: " + task.getException().getMessage()).show();
                }
            }
        });


    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_main_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private TextView emailTxtView, numberOfAppointmentsTextView;

        public ViewHolder(@NonNull View v) {
            super(v);
            emailTxtView = v.findViewById(R.id.emailTxtView);
            numberOfAppointmentsTextView = v.findViewById(R.id.numberOfAppointmentTxtView);

            v.setOnClickListener(this);
            //        v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClicked.itemClickedDocumentSnapshot(getSnapshots().getSnapshot(getAdapterPosition()));
        }

    }

    public interface ItemClicked {
        void itemClickedDocumentSnapshot(DocumentSnapshot document);



    }
}
