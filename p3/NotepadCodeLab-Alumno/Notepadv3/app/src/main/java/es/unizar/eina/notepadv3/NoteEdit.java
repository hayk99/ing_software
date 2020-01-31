package es.unizar.eina.notepadv3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

import es.unizar.eina.category.CategoryDbAdapter;

public class NoteEdit extends AppCompatActivity {

    private EditText mRowIdText;
    private EditText mTitleText;
    private EditText mBodyText;
   // private TextView mCatText;
    private int mCatText;
    private Long mRowId;
    private  NotesDbAdapter  mDbHelper;
    private CategoryDbAdapter catDbHelper;
    private Spinner mCategorySpin;


    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<Long> catIds = new ArrayList<>();
    private ArrayAdapter<String> spinner;

    private  void  populateFields () {
        Long catId = null;
        catDbHelper.open();
        Cursor catCursor = catDbHelper.fetchAllCategories();

        if (catCursor.moveToFirst()) {
            categories.clear();
            categories.add("");
            do {
                String name = catCursor.getString(catCursor.getColumnIndex(catDbHelper.KEY_TITLE));
                categories.add(name);
                Long id = catCursor.getLong(catCursor.getColumnIndex(catDbHelper.KEY_ROWID));
                catIds.add(id);

            }while (catCursor.moveToNext());
        }

        //spinner
        //mCategorySpin.setAdapter(null);
        spinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpin = (Spinner) findViewById(R.id.spinner_category);
        mCategorySpin.setAdapter(spinner);

        String title= "none", body="none", cat="none";
        if (mRowId  != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mRowIdText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_ROWID)));
            mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            title=note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE));
            mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
            body=note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY));

            mCatText = note.getInt(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATEGORY));
            Log.d("pop", ""+mCatText);
            mCategorySpin.setSelection(mCatText);
            //mCatText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATEGORY)));
            //cat = note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATEGORY));
        }

        Log.d("VARS POPULATES: ", "  rowId: "+ mRowId+  " title: "+title+  " body: " + body );


    }

    public  void  onClick(View  view) {
        //((Bundle)((Object) view)).clear();
        setResult(RESULT_OK);
        finish ();
    }

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new  NotesDbAdapter(this);
        mDbHelper.open();

        Log.d("Noteedit", "starting oncreate");
        catDbHelper = new CategoryDbAdapter(this);
        catDbHelper.open();

        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mRowIdText = (EditText) findViewById(R.id.idRow);
        //mCategorySpin = (Spinner) findViewById(R.id.spinner_category)
        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState  == null) ? null :(Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId  == null) {
            Bundle  extras = getIntent ().getExtras ();
            mRowId = (extras  != null) ? extras.getLong(NotesDbAdapter.KEY_ROWID): null;
        }

        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                //mCatText = mCategorySpin.getSelectedItem().toString();

                Bundle bundle = new Bundle();

                bundle.putString(NotesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
                bundle.putString(NotesDbAdapter.KEY_BODY, mBodyText.getText().toString());
                if (mRowId != null) {
                    bundle.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                }
                if (mCatText != -1){
                    bundle.putInt(NotesDbAdapter.KEY_CATEGORY, mCatText);
                }
                else{
                    bundle.putInt(NotesDbAdapter.KEY_CATEGORY, -1);
                }

                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);

                Log.d("noteedit", "fin");
                finish();
            }

        });
    }

    private  void  saveState () {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String cat = null;
        try {cat = mCategorySpin.getSelectedItem().toString();}
        catch (Exception e){
            cat = "";
        }
        //String catId = catDbHelper.idFromName(cat);

        if (mRowId  == null) {
            Long idCat = catDbHelper.idFromName(cat);
            Log.d("Debugg saveState: ", "tit: "+title+ " body: "+body+"  cat: "+idCat.toString());
            long id = mDbHelper.createNote(title , body, idCat.intValue());
            if (id > 0) {mRowId = id;
            }
        } else {
            Long id = catDbHelper.idFromName(cat);
            Log.d("saveState", cat);
            Log.d("saveState", id.toString());
            Log.d("Debugg saveState: ", "tit: "+title+ " body: "+body+"  cat: "+ id.toString());
            mDbHelper.updateNote(mRowId , title , body, id.intValue());
        }
    }

    @Override
    protected  void  onSaveInstanceState(Bundle  outState){
        super.onSaveInstanceState(outState);
        saveState ();
        outState.putSerializable(NotesDbAdapter.KEY_ROWID , mRowId);
    }

    @Override
    protected  void  onPause () {
        super.onPause ();
        saveState ();
    }

    @Override
    protected  void  onResume () {
        super.onResume ();
        populateFields ();
    }

}
