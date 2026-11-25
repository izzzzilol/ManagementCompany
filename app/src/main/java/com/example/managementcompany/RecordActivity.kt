package com.example.managementcompany

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.TableDefinition

/**
 * Отдельное окно добавления/изменения записи.
 * Аналог форм «Окно добавления дома», «Окно изменения данных о доме» и т.д.
 */
class RecordActivity : Activity() {

    companion object {
        const val EXTRA_TABLE = TableActivity.EXTRA_TABLE
        const val EXTRA_RECORD_ID = "record_id"
    }

    private lateinit var def: TableDefinition
    private lateinit var db: AppRepository
    private var record: Record? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val definition = Definitions.byKey(intent.getStringExtra(EXTRA_TABLE))
        if (definition == null) {
            finish()
            return
        }
        def = definition

        val id = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
        db = AppRepository(this)
        record = if (id > 0) db.getRecordById(def, id) else null

        setTitle(if (record == null) R.string.title_add else R.string.title_edit)
        setContentView(R.layout.activity_record)
        Ui.header(
            this,
            getString(if (record == null) R.string.title_add else R.string.title_edit),
            def.displayName,
            true
        )

        val container = findViewById<ViewGroup>(R.id.fields_container)
        val fields = FormFields.build(this, container, def, record, db)

        findViewById<View>(R.id.save).setOnClickListener {
            val values = LinkedHashMap<String, String>()
            for (field in fields) {
                val value = field.getValue()
                if (value.trim().isEmpty()) {
                    Toast.makeText(this, R.string.fill_required, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                values[field.column.name] = value.trim()
            }
            try {
                val current = record
                if (current == null) {
                    db.insert(def, values)
                } else {
                    db.update(def, current.id, values)
                }
                Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this, e.message ?: "", Toast.LENGTH_LONG).show()
            }
        }

        val delete = findViewById<Button>(R.id.delete)
        val current = record
        if (current == null) {
            delete.visibility = View.GONE
        } else {
            delete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setMessage(R.string.confirm_delete)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        db.delete(def, current.id)
                        Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .show()
            }
        }

        findViewById<View>(R.id.cancel).setOnClickListener { finish() }
    }

    override fun onDestroy() {
        if (::db.isInitialized) db.close()
        super.onDestroy()
    }
}
