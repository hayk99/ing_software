package es.unizar.eina.notepadv3;

import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class NoteEdit extends AppCompatActivity {

    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;
    private  NotesDbAdapter  mDbHelper;

    private  void  populateFields () {
        if (mRowId  != null) {
            Cursor note = mDbHelper.fetchNote(mRowId);startManagingCursor(note);
            mTitleText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_TITLE)));
            mBodyText.setText(note.getString(note.getColumnIndexOrThrow(NotesDbAdapter.KEY_BODY)));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDbHelper = new  NotesDbAdapter(this);
        mDbHelper.open();
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);

        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mRowId = (savedInstanceState  == null) ? null :(Long) savedInstanceState.getSerializable(NotesDbAdapter.KEY_ROWID);
        if (mRowId  == null) {
            Bundle  extras = getIntent ().getExtras ();
            mRowId = (extras  != null) ? extras.getLong(NotesDbAdapter.KEY_ROWID): null;
        }

        populateFields();
        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Bundle bundle = new Bundle();

                bundle.putString(NotesDbAdapter.KEY_TITLE, mTitleText.getText().toString());
                bundle.putString(NotesDbAdapter.KEY_BODY, mBodyText.getText().toString());
                if (mRowId != null) {
                    bundle.putLong(NotesDbAdapter.KEY_ROWID, mRowId);
                }

                Intent mIntent = new Intent();
                mIntent.putExtras(bundle);
                setResult(RESULT_OK, mIntent);
                finish();
            }

        });
    }

    public  void  onClick(View  view) {
        setResult(RESULT_OK);
        finish ();
    }

    private  void  saveState () {
        String  title = mTitleText.getText ().toString ();
        String  body = mBodyText.getText ().toString ();
        if (mRowId  == null) {
            long id = mDbHelper.createNote(title , body);
            if (id > 0) {mRowId = id;
            }
        } else {
            mDbHelper.updateNote(mRowId , title , body);
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

    /*
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_note_edit, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    */
}
