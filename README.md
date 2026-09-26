# Manga Studio AI - Android Application

Application Android pour créer des mangas, manhuas et comics personnalisés par IA avec cohérence des personnages, décors et styles artistiques personnalisables.

---

## 🚀 Compilation automatique sur GitHub (CI/CD)

Le projet est configuré avec **GitHub Actions** pour compiler automatiquement l'application et générer un fichier APK téléchargeable à chaque commit ou pull request.

### Fonctionnement du workflow (`.github/workflows/build.yml`)
1. **Déclencheurs** : 
   - À chaque `push` sur la branche `main` ou `master`
   - À chaque `pull request` vers `main` ou `master`
   - Déclenchement manuel via l'onglet **Actions** de votre dépôt GitHub (**Run workflow**)
2. **Environnement** :
   - Runner `ubuntu-latest`
   - **Java 21 (Temurin)** (requis pour Android Gradle Plugin 9.x et Gradle 9.3)
   - Configuration automatique du SDK Android via `android-actions/setup-android`
   - Mise en cache intelligente des dépendances Gradle via `gradle/actions/setup-gradle`
3. **Préparation automatique** :
   - Restauration automatique du keystore de signature debug (`debug.keystore`)
   - Configuration de `.env` depuis `.env.example`
   - Injection automatique de `GEMINI_API_KEY` si configuré dans les **GitHub Secrets**
4. **Artefact** :
   - L'APK Debug compilé (`MangaStudio-Debug-APK`) est généré et mis à disposition en téléchargement direct dans les artefacts du build GitHub Actions.

---

## 🔑 Configuration des secrets GitHub (Optionnel)

Pour utiliser les fonctionnalités d'IA Gemini dans votre build GitHub :
1. Rendez-vous dans les paramètres de votre dépôt GitHub : **Settings > Secrets and variables > Actions**.
2. Créez un nouveau secret nommé **`GEMINI_API_KEY`**.
3. Renseignez votre clé API Google Gemini. Elle sera injectée automatiquement lors de la compilation.

---

## 💻 Compilation locale (sur votre machine)

Pour compiler l'application en local :

```bash
# Rendre le wrapper Gradle exécutable (si sous Linux / macOS)
chmod +x ./gradlew

# Compiler l'APK Debug
./gradlew assembleDebug
```

L'APK sera généré dans : `app/build/outputs/apk/debug/app-debug.apk`.
