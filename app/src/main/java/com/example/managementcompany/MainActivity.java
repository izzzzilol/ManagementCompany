package com.example.managementcompany;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

/** Рабочий стол управляющей компании: сводка, финансы, заявки и быстрые действия. */
public class MainActivity extends Activity {

    private AppRepository db;
    private LinearLayout container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new AppRepository(this);
        setContentView(R.layout.activity_main);
        Ui.header(this, getString(R.string.app_name), "Панель управления", false);
        container = findViewById(R.id.dashboard_container);
    }

    @Override
    protected void onResume() {
        super.onResume();
        render();
    }

    private void render() {
        container.removeAllViews();

        // Приветственный блок
        LinearLayout welcome = Ui.card(this);
        welcome.addView(Ui.text(this, getString(R.string.title_main), R.style.SectionTitle, 6));
        welcome.addView(Ui.text(this, getString(R.string.hint_main), R.style.FieldValue, 0));
        TextView chip = Ui.chip(this, "Рабочий стол", R.drawable.chip,
                getResources().getColor(R.color.primary));
        LinearLayout.LayoutParams chipParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        chipParams.topMargin = Ui.dp(12);
        welcome.addView(chip, chipParams);
        container.addView(welcome);

        // Обзор
        container.addView(section(R.string.dashboard_overview));
        LinearLayout stats = new LinearLayout(this);
        stats.setOrientation(LinearLayout.VERTICAL);
        stats.addView(rowOf(
                statCard(String.valueOf(db.count(TableDefinition.byKey("houses"))),
                        getString(R.string.dashboard_houses), "Д"),
                statCard(String.valueOf(db.count(TableDefinition.byKey("kvartira"))),
                        getString(R.string.dashboard_apartments), "Кв")));
        stats.addView(rowOf(
                statCard(String.valueOf(db.count(TableDefinition.byKey("cobstveniki"))),
                        getString(R.string.dashboard_owners), "С"),
                statCard(String.valueOf(db.countWhere("Zaivki",
                        "Status IN ('Новая', 'В работе')", null)),
                        getString(R.string.dashboard_active_requests), "З")));
        container.addView(stats);

        // Финансы
        container.addView(section(R.string.dashboard_finance));
        LinearLayout finance = Ui.card(this);
        double paid = db.sumWhere("Nachislenie", "Amount", "PaymentStatus='Оплачено'");
        double unpaid = db.sumWhere("Nachislenie", "Amount", "PaymentStatus='Не оплачено'");
        Ui.row(this, finance, getString(R.string.dashboard_paid), money(paid));
        Ui.row(this, finance, getString(R.string.dashboard_unpaid), money(unpaid));

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setBackgroundResource(R.drawable.card_soft);
        bar.setMinimumHeight(Ui.dp(10));
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(10));
        barParams.setMargins(Ui.dp(2), Ui.dp(6), Ui.dp(2), Ui.dp(2));
        bar.setLayoutParams(barParams);

        View paidPart = new View(this);
        paidPart.setBackgroundColor(getResources().getColor(R.color.primary));
        float total = (float) (paid + unpaid);
        float share = (float) (total > 0 ? paid / total : 0.0);
        bar.addView(paidPart, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, share));
        View restPart = new View(this);
        bar.addView(restPart, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.MATCH_PARENT, Math.max(0.001f, 1f - share)));
        finance.addView(bar);
        container.addView(finance);

        // Последние заявки
        container.addView(section(R.string.dashboard_recent));
        LinearLayout requestsCard = Ui.card(this);
        List<Record> requests = db.getRecordsWhere(TableDefinition.byKey("zaivki"),
                null, null, "RequestDate DESC");
        if (requests.isEmpty()) {
            requestsCard.addView(Ui.text(this, getString(R.string.dashboard_empty_requests),
                    R.style.FieldValue, 0));
        } else {
            int limit = Math.min(3, requests.size());
            for (int i = 0; i < limit; i++) {
                final Record request = requests.get(i);
                String owner = ownerName(request.get("OwnerId"));
                String title = request.get("RequestDate") + "  ·  " + owner
                        + "\n" + request.get("Description");
                LinearLayout card = Ui.miniCard(this, title,
                        request.get("Status"), Ui.iconFor(TableDefinition.byKey("zaivki")));
                card.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) {
                        openDetail("zaivki", request.getId());
                    }
                });
                requestsCard.addView(card);
            }
        }
        Button allRequests = new Button(this);
        allRequests.setText("Все заявки");
        allRequests.setTextSize(12);
        allRequests.setBackgroundResource(R.drawable.btn_secondary);
        allRequests.setTextColor(getResources().getColor(R.color.primary));
        allRequests.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openTable("zaivki"); }
        });
        LinearLayout.LayoutParams allParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        allParams.topMargin = Ui.dp(4);
        requestsCard.addView(allRequests, allParams);
        container.addView(requestsCard);

        // Быстрые действия
        container.addView(section(R.string.dashboard_quick));
        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        actions.addView(action(getString(R.string.dashboard_new_request), true, "zaivki"),
                rowParams());
        actions.addView(action(getString(R.string.dashboard_new_charge), false, "nachislenie"),
                rowParams());
        actions.addView(action(getString(R.string.dashboard_new_owner), false, "cobstveniki"),
                rowParams());
        container.addView(actions);

        // Реестры
        container.addView(section(R.string.dashboard_registers));
        LinearLayout registers = new LinearLayout(this);
        registers.setOrientation(LinearLayout.VERTICAL);
        registers.addView(rowOf(
                registerCard("Д", "Дома", "Жилой фонд", "houses"),
                registerCard("Кв", "Квартиры", "Помещения", "kvartira")));
        registers.addView(rowOf(
                registerCard("С", "Собственники", "Реестр", "cobstveniki"),
                registerCard("У", "Услуги", "Тарифы", "yslygi")));
        registers.addView(rowOf(
                registerCard("Н", "Начисления", "Платежи", "nachislenie"),
                registerCard("З", "Заявки", "Обращения", "zaivki")));
        registers.addView(rowOf(
                registerCard("Р", "Расходы", "Хозяйство", "rashod"), null));
        container.addView(registers);

        TextView footer = Ui.text(this, getString(R.string.footer_db), R.style.FieldLabel, 0);
        footer.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams footerParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        footerParams.topMargin = Ui.dp(14);
        footer.setLayoutParams(footerParams);
        container.addView(footer);
    }

    private TextView section(int textRes) {
        TextView view = Ui.text(this, getString(textRes), R.style.SectionTitle, 8);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = Ui.dp(10);
        view.setLayoutParams(params);
        return view;
    }

    private LinearLayout rowOf(View left, View right) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        leftParams.setMargins(0, 0, Ui.dp(4), Ui.dp(8));
        left.setLayoutParams(leftParams);
        row.addView(left);
        if (right != null) {
            LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            rightParams.setMargins(Ui.dp(4), 0, 0, Ui.dp(8));
            right.setLayoutParams(rightParams);
            row.addView(right);
        }
        return row;
    }

    private LinearLayout statCard(String value, String label, String icon) {
        LinearLayout card = Ui.card(this);
        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setBackgroundResource(R.drawable.avatar);
        iconView.setGravity(Gravity.CENTER);
        iconView.setTextColor(Color.WHITE);
        iconView.setTextSize(12);
        iconView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(iconView, new LinearLayout.LayoutParams(Ui.dp(32), Ui.dp(32)));

        TextView valueView = Ui.text(this, value, R.style.SectionTitle, 2);
        valueView.setTextSize(24);
        valueView.setTextColor(getResources().getColor(R.color.primary));
        LinearLayout.LayoutParams valueParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        valueParams.topMargin = Ui.dp(10);
        valueView.setLayoutParams(valueParams);
        card.addView(valueView);

        TextView labelView = Ui.text(this, label, R.style.FieldLabel, 0);
        card.addView(labelView);
        return card;
    }

    private LinearLayout registerCard(String icon, String title, String subtitle, final String key) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.card);
        card.setElevation(2f);
        card.setPadding(Ui.dp(14), Ui.dp(16), Ui.dp(14), Ui.dp(16));
        card.setClickable(true);
        card.setFocusable(true);
        card.setBackgroundResource(R.drawable.list_selector);
        card.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openTable(key); }
        });
        TextView iconView = new TextView(this);
        iconView.setText(icon);
        iconView.setBackgroundResource(R.drawable.avatar);
        iconView.setGravity(Gravity.CENTER);
        iconView.setTextColor(Color.WHITE);
        iconView.setTextSize(15);
        iconView.setTypeface(null, android.graphics.Typeface.BOLD);
        card.addView(iconView, new LinearLayout.LayoutParams(Ui.dp(40), Ui.dp(40)));

        TextView titleView = Ui.text(this, title, R.style.FieldValue, 2);
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = Ui.dp(10);
        titleView.setLayoutParams(titleParams);
        card.addView(titleView);

        card.addView(Ui.text(this, subtitle, R.style.FieldLabel, 0));
        return card;
    }

    private Button action(String title, boolean primary, final String tableKey) {
        Button button = new Button(this);
        button.setText(title);
        button.setTextSize(11);
        button.setBackgroundResource(primary ? R.drawable.btn_primary : R.drawable.btn_secondary);
        button.setTextColor(primary ? Color.WHITE : getResources().getColor(R.color.primary));
        button.setPadding(Ui.dp(4), Ui.dp(8), Ui.dp(4), Ui.dp(8));
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { openAdd(tableKey); }
        });
        return button;
    }

    private LinearLayout.LayoutParams rowParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(Ui.dp(2), 0, Ui.dp(2), 0);
        return params;
    }

    private String ownerName(String rawId) {
        try {
            Record owner = db.getRecordById(TableDefinition.byKey("cobstveniki"),
                    Long.parseLong(rawId));
            if (owner == null) return "—";
            return owner.get("LastName") + " " + owner.get("FirstName");
        } catch (NumberFormatException e) {
            return "—";
        }
    }

    private String money(double value) {
        return String.format(Locale.getDefault(), "%,.2f ₽", value);
    }

    private void openTable(String tableKey) {
        Intent intent = new Intent(this, TableActivity.class);
        intent.putExtra(TableActivity.EXTRA_TABLE, tableKey);
        startActivity(intent);
    }

    private void openAdd(String tableKey) {
        Intent intent = new Intent(this, RecordActivity.class);
        intent.putExtra(RecordActivity.EXTRA_TABLE, tableKey);
        startActivity(intent);
    }

    private void openDetail(String tableKey, long id) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_TABLE, tableKey);
        intent.putExtra(DetailActivity.EXTRA_RECORD_ID, id);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (db != null) db.close();
        super.onDestroy();
    }
}
