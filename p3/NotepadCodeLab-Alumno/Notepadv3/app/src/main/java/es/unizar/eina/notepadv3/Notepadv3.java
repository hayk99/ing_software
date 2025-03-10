package es.unizar.eina.notepadv3;


import es.unizar.eina.Test.Tests;
import es.unizar.eina.category.Category;
import es.unizar.eina.category.CategoryDbAdapter;
import es.unizar.eina.category.CategoryEdit;
import es.unizar.eina.send.SendAbstraction;
import es.unizar.eina.send.SendAbstractionImpl;
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

    public static final int ACTIVITY_CREATE=0;
    public static final int ACTIVITY_EDIT=1;
    public static final int ACTIVITY_SEND=2;
    public static final int ACTIVITY_CREATE_CAT = 3;
    private static final int ACTIVITY_DELETE_CAT = 4;
    private static final int ACTIVITY_FILTER = 5;
    private static final int ACTIVITY_EDIT_CAT = 6;

    private static final int INSERT_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 1;
    private static final int EDIT_ID = Menu.FIRST + 2;
    private static final int SEND_ID = Menu.FIRST + 3;


    private static final int TEST_ID = Menu.FIRST +4;
    private static final int DELETE_ALL_ID = Menu.FIRST +5;
    private static final int ORDER_CAT = Menu.FIRST +6;
    private static final int ORDER_TIT = Menu.FIRST +7;
    private static final int FILTER_CAT =Menu.FIRST +8;
    private static final int CREATE_CAT = Menu.FIRST+9;
    private static final int DELETE_CAT = Menu.FIRST+10;
    private static final int EDIT_CAT = Menu.FIRST+11;

    private static String orderBy = "Title";

    private NotesDbAdapter mDbHelper;
    private CategoryDbAdapter catDbHelper;
    private Cursor mNotesCursor;
    private ListView mList;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepadv3);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        catDbHelper = new CategoryDbAdapter(this);
        catDbHelper.open();


        mList = (ListView)findViewById(R.id.list);
        fillData();

        registerForContextMenu(mList);

    }

    private void fillData() {
        // Get all of the notes from the database and create the item list
        Cursor  notesCursor = mDbHelper.fetchAllNotes (orderBy);
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

    private void fillFiltredData(String cat_name){
        Cursor filtrado = mDbHelper.fetchAllFromCategory(cat_name);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{NotesDbAdapter.KEY_TITLE};

        // and an array of the fields we want to bind those fields to (in this case just text1)
        int[] to = new int[]{R.id.text1};

        // Now create an array adapter and set it to display using our row
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, filtrado, from, to);
        mList.setAdapter(notes);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, INSERT_ID, Menu.NONE, R.string.menu_insert);
        menu.add(Menu.NONE, ORDER_TIT, Menu.NONE, R.string.menu_ordTit);
        menu.add(Menu.NONE, ORDER_CAT, Menu.NONE, R.string.menu_ordCat);
        menu.add(Menu.NONE, FILTER_CAT, Menu.NONE, R.string.menu_filter);
        menu.add(Menu.NONE, TEST_ID, Menu.NONE, R.string.menu_test);
        menu.add(Menu.NONE, DELETE_ALL_ID, Menu.NONE, R.string.menu_delete_all_notes);
        menu.add(Menu.NONE, CREATE_CAT, Menu.NONE, R.string.menu_create_cat);
        menu.add(Menu.NONE, DELETE_CAT, Menu.NONE, R.string.menu_delete_cat);
        menu.add(Menu.NONE, EDIT_CAT, Menu.NONE, R.string.menu_edit_cat );

        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case CREATE_CAT:
                createCategory();
                return true;
            case INSERT_ID:
                createNote();
                return true;
            case ORDER_CAT:
                orderBy="CATEGORY";
                mDbHelper.fetchAllNotes(orderBy);
                //startActivity(getIntent());
                fillData();
                return true;
            case ORDER_TIT:
                orderBy="TITLE";
                mDbHelper.fetchAllNotes(orderBy);
                //startActivity(getIntent());
                fillData();
                return true;
            case FILTER_CAT:
                filter();
                return true;
            case TEST_ID:
                new Tests(mDbHelper, catDbHelper).throwAllTest();
                return true;
            case DELETE_ALL_ID:
                mDbHelper.deleteAllNotes();
                catDbHelper.deleteAllCategories();
                fillData();
                return true;
            case DELETE_CAT:
                deleteCat();
                return true;
            case EDIT_CAT:
                editCat();
                fillData();
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
        }
        return super.onContextItemSelected(item);
    }


    private void deleteCat(){
        Intent i = new Intent(this, CategoryEdit.class);
        Bundle extras = new Bundle();
        extras.putString("action", "delete");
        i.putExtras(extras);
        startActivityForResult(i, ACTIVITY_DELETE_CAT);
    }

    private void editCat(){
        Intent i = new Intent (this, CategoryEdit.class);
        Bundle extras = new Bundle();
        extras.putString("action", "edit");
        i.putExtras(extras);
        startActivityForResult(i,ACTIVITY_EDIT_CAT);
    }

    private void filter(){
        Intent i = new Intent(this, CategoryEdit.class);
        Bundle extras = new Bundle();
        extras.putString("action", "filter");
        i.putExtras(extras);
        startActivityForResult(i, ACTIVITY_FILTER);
    }

    private void createCategory() {
        Log.d("notepadv3", "create cat");
        Intent i = new Intent(this, CategoryEdit.class);
        Bundle extras = new Bundle();
        extras.putString("action", "create");
        i.putExtras(extras);
        startActivityForResult(i, ACTIVITY_CREATE_CAT);
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
                String cat = extras.getString(NotesDbAdapter.KEY_CATEGORY);
                fillData();
                break;
            case ACTIVITY_CREATE_CAT:
                String catTitle = extras.getString(CategoryDbAdapter.KEY_TITLE);
                fillData();
                break;
            case ACTIVITY_EDIT_CAT:
                break;
            case ACTIVITY_FILTER:
                String op = extras.getString("op");
                String cat_tit = extras.getString("tit_cat");
                if (cat_tit.equals("") && op.equals("filter")) {
                    fillData();
                }
                else {
                    fillFiltredData(cat_tit);
                }
                break;
            case ACTIVITY_DELETE_CAT:
                op = extras.getString("op");
                //if(op.equals("delete")) {
                    Long id_cat = extras.getLong("id_cat");
                    Log.d("notepadv3", "borro cat " + id_cat);
                    catDbHelper.deleteCategory(id_cat);
                //}
                break;
            case ACTIVITY_EDIT:
                Long rowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
                if (rowId != null) {

                    String editTitle = extras.getString(NotesDbAdapter.KEY_TITLE);
                    String editBody = extras.getString(NotesDbAdapter.KEY_BODY);
                    int editCategory = extras.getInt(NotesDbAdapter.KEY_CATEGORY);
                    //mDbHelper.updateNote(rowId, editTitle, editBody, editCategory);
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
                break;
            default:
                break;
        }
    }

}
