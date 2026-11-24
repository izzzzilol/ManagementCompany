package com.example.managementcompany;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Подробный просмотр записи: структурированные поля, связи и рабочие статусы. */
public class DetailActivity extends Activity {

    public static final String EXTRA_TABLE = TableActivity.EXTRA_TABLE;
    public static final String EXTRA_RECORD_ID = "record_id";

    private TableDefinition def;
    private AppRepository db;
    private long recordId;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        def = TableDefinition.byKey(getIntent().getStringExtra(EXTRA_TABLE));
        recordId = getIntent().getLongExtra(EXTRA_RECORD_ID, -1);
        if (def == null || recordId <= 0) {
            finish();
            return;
        }

        db = new AppRepository(this);
        setTitle(R.string.title_detail);
        setContentView(R.layout.activity_detail);
        Ui.header(this, def.getDisplayName(), getString(R.string.title_detail), true);
        container = findViewById(R.id.detail_container);

        findViewById(R.id.detail_edit).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(DetailActivity.this, RecordActivity.class);
                intent.putExtra(RecordActivity.EXTRA_TABLE, def.getKey());
                intent.putExtra(RecordActivity.EXTRA_RECORD_ID, recordId);
                startActivity(intent);
            }
        });

        findViewById(R.id.detail_delete).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                new AlertDialog.Builder(DetailActivity.this)
                        .setMessage(R.string.confirm_delete)
                        .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                            @Override public void onClick(DialogInterface dialog, int which) {
                                db.delete(def, recordId);
                                Toast.makeText(DetailActivity.this, R.string.deleted, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        Record record = db.getRecordById(def, recordId);
        if (record == null) {
            finish();
            return;
        }
        container.removeAllViews();

        container.addView(Ui.recordCard(this, db, def, record, -1));

        List<Related> related = relatedFor(record);
        if (!related.isEmpty()) {
            LinearLayout block = Ui.card(this);
            block.addView(Ui.text(this, getString(R.string.section_related), R.style.SectionTitle, 12));
            for (Related item : related) {
                TextView heading = Ui.text(this, item.title, R.style.FieldLabel, 6);
                heading.setTextColor(getResources().getColor(R.color.primary));
                block.addView(heading);

                if (item.records.isEmpty()) {
                    block.addView(Ui.text(this, getString(R.string.empty_related),
                            R.style.FieldValue, 12));
                } else {
                    for (final Record linked : item.records) {
                        final TableDefinition linkedDef = item.def;
                        LinearLayout card = Ui.recordCard(this, db, linkedDef, linked, 3);
                        card.setClickable(true);
                        card.setFocusable(true);
                        card.setBackgroundResource(R.drawable.list_selector);
                        card.setOnClickListener(new View.OnClickListener() {
                            @Override public void onClick(View v) {
                                Intent intent = new Intent(DetailActivity.this, DetailActivity.class);
                                intent.putExtra(EXTRA_TABLE, linkedDef.getKey());
                                intent.putExtra(EXTRA_RECORD_ID, linked.getId());
                                startActivity(intent);
                            }
                        });
                        block.addView(card);
                    }
                }
            }
            container.addView(block);
        }

        addStatusActions(record);
    }

    private void addStatusActions(final Record record) {
        if (!"zaivki".equals(def.getKey()) && !"nachislenie".equals(def.getKey())) return;

        boolean isRequest = "zaivki".equals(def.getKey());
        LinearLayout card = Ui.card(this);
        card.addView(Ui.text(this, getString(isRequest ? R.string.section_status
                : R.string.section_payment), R.style.SectionTitle, 10));

        String current = isRequest ? record.get("Status") : record.get("PaymentStatus");
        String[] values = isRequest
                ? new String[]{getString(R.string.status_new), getString(R.string.status_in_progress),
                    getString(R.string.status_done)}
                : new String[]{getString(R.string.status_paid), getString(R.string.status_not_paid)};

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (final String value : values) {
            Button button = new Button(this);
            button.setText(value);
            button.setTextSize(12);
            boolean active = value.equals(current);
            button.setBackgroundResource(active ? R.drawable.btn_primary : R.drawable.btn_secondary);
            button.setTextColor(active ? android.graphics.Color.WHITE
                    : getResources().getColor(R.color.primary));
            button.setPadding(Ui.dp(12), Ui.dp(8), Ui.dp(12), Ui.dp(8));
            button.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    setStatus(record, isRequest ? "Status" : "PaymentStatus", value);
                }
            });
            LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            buttonParams.setMargins(Ui.dp(3), 0, Ui.dp(3), 0);
            row.addView(button, buttonParams);
        }
        card.addView(row);
        container.addView(card);
    }

    private void setStatus(Record record, String column, String value) {
        Map<String, String> values = new LinkedHashMap<>();
        for (TableDefinition.Column col : def.getColumns()) {
            values.put(col.name, record.get(col.name));
        }
        values.put(column, value);
        db.update(def, recordId, values);
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        render();
    }

    private static class Related {
        final String title;
        final TableDefinition def;
        final List<Record> records;

        Related(String title, TableDefinition def, List<Record> records) {
            this.title = title;
            this.def = def;
            this.records = records;
        }
    }

    private List<Related> relatedFor(Record record) {
        List<Related> result = new ArrayList<>();
        String id = String.valueOf(record.getId());

        switch (def.getKey()) {
            case "houses":
                result.add(related(getString(R.string.dashboard_apartments), "kvartira", "HouseId=?", id, "ApartmentNumber"));
                result.add(related(getString(R.string.dashboard_expenses), "rashod", "HouseId=?", id, "Date DESC"));
                break;
            case "kvartira":
                result.add(related(getString(R.string.dashboard_charges), "nachislenie", "ApartmentId=?", id, "Period DESC"));
                result.add(single(getString(R.string.dashboard_owners), "cobstveniki", record.get("OwnerId")));
                result.add(single(getString(R.string.dashboard_houses), "houses", record.get("HouseId")));
                break;
            case "cobstveniki":
                result.add(related(getString(R.string.dashboard_apartments), "kvartira", "OwnerId=?", id, "ApartmentNumber"));
                result.add(related(getString(R.string.section_requests), "zaivki", "OwnerId=?", id, "RequestDate DESC"));
                result.add(related(getString(R.string.dashboard_charges), "nachislenie",
                        "ApartmentId IN (SELECT Id FROM Kvartira WHERE OwnerId=?)", id, "Period DESC"));
                break;
            case "yslygi":
                result.add(related(getString(R.string.dashboard_charges), "nachislenie", "ServiceId=?", id, "Period DESC"));
                break;
            case "nachislenie":
                result.add(single(getString(R.string.dashboard_apartments), "kvartira", record.get("ApartmentId")));
                result.add(single("Услуга", "yslygi", record.get("ServiceId")));
                break;
            case "zaivki":
                result.add(single(getString(R.string.dashboard_owners), "cobstveniki", record.get("OwnerId")));
                break;
            case "rashod":
                result.add(single(getString(R.string.dashboard_houses), "houses", record.get("HouseId")));
                break;
            default:
                break;
        }
        return result;
    }

    private Related related(String title, String tableKey, String where, String arg, String order) {
        TableDefinition target = TableDefinition.byKey(tableKey);
        return new Related(title, target, db.getRecordsWhere(target, where, new String[]{arg}, order));
    }

    private Related single(String title, String tableKey, String rawId) {
        TableDefinition target = TableDefinition.byKey(tableKey);
        try {
            Record record = db.getRecordById(target, Long.parseLong(rawId));
            return new Related(title, target,
                    record == null ? Collections.<Record>emptyList() : Collections.singletonList(record));
        } catch (NumberFormatException e) {
            return new Related(title, target, Collections.<Record>emptyList());
        }
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }
}
