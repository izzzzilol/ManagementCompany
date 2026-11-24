package com.example.managementcompany;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Карточка собственника: вкладки «Собственник» и «Записи». */
public class OwnerCardActivity extends Activity {

    public static final String EXTRA_OWNER_ID = "owner_id";

    private AppRepository db;
    private TableDefinition ownerDef;
    private long ownerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ownerDef = TableDefinition.byKey("cobstveniki");
        ownerId = getIntent().getLongExtra(EXTRA_OWNER_ID, -1);
        db = new AppRepository(this);

        setTitle(R.string.owner_card_title);
        setContentView(R.layout.activity_owner_card);
        Ui.header(this, getString(R.string.owner_card_title), ownerDef.getDisplayName(), true);

        final View ownerContent = findViewById(R.id.owner_content);
        final View recordsContent = findViewById(R.id.records_content);
        final Button ownerTab = findViewById(R.id.tab_owner_btn);
        final Button recordsTab = findViewById(R.id.tab_records_btn);

        ownerTab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ownerContent.setVisibility(View.VISIBLE);
                recordsContent.setVisibility(View.GONE);
                ownerTab.setBackgroundResource(R.drawable.tab_active);
                ownerTab.setTextColor(android.graphics.Color.WHITE);
                recordsTab.setBackgroundResource(R.drawable.tab_inactive);
                recordsTab.setTextColor(getResources().getColor(R.color.primary));
            }
        });
        recordsTab.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ownerContent.setVisibility(View.GONE);
                recordsContent.setVisibility(View.VISIBLE);
                recordsTab.setBackgroundResource(R.drawable.tab_active);
                recordsTab.setTextColor(android.graphics.Color.WHITE);
                ownerTab.setBackgroundResource(R.drawable.tab_inactive);
                ownerTab.setTextColor(getResources().getColor(R.color.primary));
            }
        });

        findViewById(R.id.edit_owner).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent intent = new Intent(OwnerCardActivity.this, RecordActivity.class);
                intent.putExtra(RecordActivity.EXTRA_TABLE, ownerDef.getKey());
                intent.putExtra(RecordActivity.EXTRA_RECORD_ID, ownerId);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fill();
    }

    private void fill() {
        Record owner = db.getRecordById(ownerDef, ownerId);
        if (owner == null) {
            finish();
            return;
        }

        ((TextView) findViewById(R.id.owner_name)).setText(
                owner.get("LastName") + " " + owner.get("FirstName") + " " + owner.get("MiddleName"));

        LinearLayout details = findViewById(R.id.owner_details);
        details.removeAllViews();
        for (TableDefinition.Column column : ownerDef.getColumns()) {
            Ui.row(this, details, column.label, owner.get(column.name));
        }

        fillRelated(R.id.apartments, db.getApartmentsByOwner(ownerId),
                TableDefinition.byKey("kvartira"));
        fillRelated(R.id.requests, db.getRequestsByOwner(ownerId),
                TableDefinition.byKey("zaivki"));
        fillRelated(R.id.charges, db.getChargesByOwner(ownerId),
                TableDefinition.byKey("nachislenie"));
    }

    private void fillRelated(int containerId, java.util.List<Record> records, TableDefinition def) {
        LinearLayout container = findViewById(containerId);
        container.removeAllViews();
        if (records.isEmpty()) {
            container.addView(Ui.text(this, getString(R.string.empty_related),
                    R.style.FieldValue, 0));
            return;
        }
        for (final Record record : records) {
            LinearLayout card = Ui.recordCard(this, db, def, record, 3);
            card.setClickable(true);
            card.setFocusable(true);
            card.setBackgroundResource(R.drawable.list_selector);
            card.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { openDetail(record, def); }
            });
            container.addView(card);
        }
    }

    private void openDetail(Record record, TableDefinition def) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TABLE, def.getKey());
        intent.putExtra(DetailActivity.EXTRA_RECORD_ID, record.getId());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }
}
