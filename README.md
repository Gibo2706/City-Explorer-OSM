# CityExplorer OSM

## 📖 Opis

**CityExplorer OSM** je Android aplikacija razvijena u **Javi** sa XML
layout-ima, koja omogućava korisnicima istraživanje grada kroz
interaktivnu mapu baziranu na **OpenStreetMap (osmdroid)**.\
Aplikacija integriše ključne Android koncepte kao što su: - **Room
Database** -- lokalno skladištenje podataka i offline režim -
**Retrofit** -- komunikacija sa REST servisima - **ContentProvider** --
deljenje omiljenih lokacija - **WorkManager** -- pozadinska
sinhronizacija podataka - **SensorManager (kompas)** -- rad sa senzorima
uređaja - **Notifications** -- obaveštavanje korisnika - **Runtime
permissions** -- kontrola pristupa resursima - **Responsive layout-i**
-- optimizovani za telefon i tablet (`layout` + `layout-sw600dp`)

## 🛠️ Tehnologije

-   **Jezik**: Java (UI u XML)
-   **Build system**: Gradle 8.1.4 (AGP 8.0.2, Groovy DSL)
-   **minSdk**: 26 (Android 8.0 Oreo)
-   **targetSdk**: 33
-   **Arhitektura**: MVVM + Repository pattern

## 📂 Struktura paketa

    com.example.cityexplorer
     ├── data
     │    ├── local        # Room entiteti, DAO, Database
     │    └── remote       # Retrofit API interfejsi
     ├── repo              # Repository sloj (spajanje remote + local)
     ├── provider          # ContentProvider implementacija
     ├── sync              # WorkManager worker-i
     ├── notifications     # Notification helper klase
     ├── util              # Pomoćne klase i helper funkcije
     └── ui                # Activity, Fragmenti, ViewModel-i

## 🚀 Build i pokretanje

1.  **Instaliraj JDK 17**

    ``` bash
    java -version
    ```

    Očekivani izlaz:

        openjdk version "17"

2.  **Build projekta**

    ``` bash
    ./gradlew assembleDebug
    ```

3.  **Instalacija na emulator ili uređaj**

    ``` bash
    adb install -r app/build/outputs/apk/debug/app-debug.apk
    ```

## 📱 Emulator

-   Kreiraj AVD sa Android 8.0 (API 26) ili novijim.\
-   Za tablet layout koristi konfiguraciju `sw600dp` u emulatoru.

## 🎨 Dizajn

-   **Light/Dark tema** podržane kroz `styles.xml`
-   **Material Components** za UI elemente
-   Responsive grid/list prikazi u zavisnosti od veličine ekrana

------------------------------------------------------------------------

© 2025 Bogdan Marković -- *CityExplorer OSM*
