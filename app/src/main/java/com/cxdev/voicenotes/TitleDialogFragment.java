package com.cxdev.voicenotes;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.DialogFragment;

public class TitleDialogFragment extends DialogFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // inflate the layout for the dialog fragment
        View view = inflater.inflate(R.layout.title_dialog, container, false);

        // set the title for the dialog fragment
        getDialog().setTitle("Enter a title for the note");

        // initialize the EditText widget and set any necessary attributes
        EditText titleInput = view.findViewById(R.id.title);
        titleInput.setHint("Title");
        titleInput.setInputType(InputType.TYPE_CLASS_TEXT);

        // initialize the buttons for confirming or canceling the input
        Button confirmButton = view.findViewById(R.id.ok);
        Button cancelButton = view.findViewById(R.id.ok);

        // add listeners for the buttons
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // get the input from the EditText widget
                String title = titleInput.getText().toString();

                // validate the input and set the title for the note
                if (!title.isEmpty()) {
                    // set the title for the note
//                    setTitle(title);

                    // dismiss the dialog fragment
                    dismiss();
                } else {
                    // show an error message if the input is invalid
                    Toast.makeText(getContext(), "Please enter a valid title", Toast.LENGTH_SHORT).show();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // dismiss the dialog fragment
                dismiss();
            }
        });

        return view;
    }
}
