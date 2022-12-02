package com.cxdev.voicenotes;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

public class NotesDBH extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "notes_database.db";
    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_NOTE = "note";
    public static final String COLUMN_TS = "timestamp";
    public static final String COLUMN_AUDIO = "audio";

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_TITLE + " TEXT," + COLUMN_NOTE + " TEXT," + COLUMN_TS + " TEXT," + COLUMN_AUDIO + " TEXT" + ")";
        sqLiteDatabase.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(sqLiteDatabase);
    }

    public NotesDBH(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // add note
    public void addNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_NOTE, note.getNote());
        values.put(COLUMN_TS, note.getTimestamp());
        values.put(COLUMN_AUDIO, note.getAudio());
        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_NOTES, null, values);
        db.close();
    }

    // get note
    public Note getNote(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTES, new String[]{COLUMN_ID, COLUMN_TITLE, COLUMN_NOTE, COLUMN_TS, COLUMN_AUDIO}, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        assert cursor != null;
        Note note = new Note(Integer.parseInt(cursor.getString(0)), cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4));
        return note;
    }

    // get all notes
    public Cursor getAllNotes() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTES, null);
        return cursor;
    }

    // update note
    public int updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, note.getTitle());
        values.put(COLUMN_NOTE, note.getNote());
        values.put(COLUMN_TS, note.getTimestamp());
        values.put(COLUMN_AUDIO, note.getAudio());
        return db.update(TABLE_NOTES, values, COLUMN_ID + "=?", new String[]{String.valueOf(note.getID())});
    }

    // delete note
    public void deleteNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTES, COLUMN_ID + "=?", new String[]{String.valueOf(note.getID())});
        db.close();
    }

    // get notes count
    public int getNotesCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTES, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    // delete all notes
    public void deleteAllNotes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_NOTES);
        db.close();
    }

    // filter notes
    public Cursor filterNotes(String query) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NOTES + " WHERE " + COLUMN_TITLE + " LIKE '%" + query + "%' OR " + COLUMN_NOTE + " LIKE '%" + query + "%'", null);
        return cursor;
    }

}
