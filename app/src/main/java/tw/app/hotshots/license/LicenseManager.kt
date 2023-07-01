package tw.app.hotshots.license

import tw.app.hotshots.BuildConfig
import tw.app.hotshots.R
import tw.app.hotshots.license.model.License

class LicenseManager {
    private var licenseList: ArrayList<License> = arrayListOf()

    init {
        val app = License(
            name = "HotShots",
            author = "Anonymous",
            url = "https://github.com/",
            version = BuildConfig.VERSION_NAME,
            shortDescription = "Aplikacja HotShots",
            icon = R.mipmap.ic_launcher
        )

        val exoplayer = License(
            name = "ExoPlayer",
            author = "Google",
            url = "https://github.com/google/ExoPlayer",
            version = "2.18.7",
            shortDescription = "ExoPlayer is an application level media player for Android.",
            icon = R.drawable.exoplayer_icon
        )

        val firebaseAuth = License(
            name = "Firebase Auth",
            author = "Google",
            url = "https://firebase.google.com/",
            version = com.google.firebase.auth.ktx.BuildConfig.VERSION_NAME,
            shortDescription = "Firebase Authentication provides backend services, easy-to-use SDKs, and ready-made UI libraries to authenticate users to your app.",
            icon = R.drawable.firebase_icon
        )

        val firebaseFirestore = License(
            name = "Firebase Firestore",
            author = "Google",
            url = "https://firebase.google.com/",
            version = com.google.firebase.firestore.BuildConfig.VERSION_NAME,
            shortDescription = "Cloud Firestore is a flexible, scalable database for mobile, web, and server development from Firebase and Google Cloud.",
            icon = R.drawable.firebase_icon
        )

        val firebaseStorage = License(
            name = "Firebase Storage",
            author = "Google",
            url = "https://firebase.google.com/",
            version = com.google.firebase.storage.BuildConfig.VERSION_NAME,
            shortDescription = "Cloud Storage for Firebase is a powerful, simple, and cost-effective object storage service built for Google scale.",
            icon = R.drawable.firebase_icon
        )

        val firebaseCrashlytics = License(
            name = "Firebase Crashlytics",
            author = "Google",
            url = "https://firebase.google.com/",
            version = com.google.firebase.crashlytics.BuildConfig.VERSION_NAME,
            shortDescription = "Firebase Crashlytics is a lightweight, realtime crash reporter that helps you track, prioritize, and fix stability issues that erode your app quality.",
            icon = R.drawable.firebase_icon
        )

        val firebaseAnalytics = License(
            name = "Firebase Analytics",
            author = "Google",
            url = "https://firebase.google.com/",
            version = "21.2.2",
            shortDescription = "Google Analytics is an app measurement solution, available at no charge, that provides insight on app usage and user engagement.",
            icon = R.drawable.firebase_icon
        )

        licenseList.add(exoplayer)
        licenseList.add(firebaseAuth)
        licenseList.add(firebaseFirestore)
        licenseList.add(firebaseStorage)
        licenseList.add(firebaseCrashlytics)
        licenseList.add(firebaseAnalytics)
        licenseList.sortBy { licenseItemSort: License -> licenseItemSort.name }

        licenseList.reverse()
        licenseList.add(app)
        licenseList.reverse()
    }

    public fun getLicenses(): ArrayList<License> {
        return licenseList
    }
}