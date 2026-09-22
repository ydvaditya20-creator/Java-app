# Tally Ledger (Android)

A modern Android application built with Kotlin, Jetpack Compose, and Material Design 3, providing a Tally-style multi-line journal voucher ledger and accounting dashboard.

## Features

- **Branch Management**: Toggle effortlessly between **Natwa (🏠)** and **Basahi (🏢)** branch ledgers.
- **Smart Voucher Auto-Numbering**: Generates compliant voucher numbers (`N-YY-0001` / `B-YY-0001`) and millisecond-accurate transaction identifiers (`TXN-HHMMSS-mmm`).
- **Double Entry Journal Vouchers**: Multi-line entry grid supporting Debit (Dr) and Credit (Cr) allocations, real-time balance calculations, and validation requiring debits to equal credits.
- **Autocomplete Suggestions**: Auto-suggests existing ledger account names and historic narrations as you type.
- **Date Range Filters**: Filter journal records by custom date ranges.
- **Voucher Actions**:
  - **Inline / Dialog Editing**: Modify accounts, amounts, dates, and attachments with live balance verification.
  - **Deletion**: Remove records from both cloud and local cache.
  - **Print / Share Receipt**: Generate standardized Journal Voucher Receipts ready for system sharing and printing.
- **Real-Time Cloud & Offline Sync**: Connects to Firebase Realtime Database with local persistence fallback.

## GitHub Actions Automated APK Build

This project is pre-configured with a GitHub Actions workflow (`.github/workflows/build-apk.yml`) to automatically build and provide an installable APK on every push or manual trigger.

### How to use on GitHub:
1. Push this project to your GitHub repository:
   ```bash
   git add .
   git commit -m "Initial commit with GitHub Actions APK workflow"
   git push origin main
   ```
2. Go to your repository on GitHub.
3. Click on the **Actions** tab.
4. Select **Build Android APK** workflow and view progress or trigger it manually via **Run workflow**.
5. Once the build finishes, download the generated **`app-debug-apk`** artifact from the workflow run summary page.

