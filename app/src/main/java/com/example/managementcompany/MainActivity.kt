package com.example.managementcompany

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.TableDefinition
import java.util.Locale

/** Рабочий стол управляющей компании: сводка, финансы, заявки и быстрые действия. */
class MainActivity : Activity() {

    private lateinit var db: AppRepository
    private lateinit var container: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = AppRepository(this)
        setContentView(R.layout.activity_main)
        Ui.header(this, getString(R.string.app_name), "Панель управления", false)
        container = findViewById(R.id.dashboard_container)
    }

    override fun onResume() {
        super.onResume()
        render()
    }

    private fun render() {
        container.removeAllViews()

        // Приветственный блок
        val welcome = Ui.card(this)
        welcome.addView(Ui.text(this, getString(R.string.title_main), R.style.SectionTitle, 6))
        welcome.addView(Ui.text(this, getString(R.string.hint_main), R.style.FieldValue, 0))
        val chip = Ui.chip(
            this, "Рабочий стол", R.drawable.chip,
            resources.getColor(R.color.primary, theme)
        )
        chip.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = Ui.dp(12) }
        welcome.addView(chip)
        container.addView(welcome)

        // Обзор
        container.addView(section(R.string.dashboard_overview))
        val stats = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        stats.addView(
            rowOf(
                statCard(
                    db.count(table("houses")).toString(),
                    getString(R.string.dashboard_houses), "Д"
                ),
                statCard(
                    db.count(table("kvartira")).toString(),
                    getString(R.string.dashboard_apartments), "Кв"
                )
            )
        )
        stats.addView(
            rowOf(
                statCard(
                    db.count(table("cobstveniki")).toString(),
                    getString(R.string.dashboard_owners), "С"
                ),
                statCard(
                    db.countWhere("Zaivki", "Status IN ('Новая', 'В работе')", emptyArray()).toString(),
                    getString(R.string.dashboard_active_requests), "З"
                )
            )
        )
        container.addView(stats)

        // Финансы
        container.addView(section(R.string.dashboard_finance))
        val finance = Ui.card(this)
        val paid = db.sumWhere("Nachislenie", "Amount", "PaymentStatus='Оплачено'")
        val unpaid = db.sumWhere("Nachislenie", "Amount", "PaymentStatus='Не оплачено'")
        Ui.row(this, finance, getString(R.string.dashboard_paid), money(paid))
        Ui.row(this, finance, getString(R.string.dashboard_unpaid), money(unpaid))

        val bar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.card_soft)
            minimumHeight = Ui.dp(10)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, Ui.dp(10)
            ).apply { setMargins(Ui.dp(2), Ui.dp(6), Ui.dp(2), Ui.dp(2)) }
        }
        val paidPart = View(this).apply {
            setBackgroundColor(resources.getColor(R.color.primary, theme))
        }
        val total = paid + unpaid
        val share = if (total > 0.0) (paid / total).toFloat() else 0f
        bar.addView(paidPart, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, share))
        val restPart = View(this)
        bar.addView(
            restPart,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, maxOf(0.001f, 1f - share))
        )
        finance.addView(bar)
        container.addView(finance)

        // Последние заявки
        container.addView(section(R.string.dashboard_recent))
        val requestsCard = Ui.card(this)
        val requests = db.getRecordsWhere(table("zaivki"), "", emptyArray(), "RequestDate DESC")
        if (requests.isEmpty()) {
            requestsCard.addView(Ui.text(this, getString(R.string.dashboard_empty_requests), R.style.FieldValue, 0))
        } else {
            val limit = minOf(3, requests.size)
            for (i in 0 until limit) {
                val request = requests[i]
                val owner = ownerName(request["OwnerId"])
                val title = request["RequestDate"] + "  ·  " + owner + "\n" + request["Description"]
                val card = Ui.miniCard(this, title, request["Status"], Ui.iconFor(table("zaivki")))
                card.setOnClickListener { openDetail("zaivki", request.id) }
                requestsCard.addView(card)
            }
        }
        val allRequests = Button(this).apply {
            text = "Все заявки"
            textSize = 12f
            setBackgroundResource(R.drawable.btn_secondary)
            setTextColor(resources.getColor(R.color.primary, theme))
            setOnClickListener { openTable("zaivki") }
        }
        requestsCard.addView(
            allRequests,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = Ui.dp(4) }
        )
        container.addView(requestsCard)

        // Быстрые действия
        container.addView(section(R.string.dashboard_quick))
        val actions = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        actions.addView(action(getString(R.string.dashboard_new_request), true, "zaivki"), rowParams())
        actions.addView(action(getString(R.string.dashboard_new_charge), false, "nachislenie"), rowParams())
        actions.addView(action(getString(R.string.dashboard_new_owner), false, "cobstveniki"), rowParams())
        container.addView(actions)

        // Реестры
        container.addView(section(R.string.dashboard_registers))
        val registers = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        registers.addView(
            rowOf(
                registerCard("Д", "Дома", "Жилой фонд", "houses"),
                registerCard("Кв", "Квартиры", "Помещения", "kvartira")
            )
        )
        registers.addView(
            rowOf(
                registerCard("С", "Собственники", "Реестр", "cobstveniki"),
                registerCard("У", "Услуги", "Тарифы", "yslygi")
            )
        )
        registers.addView(
            rowOf(
                registerCard("Н", "Начисления", "Платежи", "nachislenie"),
                registerCard("З", "Заявки", "Обращения", "zaivki")
            )
        )
        registers.addView(
            rowOf(
                registerCard("Р", "Расходы", "Хозяйство", "rashod"),
                null
            )
        )
        container.addView(registers)

        val footer = Ui.text(this, getString(R.string.footer_db), R.style.FieldLabel, 0)
        footer.gravity = Gravity.CENTER
        footer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = Ui.dp(14) }
        container.addView(footer)
    }

    private fun section(textRes: Int): TextView {
        val view = Ui.text(this, getString(textRes), R.style.SectionTitle, 8)
        view.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply { topMargin = Ui.dp(10) }
        return view
    }

    private fun rowOf(left: View, right: View?): LinearLayout {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        left.layoutParams = LinearLayout.LayoutParams(
            0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
        ).apply { setMargins(0, 0, Ui.dp(4), Ui.dp(8)) }
        row.addView(left)
        if (right != null) {
            right.layoutParams = LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
            ).apply { setMargins(Ui.dp(4), 0, 0, Ui.dp(8)) }
            row.addView(right)
        }
        return row
    }

    private fun statCard(value: String, label: String, icon: String): LinearLayout {
        val card = Ui.card(this)
        val iconView = TextView(this).apply {
            text = icon
            setBackgroundResource(R.drawable.avatar)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
        }
        card.addView(iconView, LinearLayout.LayoutParams(Ui.dp(32), Ui.dp(32)))

        val valueView = Ui.text(this, value, R.style.SectionTitle, 2).apply {
            textSize = 24f
            setTextColor(resources.getColor(R.color.primary, theme))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = Ui.dp(10) }
        }
        card.addView(valueView)

        card.addView(Ui.text(this, label, R.style.FieldLabel, 0))
        return card
    }

    private fun registerCard(icon: String, title: String, subtitle: String, tableKey: String): LinearLayout {
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundResource(R.drawable.list_selector)
            elevation = 2f
            setPadding(Ui.dp(14), Ui.dp(16), Ui.dp(14), Ui.dp(16))
            isClickable = true
            isFocusable = true
            setOnClickListener { openTable(tableKey) }
        }
        val iconView = TextView(this).apply {
            text = icon
            setBackgroundResource(R.drawable.avatar)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(null, Typeface.BOLD)
        }
        card.addView(iconView, LinearLayout.LayoutParams(Ui.dp(40), Ui.dp(40)))

        val titleView = Ui.text(this, title, R.style.FieldValue, 2).apply {
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = Ui.dp(10) }
        }
        card.addView(titleView)

        card.addView(Ui.text(this, subtitle, R.style.FieldLabel, 0))
        return card
    }

    private fun action(title: String, primary: Boolean, tableKey: String): Button = Button(this).apply {
        text = title
        textSize = 11f
        setBackgroundResource(if (primary) R.drawable.btn_primary else R.drawable.btn_secondary)
        setTextColor(if (primary) Color.WHITE else resources.getColor(R.color.primary, theme))
        setPadding(Ui.dp(4), Ui.dp(8), Ui.dp(4), Ui.dp(8))
        setOnClickListener { openAdd(tableKey) }
    }

    private fun rowParams(): LinearLayout.LayoutParams = LinearLayout.LayoutParams(
        0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f
    ).apply { setMargins(Ui.dp(2), 0, Ui.dp(2), 0) }

    private fun ownerName(rawId: String): String {
        return try {
            val owner = db.getRecordById(table("cobstveniki"), rawId.toLong())
            if (owner == null) "—" else owner["LastName"] + " " + owner["FirstName"]
        } catch (e: NumberFormatException) {
            "—"
        }
    }

    private fun money(value: Double): String = String.format(Locale.getDefault(), "%,.2f ₽", value)

    private fun openTable(tableKey: String) {
        val intent = Intent(this, TableActivity::class.java)
        intent.putExtra(TableActivity.EXTRA_TABLE, tableKey)
        startActivity(intent)
    }

    private fun openAdd(tableKey: String) {
        val intent = Intent(this, RecordActivity::class.java)
        intent.putExtra(RecordActivity.EXTRA_TABLE, tableKey)
        startActivity(intent)
    }

    private fun openDetail(tableKey: String, id: Long) {
        val intent = Intent(this, DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_TABLE, tableKey)
        intent.putExtra(DetailActivity.EXTRA_RECORD_ID, id)
        startActivity(intent)
    }

    /** Ключи заданы в TableDefinition, поэтому они всегда известны. */
    private fun table(key: String): TableDefinition = Definitions.byKey(key)!!

    override fun onDestroy() {
        if (::db.isInitialized) db.close()
        super.onDestroy()
    }
}
