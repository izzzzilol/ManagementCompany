package com.example.managementcompany;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

/** Единое современное оформление экранов и структурированных карточек. */
public final class Ui {

    private Ui() { }

    /** Все размеры в коде задаются в dp — иначе на плотных экранах отступы исчезают. */
    public static int dp(int value) {
        return Math.round(value * Resources.getSystem().getDisplayMetrics().density);
    }

    public static void header(Activity activity, String title, String subtitle, boolean showBack) {
        TextView titleView = activity.findViewById(R.id.header_title);
        if (titleView != null) titleView.setText(title);

        TextView subtitleView = activity.findViewById(R.id.header_subtitle);
        if (subtitleView != null) {
            if (subtitle == null || subtitle.isEmpty()) {
                subtitleView.setVisibility(View.GONE);
            } else {
                subtitleView.setText(subtitle);
                subtitleView.setVisibility(View.VISIBLE);
            }
        }

        View back = activity.findViewById(R.id.header_back);
        if (back != null) {
            if (showBack) {
                back.setVisibility(View.VISIBLE);
                back.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View v) { activity.finish(); }
                });
            } else {
                back.setVisibility(View.GONE);
            }
        }
    }

    public static TextView text(Context context, String value, int styleRes, int bottomMarginDp) {
        TextView view = new TextView(context);
        view.setText(value);
        view.setTextAppearance(context, styleRes);
        view.setPadding(dp(2), dp(2), dp(2), dp(2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(bottomMarginDp));
        view.setLayoutParams(params);
        return view;
    }

    public static LinearLayout card(Context context) {
        LinearLayout card = new LinearLayout(context);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackgroundResource(R.drawable.card);
        card.setElevation(dp(2));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, dp(14));
        card.setLayoutParams(params);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        return card;
    }

    public static void row(Context context, LinearLayout container, String label, String value) {
        TextView labelView = new TextView(context);
        labelView.setText(label);
        labelView.setTextAppearance(context, R.style.FieldLabel);
        labelView.setPadding(dp(2), dp(6), dp(2), 0);
        container.addView(labelView);

        TextView valueView = new TextView(context);
        valueView.setText(value == null || value.isEmpty() ? "—" : value);
        valueView.setTextAppearance(context, R.style.FieldValue);
        valueView.setPadding(dp(2), dp(2), dp(2), dp(10));
        container.addView(valueView);
    }

    public static String iconFor(TableDefinition def) {
        if (def == null) return "•";
        switch (def.getKey()) {
            case "houses": return "Д";
            case "kvartira": return "Кв";
            case "cobstveniki": return "С";
            case "yslygi": return "У";
            case "nachislenie": return "Н";
            case "zaivki": return "З";
            case "rashod": return "Р";
            default: return "•";
        }
    }

    /** Карточка записи: заголовок с иконкой и аккуратные строки «подпись — значение». */
    public static LinearLayout recordCard(Context context, AppRepository db,
                                          TableDefinition def, Record record, int maxFields) {
        LinearLayout card = card(context);

        LinearLayout head = new LinearLayout(context);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setPadding(0, dp(2), 0, dp(10));
        card.addView(head);

        TextView icon = new TextView(context);
        icon.setText(iconFor(def));
        icon.setBackgroundResource(R.drawable.avatar);
        icon.setGravity(android.view.Gravity.CENTER);
        icon.setTextColor(android.graphics.Color.WHITE);
        icon.setTextSize(13);
        icon.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(40), dp(40));
        head.addView(icon, iconParams);

        TextView title = new TextView(context);
        title.setText(record.summary(def));
        title.setTextAppearance(context, R.style.FieldValue);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setPadding(dp(12), 0, dp(4), 0);
        title.setMaxLines(1);
        title.setEllipsize(android.text.TextUtils.TruncateAt.END);
        head.addView(title, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        int total = def.getColumns().size();
        int limit = maxFields < 0 ? total : Math.min(maxFields, total);
        for (int i = 0; i < limit; i++) {
            TableDefinition.Column column = def.getColumns().get(i);
            row(context, card, column.label, resolve(db, record, column));
        }
        if (limit < total) {
            TextView more = new TextView(context);
            more.setText("… ещё " + (total - limit));
            more.setTextAppearance(context, R.style.FieldLabel);
            more.setPadding(dp(2), dp(4), dp(2), dp(2));
            card.addView(more);
        }
        return card;
    }

    /** Компактная строка для списка последних заявок на главном экране. */
    public static LinearLayout miniCard(Context context, String title, String status, String icon) {
        LinearLayout card = card(context);
        card.setClickable(true);
        card.setFocusable(true);
        card.setBackgroundResource(R.drawable.list_selector);

        LinearLayout head = new LinearLayout(context);
        head.setOrientation(LinearLayout.HORIZONTAL);
        head.setGravity(android.view.Gravity.CENTER_VERTICAL);
        head.setPadding(dp(2), dp(4), dp(2), dp(4));
        card.addView(head);

        TextView badge = new TextView(context);
        badge.setText(icon);
        badge.setBackgroundResource(R.drawable.avatar);
        badge.setGravity(android.view.Gravity.CENTER);
        badge.setTextColor(android.graphics.Color.WHITE);
        badge.setTextSize(13);
        head.addView(badge, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView text = new TextView(context);
        text.setText(title);
        text.setTextAppearance(context, R.style.FieldValue);
        text.setMaxLines(2);
        text.setEllipsize(android.text.TextUtils.TruncateAt.END);
        text.setPadding(dp(10), 0, dp(8), 0);
        head.addView(text, new LinearLayout.LayoutParams(0,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView chip = new TextView(context);
        chip.setText(status);
        chip.setBackgroundResource(R.drawable.chip);
        chip.setTextColor(android.graphics.Color.parseColor("#5B5BD6"));
        chip.setTextSize(11);
        chip.setPadding(dp(10), dp(5), dp(10), dp(5));
        head.addView(chip);
        return card;
    }

    public static TextView chip(Context context, String text, int bgRes, int textColor) {
        TextView chip = new TextView(context);
        chip.setText(text);
        chip.setBackgroundResource(bgRes);
        chip.setTextColor(textColor);
        chip.setTextSize(12);
        chip.setPadding(dp(12), dp(6), dp(12), dp(6));
        return chip;
    }

    private static String resolve(AppRepository db, Record record, TableDefinition.Column column) {
        String raw = record.get(column.name);
        if (column.fkTable != null && !raw.isEmpty()) {
            try {
                TableDefinition refDef = TableDefinition.byTableName(column.fkTable);
                Record ref = db.getRecordById(refDef, Long.parseLong(raw));
                if (ref != null) return ref.summary(refDef);
            } catch (NumberFormatException ignored) {
                return raw;
            }
        }
        return raw;
    }
}
