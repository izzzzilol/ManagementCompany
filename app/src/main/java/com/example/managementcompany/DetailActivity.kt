package com.example.managementcompany

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.TableDefinition

/** Подробный просмотр записи: структурированные поля, связи и рабочие статусы. */
class DetailActivity : Activity() {

    companion object {
        const val EXTRA_TABLE = TableActivity.EXTRA_TABLE
        const val EXTRA_RECORD_ID = "record_id"
    }

    private lateinit var def: TableDefinition
    private lateinit var db: AppRepository
    private var recordId = -1L
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val definition = Definitions.byKey(intent.getStringExtra(EXTRA_TABLE))
        recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
        if (definition == null || recordId <= 0) {
            finish()
            return
        }
        def = definition

        db = AppRepository(this)
        setTitle(R.string.title_detail)
        setContentView(R.layout.activity_detail)
        Ui.header(this, def.displayName, getString(R.string.title_detail), true)
        container = findViewById(R.id.detail_container)

        findViewById<View>(R.id.detail_edit).setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            intent.putExtra(RecordActivity.EXTRA_TABLE, def.key)
            intent.putExtra(RecordActivity.EXTRA_RECORD_ID, recordId)
            startActivity(intent)
        }

        findViewById<View>(R.id.detail_delete).setOnClickListener {
            AlertDialog.Builder(this)
                .setMessage(R.string.confirm_delete)
                .setPositiveButton(R.string.delete) { _, _ ->
                    db.delete(def, recordId)
                    Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    override fun onDestroy() {
        if (::db.isInitialized) db.close()
        super.onDestroy()
    }

    private fun render() {
        val record = db.getRecordById(def, recordId)
        if (record == null) {
            finish()
            return
        }
        container.removeAllViews()

        container.addView(Ui.recordCard(this, db, def, record, -1))

        val related = relatedFor(record)
        if (related.isNotEmpty()) {
            val block = Ui.card(this)
            block.addView(Ui.text(this, getString(R.string.section_related), R.style.SectionTitle, 12))
            for (item in related) {
                val heading = Ui.text(this, item.title, R.style.FieldLabel, 6)
                heading.setTextColor(resources.getColor(R.color.primary, theme))
                block.addView(heading)

                if (item.records.isEmpty()) {
                    block.addView(Ui.text(this, getString(R.string.empty_related), R.style.FieldValue, 12))
                } else {
                    for (linked in item.records) {
                        val linkedDef = item.def
                        val card = Ui.recordCard(this, db, linkedDef, linked, 3)
                        card.isClickable = true
                        card.isFocusable = true
                        card.setBackgroundResource(R.drawable.list_selector)
                        card.setOnClickListener {
                            val intent = Intent(this, DetailActivity::class.java)
                            intent.putExtra(EXTRA_TABLE, linkedDef.key)
                            intent.putExtra(EXTRA_RECORD_ID, linked.id)
                            startActivity(intent)
                        }
                        block.addView(card)
                    }
                }
            }
            container.addView(block)
        }

        addStatusActions(record)
    }

    private fun addStatusActions(record: Record) {
        if (def.key != "zaivki" && def.key != "nachislenie") return

        val isRequest = def.key == "zaivki"
        val card = Ui.card(this)
        card.addView(
            Ui.text(
                this,
                getString(if (isRequest) R.string.section_status else R.string.section_payment),
                R.style.SectionTitle,
                10
            )
        )

        val current = if (isRequest) record["Status"] else record["PaymentStatus"]
        val values = if (isRequest) {
            arrayOf(
                getString(R.string.status_new),
                getString(R.string.status_in_progress),
                getString(R.string.status_done)
            )
        } else {
            arrayOf(getString(R.string.status_paid), getString(R.string.status_not_paid))
        }

        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        for (value in values) {
            val active = value == current
            val button = Button(this).apply {
                text = value
                textSize = 12f
                setBackgroundResource(if (active) R.drawable.btn_primary else R.drawable.btn_secondary)
                setTextColor(if (active) Color.WHITE else resources.getColor(R.color.primary, theme))
                setPadding(Ui.dp(12), Ui.dp(8), Ui.dp(12), Ui.dp(8))
                setOnClickListener {
                    setStatus(if (isRequest) "Status" else "PaymentStatus", value)
                }
            }
            row.addView(
                button,
                LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { setMargins(Ui.dp(3), 0, Ui.dp(3), 0) }
            )
        }
        card.addView(row)
        container.addView(card)
    }

    private fun setStatus(column: String, value: String) {
        db.setStatus(def, recordId, column, value)
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show()
        render()
    }

    private data class Related(val title: String, val def: TableDefinition, val records: List<Record>)

    private fun relatedFor(record: Record): List<Related> {
        val result = mutableListOf<Related>()
        val id = record.id.toString()

        when (def.key) {
            "houses" -> {
                result += related(getString(R.string.dashboard_apartments), "kvartira", "HouseId=?", id, "ApartmentNumber")
                result += related(getString(R.string.dashboard_expenses), "rashod", "HouseId=?", id, "Date DESC")
            }
            "kvartira" -> {
                result += related(getString(R.string.dashboard_charges), "nachislenie", "ApartmentId=?", id, "Period DESC")
                result += single(getString(R.string.dashboard_owners), "cobstveniki", record["OwnerId"])
                result += single(getString(R.string.dashboard_houses), "houses", record["HouseId"])
            }
            "cobstveniki" -> {
                result += related(getString(R.string.dashboard_apartments), "kvartira", "OwnerId=?", id, "ApartmentNumber")
                result += related(getString(R.string.section_requests), "zaivki", "OwnerId=?", id, "RequestDate DESC")
                result += related(
                    getString(R.string.dashboard_charges), "nachislenie",
                    "ApartmentId IN (SELECT Id FROM Kvartira WHERE OwnerId=?)", id, "Period DESC"
                )
            }
            "yslygi" -> {
                result += related(getString(R.string.dashboard_charges), "nachislenie", "ServiceId=?", id, "Period DESC")
            }
            "nachislenie" -> {
                result += single(getString(R.string.dashboard_apartments), "kvartira", record["ApartmentId"])
                result += single("Услуга", "yslygi", record["ServiceId"])
            }
            "zaivki" -> {
                result += single(getString(R.string.dashboard_owners), "cobstveniki", record["OwnerId"])
            }
            "rashod" -> {
                result += single(getString(R.string.dashboard_houses), "houses", record["HouseId"])
            }
            else -> Unit
        }
        return result
    }

    private fun related(title: String, tableKey: String, where: String, arg: String, order: String): Related {
        val target = table(tableKey)
        return Related(title, target, db.getRecordsWhere(target, where, arrayOf(arg), order))
    }

    private fun single(title: String, tableKey: String, rawId: String): Related {
        val target = table(tableKey)
        val id = rawId.toLongOrNull()
        val records = if (id == null) {
            emptyList()
        } else {
            val found = db.getRecordById(target, id)
            if (found == null) emptyList() else listOf(found)
        }
        return Related(title, target, records)
    }

    /** Ключи заданы в TableDefinition, поэтому они всегда известны. */
    private fun table(key: String): TableDefinition = Definitions.byKey(key)!!
}
