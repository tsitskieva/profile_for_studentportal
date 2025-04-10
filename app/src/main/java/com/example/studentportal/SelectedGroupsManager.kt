package com.example.studentportal

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object SelectedGroupsManager {
    private const val PREFS_NAME = "SelectedGroupsPrefs"
    private const val SELECTED_GROUPS_KEY = "selected_groups"
    private const val ACTIVE_GROUP_KEY = "active_group"

    fun saveSelectedGroups(context: Context, groups: List<group>) {
        val json = Gson().toJson(groups)
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(SELECTED_GROUPS_KEY, json)
            .apply()

        // Сохраняем активную группу
        val activeGroup = groups.find { it.isActive }
        activeGroup?.let {
            saveActiveGroup(context, it.direction, it.group)
        } ?: run {
            // Если нет активной группы, но есть группы, делаем первую активной
            if (groups.isNotEmpty()) {
                saveActiveGroup(context, groups[0].direction, groups[0].group)
            }
        }
    }

    fun getSelectedGroups(context: Context): List<group> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(SELECTED_GROUPS_KEY, null)
        return if (json != null) {
            val type = object : TypeToken<List<group>>() {}.type
            val groups = Gson().fromJson<List<group>>(json, type)

            // Восстанавливаем активную группу
            val activeGroup = getActiveGroup(context)
            groups.forEach {
                it.isActive = (it.direction == activeGroup?.first && it.group == activeGroup.second)
            }

            // Если нет активной группы, но есть группы, делаем первую активной
            if (groups.isNotEmpty() && !groups.any { it.isActive }) {
                groups[0].isActive = true
            }

            groups.sortedByDescending { it.isActive }
        } else {
            emptyList()
        }
    }

    private fun saveActiveGroup(context: Context, direction: String, group: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(ACTIVE_GROUP_KEY, "$direction|$group")
            .apply()
    }

    private fun getActiveGroup(context: Context): Pair<String, String>? {
        val value = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(ACTIVE_GROUP_KEY, null)
        return value?.split("|")?.let {
            if (it.size == 2) Pair(it[0], it[1]) else null
        }
    }
}