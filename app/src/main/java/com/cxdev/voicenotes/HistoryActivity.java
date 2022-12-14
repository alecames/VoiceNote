package com.cxdev.voicenotes;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

public class HistoryActivity extends AppCompatActivity {

    private Context context;
    RecyclerView noteHistory;
    NoteAdapter noteAdapter;
    NotesDBH db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new NotesDBH(this);
        setContentView(R.layout.activity_history);
        noteAdapter = new NoteAdapter (this, new Note[0]);
        noteHistory = findViewById(R.id.historyList);
        noteHistory.setAdapter(noteAdapter);
        noteHistory.setLayoutManager(new LinearLayoutManager(this));
        updateDB(db);

        // listener for title (back button in this case)
        findViewById(R.id.history_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public Context getContext() {
        return context;
    }

    private void updateDB(NotesDBH db) {
        Cursor cursor = db.getAllNotes();
        if (cursor.getCount() == 0) {
            Toast.makeText(this, "No notes?", Toast.LENGTH_SHORT).show();
        } else {
            while (cursor.moveToNext()) {
                Note note = new Note(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        cursor.getString(3)
                );
                noteAdapter.add(note);
            }
            noteAdapter.notifyDataSetChanged();
        }
    }
}