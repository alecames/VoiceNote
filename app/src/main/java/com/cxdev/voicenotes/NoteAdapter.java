package com.cxdev.voicenotes;

import android.content.Context;
import android.icu.util.Calendar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private final Context context;
    private Note[] notes;
    static NotesDBH db;
    private NoteAdapter noteAdapter;


    public NoteAdapter(Context context, Note[] notes) {
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
        noteAdapter = new NoteAdapter(context, notes);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {;
        Note note = notes[position];
        holder.noteTitle.setText(note.getTitle());
        holder.noteContent.setText(note.getNote());
        // date and time formatting
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = note.getTimestamp();
        Date date = null;
        try {
            date = dateFormat.parse(timestamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        String niceDateStr = (String) DateUtils.getRelativeTimeSpanString(date.getTime() , Calendar.getInstance().getTimeInMillis(), DateUtils.MINUTE_IN_MILLIS);
        holder.noteTimestamp.setText(String.valueOf(niceDateStr));

        holder.itemView.setOnLongClickListener(v -> {
            noteAdapter.popupDialog(holder.getAdapterPosition());
            return true;
        });
    }

    public void popupDialog(int position2) {
        // make a popup to show the title, content, and timestamp of the note
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(notes[position2].getTitle());
        builder.setMessage(notes[position2].getNote() + "\n" + notes[position2].getTimestamp());
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return notes.length;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle, noteContent, noteTimestamp;
        public NoteViewHolder(View itemView) {
            super(itemView);
            noteTitle = itemView.findViewById(R.id.noteTitle);
            noteContent = itemView.findViewById(R.id.noteContent);
            noteTimestamp = itemView.findViewById(R.id.noteTimestamp_tv);
        }
    }

    public void add(Note note) {
        Note[] newNotes = new Note[notes.length + 1];
        System.arraycopy(notes, 0, newNotes, 0, notes.length);
        newNotes[notes.length] = note;
        notes = newNotes;
    }
}
