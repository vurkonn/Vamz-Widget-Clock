# Clock Widget Studio

Proyek Android native (Kotlin) untuk widget jam homescreen bergaya referensi.

## Fitur awal
- Dua gaya: Outline + Solid dan Segmented iOS.
- Background: transparan, ungu iOS, hitam, putih.
- Jam 24 jam dan tanggal lokal.
- Widget memakai bitmap hasil render Canvas agar desain tidak dibatasi komponen RemoteViews standar.

## Build APK
1. Buka folder proyek ini di Android Studio (versi yang mendukung Android Gradle Plugin 8.6.1).
2. Tunggu Gradle Sync selesai dan pastikan Android SDK Platform 35 terpasang.
3. Pilih **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. APK debug akan muncul di `app/build/outputs/apk/debug/app-debug.apk`.

## Pasang widget
1. Install APK di perangkat Android.
2. Buka aplikasi, pilih model dan background, lalu tekan **Simpan & Perbarui Widget**.
3. Tekan lama homescreen > Widgets > Clock Widget Studio > tarik widget ke homescreen.

## Catatan
- Proyek ini adalah source project, bukan APK yang sudah dikompilasi. Lingkungan pembuatan di sesi ini tidak memiliki Android SDK/Gradle, jadi APK belum dapat dibuild di sini.
- Refresh menit menggunakan alarm inexact; Android dapat menunda update saat mode hemat baterai/Doze.
- Renderer awal menggunakan font sistem dan bentuk segmented buatan sendiri. Font custom persis seperti gambar bisa ditambahkan sebagai file TTF berlisensi pada iterasi berikutnya.


## Build khusus lewat GitHub Actions (tanpa build lokal di HP)
1. Buat repository GitHub baru.
2. Upload seluruh isi folder proyek ini ke repository (pastikan folder `.github/workflows/` ikut ter-upload).
3. Buka tab **Actions** dan pilih workflow **Build Android APK**.
4. Tekan **Run workflow**. Workflow juga berjalan otomatis saat push ke branch `main`.
5. Setelah selesai, buka hasil run dan unduh artifact **ClockWidgetStudio-debug-apk**.
6. Ekstrak artifact ZIP; file di dalamnya adalah `app-debug.apk`, lalu install di Android.

Build menggunakan JDK 17 dan Gradle 8.7 di runner GitHub Actions. APK yang dihasilkan adalah debug APK untuk pengujian, belum APK release bertanda tangan untuk distribusi publik.
