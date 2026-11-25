package com.example.managementcompany

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.TableDefinition

/** Карточка собственника: вкладки «Собственник» и «Записи». */
class OwnerCardActivity : Activity() {

    companion object {
        const val EXTRA_OWNER_ID = "owner_id"
    }

    private lateinit var db: AppRepository
    private lateinit var ownerDef: TableDefinition
    private var ownerId = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ownerDef = table("cobstveniki")
        ownerId = intent.getLongExtra(EXTRA_OWNER_ID, -1L)
        db = AppRepository(this)

        setTitle(R.string.owner_card_title)
        setContentView(R.layout.activity_owner_card)
        Ui.header(this, getString(R.string.owner_card_title), ownerDef.displayName, true)

        val ownerContent = findViewById<View>(R.id.owner_content)
        val recordsContent = findViewById<View>(R.id.records_content)
        val ownerTab = findViewById<Button>(R.id.tab_owner_btn)
        val recordsTab = findViewById<Button>(R.id.tab_records_btn)

        fun showOwnerTab() {
            ownerContent.visibility = View.VISIBLE
            recordsContent.visibility = View.GONE
            ownerTab.setBackgroundResource(R.drawable.tab_active)
            ownerTab.setTextColor(Color.WHITE)
            recordsTab.setBackgroundResource(R.drawable.tab_inactive)
            recordsTab.setTextColor(resources.getColor(R.color.primary, theme))
        }

        fun showRecordsTab() {
            ownerContent.visibility = View.GONE
            recordsContent.visibility = View.VISIBLE
            recordsTab.setBackgroundResource(R.drawable.tab_active)
            recordsTab.setTextColor(Color.WHITE)
            ownerTab.setBackgroundResource(R.drawable.tab_inactive)
            ownerTab.setTextColor(resources.getColor(R.color.primary, theme))
        }

        ownerTab.setOnClickListener { showOwnerTab() }
        recordsTab.setOnClickListener { showRecordsTab() }

        findViewById<View>(R.id.edit_owner).setOnClickListener {
            val intent = Intent(this, RecordActivity::class.java)
            intent.putExtra(RecordActivity.EXTRA_TABLE, ownerDef.key)
            intent.putExtra(RecordActivity.EXTRA_RECORD_ID, ownerId)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        fill()
    }

    override fun onDestroy() {
        if (::db.isInitialized) db.close()
        super.onDestroy()
    }

    private fun fill() {
        val owner = db.getRecordById(ownerDef, ownerId)
        if (owner == null) {
            finish()
            return
        }

        findViewById<TextView>(R.id.owner_name).text =
            "${owner["LastName"]} ${owner["FirstName"]} ${owner["MiddleName"]}"

        val details = findViewById<LinearLayout>(R.id.owner_details)
        details.removeAllViews()
        for (column in ownerDef.columns) {
            Ui.row(this, details, column.label, owner[column.name])
        }

        fillRelated(R.id.apartments, db.getApartmentsByOwner(ownerId), table("kvartira"))
        fillRelated(R.id.requests, db.getRequestsByOwner(ownerId), table("zaivki"))
        fillRelated(R.id.charges, db.getChargesByOwner(ownerId), table("nachislenie"))
    }

    private fun fillRelated(containerId: Int, records: List<Record>, def: TableDefinition) {
        val container = findViewById<LinearLayout>(containerId)
        container.removeAllViews()
        if (records.isEmpty()) {
            container.addView(Ui.text(this, getString(R.string.empty_related), R.style.FieldValue, 0))
            return
        }
        for (record in records) {
            val card = Ui.recordCard(this, db, def, record, 3)
            card.isClickable = true
            card.isFocusable = true
            card.setBackgroundResource(R.drawable.list_selector)
            card.setOnClickListener { openDetail(record, def) }
            container.addView(card)
        }
    }

    private fun openDetail(record: Record, def: TableDefinition) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_TABLE, def.key)
        intent.putExtra(DetailActivity.EXTRA_RECORD_ID, record.id)
        startActivity(intent)
    }

    /** Ключи заданы в TableDefinition, поэтому они всегда известны. */
    private fun table(key: String): TableDefinition = Definitions.byKey(key)!!
}
