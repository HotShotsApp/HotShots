package tw.app.hotshots.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import tw.app.hotshots.R
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.posts.user.UserSingleton
import tw.app.hotshots.database.user.UserDatabase
import tw.app.hotshots.database.user.UserDatabaseListener
import tw.app.hotshots.ui.loading.LoadingDialog
import tw.app.hotshots.ui.settings.ChangePasswordDialog
import tw.app.hotshots.ui.settings.PasswordChangeListener
import tw.app.hotshots.ui.settings.PinSetupDialog
import tw.app.hotshots.ui.settings.PinSetupListener
import kotlin.coroutines.CoroutineContext

class SettingsFragment : PreferenceFragmentCompat(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private lateinit var userDatabase: UserDatabase

    private lateinit var loadingDialog: LoadingDialog

    private lateinit var pinLockPref: SwitchPreferenceCompat
    private lateinit var logout: Preference
    private lateinit var passwordChangePref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_screen)

        setup()
    }

    private fun setup() {
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.build()

        userDatabase = UserDatabase(object : UserDatabaseListener {
            override fun onUpdated(user: User) {
                requireActivity().runOnUiThread {
                    UserSingleton.instance?.user = user
                    validate(user)
                    loadingDialog.dismiss()
                }
            }

            override fun onError(exception: Exception) {
                requireActivity().runOnUiThread {
                    loadingDialog.dismiss()
                    Toast.makeText(context, exception.message.toString(), Toast.LENGTH_LONG).show()
                }
            }
        })

        pinLockPref = findPreference("isPinEnabled")!!
        passwordChangePref = findPreference("passwordChange")!!
        logout = findPreference("logoutButton")!!

        var passwordDialog =
            ChangePasswordDialog(requireContext(), object : PasswordChangeListener {
                override fun onChanged(password: String) {
                    loadingDialog.show()
                    launch {
                        FirebaseAuth.getInstance().currentUser?.updatePassword(password)!!
                            .addOnSuccessListener {
                                requireActivity().runOnUiThread {
                                    loadingDialog.dismiss()
                                }
                            }
                            .addOnFailureListener {
                                requireActivity().runOnUiThread {
                                    loadingDialog.dismiss()
                                    Toast.makeText(requireContext(), it.message.toString(), Toast.LENGTH_LONG).show()
                                }
                            }.await()
                    }
                }
            })
        passwordChangePref.setOnPreferenceClickListener {
            passwordDialog.show()

            false
        }

        validate(UserSingleton.instance?.user!!)

        pinLockPref.setOnPreferenceChangeListener { preference, newValue ->
            val value = newValue as Boolean

            if (value) {
                var pinDialog = PinSetupDialog(requireContext(), object : PinSetupListener {
                    override fun onSaved(otp: String) {
                        loadingDialog.show()
                        launch {
                            userDatabase.changePin(otp)
                        }
                    }
                })

                pinDialog.show()
            } else {
                loadingDialog.show()
                launch {
                    userDatabase.togglePin(false)
                }
            }

            false
        }

        logout.setOnPreferenceClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Wyloguj się")
                .setMessage("Czy na pewno chcesz się wylogować?")
                .setPositiveButton("Wyloguj") {_, _ ->
                    FirebaseAuth.getInstance().signOut()

                    val intent = Intent(activity, AuthActivity::class.java)
                    startActivity(intent)
                    activity?.finishAffinity()
                }
                .setNegativeButton("Anuluj", null)
                .create()
                .show()

            false
        }
    }

    private fun validate(user: User) {
        pinLockPref.isChecked = user.isPinEnabled
    }
}