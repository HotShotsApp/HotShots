package tw.app.hotshots.fragment.link_manager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tw.app.hotshots.fragment.link_manager.model.Link

class LinkManager(
    private val context: Context,
    private val listener: LinkManagerListener
) {
    private var preferences: SharedPreferences =
        context.getSharedPreferences("linkManager", MODE_PRIVATE)
    private var edit: SharedPreferences.Editor = preferences.edit()
    private var linksList: MutableList<Link> = arrayListOf()

    private val tokenType = object : TypeToken<MutableList<Link>>() {}.type

    private val LINKS = "links"

    fun addLink(link: Link) {
        linksList.add(0, link)
        save()

        listener.onAdded(link, 0)
    }

    fun modifyLink(link: Link) {
        var position = 0

        val thread = Thread {
            for (linkInList in linksList) {
                if (linkInList.id == link.id) {
                    linksList[position] = link
                    break
                } else {
                    position++
                }
            }

            listener.onModified(link, position)

            save()
        }.start()
    }

    fun removeLink(link: Link) {
        var position = 0

        val thread = Thread {
            for (linkInList in linksList) {
                if (linkInList.id == link.id) {
                    linksList.removeAt(position)
                    break
                } else {
                    position++
                }
            }

            listener.onRemoved(link, position)

            save()
        }.start()
    }

    fun removeAll() {
        edit.putString(LINKS, "").apply()
        save()
    }

    fun getLinks(): MutableList<Link> {
        if (getString(LINKS).isNotBlank())
            linksList = Gson().fromJson(getString(LINKS), tokenType)

        return linksList
    }

    private fun sort() {
        linksList.sortBy { it.createdAt }
    }

    private fun save() {
        edit.putString(LINKS, Gson().toJson(linksList)).apply()
    }

    private fun getString(key: String): String {
        return preferences.getString(key, "").toString()
    }
}

interface LinkManagerListener {
    fun onAdded(link: Link, position: Int) {}

    fun onModified(link: Link, position: Int) {}

    fun onRemoved(link: Link, position: Int) {}

    fun onSaved() {}
}