Cheque Scanner AI - ready Android Studio project (offline ML Kit, XLSX)
=====================================================================

What's included:
- Android_MLKit/ : complete Android Studio project skeleton with:
  - Camera capture (saves image to Downloads/ChequeScanner/images)
  - ML Kit on-device OCR helper (MLKitTextHelper.kt)
  - Editable confirmation dialog so you can correct OCR results before saving
  - Excel (.xlsx) writing via Apache POI to Downloads/ChequeScanner/cheque_records.xlsx
  - View Saved Records screen (reads XLSX and shows brief list)

How to get APK via GitHub Actions (no Android Studio needed):
1. Create a new public GitHub repo named `ChequeScanner` (or any name).
2. Upload the contents of this folder (upload the entire Android_MLKit folder as the repo root content).
3. Add the provided `.github/workflows/build-apk.yml` to the repo so Actions can build the APK.
4. After push, go to your repo -> Actions -> click the latest workflow run -> wait for success -> download artifact `app-release-apk` from the workflow run page.

Important notes:
- The app saves the Excel file to your phone's Downloads/ChequeScanner/cheque_records.xlsx and cheque images to Downloads/ChequeScanner/images/ so you can access them easily.
- After OCR the app shows an editable dialog; correct any mistakes and press Save — the record and the image are saved.
- The APK built with Apache POI may be larger due to the library (~6-8MB added).
