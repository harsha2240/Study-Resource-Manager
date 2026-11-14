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

Bookmarks and notes are stored in memory for the current app session. Room
persistence is pending.
