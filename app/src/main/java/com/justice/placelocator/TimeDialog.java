package com.justice.placelocator;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class TimeDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (TimePickerDialog.OnTimeSetListener) getActivity(), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
        if (BookAppointMentActivity.from) {
            timePickerDialog.setTitle("Start Time of the appointment");
        } else {
            timePickerDialog.setTitle("End Time of the appointment");

        }
        return timePickerDialog;
    }
}
