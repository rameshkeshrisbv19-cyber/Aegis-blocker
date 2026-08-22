package com.aegis.appblocker.schedule

import android.content.Context
import org.json.JSONObject

/** Tiny persisted snapshot of the last-known blocked target ids -> labels, for diffing. */
object LastState {
    private const val PREF = "aegis_last_state"
    private const val KEY = "blocked_map"

    fun save(context: Context, map: Map<Long, String>) {
        val json = JSONObject()
        map.forEach { (k, v) -> json.put(k.toString(), v) }
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit().putString(KEY, json.toString()).apply()
    }

    fun load(context: Context): Set<Long> = readMap(context).keys

    fun labelFor(context: Context, id: Long): String? = readMap(context)[id]

    private fun readMap(context: Context): Map<Long, String> {
        val raw = context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY, null) ?: return emptyMap()
        return try {
            val json = JSONObject(raw)
            buildMap { json.keys().forEach { put(it.toLong(), json.getString(it)) } }
        } catch (e: Exception) { emptyMap() }
    }
}
