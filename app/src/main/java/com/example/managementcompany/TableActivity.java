package com.example.managementcompany;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Универсальный экран таблицы: DataGridView, панель команд (ToolStrip),
 * поиск, навигация по записям и переходы в окна добавления/изменения.
 */
public class TableActivity extends Activity {

    public static final String EXTRA_TABLE = "table_key";

    private TableDefinition def;
    private AppRepository db;
    private RecordAdapter adapter;
    private EditText search;
    private EditText recordNumber;
    private TextView count;
    private TextView status;
    private ListView list;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        def = TableDefinition.byKey(getIntent().getStringExtra(EXTRA_TABLE));
        if (def == null) {
            finish();
            return;
        }

        setTitle(def.getDisplayName());
        setContentView(R.layout.activity_table);
        Ui.header(this, def.getDisplayName(), getString(R.string.app_name), true);
        db = new AppRepository(this);

        search = findViewById(R.id.search);
        recordNumber = findViewById(R.id.record_number);
        count = findViewById(R.id.count);
        status = findViewById(R.id.status);
        list = findViewById(R.id.list);

        adapter = new RecordAdapter();
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openDetail(position);
            }
        });
        list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                select(position);
                Toast.makeText(TableActivity.this, "Запись выбрана", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                selectedPosition = -1;
                recordNumber.setText("");
                list.clearChoices();
                load();
            }
            @Override public void afterTextChanged(Editable s) { }
        });

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openRecord(-1); }
        });
        findViewById(R.id.edit_record).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (selectedPosition >= 0) openRecord(adapter.getItem(selectedPosition).getId());
                else Toast.makeText(TableActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.open).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (selectedPosition >= 0) openDetail(selectedPosition);
                else Toast.makeText(TableActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.delete_selected).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { confirmDelete(); }
        });
        findViewById(R.id.nav_first).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { if (adapter.getCount() > 0) select(0); }
        });
        findViewById(R.id.nav_prev).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int n = adapter.getCount();
                if (n == 0) return;
                select(selectedPosition <= 0 ? n - 1 : selectedPosition - 1);
            }
        });
        findViewById(R.id.nav_next).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int n = adapter.getCount();
                if (n == 0) return;
                select(selectedPosition < 0 ? 0 : (selectedPosition + 1) % n);
            }
        });
        findViewById(R.id.nav_last).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int n = adapter.getCount();
                if (n > 0) select(n - 1);
            }
        });
        findViewById(R.id.find).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { findByNumber(); }
        });

        Button ownerCard = findViewById(R.id.owner_card);
        if ("cobstveniki".equals(def.getKey())) {
            ownerCard.setVisibility(View.VISIBLE);
            ownerCard.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (selectedPosition < 0) {
                        Toast.makeText(TableActivity.this, R.string.no_selection, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Intent intent = new Intent(TableActivity.this, OwnerCardActivity.class);
                    intent.putExtra(OwnerCardActivity.EXTRA_OWNER_ID,
                            adapter.getItem(selectedPosition).getId());
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        load();
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }

    private void load() {
        List<Record> items = db.getAll(def, search.getText().toString());
        adapter.setItems(items);
        count.setText(getString(R.string.records_count, items.size()));
        status.setText(items.isEmpty() ? R.string.status_empty : R.string.status_loaded);

        if (selectedPosition >= items.size()) {
            selectedPosition = -1;
            recordNumber.setText("");
            list.clearChoices();
        }
        if (selectedPosition >= 0) {
            list.setItemChecked(selectedPosition, true);
            list.setSelection(selectedPosition);
        }
        updateButtons();
    }

    /** Кнопки доступны только тогда, когда есть данные и/или выбранная запись. */
    private void updateButtons() {
        boolean hasItems = adapter.getCount() > 0;
        boolean hasSelection = selectedPosition >= 0 && selectedPosition < adapter.getCount();
        boolean filtered = !search.getText().toString().trim().isEmpty();

        findViewById(R.id.add).setEnabled(hasItems || !filtered);
        findViewById(R.id.nav_first).setEnabled(hasItems);
        findViewById(R.id.nav_prev).setEnabled(hasItems);
        findViewById(R.id.nav_next).setEnabled(hasItems);
        findViewById(R.id.nav_last).setEnabled(hasItems);
        findViewById(R.id.find).setEnabled(hasItems);
        findViewById(R.id.open).setEnabled(hasSelection);
        findViewById(R.id.edit_record).setEnabled(hasSelection);
        findViewById(R.id.delete_selected).setEnabled(hasSelection);
        findViewById(R.id.owner_card).setEnabled(hasSelection);
    }

    private void select(int position) {
        if (position < 0 || position >= adapter.getCount()) return;
        selectedPosition = position;
        list.setItemChecked(position, true);
        list.setSelection(position);
        recordNumber.setText(String.valueOf(adapter.getItem(position).getId()));
        updateButtons();
    }

    /** Аналог поиска записи по значению в TextBox Number_Emloyee. */
    private void findByNumber() {
        String raw = recordNumber.getText().toString().trim();
        long id;
        try {
            id = Long.parseLong(raw);
        } catch (NumberFormatException e) {
            status.setText(R.string.status_empty);
            return;
        }
        for (int i = 0; i < adapter.getCount(); i++) {
            if (adapter.getItem(i).getId() == id) {
                select(i);
                status.setText(R.string.status_loaded);
                return;
            }
        }
        selectedPosition = -1;
        list.clearChoices();
        status.setText(R.string.status_empty);
        updateButtons();
    }

    private void openDetail(int position) {
        if (position < 0 || position >= adapter.getCount()) return;
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TABLE, def.getKey());
        intent.putExtra(DetailActivity.EXTRA_RECORD_ID, adapter.getItem(position).getId());
        startActivity(intent);
    }

    private void openRecord(long id) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra(RecordActivity.EXTRA_TABLE, def.getKey());
        intent.putExtra(RecordActivity.EXTRA_RECORD_ID, id);
        startActivity(intent);
    }

    private void confirmDelete() {
        if (selectedPosition < 0) {
            Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show();
            return;
        }
        final long id = adapter.getItem(selectedPosition).getId();
        new AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override public void onClick(DialogInterface dialog, int which) {
                        db.delete(def, id);
                        Toast.makeText(TableActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
                        selectedPosition = -1;
                        recordNumber.setText("");
                        list.clearChoices();
                        load();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private class RecordAdapter extends BaseAdapter {
        private final List<Record> items = new ArrayList<>();

        void setItems(List<Record> newItems) {
            items.clear();
            items.addAll(newItems);
            notifyDataSetChanged();
        }

        @Override public int getCount() { return items.size(); }
        @Override public Record getItem(int position) { return items.get(position); }
        @Override public long getItemId(int position) { return items.get(position).getId(); }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(TableActivity.this).inflate(R.layout.list_item, parent, false);
            }
            Record record = getItem(position);
            ((TextView) view.findViewById(R.id.item_icon)).setText(Ui.iconFor(def));
            ((TextView) view.findViewById(R.id.item_title)).setText(record.summary(def));
            ((TextView) view.findViewById(R.id.item_subtitle)).setText(record.details(def));
            return view;
        }
    }
}
