package es.unizar.eina.notepadv3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import es.unizar.eina.send.SendAbstractionImpl;
import es.unizar.eina.tests.Test;

/**
 * Main activity for the notes app.
 * Shows notes' titles in a list ordered by title or category.
 * It can also filter by category and show only those notes that
 * share the chosen category.
 * @autor David Mañas Vidorreta (614590)
 */
public class Notepadv3 extends AppCompatActivity {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    // Long press on note menu
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final int SEND_ID = Menu.FIRST + 3;

    // Main menu
    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ALL_NOTES_ID = Menu.FIRST + 5;
    private static final int TEST_ID = Menu.FIRST + 4;
    private static final int SHOW_CAT_ID = Menu.FIRST + 6;
    private static final int ORDER_BY_TITLE = Menu.FIRST+7;
    private static final int ORDER_BY_CAT = Menu.FIRST+8;
    private static final int FILTER_BY_CAT = Menu.FIRST+9;

    private static boolean orderByTitle = true;
    private NotesDbAdapter mDbHelper;
    private ListView mList;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepadv3);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        mList = (ListView)findViewById(R.id.list);
        fillData();

        registerForContextMenu(mList);

    }
    /**
     * Fills the layout with all the notes stored in the database ordered by title or
     * by category depending on orderByTitle
     */
    private void fillData() {
        Cursor notesCursor;
        if(orderByTitle){
            notesCursor = mDbHelper.fetchAllNotes();
        }
        else{
            notesCursor = mDbHelper.fetchAllNotesByCategory();
        }
        // Get all of the notes from the database and create the item list
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[] { NotesDbAdapter.NOTE_KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        mList.setAdapter(notes);
    }

    /**
     * Fills the screen with all notes that share the category catId
     * @param catId category shared by shown notes
     */
    private void fillData(Long catId) {
        Cursor notesCursor;
        notesCursor = mDbHelper.fetchAllNotesMatching(catId);
        // Get all of the notes from the database and create the item list
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[] { NotesDbAdapter.NOTE_KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        mList.setAdapter(notes);
    }
    /**
     * Creates the options menu
     * @param menu
     *          menu in which the options will be added
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_insert);
        menu.add(Menu.NONE, SHOW_CAT_ID, Menu.NONE, R.string.menu_manage_categories);
        menu.add(Menu.NONE, ORDER_BY_CAT, Menu.NONE, R.string.menu_order_cat);
        menu.add(Menu.NONE, ORDER_BY_TITLE, Menu.NONE, R.string.menu_order_title);
        menu.add(Menu.NONE, FILTER_BY_CAT, Menu.NONE, R.string.menu_filter);
        menu.add(Menu.NONE, TEST_ID, Menu.NONE, R.string.menu_run_tests);
        menu.add(Menu.NONE, DELETE_ALL_NOTES_ID, Menu.NONE, R.string.menu_delete_all);

        return result;
    }
    /**
     * Executes the action that corresponds with the chosen menu option
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
            case SHOW_CAT_ID:
                manageCategories();
                return true;
            case TEST_ID:
                new Test(mDbHelper).runAllTests();
                return true;
            case DELETE_ALL_NOTES_ID:
                mDbHelper.deleteAllNotes();
                return true;
            case ORDER_BY_CAT:
                orderByTitle = false;
                fillData();
                return true;
            case ORDER_BY_TITLE:
                orderByTitle = true;
                fillData();
                return true;
            case FILTER_BY_CAT:
                filterByCategory();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    /**
     * Creates the context menu for a category
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_delete);
        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.menu_edit);
        menu.add(Menu.NONE, SEND_ID, Menu.NONE, R.string.menu_send);
    }
    /**
     * Executes the action that corresponds with the chosen menu option
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editNote(info.id);
                return true;
            case SEND_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                sendNote(info.id);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    /**
     * Launches CategoryEdit to create a new note
     */
    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    /**
     * Launches CategorySelector to get a category to filter
     * notes
     */
    private void filterByCategory() {
        Intent i = new Intent(this, CategorySelector.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }
    /**
     * Launches CategoryEdit to update a note
     */
    private void editNote(long id) {
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.NOTE_KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    /**
     * Starts the manageCategories activity
     */
    private void manageCategories(){
        Intent i = new Intent(this, CategoryManager.class);
        startActivityForResult(i, 0);
    }
    /**
     * Starts the phone's messaging apps. If the note's body is more than 100 characters long
     * e-mail options will appear, if it isn't the sms messenger will appear.
     */
    private void sendNote(long id) {
        Cursor note = mDbHelper.fetchNote(id);
        startManagingCursor(note);
        // Title of the note
        String title = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_TITLE));
        // Body of the note
        String body = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_BODY));
        // SMS o Email
        String method;
        // If the body has less than 100 characters, it will be sent via SMS
        if (body.length() < 100) {
            method = "SMS";
            // If it has more, it is sent by email
        } else {
            method = "EMAIL";
        }
        new SendAbstractionImpl(this, method).send(title, body);
    }

    /**
     * Receives results from activities refilling the layout in case some note
     * or the order changed
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if(intent != null){
            Long filter = intent.getLongExtra("CatId", -1);
            if(filter != null){
                fillData(filter);
            }
        }
        else{
            fillData();
        }
    }

}
