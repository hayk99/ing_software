package es.unizar.eina.notepadv3;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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
    private TextView mCatText;
    private Long mRowId;
    private  NotesDbAdapter  mDbHelper;
    private CategoryDbAdapter catDbHelper;
    private Spinner mCategorySpin;


    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<Long> catIds = new ArrayList<>();


    private  void  populateFields () {
        Long catId = null;
        if (mRowId  != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mRowIdText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_ROWID)));
            mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));

            mCatText.setText("Category: NOT DEFINED");
            catId = note.getLong(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_CATEGORY));
        }
        catDbHelper.open();
        Cursor catCursor = catDbHelper.fetchAllCategories();

        //if (catCursor.moveToFirst()) {
            while (catCursor.moveToNext()) {
                String name = catCursor.getString(catCursor.getColumnIndex(catDbHelper.KEY_TITLE));
                categories.add(name);
                Long id = catCursor.getLong(catCursor.getColumnIndex(catDbHelper.KEY_ROWID));
                catIds.add(id);

            }
        //}

        //spiner
        ArrayAdapter<String> spinner = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        spinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCategorySpin = (Spinner) findViewById(R.id.spinner_category);
        mCategorySpin.setAdapter(spinner);


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
        mCatText = (TextView) findViewById(R.id.cat_txt);
        Button confirmButton = (Button) findViewById(R.id.confirm);

        Log.d("Noteedit", "llego hasta button");

        mRowId = (savedInstanceState  == null) ? null :(Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId  == null) {
            Bundle  extras = getIntent ().getExtras ();
            mRowId = (extras  != null) ? extras.getLong(NotesDbAdapter.KEY_ROWID): null;
        }
        Log.d("noteedit", "voy al populates: "+mRowId);

        //populateFields();
        Log.d("noteedit", "vuelvo populates");
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();

                bundle.putString(NotesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
                bundle.putString(NotesDbAdapter.KEY_BODY, mBodyText.getText().toString());
                if (mRowId != null) {
                    bundle.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                }
                if (mCatText != null){
                    bundle.putString(NotesDbAdapter.KEY_CATEGORY, mCatText.getText().toString());
                }
                else{
                    bundle.putString(NotesDbAdapter.KEY_CATEGORY, "Category: none");
                }
                Log.d("noteedit", "antes intent");

                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);

                Log.d("noteedit", "fin");
                finish();
            }

        });
    }

    private  void  saveState () {
        String title ="", body="", cat="";
        if (mTitleText != null) {
            title = mTitleText.getText().toString();
        }
        else if (mBodyText != null) {
            body = mBodyText.getText().toString();
        }
        else if (mCategorySpin != null){

            cat = mCategorySpin.getSelectedItem().toString();
        }
        Long catId = null;

        catId = catDbHelper.idFromName(cat);
        if (mRowId  == null) {
            long id = mDbHelper.createNote(title , body, catId);
            if (id > 0) {mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId , title , body, catId);
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
