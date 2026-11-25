package com.example.managementcompany

import android.content.Context
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.Column
import com.example.managementcompany.TableDefinition.TableDefinition

/** Построение современных полей ввода для окна добавления/изменения записи. */
object FormFields {

    class InputField(
        val column: Column,
        private val edit: EditText?,
        private val spinner: Spinner?,
        private val ids: List<Long>?
    ) {
        fun getValue(): String {
            if (edit != null) return edit.text.toString()
            val position = spinner?.selectedItemPosition ?: return ""
            return if (ids != null && position in ids.indices) ids[position].toString() else ""
        }
    }

    fun build(
        context: Context,
        container: ViewGroup,
        def: TableDefinition,
        record: Record?,
        db: AppRepository
    ): List<InputField> {
        val fields = mutableListOf<InputField>()
        val textColor = context.resources.getColor(R.color.text_primary, context.theme)
        val hintColor = context.resources.getColor(R.color.text_secondary, context.theme)

        for (column in def.columns) {
            val label = TextView(context).apply {
                text = column.label
                textSize = 12f
                setTextColor(hintColor)
            }
            add(container, label, 6)

            val fkTable = column.fkTable
            if (fkTable != null) {
                val references = db.getReferenceItems(fkTable, column.fkDisplay.orEmpty())
                val labels = mutableListOf<String>()
                val ids = mutableListOf<Long>()
                var selected = 0
                val current = record?.get(column.name)

                references.forEachIndexed { i, item ->
                    labels += if (item.label.isEmpty()) "ID ${item.id}" else item.label
                    ids += item.id
                    if (current != null && current == item.id.toString()) selected = i
                }

                val spinner = Spinner(context).apply {
                    adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, labels).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    setBackgroundResource(R.drawable.search_box)
                    minimumHeight = Ui.dp(50)
                    if (ids.isNotEmpty()) setSelection(selected)
                }
                add(container, spinner, 14)
                fields += InputField(column, null, spinner, ids)
            } else {
                val edit = EditText(context).apply {
                    hint = column.hint ?: column.label
                    setText(record?.get(column.name) ?: "")
                    setBackgroundResource(R.drawable.search_box)
                    setPadding(Ui.dp(16), Ui.dp(12), Ui.dp(16), Ui.dp(12))
                    minimumHeight = Ui.dp(50)
                    setTextColor(textColor)
                    setHintTextColor(hintColor)
                    inputType = when (column.type) {
                        Definitions.TYPE_INTEGER ->
                            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
                        Definitions.TYPE_REAL ->
                            InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or
                                InputType.TYPE_NUMBER_FLAG_SIGNED
                        else -> InputType.TYPE_CLASS_TEXT
                    }
                }
                add(container, edit, 14)
                fields += InputField(column, edit, null, null)
            }
        }
        return fields
    }

    private fun add(container: ViewGroup, view: View, bottomMargin: Int) {
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { setMargins(0, 0, 0, Ui.dp(bottomMargin)) }
        container.addView(view)
    }
}
