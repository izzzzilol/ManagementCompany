package com.example.managementcompany;

import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** Построение современных полей ввода для окна добавления/изменения записи. */
public final class FormFields {

    private FormFields() { }

    public static final class InputField {
        public final TableDefinition.Column column;
        private final EditText edit;
        private final Spinner spinner;
        private final List<Long> ids;

        private InputField(TableDefinition.Column column, EditText edit) {
            this.column = column;
            this.edit = edit;
            this.spinner = null;
            this.ids = null;
        }

        private InputField(TableDefinition.Column column, Spinner spinner, List<Long> ids) {
            this.column = column;
            this.edit = null;
            this.spinner = spinner;
            this.ids = ids;
        }

        public String getValue() {
            if (edit != null) return edit.getText().toString();
            int position = spinner.getSelectedItemPosition();
            if (ids != null && position >= 0 && position < ids.size()) {
                return String.valueOf(ids.get(position));
            }
            return "";
        }
    }

    public static List<InputField> build(Context context, ViewGroup container,
                                         TableDefinition def, Record record, AppRepository db) {
        List<InputField> fields = new ArrayList<>();
        int textColor = context.getResources().getColor(R.color.text_primary);
        int hintColor = context.getResources().getColor(R.color.text_secondary);

        for (TableDefinition.Column column : def.getColumns()) {
            TextView label = new TextView(context);
            label.setText(column.label);
            label.setTextSize(12);
            label.setTextColor(hintColor);
            add(container, label, 6);

            if (column.fkTable != null) {
                List<RefItem> references = db.getReferenceItems(column.fkTable, column.fkDisplay);
                List<String> labels = new ArrayList<>();
                List<Long> ids = new ArrayList<>();
                int selected = 0;
                String current = record == null ? null : record.get(column.name);

                for (int i = 0; i < references.size(); i++) {
                    RefItem item = references.get(i);
                    labels.add(item.label.isEmpty() ? "ID " + item.id : item.label);
                    ids.add(item.id);
                    if (current != null && current.equals(String.valueOf(item.id))) selected = i;
                }

                Spinner spinner = new Spinner(context);
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        context, android.R.layout.simple_spinner_item, labels);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setBackgroundResource(R.drawable.search_box);
                spinner.setMinimumHeight(Ui.dp(50));
                if (!ids.isEmpty()) spinner.setSelection(selected);
                add(container, spinner, 14);
                fields.add(new InputField(column, spinner, ids));
            } else {
                EditText edit = new EditText(context);
                edit.setHint(column.hint == null ? column.label : column.hint);
                edit.setText(record == null ? "" : record.get(column.name));
                edit.setBackgroundResource(R.drawable.search_box);
                edit.setPadding(Ui.dp(16), Ui.dp(12), Ui.dp(16), Ui.dp(12));
                edit.setMinimumHeight(Ui.dp(50));
                edit.setTextColor(textColor);
                edit.setHintTextColor(hintColor);
                if (TableDefinition.TYPE_INTEGER.equals(column.type)) {
                    edit.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                } else if (TableDefinition.TYPE_REAL.equals(column.type)) {
                    edit.setInputType(InputType.TYPE_CLASS_NUMBER
                            | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
                } else {
                    edit.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                add(container, edit, 14);
                fields.add(new InputField(column, edit));
            }
        }
        return fields;
    }

    private static void add(ViewGroup container, View view, int bottomMargin) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, Ui.dp(bottomMargin));
        view.setLayoutParams(params);
        container.addView(view);
    }
}
