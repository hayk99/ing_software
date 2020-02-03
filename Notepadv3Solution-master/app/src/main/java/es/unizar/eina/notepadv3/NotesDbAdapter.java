package es.unizar.eina.notepadv3;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 */
public class NotesDbAdapter {

    public static final String NOTE_KEY_TITLE = "title";
    public static final String NOTE_KEY_BODY = "body";
    public static final String NOTE_KEY_ROWID = "_id";
    public static final String NOTE_KEY_CATEGORY = "category";

    public static final String CAT_KEY_NAME = "name";
    public static final String CAT_KEY_ROWID = "_id";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statements
     */
    private static final String CATEGORIES_CREATE = "create table categories" +
            " (_id integer primary key autoincrement, name text not null);";
    private static final String NOTES_CREATE =
            "create table notes (_id integer primary key autoincrement, "
                    + "title text not null, body text not null, category integer," +
                    " foreign key (category) references categories(_id) ON DELETE SET NULL);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_NOTE_TABLE = "notes";
    private static final String DATABASE_CAT_TABLE = "categories";
    private static final int DATABASE_VERSION = 2;

    private final Context mCtx;

    /**
     * Manages the notes database creating it when needed.
     */
    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CATEGORIES_CREATE);
            db.execSQL(NOTES_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_NOTE_TABLE );
            db.execSQL("DROP TABLE IF EXISTS " + DATABASE_CAT_TABLE );
            onCreate(db);
        }
    }
    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     *
     * @param ctx the Context within which to work
     */
    public NotesDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     *
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public NotesDbAdapter open() throws SQLException {

        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        mDb.execSQL("PRAGMA foreign_keys = ON;");
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createNote(String title, String body, Long catId) {
        if(body == null || title == null || title.equals("")){
            return -1;
        }
        else {
            ContentValues initialValues = new ContentValues();
            initialValues.put(NOTE_KEY_TITLE, title);
            initialValues.put(NOTE_KEY_BODY, body);
            initialValues.put(NOTE_KEY_CATEGORY, catId);
            return mDb.insert(DATABASE_NOTE_TABLE, null, initialValues);
        }
    }

    /**
     * Delete the note with the given rowId
     *
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteNote(long rowId) {
        if(rowId < 0){
            return false;
        }
        return mDb.delete(DATABASE_NOTE_TABLE, NOTE_KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Deletes all notes stored in the database
     * @return true if the notes table is empty after it's execution.
     */
    public boolean deleteAllNotes(){
        return mDb.delete(DATABASE_NOTE_TABLE, NOTE_KEY_ROWID, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     *
     * @return Cursor over all notes
     */
    public Cursor fetchAllNotes() {

        return mDb.query(DATABASE_NOTE_TABLE, new String[] {NOTE_KEY_ROWID, NOTE_KEY_TITLE,
                NOTE_KEY_BODY, NOTE_KEY_CATEGORY}, null, null, null, null, NOTE_KEY_TITLE + " ASC");
    }
    /**
     * Return a Cursor over the list of all notes in the database ordered by category
     *
     * @return Cursor over all notes ordered by category
     */
    public Cursor fetchAllNotesByCategory() {

        return mDb.query(DATABASE_NOTE_TABLE, new String[] {NOTE_KEY_ROWID, NOTE_KEY_TITLE,
                NOTE_KEY_BODY, NOTE_KEY_CATEGORY}, null, null, null, null, NOTE_KEY_CATEGORY + " ASC");
    }

    /**
     * Return a Cursor over the list of all notes in the database that share the category
     * @param category
     *              category that all notes returned share
     * @return Cursor over all notes with category category
     */
    public Cursor fetchAllNotesMatching(Long category) {

        return mDb.query(DATABASE_NOTE_TABLE, new String[] {NOTE_KEY_ROWID, NOTE_KEY_TITLE,
                NOTE_KEY_BODY, NOTE_KEY_CATEGORY}, NOTE_KEY_CATEGORY + " = " + category, null, null, null, NOTE_KEY_TITLE + " ASC");
    }
    /**
     * Return a Cursor positioned at the note that matches the given rowId
     *
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchNote(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_NOTE_TABLE, new String[] {NOTE_KEY_ROWID, NOTE_KEY_TITLE,
                                NOTE_KEY_BODY, NOTE_KEY_CATEGORY}, NOTE_KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     *
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateNote(long rowId, String title, String body, Long catId) {
        if(body == null || title == null || title.equals("")){
            return false;
        }
        else{
            ContentValues args = new ContentValues();
            if(rowId < 1){ return false;}
            args.put(NOTE_KEY_TITLE, title);
            args.put(NOTE_KEY_BODY, body);
            args.put(NOTE_KEY_CATEGORY, catId);
            return mDb.update(DATABASE_NOTE_TABLE, args, NOTE_KEY_ROWID + "=" + rowId, null) > 0;
        }
    }
    /////Category methods//////
    /**
     * Create a new category using the name provided. If the category is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     *
     * @param name the title of the note
     * @return rowId or -1 if failed
     */
    public long createCategory(String name) {
        if(name == null || name.equals("")){
            return -1;
        }
        else {
            ContentValues initialValues = new ContentValues();
            initialValues.put(CAT_KEY_NAME, name.replaceAll("(\\s)+", "$1"));
            return mDb.insert(DATABASE_CAT_TABLE, null, initialValues);
        }
    }

    /**
     * Delete the category with the given rowId
     *
     * @param rowId id of category to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteCategory(long rowId) {
        if(rowId < 0){
            return false;
        }
        return mDb.delete(DATABASE_CAT_TABLE, CAT_KEY_ROWID + "=" + rowId, null) > 0;
    }
    /**
     * Deletes all categories stored in the database
     * @return true if the categories table is empty after it's execution.
     */
    public boolean deleteAllCategories(){
        return mDb.delete(DATABASE_CAT_TABLE, CAT_KEY_ROWID, null) > 0;
    }

    /**
     * Return a Cursor over the list of all categories in the database
     *
     * @return Cursor over all categories
     */
    public Cursor fetchAllCategories() {

        return mDb.query(DATABASE_CAT_TABLE, new String[] {CAT_KEY_ROWID, CAT_KEY_NAME}
                , null, null, null, null, CAT_KEY_NAME + " ASC");
    }

    /**
     * Return a Cursor positioned at the category that matches the given rowId
     *
     * @param rowId id of category to retrieve
     * @return Cursor positioned to matching category, if found
     * @throws SQLException if category could not be found/retrieved
     */
    public Cursor fetchCategory(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_CAT_TABLE, new String[] {CAT_KEY_ROWID,
                                CAT_KEY_NAME}, CAT_KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    /**
     * Update the category using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the name passed in
     *
     * @param rowId id of note to update
     * @param name value to set note name to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean updateCategory(long rowId, String name) {
        if(name == null ||name.equals("") ||rowId < 1){
            return false;
        }
        else{
            ContentValues args = new ContentValues();
            args.put(CAT_KEY_NAME, name.replaceAll("(\\s)+", "$1"));

            return mDb.update(DATABASE_CAT_TABLE, args, CAT_KEY_ROWID + "=" + rowId, null) > 0;
        }
    }

}