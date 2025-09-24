# CityExplorer OSM

## Opis
Android aplikacija koja koristi **OpenStreetMap (osmdroid)** za prikaz lokacija i omogućava istraživanje grada.  
Implementirani su ključni Android koncepti: Room, Retrofit, ContentProvider, WorkManager, SensorManager (kompas), Notifications, runtime permissions, različiti layout-ovi.

## Tehnologije
- **Jezik**: Java (UI u XML)
- **Build system**: Gradle 8.1.4 (AGP 8.0.2)
- **minSdk**: 26 (Android 8.0)
- **targetSdk**: 33
- **Arhitektura**: MVVM + Repository

## Struktura paketa
com.example.cityexplorer
 ├── data
 │    ├── local (Room entiteti, DAO, DB)
 │    └── remote (Retrofit API)
 ├── repo (Repository sloj)
 ├── provider (ContentProvider)
 ├── sync (WorkManager worker)
 ├── notifications
 ├── util
 └── ui (Activity, Fragmenti, ViewModel-i)

## Build i pokretanje
1. Instaliraj JDK 17  
   java -version

   Rezultat treba da sadrži `openjdk version "17"`.

2. Build:  
   ./gradlew assembleDebug

3. Instaliraj na emulator ili uređaj:  
   adb install -r app/build/outputs/apk/debug/app-debug.apk

## Emulator
- Kreiraj AVD sa Android 8.0 ili novijim (API 26+).  
- Za tablet layout koristi `sw600dp` konfiguraciju u emulatoru.  

## Sledeći koraci
- Step 2: Room baza (entiteti, DAO, DB setup).
