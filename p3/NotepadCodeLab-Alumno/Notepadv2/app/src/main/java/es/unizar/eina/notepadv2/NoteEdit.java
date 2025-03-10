package es.unizar.eina.notepadv2.NoteEdit;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import es.unizar.eina.notepadv2.NotesDbAdapter;
import es.unizar.eina.notepadv2.R;

public class NoteEdit extends AppCompatActivity {
    private EditText mTitleText;
    private EditText mBodyText;
    private Long mRowId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.note_edit);
        setTitle(R.string.edit_note);
        mTitleText = (EditText) findViewById(R.id.title);
        mBodyText = (EditText) findViewById(R.id.body);
        Button  confirmButton = (Button) findViewById(R.id.confirm);
        mRowId = null;
        Bundle  extras = getIntent ().getExtras ();
        if (extras  != null) {
            String  title = extras.getString(NotesDbAdapter.KEY_TITLE);
            String  body = extras.getString(NotesDbAdapter.KEY_BODY);
            mRowId = extras.getLong(NotesDbAdapter.KEY_ROWID);
            if (title != null) {
                mTitleText.setText(title);
            }
            if (body != null) {
                mBodyText.setText(body);
            }
        }
        confirmButton.setOnClickListener(new  View.
                     OnClickListener () {
                 public  void  onClick(View view) {
                 }
             }
        );
    }

    private void onClick(){
        Bundle  bundle = new  Bundle ();
        bundle.putString(NotesDbAdapter.KEY_TITLE , mTitleText.getText ().toString ());
        bundle.putString(NotesDbAdapter.KEY_BODY , mBodyText.getText ().toString ());
        if (mRowId  != null) {
            bundle.putLong(NotesDbAdapter.KEY_ROWID , mRowId);
        }
        Intent mIntent = new  Intent ();
        mIntent.putExtras(bundle);
        setResult(RESULT_OK , mIntent);
        finish ();
    }
}

