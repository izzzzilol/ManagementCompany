package com.example.managementcompany;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Отдельное окно добавления/изменения записи.
 * Аналог форм «Окно добавления дома», «Окно изменения данных о доме» и т.д.
 */
public class RecordActivity extends Activity {

    public static final String EXTRA_TABLE = TableActivity.EXTRA_TABLE;
    public static final String EXTRA_RECORD_ID = "record_id";

    private TableDefinition def;
    private AppRepository db;
    private Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        def = TableDefinition.byKey(getIntent().getStringExtra(EXTRA_TABLE));
        if (def == null) {
            finish();
            return;
        }

        long id = getIntent().getLongExtra(EXTRA_RECORD_ID, -1);
        db = new AppRepository(this);
        record = id > 0 ? db.getRecordById(def, id) : null;

        setTitle(record == null ? R.string.title_add : R.string.title_edit);
        setContentView(R.layout.activity_record);
        Ui.header(this, getString(record == null ? R.string.title_add : R.string.title_edit),
                def.getDisplayName(), true);

        ViewGroup container = findViewById(R.id.fields_container);
        final List<FormFields.InputField> fields =
                FormFields.build(this, container, def, record, db);

        findViewById(R.id.save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, String> values = new LinkedHashMap<>();
                for (FormFields.InputField field : fields) {
                    String value = field.getValue();
                    if (value == null || value.trim().isEmpty()) {
                        Toast.makeText(RecordActivity.this, R.string.fill_required, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    values.put(field.column.name, value.trim());
                }
                try {
                    if (record == null) {
                        db.insert(def, values);
                    } else {
                        db.update(def, record.getId(), values);
                    }
                    Toast.makeText(RecordActivity.this, R.string.saved, Toast.LENGTH_SHORT).show();
                    finish();
                } catch (IllegalArgumentException e) {
                    Toast.makeText(RecordActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button delete = findViewById(R.id.delete);
        if (record == null) {
            delete.setVisibility(View.GONE);
        } else {
            delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(RecordActivity.this)
                            .setMessage(R.string.confirm_delete)
                            .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    db.delete(def, record.getId());
                                    Toast.makeText(RecordActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
                                    finish();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                }
            });
        }

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }
}
