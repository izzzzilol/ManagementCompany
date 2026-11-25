package com.example.managementcompany

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.managementcompany.TableDefinition as Definitions
import com.example.managementcompany.TableDefinition.Column
import com.example.managementcompany.TableDefinition.TableDefinition
import kotlin.math.min
import kotlin.math.roundToInt

/** Единое современное оформление экранов и структурированных карточек. */
object Ui {

    /** Все размеры в коде задаются в dp — иначе на плотных экранах отступы исчезают. */
    @JvmStatic
    fun dp(value: Int): Int =
        (value * Resources.getSystem().displayMetrics.density).roundToInt()

    @JvmStatic
    fun header(activity: Activity, title: String?, subtitle: String?, showBack: Boolean) {
        activity.findViewById<TextView>(R.id.header_title)?.text = title

        activity.findViewById<TextView>(R.id.header_subtitle)?.apply {
            if (subtitle.isNullOrEmpty()) {
                visibility = View.GONE
            } else {
                text = subtitle
                visibility = View.VISIBLE
            }
        }

        activity.findViewById<View>(R.id.header_back)?.apply {
            if (showBack) {
                visibility = View.VISIBLE
                // Нажатие «назад» закрывает текущий экран.
                setOnClickListener { activity.finish() }
            } else {
                visibility = View.GONE
            }
        }
    }

    @JvmStatic
    fun text(context: Context, value: String, styleRes: Int, bottomMarginDp: Int): TextView =
        TextView(context).apply {
            text = value
            setTextAppearance(styleRes)
            setPadding(dp(2), dp(2), dp(2), dp(2))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { setMargins(0, 0, 0, dp(bottomMarginDp)) }
        }

    @JvmStatic
    fun card(context: Context): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setBackgroundResource(R.drawable.card)
        elevation = dp(2).toFloat()
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        ).apply { setMargins(0, 0, 0, dp(14)) }
        setPadding(dp(18), dp(16), dp(18), dp(16))
    }

    @JvmStatic
    fun row(context: Context, container: LinearLayout, label: String, value: String?) {
        container.addView(TextView(context).apply {
            text = label
            setTextAppearance(R.style.FieldLabel)
            setPadding(dp(2), dp(6), dp(2), 0)
        })

        container.addView(TextView(context).apply {
            text = if (value.isNullOrEmpty()) "—" else value
            setTextAppearance(R.style.FieldValue)
            setPadding(dp(2), dp(2), dp(2), dp(10))
        })
    }

    @JvmStatic
    fun iconFor(def: TableDefinition?): String = when (def?.key) {
        "houses" -> "Д"
        "kvartira" -> "Кв"
        "cobstveniki" -> "С"
        "yslygi" -> "У"
        "nachislenie" -> "Н"
        "zaivki" -> "З"
        "rashod" -> "Р"
        else -> "•"
    }

    /** Карточка записи: заголовок с иконкой и аккуратные строки «подпись — значение». */
    @JvmStatic
    fun recordCard(
        context: Context,
        db: AppRepository,
        def: TableDefinition,
        record: Record,
        maxFields: Int,
    ): LinearLayout {
        val card = card(context)

        val head = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(2), 0, dp(10))
        }
        card.addView(head)

        head.addView(TextView(context).apply {
            text = iconFor(def)
            setBackgroundResource(R.drawable.avatar)
            gravity = Gravity.CENTER
            setTextColor(Color.WHITE)
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
        }, LinearLayout.LayoutParams(dp(40), dp(40)))

        head.addView(TextView(context).apply {
            text = record.summary(def)
            setTextAppearance(R.style.FieldValue)
            setTypeface(null, Typeface.BOLD)
            setPadding(dp(12), 0, dp(4), 0)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
        }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

        val total = def.columns.size
        val limit = if (maxFields < 0) total else min(maxFields, total)
        def.columns.take(limit).forEach { column ->
            row(context, card, column.label, resolve(db, record, column))
        }
        if (limit < total) {
            card.addView(TextView(context).apply {
                text = "… ещё ${total - limit}"
                setTextAppearance(R.style.FieldLabel)
                setPadding(dp(2), dp(4), dp(2), dp(2))
            })
        }
        return card
    }

    /** Компактная строка для списка последних заявок на главном экране. */
    @JvmStatic
    fun miniCard(context: Context, title: String, status: String, icon: String): LinearLayout =
        card(context).apply {
            isClickable = true
            isFocusable = true
            setBackgroundResource(R.drawable.list_selector)

            val head = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(2), dp(4), dp(2), dp(4))
            }
            addView(head)

            head.addView(TextView(context).apply {
                text = icon
                setBackgroundResource(R.drawable.avatar)
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                textSize = 13f
            }, LinearLayout.LayoutParams(dp(36), dp(36)))

            head.addView(TextView(context).apply {
                text = title
                setTextAppearance(R.style.FieldValue)
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                setPadding(dp(10), 0, dp(8), 0)
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))

            head.addView(TextView(context).apply {
                text = status
                setBackgroundResource(R.drawable.chip)
                setTextColor(Color.parseColor("#5B5BD6"))
                textSize = 11f
                setPadding(dp(10), dp(5), dp(10), dp(5))
            })
        }

    @JvmStatic
    fun chip(context: Context, text: String, bgRes: Int, textColor: Int): TextView =
        TextView(context).apply {
            this.text = text
            setBackgroundResource(bgRes)
            setTextColor(textColor)
            textSize = 12f
            setPadding(dp(12), dp(6), dp(12), dp(6))
        }

    /** Подпись внешнего ключа берём из справочника; нечисловой Id оставляем как есть. */
    private fun resolve(db: AppRepository, record: Record, column: Column): String {
        val raw = record[column.name]
        if (column.fkTable != null && raw.isNotEmpty()) {
            val refDef = Definitions.byTableName(column.fkTable)
            if (refDef != null) {
                val id = raw.toLongOrNull() ?: return raw
                val ref = db.getRecordById(refDef, id)
                if (ref != null) return ref.summary(refDef)
            }
        }
        return raw
    }
}
