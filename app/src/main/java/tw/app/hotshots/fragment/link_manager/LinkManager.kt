package tw.app.hotshots.fragment.link_manager

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import tw.app.hotshots.fragment.link_manager.model.Link
import tw.app.hotshots.util.FileUtil

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
        edit.putString(LINKS, "").commit()
    }

    fun isBackupAvailable(): Boolean {
        return FileUtil.isLinkBackupAvailable(context)
    }

    fun removeBackup() {
        FileUtil.removeLinkBackupFile(context)
    }

    suspend fun getLinks(listener: GetLinksListener) {
        if (getString(LINKS).isNotBlank())
            linksList = Gson().fromJson(getString(LINKS), tokenType)

        listener.onReceived(linksList)
    }

    fun backupWrite(listener: FileUtil.OnWriteToFileListener) {
        FileUtil.writeLinkBackupToFile(
            getString(LINKS),
            context,
            listener
        )
    }

    fun backupRestore() {
        var listenerFile = object : FileUtil.OnReadFromFileListener {
            override fun onSuccess(content: String) {
                setString(LINKS, content)
                Toast.makeText(context, "Pomyślnie przywrócono!", Toast.LENGTH_LONG).show()
            }

            override fun onError(reason: String) {
                Toast.makeText(context, reason, Toast.LENGTH_LONG).show()
            }
        }

        FileUtil.readLinkBackupFromFile(
            context,
            listenerFile
        )
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

    private fun setString(key: String, value: String) {
        edit.putString(key, value).commit()
    }
}

interface GetLinksListener {
    fun onReceived(links: MutableList<Link>)
}

interface LinkManagerListener {
    fun onAdded(link: Link, position: Int) {}

    fun onModified(link: Link, position: Int) {}

    fun onRemoved(link: Link, position: Int) {}

    fun onSaved() {}
}