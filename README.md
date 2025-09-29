# CityExplorer OSM 🗺️

![Platform](https://img.shields.io/badge/Platform-Android-green.svg)
![API](https://img.shields.io/badge/API-26%2B-brightgreen.svg)
![License](https://img.shields.io/badge/License-MIT-blue.svg)
![Build](https://img.shields.io/badge/Build-Passing-success.svg)

**CityExplorer OSM** je napredna Android aplikacija za istraživanje urbanih destinacija kroz interaktivnu mapu sa gamifikovanim sistemom nagrada. Aplikacija koristi **OpenStreetMap** za preciznu navigaciju i implementira moderne Android standarde za komercijalni kvalitet korisničkog iskustva.

---

## ✨ Ključne Funkcionalnosti

### 🎯 **Core Features**
- **Interaktivna mapa** - OpenStreetMap integracija sa custom markerima
- **Gamifikacija** - Sistem poena, bedževa i leaderboard-a
- **Multi-modal verifikacija** - GPS tracking, QR skeniranje, dwell time
- **Offline-first arhitektura** - Room database sa Firebase sync
- **Smart onboarding** - Multi-step setup sa fallback opcijama
- **Push notifikacije** - Firebase Cloud Messaging integracija

### 🏆 **Advanced Features**
- **Real-time leaderboard** - Firestore-powered ranking sistem
- **Profile management** - Firebase Authentication sa user sync
- **Background sync** - WorkManager periodic data synchronization
- **Permission UX** - Proaktivno objašnjavanje dozvola
- **Limited mode** - Fallback funkcionalnost bez lokacije
- **Content sharing** - ContentProvider za omiljene lokacije

---

## 🛠️ Tehnologije & Arhitektura

### **Tech Stack**
```
Platform: Android Native (Java + XML)
Architecture: Clean Architecture + MVVM
Database: Room (SQLite) + Firebase Firestore
Networking: Retrofit 2 + OkHttp
Maps: OpenStreetMap (osmdroid)
Dependency Injection: Hilt (Dagger)
Background Work: WorkManager
Authentication: Firebase Auth
Push Notifications: Firebase Cloud Messaging
Image Loading: Glide
QR Scanning: ZXing
UI Framework: Material Design 3
Testing: JUnit + Espresso
```

### **Arhitektura Slojeva**
```
┌─────────────────────────────────────────┐
│                    UI                   │
│  Activities │ Fragments │ ViewModels    │
├─────────────────────────────────────────┤
│                 Domain                  │
│   Use Cases  │  Models  │  Repository   │
│              │ Interfaces              │
├─────────────────────────────────────────┤
│                  Data                   │
│ Room │ Retrofit │ Firebase │ Prefs     │
├─────────────────────────────────────────┤
│               Framework                 │
│ Android SDK │ OSM │ WorkManager │ Hilt │
└─────────────────────────────────────────┘
```

---

## 📂 Struktura Projekta

```
pmf.rma.cityexplorerosm/
├── 🏗️  di/                    # Dependency Injection
│   ├── AppModule.java
│   ├── AuthModule.java
│   └── FirebaseModule.java
│
├── 💾  data/                  # Data Layer
│   ├── local/
│   │   ├── entities/         # Room entities
│   │   ├── dao/             # Data Access Objects  
│   │   └── db/              # Database setup
│   ├── remote/              # Network layer
│   │   ├── model/           # API DTOs
│   │   └── ApiService.java
│   └── repo/                # Repository implementations
│
├── 🎯  domain/               # Business Logic
│   ├── model/               # Domain models
│   └── usecase/             # Business use cases
│
├── 🔧  sync/                 # Background Sync
│   ├── FirebaseSyncManager.java
│   └── PeriodicFirebaseSyncWorker.java
│
├── 🔐  auth/                 # Authentication
│   ├── AuthManager.java
│   └── UserAccountRepository.java
│
├── 📱  ui/                   # Presentation Layer
│   ├── onboarding/          # First-run experience
│   ├── auth/                # Login/Register
│   ├── map/                 # Main map interface
│   ├── detail/              # Place details & verification
│   ├── profile/             # User profile & badges
│   ├── leaderboard/         # Global rankings
│   └── base/                # Base classes
│
├── 🔔  notifications/        # Push & Local notifications
├── 📡  fcm/                  # Firebase Cloud Messaging
├── 📍  location/             # Location services
├── 📊  sensors/              # Device sensors
├── 🔄  provider/             # Content providers
└── 🛠️  util/                # Utilities & helpers
```

---

## 🚀 Quick Start

### **Preduslovi**
- **JDK 17+** (Oracle ili OpenJDK)
- **Android Studio Hedgehog** (2023.1.1) ili noviji
- **Android SDK 26+** (Android 8.0 Oreo)
- **Firebase project** sa Firestore i FCM

### **Setup**

1. **Clone repository**
   ```bash
   git clone https://github.com/your-org/cityexplorer-osm.git
   cd cityexplorer-osm
   ```

2. **Firebase konfiguracija**
   ```bash
   # Preuzmi google-services.json iz Firebase Console
   # Kopiraj u app/ direktorijum
   cp ~/Downloads/google-services.json app/
   ```

3. **Build & Run**
   ```bash
   ./gradlew assembleDebug
   ./gradlew installDebug
   ```

### **Development Build**
```bash
# Debug build sa logging-om
./gradlew assembleDebug

# Release build (optimizovan)
./gradlew assembleRelease

# Unit testovi
./gradlew testDebugUnitTest

# UI testovi
./gradlew connectedDebugAndroidTest
```

---

## 🎨 UI/UX Design System

### **Material Design 3**
- **Dynamic Color** - Adaptive theming
- **Motion** - Smooth transitions sa SharedElement
- **Typography** - Roboto font scale
- **Spacing** - 4dp grid system
- **Elevation** - Surface tonal variations

### **Responsive Design**
```xml
<!-- Phone (default) -->
res/layout/

<!-- Tablet (600dp+) -->  
res/layout-sw600dp/

<!-- Dark theme -->
res/values-night/

<!-- Landscape -->
res/layout-land/
```

### **Accessibility**
- **TalkBack** podrška
- **Minimum touch targets** (48dp)
- **Color contrast** WCAG AA compliant
- **Screen reader** labels
- **Focus navigation** optimizovano

---

## 🔧 Napredne Funkcije

### **Offline-First Strategy**
```java
// Automatski sync na network reconnect
@HiltWorker
public class PeriodicFirebaseSyncWorker extends Worker {
    // Sync svaki sat kada ima internet
    // Fallback na lokalne Room podatke
}
```

### **Smart Permissions**
```java
// Proaktivni onboarding umesto "just-in-time" 
OnboardingActivity -> Multi-step explanation
Limited Mode -> Fallback bez lokacije
```

### **Gamifikacija**
- **Points system** - 10 poena po verifikovanoj poseti  
- **Badge unlocking** - Progressive achievement system
- **Leaderboard** - Real-time Firestore rankings
- **Social features** - Profile sharing

### **Verification Methods**
```java
GPS_DWELL      // Ostani 30s u krugu 50m
QR_CODE        // Skeniraj jedinstveni kod  
PHOTO_PROOF    // AI-powered image recognition
AUTO_CHECK     // Javna mesta bez verifikacije
```

---

## 🧪 Testing Strategy

### **Unit Tests**
```bash
# Repository layer testovi
./gradlew :app:testDebugUnitTest --tests="*Repository*"

# ViewModel testovi  
./gradlew :app:testDebugUnitTest --tests="*ViewModel*"

# Use Case testovi
./gradlew :app:testDebugUnitTest --tests="*UseCase*"
```

### **Integration Tests**
```bash
# Database migracije
./gradlew :app:testDebugUnitTest --tests="*Migration*"

# API layer testovi
./gradlew :app:testDebugUnitTest --tests="*ApiService*"
```

### **UI Tests**
```bash
# End-to-end flow testovi
./gradlew connectedDebugAndroidTest
```

---

## 🔐 Security & Privacy

### **Data Protection**
- **EncryptedSharedPreferences** za sensitive data
- **Certificate pinning** za API komunikaciju
- **Proguard/R8** obfuscation za release
- **Firebase Firestore Rules** server-side validation

### **GDPR Compliance**
```java
// User consent management
AuthManager.setAnalyticsConsent(boolean)
AuthManager.setMarketingConsent(boolean)

// Data portability
UserRepository.exportUserData()
UserRepository.deleteAllUserData()
```

### **Firestore Security Rules**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null 
        && request.auth.uid == userId;
    }
    
    match /users/{userId}/visits/{visitId} {
      allow read, write: if request.auth != null 
        && request.auth.uid == userId
        && isValidVisitData(resource.data);
    }
  }
}
```

---

## 📊 Performance Optimizations

### **Memory Management**
- **ViewBinding** umesto findViewById
- **DiffUtil** za RecyclerView updates
- **Glide** sa memorijski efikasno image caching
- **Lifecycle-aware** komponente

### **Network Optimizations**
- **OkHttp connection pooling**
- **Response caching** strategije
- **Request deduplication**
- **Retrofit call adapters** sa RxJava/Coroutines

### **Database Performance**
```java
// Room optimizacije
@Entity(indices = {@Index("userId"), @Index("placeId")})
@Query("SELECT * FROM visits WHERE userId = :uid AND status = 'VERIFIED'")
```

---

## 🚀 Deployment

### **Release Checklist**
- [ ] Proguard rules ažurirane
- [ ] Firebase project konfigurisan  
- [ ] Google Play Console setup
- [ ] Signing key generisan i backup napravljen
- [ ] Crash reporting (Crashlytics) aktiviran
- [ ] Performance monitoring setup
- [ ] Store listing kreiran (ikone, screenshots)

### **CI/CD Pipeline**
```yaml
# GitHub Actions example
name: Android Build
on: [push, pull_request]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Setup JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: 17
    - name: Build with Gradle
      run: ./gradlew assembleDebug
    - name: Run tests
      run: ./gradlew testDebugUnitTest
```

---

## 📈 Analytics & Monitoring

### **Crash Reporting**
```java
// Firebase Crashlytics integration
FirebaseCrashlytics.getInstance()
    .recordException(new Exception("Custom error"));
```

### **Performance Monitoring**  
```java  
// Custom performance traces
Trace customTrace = FirebasePerformance.getInstance()
    .newTrace("map_load_time");
customTrace.start();
// ... map loading logic
customTrace.stop();
```

### **User Analytics**
```java
// Privacy-compliant event tracking  
if (authManager.hasAnalyticsConsent()) {
    FirebaseAnalytics.getInstance(this)
        .logEvent("place_verified", bundle);
}
```

---

## 🤝 Contributing

### **Development Workflow**
```bash
# Feature development
git checkout -b feature/new-verification-method
git commit -m "feat: add photo verification with ML Kit"
git push origin feature/new-verification-method
# Create PR with detailed description
```

### **Code Style**
- **Google Java Style Guide** compliance
- **SonarLint** za code quality
- **ktlint** za Kotlin code (ako dodaš Kotlin komponente)
- **Spotless** za automatsko formatiranje

### **Pull Request Template**
```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature  
- [ ] Breaking change
- [ ] Documentation update

## Testing
- [ ] Unit tests pass
- [ ] Integration tests pass
- [ ] Manual testing completed

## Checklist
- [ ] Code follows style guidelines
- [ ] Self-review completed
- [ ] Documentation updated
```

---

## 📄 License & Legal

```
MIT License

Copyright (c) 2025 Bogdan Marković

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
```

---

## 🆘 Support & Community

- **📖 Documentation**: [Wiki](https://github.com/your-org/cityexplorer-osm/wiki)
- **🐛 Bug Reports**: [Issues](https://github.com/your-org/cityexplorer-osm/issues)
- **💡 Feature Requests**: [Discussions](https://github.com/your-org/cityexplorer-osm/discussions)
- **📧 Contact**: bogdan.markovic@example.com
- **🐦 Twitter**: [@BogdanDev](https://twitter.com/BogdanDev)

---

⭐ **Star this repo if you find it useful!**

*Built with ❤️ using Android Native Development*
