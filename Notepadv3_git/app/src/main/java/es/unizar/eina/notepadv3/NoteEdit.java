package es.unizar.eina.notepadv3;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
/**
 * Activity that modifies and/or creates notes
 *
 * @autor David Mañas Vidorreta (614590)
 */
public class NoteEdit extends AppCompatActivity {

    private EditText mIDText;
    private EditText mTitleText;
    private EditText mBodyText;
    private TextView mCatText;
    private Spinner mCatSpin;
    private Long mRowId;
    private ArrayList<String> categories = new ArrayList<>();
    private ArrayList<Long> categoryIds = new ArrayList<>();

    private NotesDbAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new NotesDbAdapter(this);
        mDbHelper.open();

        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mIDText = (EditText) findViewById(R.id.iden);
        mIDText.setText("***");
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        mCatText =  (TextView) findViewById(R.id.categoryText);
        Button confirmButton = (Button) findViewById(R.id.confirm);
        mRowId = (savedInstanceState == null) ? null :
                (Long) savedInstanceState.getSerializable(NotesDbAdapter.NOTE_KEY_ROWID);
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = (extras != null) ? extras.getLong(NotesDbAdapter.NOTE_KEY_ROWID)
                    : null;
        }

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                setResult(RESULT_OK);
                finish();
            }

        });
    }

    private void populateFields() {
        Long chosenCategory = null;
        if (mRowId != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);
            startManagingCursor(note);
            mIDText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_ROWID)));
            mTitleText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_TITLE)));
            mBodyText.setText(note.getString(
                    note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_BODY)));
            mCatText.setText("Category: None");
            chosenCategory = note.getLong(
                    note.getColumnIndexOrThrow(NotesDbAdapter.NOTE_KEY_CATEGORY));
        }
        categories.add(""); // Ninguna categoria
        categoryIds.add(null);
        // Read categories
        Cursor cursorCat = mDbHelper.fetchAllCategories();

        if (cursorCat.moveToFirst()){
            do{
                String catName = cursorCat.getString(cursorCat.getColumnIndex(NotesDbAdapter.CAT_KEY_NAME));
                categories. add(catName);
                Long id = cursorCat.getLong(cursorCat.getColumnIndex(NotesDbAdapter.CAT_KEY_ROWID));
                categoryIds.add(id);
                if(chosenCategory != null && id != null && id.equals(chosenCategory)){
                    mCatText.setText("Category: " + catName);
                }
            }while(cursorCat.moveToNext());
        }

        // Set the spinner
        ArrayAdapter<String> spCats = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        spCats.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mCatSpin = (Spinner) findViewById(R.id.spinner);
        mCatSpin.setAdapter(spCats);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(NotesDbAdapter.NOTE_KEY_ROWID, mRowId);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        populateFields();
    }

    private void saveState() {
        String title = mTitleText.getText().toString();
        String body = mBodyText.getText().toString();
        String selectedCat = mCatSpin.getSelectedItem().toString();
        Long catId = null;
        if(!selectedCat.equals("")){
            catId = categoryIds.get(categories.indexOf(selectedCat));
        }
        if (mRowId == null) {
            long id = mDbHelper.createNote(title, body, catId);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId, title, body, catId);
        }
    }

}
