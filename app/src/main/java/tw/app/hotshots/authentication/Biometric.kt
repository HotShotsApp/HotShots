package tw.app.hotshots.authentication

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

class Biometric(
    var fragment: FragmentActivity,
    var context: Context
) {
    private var biometric: BiometricManager = BiometricManager.from(fragment)

    fun authenticate(listener: BiometricListener) {
        var executor = ContextCompat.getMainExecutor(context)

        var biometricPrompt = BiometricPrompt(fragment, executor, object :
            BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                listener.onError(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                listener.onAuthenticate()
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                listener.onFailed()
            }
        })

        var promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Autoryzacja")
            .setSubtitle("Użyj biometri by odblokować aplikacje!")
            .setNegativeButtonText("Wpisz PIN")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun checkBiometricStatus(listener: BiometricStatusListener) {
        when (biometric.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                listener.onStatusPositive()
            }

            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                listener.onError("Zaktualizuj urządzenie, aby otrzymać aktualizacje bezpieczeństwa i używać biometri!")
            }

            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                listener.onError("Wymagane dodatkowe pozwolenie.\nCzekaj na aktualizacje aplikacji!")
            }

            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                listener.onError("Funkcje biometryczne są obecnie niedostępne!")
            }

            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                listener.onError("Urządzenie nie posiada opcji do uwierzytelniania biometrycznego!")
            }

            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> {
                listener.onError("Nieznany status funkcji biometrycznych!")
            }

            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                listener.onError("Funkcje biometryczne nie są wspierane na tym urządzeniu!")
            }
        }
    }
}

interface BiometricListener {
    fun onAuthenticate()

    fun onFailed()

    fun onError(errorCode: Int, errorMessage: String)
}

interface BiometricStatusListener {
    fun onStatusPositive()

    fun onError(reason: String)
}