package com.justice.placelocator;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;

public class DateDialog extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (DatePickerDialog.OnDateSetListener) getActivity(), calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        if (BookAppointMentActivity.from) {
            datePickerDialog.setTitle("Start Date Of The Appointment");
        } else {
            datePickerDialog.setTitle("End Date Of The Appointment");
        }

        return datePickerDialog;
    }
}
