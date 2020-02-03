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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import es.unizar.eina.catlist.CatList;
import es.unizar.eina.credits.Credits;
import es.unizar.eina.send.SendAbstractionImpl;


public class Notepadv3 extends AppCompatActivity {

    private static final int ACTIVITY_CREATE = 0;
    private static final int ACTIVITY_EDIT = 1;
    private static final int ACTIVITY_CATEGORY = 2;

    private static final int CREDITS_ID = Menu.FIRST;
    private static final int INSERT_ID = Menu.FIRST + 1;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int EDIT_ID = Menu.FIRST + 3;
    private static final int EMAIL_ID = Menu.FIRST + 4;
    private static final int SMS_ID = Menu.FIRST + 5;

    private NotesDbAdapter mDbHelper;
    private ListView mList;

    private boolean mByCategory;
    private TextView mCategory;
    private TextView mOrder;
    private Button orderButton;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepadv3);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        mList = (ListView)findViewById(R.id.list);

        mOrder = (TextView) findViewById(R.id.notepad_order);
        mCategory = (TextView) findViewById(R.id.notepad_category);

        fillData();

        registerForContextMenu(mList);

        mByCategory = false;
        orderButton = (Button) findViewById(R.id.notepad_button);
        orderButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                mByCategory = !mByCategory;
                fillData();
            }

        });

        Button catButton = (Button) findViewById(R.id.notepad_buttonCat);
        catButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(view.getContext(), CatList.class);
                startActivityForResult(i, ACTIVITY_CATEGORY);
                mByCategory = false;
                orderButton.setClickable(false);
            }

        });

        Button resetButton = (Button) findViewById(R.id.notepad_buttonReset);
        resetButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                mCategory.setText(null);
                orderButton.setClickable(true);
                fillData();
            }

        });

    }

    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor notesCursor = mDbHelper.fetchAllNotes(mByCategory, mCategory.getText().toString());
        startManagingCursor(notesCursor);

        // Create an array to specify the fields we want to display in the list
        // (only TITLE)
        String[] from = new String[] { NotesDbAdapter.KEY_TITLE };

        // and an array of the fields we want to bind those fields to (in this
        // case just text1)
        int[] to = new int[] { R.id.text1 };

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row,
                        notesCursor, from, to);
        mList.setAdapter(notes);

        if (mByCategory) {
            mOrder.setText(R.string.category);
        }
        else {
            mOrder.setText(R.string.title);
        }
    }

    private void credits() {
        Intent i = new Intent(this, Credits.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, CREDITS_ID, Menu.NONE, R.string.credits);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_insert);
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CREDITS_ID:
                credits();
                return true;
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
        menu.add(Menu.NONE, EMAIL_ID, Menu.NONE, R.string.send_email);
        menu.add(Menu.NONE, SMS_ID, Menu.NONE, R.string.send_sms);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info =
                        (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                fillData();
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                editNote(info.id);
                return true;
            case EMAIL_ID:
                sendNote(item, EMAIL_ID);
                return true;
            case SMS_ID:
                sendNote(item, SMS_ID);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, NoteEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }


    private void editNote(long id) {
        Intent i = new Intent(this, NoteEdit.class);
        i.putExtra(NotesDbAdapter.KEY_ROWID, id);
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    private void sendNote(MenuItem item, int type) { // Practica 5
        AdapterView.AdapterContextMenuInfo info =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Long mRowId = info.id;

        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            String subject = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
            String body = note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));

            SendAbstractionImpl a;
            if (type == SMS_ID)
                a = new SendAbstractionImpl(this, "SMS");
            else
                a = new SendAbstractionImpl(this, "");

            a.send(subject, body);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == ACTIVITY_CATEGORY) {
            if (resultCode == RESULT_OK) {
                mCategory.setText(intent.getStringExtra("cat_result"));
            }
            else {
                orderButton.setClickable(true);
            }
        }
        fillData();
    }

}
