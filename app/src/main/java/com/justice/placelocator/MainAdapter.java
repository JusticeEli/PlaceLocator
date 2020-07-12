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
import com.google.firebase.firestore.DocumentSnapshot;

import java.text.SimpleDateFormat;

public class MainAdapter extends FirestoreRecyclerAdapter<LocationData, MainAdapter.ViewHolder> {

    private Context context;

    private ItemClicked itemClicked;


    public MainAdapter(Context context, @NonNull FirestoreRecyclerOptions<LocationData> options) {
        super(options);
        this.context = context;
        itemClicked = (ItemClicked) context;

    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull LocationData model) {
        if (model.getTimeStamp() != null) {
            String time = new SimpleDateFormat("HH:mm  dd/MM/yyyy").format(model.getTimeStamp());
            String finalTime = "Date: " + time;
            holder.timeStampTxtView.setText(finalTime);

        }
        //   String time="2:34 pm";
        holder.placeTxtView.setText(model.getAddress());

    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        return viewHolder;

    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private TextView placeTxtView, timeStampTxtView;

        public ViewHolder(@NonNull View v) {
            super(v);
            placeTxtView = v.findViewById(R.id.placeTxtView);
            timeStampTxtView = v.findViewById(R.id.timeStampTxtView);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            itemClicked.itemClickedDocumentSnapshot(getSnapshots().getSnapshot(getAdapterPosition()));
        }

        @Override
        public boolean onLongClick(View v) {
            itemClicked.itemLongClicked(getSnapshots().getSnapshot(getAdapterPosition()));
            return true;
        }
    }

    public interface ItemClicked {
        void itemClickedDocumentSnapshot(DocumentSnapshot document);
        void itemLongClicked(DocumentSnapshot document);

    }
}
