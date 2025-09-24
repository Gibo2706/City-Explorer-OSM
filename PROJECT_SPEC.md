# CityExplorer OSM — Projektna specifikacija po iteracijama

Ovaj dokument prati šta je urađeno u svakoj iteraciji (koraku).

| Iteracija | Naziv | Fajlovi | Cilj |
|-----------|-------|---------|------|
| Step 1 | Skeleton i build | `settings.gradle`, `build.gradle`, `gradle.properties`, `app/build.gradle`, `proguard-rules.pro`, `AndroidManifest.xml`, `MainActivity.java`, `activity_main.xml` | Postaviti bazni projekat, proveriti build i pokretanje |
| Step 2 | Room (entiteti, DAO, DB) | Entiteti `Place`, `Favorite`, DAO-i, `AppDatabase` | Uvesti lokalnu bazu |
| Step 3 | Retrofit API | `ApiService`, modeli | Uspostaviti komunikaciju sa REST serverom |
| Step 4 | Repository sloj | `PlaceRepository` | Povezivanje Room + Retrofit |
| Step 5 | ContentProvider | `FavoritesProvider` | Deljenje podataka o favoritima |
| Step 6 | WorkManager | `SyncWorker` | Background sinhronizacija |
| Step 7 | SensorManager | `CompassSensorHelper` | Kompas funkcionalnost |
| Step 8 | Notifications | `NotificationHelper` | Lokalne notifikacije |
| Step 9 | UI | `ListFragment`, `DetailFragment`, `MapFragment`, layout-i (phone + tablet) | Vizuelni deo |
| Step 10 | README, TEST_PLAN, DEMO_SCRIPT | Dokumentacija | Spremno za odbranu |
