package com.example.managementcompany

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.TableDefinition

/**
 * Универсальный экран таблицы: DataGridView, панель команд (ToolStrip),
 * поиск, навигация по записям и переходы в окна добавления/изменения.
 */
class TableActivity : Activity() {

    companion object {
        const val EXTRA_TABLE = "table_key"
    }

    private lateinit var def: TableDefinition
    private lateinit var db: AppRepository
    private lateinit var adapter: RecordAdapter
    private lateinit var search: EditText
    private lateinit var recordNumber: EditText
    private lateinit var count: TextView
    private lateinit var status: TextView
    private lateinit var list: ListView
    private var selectedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val definition = Definitions.byKey(intent.getStringExtra(EXTRA_TABLE))
        if (definition == null) {
            finish()
            return
        }
        def = definition

        setTitle(def.displayName)
        setContentView(R.layout.activity_table)
        Ui.header(this, def.displayName, getString(R.string.app_name), true)
        db = AppRepository(this)

        search = findViewById(R.id.search)
        recordNumber = findViewById(R.id.record_number)
        count = findViewById(R.id.count)
        status = findViewById(R.id.status)
        list = findViewById(R.id.list)

        adapter = RecordAdapter()
        list.adapter = adapter
        list.setOnItemClickListener { _, _, position, _ -> openDetail(position) }
        list.setOnItemLongClickListener { _, _, position, _ ->
            select(position)
            Toast.makeText(this, "Запись выбрана", Toast.LENGTH_SHORT).show()
            true
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                selectedPosition = -1
                recordNumber.setText("")
                list.clearChoices()
                load()
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        findViewById<View>(R.id.add).setOnClickListener { openRecord(-1) }
        findViewById<View>(R.id.edit_record).setOnClickListener {
            if (selectedPosition >= 0) {
                openRecord(adapter.getItem(selectedPosition).id)
            } else {
                Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
            }
        }
        findViewById<View>(R.id.open).setOnClickListener {
            if (selectedPosition >= 0) openDetail(selectedPosition)
            else Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
        }
        findViewById<View>(R.id.delete_selected).setOnClickListener { confirmDelete() }
        findViewById<View>(R.id.nav_first).setOnClickListener {
            if (adapter.count > 0) select(0)
        }
        findViewById<View>(R.id.nav_prev).setOnClickListener {
            val n = adapter.count
            if (n == 0) return@setOnClickListener
            select(if (selectedPosition <= 0) n - 1 else selectedPosition - 1)
        }
        findViewById<View>(R.id.nav_next).setOnClickListener {
            val n = adapter.count
            if (n == 0) return@setOnClickListener
            select(if (selectedPosition < 0) 0 else (selectedPosition + 1) % n)
        }
        findViewById<View>(R.id.nav_last).setOnClickListener {
            val n = adapter.count
            if (n > 0) select(n - 1)
        }
        findViewById<View>(R.id.find).setOnClickListener { findByNumber() }

        val ownerCard = findViewById<Button>(R.id.owner_card)
        if (def.key == "cobstveniki") {
            ownerCard.visibility = View.VISIBLE
            ownerCard.setOnClickListener {
                if (selectedPosition < 0) {
                    Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                val intent = Intent(this, OwnerCardActivity::class.java)
                intent.putExtra(OwnerCardActivity.EXTRA_OWNER_ID, adapter.getItem(selectedPosition).id)
                startActivity(intent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onDestroy() {
        if (::db.isInitialized) db.close()
        super.onDestroy()
    }

    private fun load() {
        val items = db.getAll(def, search.text.toString())
        adapter.setItems(items)
        count.text = getString(R.string.records_count, items.size)
        status.setText(if (items.isEmpty()) R.string.status_empty else R.string.status_loaded)

        if (selectedPosition >= items.size) {
            selectedPosition = -1
            recordNumber.setText("")
            list.clearChoices()
        }
        if (selectedPosition >= 0) {
            list.setItemChecked(selectedPosition, true)
            list.setSelection(selectedPosition)
        }
        updateButtons()
    }

    /** Кнопки доступны только тогда, когда есть данные и/или выбранная запись. */
    private fun updateButtons() {
        val hasItems = adapter.count > 0
        val hasSelection = selectedPosition >= 0 && selectedPosition < adapter.count
        val filtered = search.text.toString().trim().isNotEmpty()

        findViewById<View>(R.id.add).isEnabled = hasItems || !filtered
        findViewById<View>(R.id.nav_first).isEnabled = hasItems
        findViewById<View>(R.id.nav_prev).isEnabled = hasItems
        findViewById<View>(R.id.nav_next).isEnabled = hasItems
        findViewById<View>(R.id.nav_last).isEnabled = hasItems
        findViewById<View>(R.id.find).isEnabled = hasItems
        findViewById<View>(R.id.open).isEnabled = hasSelection
        findViewById<View>(R.id.edit_record).isEnabled = hasSelection
        findViewById<View>(R.id.delete_selected).isEnabled = hasSelection
        findViewById<View>(R.id.owner_card).isEnabled = hasSelection
    }

    private fun select(position: Int) {
        if (position < 0 || position >= adapter.count) return
        selectedPosition = position
        list.setItemChecked(position, true)
        list.setSelection(position)
        recordNumber.setText(adapter.getItem(position).id.toString())
        updateButtons()
    }

    /** Аналог поиска записи по значению в TextBox Number_Emloyee. */
    private fun findByNumber() {
        val id = recordNumber.text.toString().trim().toLongOrNull()
        if (id == null) {
            status.setText(R.string.status_empty)
            return
        }
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).id == id) {
                select(i)
                status.setText(R.string.status_loaded)
                return
            }
        }
        selectedPosition = -1
        list.clearChoices()
        status.setText(R.string.status_empty)
        updateButtons()
    }

    private fun openDetail(position: Int) {
        if (position < 0 || position >= adapter.count) return
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_TABLE, def.key)
        intent.putExtra(DetailActivity.EXTRA_RECORD_ID, adapter.getItem(position).id)
        startActivity(intent)
    }

    private fun openRecord(id: Long) {
        val intent = Intent(this, RecordActivity::class.java)
        intent.putExtra(RecordActivity.EXTRA_TABLE, def.key)
        intent.putExtra(RecordActivity.EXTRA_RECORD_ID, id)
        startActivity(intent)
    }

    private fun confirmDelete() {
        if (selectedPosition < 0) {
            Toast.makeText(this, R.string.no_selection, Toast.LENGTH_SHORT).show()
            return
        }
        val id = adapter.getItem(selectedPosition).id
        AlertDialog.Builder(this)
            .setMessage(R.string.confirm_delete)
            .setPositiveButton(R.string.delete) { _, _ ->
                db.delete(def, id)
                Toast.makeText(this, R.string.deleted, Toast.LENGTH_SHORT).show()
                selectedPosition = -1
                recordNumber.setText("")
                list.clearChoices()
                load()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private inner class RecordAdapter : BaseAdapter() {
        private val items = mutableListOf<Record>()

        fun setItems(newItems: List<Record>) {
            items.clear()
            items.addAll(newItems)
            notifyDataSetChanged()
        }

        override fun getCount(): Int = items.size

        override fun getItem(position: Int): Record = items[position]

        override fun getItemId(position: Int): Long = items[position].id

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView
                ?: LayoutInflater.from(this@TableActivity).inflate(R.layout.list_item, parent, false)
            val record = getItem(position)
            view.findViewById<TextView>(R.id.item_icon).text = Ui.iconFor(def)
            view.findViewById<TextView>(R.id.item_title).text = record.summary(def)
            view.findViewById<TextView>(R.id.item_subtitle).text = record.details(def)
            return view
        }
    }

    /** Ключи заданы в TableDefinition, поэтому они всегда известны. */
    private fun table(key: String): TableDefinition = Definitions.byKey(key)!!
}
