package com.cxdev.voicenotes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {
    private Context context;
    private Note[] notes;

    public NoteAdapter(Context context, Note[] notes) {
        this.context = context;
        this.notes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.note_item, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder holder, int position) {
        Note note = notes[position];
        holder.noteTitle.setText(note.getTitle());
        holder.noteContent.setText(note.getNote());
        holder.noteTimestamp.setText(note.getTimestamp());
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

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
    }


    public void add(Note note) {
        Note[] newNotes = new Note[notes.length + 1];
        System.arraycopy(notes, 0, newNotes, 0, notes.length);
        newNotes[notes.length] = note;
        notes = newNotes;
    }
}
