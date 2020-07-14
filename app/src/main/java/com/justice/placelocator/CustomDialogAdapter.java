package com.justice.placelocator;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;

public class CustomDialogAdapter extends FirestoreRecyclerAdapter<AppointmentData, CustomDialogAdapter.ViewHolder> {

    private Context context;

    private ItemClicked itemClicked;


    public CustomDialogAdapter(Context context, @NonNull FirestoreRecyclerOptions<AppointmentData> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull AppointmentData model) {
        if (model.getExpectedFromTime() != null) {
            String fromTime = new SimpleDateFormat("HH:mm  dd/MM/yyyy").format(model.getExpectedFromTime());
            holder.fromTxtView.setText(fromTime);

        }
        if (model.getExpectToTime() != null) {
            String toTime = new SimpleDateFormat("HH:mm  dd/MM/yyyy").format(model.getExpectToTime());
            holder.toTxtView.setText(toTime);

        }
        //   String time="2:34 pm";
        holder.emailTxtView.setText(model.getEmail());
        holder.destinationTxtView.setText(model.getDestinationLocation());
        holder.approvedCheckbox.setChecked(model.isApproved());
        if (model.isApproved()) {
            holder.approvedCheckbox.setText("approved");

        } else {
            holder.approvedCheckbox.setText("not approved");

        }
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dialog_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, View.OnLongClickListener {
        private TextView emailTxtView, destinationTxtView, fromTxtView, toTxtView;
        private CheckBox approvedCheckbox;

        public ViewHolder(@NonNull View v) {
            super(v);
            emailTxtView = v.findViewById(R.id.emailTxtView);
            destinationTxtView = v.findViewById(R.id.destinationTxtView);
            fromTxtView = v.findViewById(R.id.fromTxtView);
            toTxtView = v.findViewById(R.id.toTxtView);
            approvedCheckbox = v.findViewById(R.id.approvedCheckbox);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
            approvedCheckbox.setOnCheckedChangeListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClicked.itemOnDialogClicked(getSnapshots().getSnapshot(getAdapterPosition()));
        }


        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            itemClicked.approveAppointment(getSnapshots().getSnapshot(getAdapterPosition()), isChecked);
            if (isChecked) {
                approvedCheckbox.setText("approved");

            } else {
                approvedCheckbox.setText("not approved");

            }
        }

        @Override
        public boolean onLongClick(View v) {
            itemClicked.itemOnDialogLongClicked(getSnapshots().getSnapshot(getAdapterPosition()));
            return true;
        }
    }

    public interface ItemClicked {
        void itemOnDialogClicked(DocumentSnapshot document);

        void itemOnDialogLongClicked(DocumentSnapshot document);

        void approveAppointment(DocumentSnapshot document, boolean approved);

    }
}
