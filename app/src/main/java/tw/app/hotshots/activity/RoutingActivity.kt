package tw.app.hotshots.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import tw.app.hotshots.activity.auth.AuthActivity
import tw.app.hotshots.app.database.CheckVersion
import tw.app.hotshots.app.database.CheckVersionListener
import tw.app.hotshots.app.database.models.Version
import tw.app.hotshots.authentication.Authentication
import tw.app.hotshots.authentication.AuthenticationListener
import tw.app.hotshots.authentication.model.HotUser
import tw.app.hotshots.util.Copy
import tw.app.hotshots.util.TimeUtil
import kotlin.system.exitProcess

class RoutingActivity : BaseActivity() {

    private var _authentication: Authentication? = null
    private val authentication get() = _authentication!!

    private var _authenticationListener: AuthenticationListener? = null
    private val authenticationListener get() = _authenticationListener!!

    private var _nextPageIntent: Intent? = null
    private val nextPageIntent get() = _nextPageIntent!!

    private var nextActivity = NEXT_PAGE.AUTH_ACTIVITY

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        splashScreen.setKeepOnScreenCondition { true }

        setup()
        init()
    }

    override fun onDestroy() {
        super.onDestroy()

        _authentication = null
        _authenticationListener = null
        _nextPageIntent = null
    }

    private fun setup() {
        _authenticationListener = object : AuthenticationListener {
            override fun onAlreadyAuthenticated(user: HotUser) {
                super.onAlreadyAuthenticated(user)

                runOnUiThread {
                    if (user.isBanned) {
                        var dialog = MaterialAlertDialogBuilder(this@RoutingActivity)
                            .setTitle("Konto zablokowane!")
                            .setMessage(
                                "Konto zostało zablokowane!\n\nZostanie odblokowane dnia: ${
                                    TimeUtil.convertLongToTime(
                                        user.bannedTo,
                                        "HH:mm dd/MM/yyyy"
                                    )
                                }\nPowód:\n ${user.banReason}\n\nPoczekaj na odblokowanie lub skontaktuj się z administracją na serwerze Discord."
                            )
                            .setPositiveButton(
                                "Zamknij"
                            ) { _, _ -> exitProcess(0) }
                            .create()

                        dialog.setCancelable(false)
                        dialog.setCanceledOnTouchOutside(false)
                        dialog.show()
                    } else {
                        nextActivity = NEXT_PAGE.MAIN_ACTIVITY
                        checkForUpdate()
                    }
                }
            }

            override fun onAuthenticationNeeded() {
                super.onAuthenticationNeeded()

                runOnUiThread {
                    nextActivity = NEXT_PAGE.AUTH_ACTIVITY
                    checkForUpdate()
                }
            }

            override fun onError(exception: Exception) {
                super.onError(exception)

                runOnUiThread {
                    val errorMessage = exception.message ?: "Nieznany błąd"

                    var dialog = MaterialAlertDialogBuilder(this@RoutingActivity)
                        .setTitle("Błąd")
                        .setMessage("Wystąpił błąd podczas sprawdzania statusu autoryzacji użytkownika! Treść błędu:\n $errorMessage")
                        .setPositiveButton(
                            "Zamknij"
                        ) { _, _ -> exitProcess(0) }
                        .setNegativeButton("Skopiuj") { _, _ ->
                            Copy.Text(
                                applicationContext,
                                errorMessage
                            )
                        }
                        .create()

                    dialog.setCancelable(false)
                    dialog.setCanceledOnTouchOutside(false)
                    dialog.show()
                }
            }
        }

        launch {
            _authentication = Authentication(authenticationListener).splashCheck()
        }
    }

    private fun init() {

    }

    private fun checkForUpdate() {
        launch {
            CheckVersion().invoke(object : CheckVersionListener {
                override fun onChecked(isUpdateAvailable: Boolean, newVersion: Version?) {
                    launch {
                        if (isUpdateAvailable) {
                            var message =
                                "Dostępna jest aktualizacja do wersji ${newVersion!!.version} w etapie ${newVersion.state.name}."
                            if (newVersion.required)
                                message =
                                    "$message Ta aktualizacja jest wymagana aby korzystać z aplikacji!"

                            val dialogMaterial = MaterialAlertDialogBuilder(this@RoutingActivity)
                                .setTitle("Aktualizacja")
                                .setMessage(message)
                                .setNegativeButton("Aktualizuj") { _, _ -> redirect(NEXT_PAGE.UPDATE_ACTIVITY) }

                            if (!newVersion.required) {
                                dialogMaterial.setPositiveButton(
                                    "Pomiń"
                                ) { _, _ ->
                                    redirect(nextActivity)
                                }

                                dialogMaterial.setOnDismissListener {
                                    redirect(nextActivity)
                                }

                                dialogMaterial.show()
                            } else {
                                val dialog = dialogMaterial.create()
                                dialog.setCanceledOnTouchOutside(false)
                                dialog.setCancelable(false)
                                dialog.show()
                            }
                        } else {
                            redirect(nextActivity)
                        }
                    }
                }

                override fun onError(exception: Exception) {
                    redirect(nextActivity)
                }
            })
        }
    }

    private fun redirect(nextPage: NEXT_PAGE) {
        _nextPageIntent = when (nextPage) {
            NEXT_PAGE.MAIN_ACTIVITY -> {
                Intent(this@RoutingActivity, MainActivity::class.java)
            }

            NEXT_PAGE.AUTH_ACTIVITY -> {
                Intent(this@RoutingActivity, AuthActivity::class.java)
            }

            NEXT_PAGE.UPDATE_ACTIVITY -> {
                Intent(this@RoutingActivity, UpdateActivity::class.java)
            }
        }

        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        startActivity(nextPageIntent)
        finish()
    }

    private enum class NEXT_PAGE {
        MAIN_ACTIVITY,
        AUTH_ACTIVITY,
        UPDATE_ACTIVITY
    }
}