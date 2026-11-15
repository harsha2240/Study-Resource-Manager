# Study Resource Manager

Android mini project for discovering educational resources, saving bookmarks,
and keeping subject-based study notes.

## Android Studio project

Open [android-app](android-app) in Android Studio. It was created with the Empty
Activity (Jetpack Compose) template. The app package is
`com.example.studyresourcemanager`.

Home, Search, Saved, Notes, Settings, and Resource Details each have their own
Android Activity. The app opens these activities with explicit intents.

The app contains Home, Search, Saved, and Notes. Enter a query or select a
subject to search Open Library and Wikipedia. Search starts with a bundled
sample catalogue and can show matching samples if the online search fails.
Results can be opened in the app, bookmarked, or opened in a browser.

Retrofit queries two study resource providers, Open Library for books and
Wikipedia for articles. Glide loads cover images when a provider supplies a
thumbnail URL. If the services are unreachable, use the bundled catalogue to
demonstrate browsing, details, bookmarks, and notes.

Room stores bookmarks and notes. SharedPreferences stores the theme and default
subject. Changing the default subject updates the Search filter immediately.

Resource Details uses two implicit intents: **Open in browser** opens the source
URL using `ACTION_VIEW`, and **Share link** opens the Android share sheet using
`ACTION_SEND`. The app also uses packaged Android resources, including string
resources for those actions and a drawable for the book cover placeholder.

## Build and demonstrate

From `android-app` on Windows:

```powershell
.\gradlew.bat testDebugUnitTest assembleDebug
.\gradlew.bat connectedDebugAndroidTest
```

For the physical device demonstration, select a connected Android phone in
Android Studio and run the app. Search for a topic on a working network, open a
result, and try both external actions. Bookmark the result, create a note, and
relaunch the app to show that both persist. Change the default subject in
Settings and open Search to show that the filter updates immediately.
