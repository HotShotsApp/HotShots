package tw.app.hotshots.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
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
import tw.app.hotshots.authentication.Biometric
import tw.app.hotshots.authentication.BiometricStatusListener
import tw.app.hotshots.authentication.model.User
import tw.app.hotshots.database.notifications.SendNotification
import tw.app.hotshots.database.user.UserSingleton
import tw.app.hotshots.database.user.UserDatabase
import tw.app.hotshots.database.user.UserDatabaseListener
import tw.app.hotshots.ui.loading.LoadingDialog
import tw.app.hotshots.ui.settings.ChangePasswordDialog
import tw.app.hotshots.ui.settings.PasswordChangeListener
import tw.app.hotshots.ui.settings.PinSetupDialog
import tw.app.hotshots.ui.settings.PinSetupListener
import tw.app.hotshots.util.TimeUtil
import kotlin.coroutines.CoroutineContext

class SettingsFragment : PreferenceFragmentCompat(), CoroutineScope {
    /* ------------------------------------------ */
    private var job: Job = Job()

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.IO + job
    /* ------------------------------------------ */

    private lateinit var userDatabase: UserDatabase

    private lateinit var loadingDialog: LoadingDialog

    private var _biometric: Biometric? = null
    private val biometric get() = _biometric!!
    private var isBiometricSupported = false

    private lateinit var pinLockPref: SwitchPreferenceCompat
    private lateinit var logout: Preference
    private lateinit var passwordChangePref: Preference
    private lateinit var banTestPref: Preference
    private lateinit var biometricPref: SwitchPreferenceCompat
    private lateinit var testNotificationPref: Preference
    private lateinit var testRejectNotificationPref: Preference

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.settings_screen)

        setup()
    }

    private fun setup() {
        loadingDialog = LoadingDialog(requireContext())
        loadingDialog.build()

        pinLockPref = findPreference("isPinEnabled")!!
        passwordChangePref = findPreference("passwordChange")!!
        logout = findPreference("logoutButton")!!
        banTestPref = findPreference("banTestButton")!!
        biometricPref = findPreference("isBiometricEnabled")!!
        testNotificationPref = findPreference("testNotificationButton")!!
        testRejectNotificationPref = findPreference("testRejectNotificationButton")!!

        _biometric = Biometric(requireActivity(), requireContext())
        biometric.checkBiometricStatus(object : BiometricStatusListener {
            override fun onStatusPositive() {
                isBiometricSupported = true
            }

            override fun onError(reason: String) {
                isBiometricSupported = false
                biometricPref.summary = reason
            }
        })

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
                        if (isBiometricSupported)
                            biometricPref.isEnabled = true

                        loadingDialog.show()
                        launch {
                            userDatabase.changePin(otp)
                        }
                    }
                })

                pinDialog.show()
            } else {
                pinLockPref.isEnabled = false
                Settings(requireContext()).setBiometricEnabled(false)
                loadingDialog.show()
                launch {
                    userDatabase.togglePin(false)
                }
            }

            false
        }

        // TODO: Make settings as variable so we can set this as null on view destroy, and add strings to strings.xml
        logout.setOnPreferenceClickListener {
            val dialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle("Wyloguj się")
                .setMessage("Czy na pewno chcesz się wylogować?\nTwoje ustawienia zostaną zresetowane!")
                .setPositiveButton("Wyloguj") {_, _ ->
                    FirebaseAuth.getInstance().signOut()

                    Settings(requireContext()).resetSettings()
                    val intent = Intent(activity, AuthActivity::class.java)
                    startActivity(intent)
                    activity?.finishAffinity()
                }
                .setNegativeButton("Anuluj", null)
                .create()
                .show()

            false
        }

        banTestPref.setOnPreferenceClickListener {
            val user = UserSingleton.instance?.user!!
            user.isBanned = true
            user.bannedTo = TimeUtil.currentTimeToLong() + TimeUtil.TEN_SECONDS
            user.banReason = "Ban Testowy"

            launch {
                UserDatabase(object : UserDatabaseListener {
                    override fun onUpdated(user: User) {
                        requireActivity().finishAffinity()
                    }

                    override fun onError(exception: java.lang.Exception) {
                        Toast.makeText(requireContext(), exception.message, Toast.LENGTH_LONG).show()
                    }
                }).saveUser(user)
            }

            false
        }

        testNotificationPref.setOnPreferenceClickListener {
            launch {
                SendNotification
                    .send(
                        UserSingleton.instance?.user!!.uid,
                        SendNotification.fastDefaultNotification("Witaj!", "Witaj w HotShots!"),
                        object : SendNotification.OnNotificationSendListener {
                            override fun onSent() {
                                requireActivity().finish()
                            }

                            override fun onError(reason: String) {
                                Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG).show()
                            }
                        }
                    )
            }

            false
        }

        testRejectNotificationPref.setOnPreferenceClickListener {
            launch {
                SendNotification.SendInfluDeclinedNotification(
                    UserSingleton.instance?.user?.uid!!,
                    "Test",
                    "Testowe powiadomienie!\n\nDecyzja:\nModerator (Test)",
                    object : SendNotification.OnNotificationSendListener {
                        override fun onSent() {
                            requireActivity().finish()
                        }

                        override fun onError(reason: String) {
                            Toast.makeText(requireContext(), reason, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                )
            }

            false
        }
    }

    private fun validate(user: User) {
        pinLockPref.isChecked = user.isPinEnabled

        if (user.isPinEnabled) {
            if (isBiometricSupported)
                biometricPref.isEnabled = true
        }
    }
}