package es.unizar.eina.notepadv3;


import es.unizar.eina.send.SendAbstraction;
import es.unizar.eina.send.SendAbstractionImpl;
import es.unizar.eina.Test.Tests.Tests;
import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import java.util.Arrays;


public class Notepadv3 extends AppCompatActivity {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;
    private static final int ACTIVITY_SEND=2;
    private static final int DELETE_ALL=3;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final int SEND_ID = Menu.FIRST + 3;
    private static final int TEST_ID = Menu.FIRST +4;
    private static final int DELETE_ALL_ID = Menu.FIRST +5;

    private NotesDbAdapter mDbHelper;
    private Cursor mNotesCursor;
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

    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor  notesCursor = mDbHelper.fetchAllNotes ();
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[] { NotesDbAdapter.KEY_TITLE };

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, notesCursor, from, to);
        mList.setAdapter(notes);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_insert);
        menu.add(Menu.NONE, 0, Menu.NONE, R.string.menu_delete );
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case INSERT_ID:
                createNote();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(Menu.NONE, DELETE_ID, Menu.NONE, R.string.menu_delete);

        menu.add(Menu.NONE, EDIT_ID, Menu.NONE, R.string.menu_edit);

        menu.add(Menu.NONE, SEND_ID, Menu.NONE, R.string.menu_send);

        menu.add(Menu.NONE, TEST_ID, Menu.NONE, "Execute All Tests");

        menu.add(Menu.NONE, DELETE_ALL_ID, Menu.NONE, "delete all");
    }

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
                editNote(info.position, info.id);
                return true;
            case SEND_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                sendNote(info.position, info.id);
                return true;
            case TEST_ID:
                new Tests(mDbHelper).throwAllTest();
                return true;
            case DELETE_ALL_ID:
                mDbHelper.deleteAllNotes();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }


    protected void editNote(int position, long id) {

        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_EDIT);
    }

    protected void sendNote(int position, long id) {

        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);

        startActivityForResult(i, ACTIVITY_SEND);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Bundle extras = intent.getExtras();
        switch(requestCode) {
            case ACTIVITY_CREATE:
                String title = extras.getString(NotesDbAdapter.KEY_TITLE);
                String body = extras.getString(NotesDbAdapter.KEY_BODY);
                break;
            case ACTIVITY_EDIT:
                Long rowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
                if (rowId != null) {

                    String editTitle = extras.getString(NotesDbAdapter.KEY_TITLE);
                    String editBody = extras.getString(NotesDbAdapter.KEY_BODY);
                }
                break;
            case ACTIVITY_SEND:
                Long rowId2 = extras.getLong(NotesDbAdapter.KEY_ROWID);
                if (rowId2 != null){
                    String sendTitle = extras.getString(NotesDbAdapter.KEY_TITLE);
                    String sendBody = extras.getString(NotesDbAdapter.KEY_BODY);
                    SendAbstractionImpl mensaje = null;
                    if (sendBody.length()>100){
                        mensaje = new  SendAbstractionImpl( this, "mail");
                    }
                    else {
                        mensaje = new  SendAbstractionImpl( this, "SMS");

                    }
                    mensaje.send(sendTitle, sendBody);
                }
        }
    }

}
