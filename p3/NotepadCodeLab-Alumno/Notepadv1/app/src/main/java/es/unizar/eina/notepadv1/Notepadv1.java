package es.unizar.eina.notepadv1;

import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class Notepadv1 extends AppCompatActivity {

    private int mNoteNumber = 1;
    private ListView mList;
    private NotesDbAdapter mDbHelper;
    public  static  final  int  INSERT_ID = Menu.FIRST;

    /** Called when the activity is first created. */


    private  void  createNote () {
        String  noteName = "Note " + mNoteNumber ++;
        mDbHelper.createNote(noteName , "Body  text");
        fillData ();
    }


    private  void  fillData () {
        // Get  all of the  notes  from  the  database  andcreate  the  item  list
        Cursor c = mDbHelper.fetchAllNotes ();
        startManagingCursor(c);
        String [] from = new  String [] { NotesDbAdapter.KEY_TITLE  };
        int[] to = new int[] { R.id.text1 };
        // Now  create  an  array  adapter  and  set it todisplay  using  our  row
        SimpleCursorAdapter notes =
                new  SimpleCursorAdapter(this , R.layout.notes_row , c, from , to);
        mList.setAdapter(notes);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notepadv1);
        this.mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();
        mList = (ListView)findViewById(R.id.list);
        fillData();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean  result = super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE , INSERT_ID , Menu.NONE , R.string.menu_insert);
        return  result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId ()) {
            case  INSERT_ID:createNote ();
            return  true;
        }
        return  super.onOptionsItemSelected(item);
    }
}

