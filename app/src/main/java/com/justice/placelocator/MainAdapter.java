package com.justice.placelocator;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.preference.TwoStatePreference;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends FirestoreRecyclerAdapter<AppointmentData, MainAdapter.ViewHolder> {

    private Context context;

    private ItemClicked itemClicked;
    private List<LinearLayout>backgroundList=new ArrayList<>();

    public MainAdapter(Context context, @NonNull FirestoreRecyclerOptions<AppointmentData> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull AppointmentData model) {
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
            holder.approvedCheckbox.setText("appointment approved");

        } else {
            holder.approvedCheckbox.setText("appointment not approved");
        }


        ///////toggles between highlighting items in the list/////
        backgroundList.add(holder.backgroundLinearLayout);

        /**
         *  ColorStateList originalColor = holder.destinationTxtView.getTextColors();
         *         if (selectedPosition == position) {
         *             holder.itemView.setSelected(true); //using selector drawable
         *             holder.destinationTxtView.setTextColor(Color.WHITE);
         *         } else {
         *             holder.itemView.setSelected(false);
         *             holder.destinationTxtView.setTextColor(originalColor);
         *         }
         *
         *         holder.itemView.setOnClickListener(new View.OnClickListener() {
         *             @Override
         *             public void onClick(View v) {
         *                 if (selectedPosition >= 0)
         *                     notifyItemChanged(selectedPosition);
         *                 selectedPosition = holder.getAdapterPosition();
         *                 notifyItemChanged(selectedPosition);
         *
         *             }
         *         });
         */

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.appointment_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        public CheckBox approvedCheckbox;
        private TextView emailTxtView, destinationTxtView, fromTxtView, toTxtView;
        private LinearLayout backgroundLinearLayout;

        public ViewHolder(@NonNull View v) {
            super(v);
            backgroundLinearLayout = v.findViewById(R.id.backgroundLinearLayout);
            emailTxtView = v.findViewById(R.id.emailTxtView);
            destinationTxtView = v.findViewById(R.id.destinationTxtView);
            fromTxtView = v.findViewById(R.id.fromTxtView);
            toTxtView = v.findViewById(R.id.toTxtView);
            approvedCheckbox = v.findViewById(R.id.approvedCheckbox);
            approvedCheckbox.setEnabled(false);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);



        }

        @Override
        public void onClick(View v) {
            DocumentSnapshot documentSnapshot = getSnapshots().getSnapshot(getAdapterPosition());
            AllData.currentLocation = documentSnapshot;
            backgroundLinearLayout.setBackground(context.getDrawable(R.drawable.button_bg));
            itemClicked.checkIfUserHasCheckedIn(documentSnapshot);
            itemClicked.itemClickedDocumentSnapshot(getSnapshots().getSnapshot(getAdapterPosition()));


            highLightOneItemInTheList();

        }

        private void highLightOneItemInTheList() {
            for (LinearLayout background:backgroundList){
                background.setBackground(context.getDrawable(R.drawable.main_bg));
            }
            backgroundLinearLayout.setBackground(context.getDrawable(R.drawable.button_bg));
            notifyDataSetChanged();
        }


        @Override
        public boolean onLongClick(View v) {
            itemClicked.itemLongClicked(getSnapshots().getSnapshot(getAdapterPosition()));
            return true;
        }
    }

    public interface ItemClicked {
        void itemClickedDocumentSnapshot(DocumentSnapshot document);

        void checkIfUserHasCheckedIn(DocumentSnapshot result);

        void itemLongClicked(DocumentSnapshot document);

    }
}
