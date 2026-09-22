package com.example.tallyledger.util

import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object HtmlExportHelper {

    fun generateStandaloneHtml(): String {
        // Return a complete single-file standalone HTML + CSS + JS application
        return """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>⚡ Advanced Tally-Style Dashboard</title>
  <script src="https://cdn.jsdelivr.net/npm/browser-image-compression@2.0.2/dist/browser-image-compression.js"></script>
  <style>
    :root {
      --primary: #4f46e5;
      --primary-hover: #4338ca;
      --success: #16a34a;
      --danger: #dc2626;
      --warning: #d97706;
      --bg: #f8fafc;
      --card-bg: #ffffff;
      --border: #e2e8f0;
      --text-main: #0f172a;
      --text-muted: #64748b;
      --dr-color: #2563eb;
      --cr-color: #dc2626;
      --radius: 10px;
    }

    * {
      box-sizing: border-box;
      margin: 0;
      padding: 0;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
    }

    body {
      background-color: var(--bg);
      color: var(--text-main);
      padding: 16px;
      line-height: 1.5;
    }

    .container {
      max-width: 1100px;
      margin: 0 auto;
    }

    header {
      display: flex;
      flex-wrap: wrap;
      justify-content: space-between;
      align-items: center;
      gap: 12px;
      margin-bottom: 20px;
      padding: 16px 20px;
      background: var(--card-bg);
      border-radius: var(--radius);
      border: 1px solid var(--border);
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }

    .brand-title {
      font-size: 1.4rem;
      font-weight: 800;
      color: var(--primary);
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .branch-selector {
      display: flex;
      background: var(--bg);
      padding: 4px;
      border-radius: 8px;
      border: 1px solid var(--border);
    }

    .branch-btn {
      padding: 8px 18px;
      border: none;
      background: transparent;
      font-weight: 600;
      font-size: 0.9rem;
      cursor: pointer;
      border-radius: 6px;
      transition: all 0.2s ease;
      color: var(--text-muted);
    }

    .branch-btn.active {
      background: var(--primary);
      color: #fff;
      box-shadow: 0 2px 4px rgba(79, 70, 229, 0.25);
    }

    .card {
      background: var(--card-bg);
      border-radius: var(--radius);
      border: 1px solid var(--border);
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 1px 3px rgba(0,0,0,0.05);
    }

    .card-title {
      font-size: 1.15rem;
      font-weight: 700;
      margin-bottom: 16px;
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .row {
      display: flex;
      flex-wrap: wrap;
      gap: 12px;
      margin-bottom: 12px;
    }

    .col {
      flex: 1;
      min-width: 200px;
    }

    label {
      display: block;
      font-size: 0.82rem;
      font-weight: 600;
      margin-bottom: 5px;
      color: var(--text-muted);
    }

    input, select, textarea {
      width: 100%;
      padding: 9px 12px;
      border: 1px solid var(--border);
      border-radius: 6px;
      font-size: 0.95rem;
      color: var(--text-main);
      background-color: #fff;
      transition: border-color 0.2s;
    }

    input:focus, select:focus, textarea:focus {
      outline: none;
      border-color: var(--primary);
      box-shadow: 0 0 0 3px rgba(79, 70, 229, 0.1);
    }

    .btn {
      display: inline-flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      padding: 9px 16px;
      border-radius: 6px;
      font-size: 0.9rem;
      font-weight: 600;
      border: none;
      cursor: pointer;
      transition: all 0.2s;
    }

    .btn-primary {
      background: var(--primary);
      color: white;
    }
    .btn-primary:hover {
      background: var(--primary-hover);
    }

    .btn-success {
      background: var(--success);
      color: white;
    }

    .btn-danger {
      background: var(--danger);
      color: white;
    }

    .btn-outline {
      background: transparent;
      border: 1px solid var(--border);
      color: var(--text-main);
    }
    .btn-outline:hover {
      background: var(--bg);
      border-color: #cbd5e1;
    }

    .btn-sm {
      padding: 5px 10px;
      font-size: 0.8rem;
    }

    /* Journal Entry Rows */
    .entry-table {
      width: 100%;
      border-collapse: collapse;
      margin: 14px 0;
    }

    .entry-table th {
      background: var(--bg);
      text-align: left;
      padding: 8px 12px;
      font-size: 0.82rem;
      font-weight: 700;
      color: var(--text-muted);
      border-bottom: 1px solid var(--border);
    }

    .entry-table td {
      padding: 8px 6px;
      border-bottom: 1px solid var(--border);
      vertical-align: middle;
    }

    .type-badge {
      display: inline-block;
      padding: 4px 8px;
      border-radius: 4px;
      font-weight: 700;
      font-size: 0.8rem;
    }
    .badge-dr {
      background: #eff6ff;
      color: var(--dr-color);
      border: 1px solid #bfdbfe;
    }
    .badge-cr {
      background: #fef2f2;
      color: var(--cr-color);
      border: 1px solid #fecaca;
    }

    /* Total Balance Box */
    .balance-box {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;
      border-radius: 8px;
      margin: 16px 0;
      font-weight: 700;
      font-size: 0.95rem;
    }
    .balanced {
      background: #f0fdf4;
      border: 1px solid #bbf7d0;
      color: #15803d;
    }
    .unbalanced {
      background: #fef2f2;
      border: 1px solid #fecaca;
      color: #b91c1c;
    }

    /* Attachments Preview */
    .attachments-container {
      display: flex;
      flex-wrap: wrap;
      gap: 10px;
      margin-top: 10px;
    }

    .attachment-badge {
      display: flex;
      align-items: center;
      gap: 8px;
      background: var(--bg);
      border: 1px solid var(--border);
      border-radius: 6px;
      padding: 6px 10px;
      font-size: 0.82rem;
      position: relative;
    }

    .attachment-img {
      width: 44px;
      height: 44px;
      object-fit: cover;
      border-radius: 4px;
      border: 1px solid var(--border);
      cursor: pointer;
    }

    .remove-att-btn {
      background: #fee2e2;
      color: var(--danger);
      border: none;
      border-radius: 50%;
      width: 20px;
      height: 20px;
      font-size: 12px;
      cursor: pointer;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    /* Voucher Cards */
    .voucher-card {
      background: #ffffff;
      border: 1px solid var(--border);
      border-radius: var(--radius);
      padding: 16px;
      margin-bottom: 12px;
      transition: all 0.2s;
    }
    .voucher-card:hover {
      box-shadow: 0 4px 10px rgba(0,0,0,0.06);
      border-color: #cbd5e1;
    }

    .voucher-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;
      padding-bottom: 10px;
      border-bottom: 1px dashed var(--border);
    }

    .voucher-no {
      font-size: 1.05rem;
      font-weight: 800;
      color: var(--primary);
    }

    .voucher-date {
      font-size: 0.85rem;
      font-weight: 600;
      color: var(--text-muted);
    }

    .voucher-narration {
      font-size: 0.88rem;
      color: #334155;
      background: var(--bg);
      padding: 8px 12px;
      border-radius: 6px;
      margin: 10px 0;
      border-left: 3px solid var(--primary);
    }

    .voucher-actions {
      display: flex;
      gap: 8px;
      justify-content: flex-end;
      margin-top: 12px;
      padding-top: 10px;
      border-top: 1px solid #f1f5f9;
    }

    /* Modal */
    .modal-overlay {
      position: fixed;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(15, 23, 42, 0.6);
      backdrop-filter: blur(2px);
      display: none;
      align-items: center;
      justify-content: center;
      z-index: 1000;
      padding: 16px;
    }

    .modal-box {
      background: #ffffff;
      border-radius: var(--radius);
      width: 100%;
      max-width: 650px;
      max-height: 90vh;
      overflow-y: auto;
      padding: 24px;
      box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
    }

    .modal-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      border-bottom: 1px solid var(--border);
      padding-bottom: 12px;
    }

    /* Print Template */
    @media print {
      body * {
        visibility: hidden;
      }
      #printArea, #printArea * {
        visibility: visible;
      }
      #printArea {
        position: absolute;
        left: 0;
        top: 0;
        width: 100%;
      }
      .no-print {
        display: none !important;
      }
    }
  </style>
</head>
<body>

<div class="container">
  <!-- Header -->
  <header>
    <div class="brand-title">
      <span>⚡</span>
      <span>Tally Multi-Line Ledger</span>
    </div>

    <!-- Branch Selector -->
    <div class="branch-selector">
      <button class="branch-btn active" id="btnBranchNatwa" onclick="switchBranch('natwa')">🏠 Natwa (N)</button>
      <button class="branch-btn" id="btnBranchBasahi" onclick="switchBranch('basahi')">🏢 Basahi (B)</button>
    </div>

    <div style="display: flex; gap: 8px; align-items: center;">
      <button class="btn btn-outline btn-sm" onclick="fetchTransactions()">🔄 Sync Realtime</button>
      <button class="btn btn-outline btn-sm" onclick="openConfigModal()" title="Configure Firebase Realtime Database URL">⚙️ Config</button>
    </div>
  </header>

  <!-- Create Voucher Section -->
  <div class="card">
    <div class="card-title">
      <span>➕ Create Journal Voucher</span>
      <span id="voucherPreviewNo" style="font-size: 0.9rem; color: var(--primary); font-weight: 700;"></span>
    </div>

    <div class="row">
      <div class="col">
        <label>Voucher Date</label>
        <input type="date" id="voucherDate" />
      </div>
      <div class="col">
        <label>Custom Voucher No (Optional)</label>
        <input type="text" id="customVoucherNo" placeholder="Auto-generated" />
      </div>
    </div>

    <!-- Multi-line Journal Entries -->
    <label>Journal Accounts (Debits & Credits)</label>
    <table class="entry-table" id="entriesTable">
      <thead>
        <tr>
          <th style="width: 110px;">Type</th>
          <th>Account Ledger</th>
          <th style="width: 160px;">Amount (₹)</th>
          <th style="width: 40px;"></th>
        </tr>
      </thead>
      <tbody id="entriesBody">
        <!-- Dynamic Entry Rows -->
      </tbody>
    </table>

    <button class="btn btn-outline btn-sm" onclick="addEntryRow()">+ Add Account Row</button>

    <div style="margin-top: 14px;">
      <label>Narration / Transaction Remarks</label>
      <textarea id="voucherNarration" rows="2" placeholder="Enter transaction details, bill references, remarks..."></textarea>
    </div>

    <!-- Photo Attachments Section (Camera & Upload) -->
    <div style="margin-top: 14px;">
      <label>Receipts & Bill Attachments (Click Photo & Upload)</label>
      <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px;">
        <!-- Hidden file inputs -->
        <input type="file" id="cameraInput" accept="image/*" capture="environment" style="display: none;" onchange="handleImageSelected(this, 'create')" />
        <input type="file" id="galleryInput" accept="image/*" style="display: none;" onchange="handleImageSelected(this, 'create')" />

        <button class="btn btn-outline btn-sm" type="button" onclick="document.getElementById('cameraInput').click()">
          📷 Click Photo (Camera)
        </button>
        <button class="btn btn-outline btn-sm" type="button" onclick="document.getElementById('galleryInput').click()">
          📁 Upload Photo (Gallery)
        </button>
      </div>

      <div style="display: flex; gap: 6px;">
        <input type="url" id="manualUrlInput" placeholder="Or paste web image / cloud link..." style="font-size: 0.85rem;" />
        <button class="btn btn-outline btn-sm" type="button" onclick="addManualUrl('create')">Add Link</button>
      </div>

      <!-- Preview container -->
      <div class="attachments-container" id="createAttachmentsPreview"></div>
    </div>

    <!-- Balance Status Box -->
    <div id="balanceSummary" class="balance-box unbalanced">
      <span id="totalDrText">Total Dr: ₹0.00</span>
      <span id="balanceStatusMsg">⚠️ Total Debits must equal Total Credits!</span>
      <span id="totalCrText">Total Cr: ₹0.00</span>
    </div>

    <button class="btn btn-primary" id="btnSaveVoucher" style="width: 100%; padding: 12px; font-size: 1rem;" onclick="saveVoucher()">
      💾 Save Voucher Entry
    </button>
  </div>

  <!-- Search & Filter Controls -->
  <div class="card" style="padding: 14px 20px;">
    <div class="row" style="margin-bottom: 0; align-items: flex-end;">
      <div class="col" style="min-width: 140px;">
        <label>From Date</label>
        <input type="date" id="filterFromDate" onchange="renderVouchers()" />
      </div>
      <div class="col" style="min-width: 140px;">
        <label>To Date</label>
        <input type="date" id="filterToDate" onchange="renderVouchers()" />
      </div>
      <div class="col" style="flex: 2; min-width: 220px;">
        <label>Quick Search (Voucher No, Account, Narration)</label>
        <input type="text" id="searchInput" placeholder="Search entries..." oninput="renderVouchers()" />
      </div>
      <div>
        <button class="btn btn-outline btn-sm" onclick="clearFilters()">Clear</button>
      </div>
    </div>
  </div>

  <!-- Transactions Ledger List -->
  <div class="card">
    <div class="card-title">
      <span>📖 Transaction Ledger (<span id="voucherCount">0</span>)</span>
      <span id="branchLabel" style="font-size: 0.9rem; color: var(--text-muted); font-weight: normal;">Branch: Natwa</span>
    </div>

    <div id="vouchersList">
      <p style="text-align: center; color: var(--text-muted); padding: 24px;">Loading transactions from Firebase...</p>
    </div>
  </div>
</div>

<!-- EDIT VOUCHER MODAL -->
<div class="modal-overlay" id="editModal">
  <div class="modal-box">
    <div class="modal-header">
      <h3 style="font-size: 1.2rem; font-weight: 700; color: var(--primary);">✏️ Edit Journal Voucher</h3>
      <button class="btn btn-outline btn-sm" onclick="closeEditModal()">✕</button>
    </div>

    <input type="hidden" id="editVoucherKey" />
    <input type="hidden" id="editTxnId" />

    <div class="row">
      <div class="col">
        <label>Voucher Date</label>
        <input type="date" id="editDate" />
      </div>
      <div class="col">
        <label>Voucher No</label>
        <input type="text" id="editVoucherNo" />
      </div>
    </div>

    <label>Journal Accounts</label>
    <table class="entry-table">
      <thead>
        <tr>
          <th style="width: 100px;">Type</th>
          <th>Account Ledger</th>
          <th style="width: 150px;">Amount (₹)</th>
          <th style="width: 40px;"></th>
        </tr>
      </thead>
      <tbody id="editEntriesBody"></tbody>
    </table>

    <button class="btn btn-outline btn-sm" onclick="addEditEntryRow()">+ Add Account Row</button>

    <div style="margin-top: 14px;">
      <label>Narration / Remarks</label>
      <textarea id="editNarration" rows="2"></textarea>
    </div>

    <!-- Edit Attachments (Camera & Upload) -->
    <div style="margin-top: 14px;">
      <label>Receipts & Bill Attachments (Click Photo & Upload)</label>
      <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-bottom: 8px;">
        <input type="file" id="editCameraInput" accept="image/*" capture="environment" style="display: none;" onchange="handleImageSelected(this, 'edit')" />
        <input type="file" id="editGalleryInput" accept="image/*" style="display: none;" onchange="handleImageSelected(this, 'edit')" />

        <button class="btn btn-outline btn-sm" type="button" onclick="document.getElementById('editCameraInput').click()">
          📷 Click Photo (Camera)
        </button>
        <button class="btn btn-outline btn-sm" type="button" onclick="document.getElementById('editGalleryInput').click()">
          📁 Upload Photo (Gallery)
        </button>
      </div>

      <div style="display: flex; gap: 6px;">
        <input type="url" id="editManualUrlInput" placeholder="Or paste web image link..." style="font-size: 0.85rem;" />
        <button class="btn btn-outline btn-sm" type="button" onclick="addManualUrl('edit')">Add Link</button>
      </div>

      <div class="attachments-container" id="editAttachmentsPreview"></div>
    </div>

    <div id="editBalanceSummary" class="balance-box unbalanced">
      <span id="editTotalDrText">Total Dr: ₹0.00</span>
      <span id="editBalanceStatusMsg">⚠️ Balanced?</span>
      <span id="editTotalCrText">Total Cr: ₹0.00</span>
    </div>

    <div style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 16px;">
      <button class="btn btn-outline" onclick="closeEditModal()">Cancel</button>
      <button class="btn btn-primary" id="btnUpdateVoucher" onclick="updateVoucher()">Save Changes</button>
    </div>
  </div>
</div>

<!-- PRINT RECEIPT MODAL -->
<div class="modal-overlay" id="printModal">
  <div class="modal-box" style="max-width: 480px;">
    <div class="modal-header no-print">
      <h3 style="font-size: 1.1rem; font-weight: 700;">🖨️ Voucher Thermal Slip</h3>
      <button class="btn btn-outline btn-sm" onclick="closePrintModal()">✕</button>
    </div>

    <div id="printArea" style="background: #fff; padding: 16px; border: 1px dashed #cbd5e1; font-family: monospace; font-size: 13px; line-height: 1.4;">
      <div style="text-align: center; font-weight: bold; font-size: 16px; margin-bottom: 4px;">TALLY LEDGER VOUCHER</div>
      <div style="text-align: center; font-size: 12px; margin-bottom: 12px;" id="slipBranchName">Branch: Natwa</div>
      <div style="border-top: 1px dashed #000; margin: 8px 0;"></div>
      <div>Voucher No: <span id="slipVoucherNo" style="font-weight: bold;"></span></div>
      <div>Txn ID:     <span id="slipTxnId"></span></div>
      <div>Date:       <span id="slipDate"></span></div>
      <div style="border-top: 1px dashed #000; margin: 8px 0;"></div>
      <div style="display: flex; justify-content: space-between; font-weight: bold;">
        <span>Type & Account</span>
        <span>Amount (₹)</span>
      </div>
      <div style="border-top: 1px dashed #000; margin: 8px 0;"></div>
      <div id="slipEntriesList"></div>
      <div style="border-top: 1px dashed #000; margin: 8px 0;"></div>
      <div style="display: flex; justify-content: space-between; font-weight: bold;">
        <span>Total:</span>
        <span id="slipTotalAmount">₹0.00</span>
      </div>
      <div id="slipNarrationBox" style="margin-top: 8px; font-size: 11px;"></div>
      <div style="border-top: 1px dashed #000; margin: 12px 0 6px 0;"></div>
      <div style="text-align: center; font-size: 10px;">Generated securely via Tally Multi-Line Ledger</div>
    </div>

    <div class="no-print" style="display: flex; gap: 10px; justify-content: flex-end; margin-top: 16px;">
      <button class="btn btn-outline" onclick="closePrintModal()">Close</button>
      <button class="btn btn-primary" onclick="window.print()">Print Receipt</button>
    </div>
  </div>
</div>

<!-- FIREBASE CONFIG MODAL -->
<div class="modal-overlay" id="configModal" style="display: none;">
  <div class="modal" style="max-width: 520px;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;">
      <h3 style="margin: 0; font-size: 1.2rem;">⚙️ Firebase Realtime Database Config</h3>
      <button class="btn btn-outline btn-sm" onclick="closeConfigModal()">✕</button>
    </div>
    <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 12px;">
      Enter your Firebase Realtime Database URL to sync live journal vouchers across mobile, desktop, and tablet.
    </p>
    <div class="form-group" style="margin-bottom: 14px;">
      <label style="display: block; margin-bottom: 4px; font-weight: 600; font-size: 0.85rem;">Database URL</label>
      <input type="url" id="cfgDbUrl" style="width: 100%; padding: 8px 10px; border: 1px solid var(--border); border-radius: 6px; box-sizing: border-box;" placeholder="https://your-project-default-rtdb.firebaseio.com" />
    </div>
    <div style="display: flex; gap: 10px; justify-content: flex-end;">
      <button class="btn btn-outline" onclick="resetDefaultConfig()">Reset Default</button>
      <button class="btn btn-primary" onclick="saveDbConfig()">Save & Sync</button>
    </div>
  </div>
</div>

<!-- JAVASCRIPT LOGIC -->
<script>
  const DEFAULT_FIREBASE_BASE_URL = "https://shemacc-3ccac-default-rtdb.asia-southeast1.firebasedatabase.app";
  let FIREBASE_BASE_URL = localStorage.getItem("tally_firebase_db_url") || DEFAULT_FIREBASE_BASE_URL;
  let currentBranch = "natwa";
  let allVouchers = [];

  function openConfigModal() {
    document.getElementById("cfgDbUrl").value = FIREBASE_BASE_URL;
    document.getElementById("configModal").style.display = "flex";
  }

  function closeConfigModal() {
    document.getElementById("configModal").style.display = "none";
  }

  function resetDefaultConfig() {
    FIREBASE_BASE_URL = DEFAULT_FIREBASE_BASE_URL;
    localStorage.removeItem("tally_firebase_db_url");
    document.getElementById("cfgDbUrl").value = FIREBASE_BASE_URL;
    closeConfigModal();
    fetchTransactions();
  }

  function saveDbConfig() {
    let url = document.getElementById("cfgDbUrl").value.trim();
    if (!url) {
      url = DEFAULT_FIREBASE_BASE_URL;
    }
    // Remove trailing slash if present
    if (url.endsWith("/")) {
      url = url.slice(0, -1);
    }
    FIREBASE_BASE_URL = url;
    localStorage.setItem("tally_firebase_db_url", url);
    closeConfigModal();
    alert("Firebase Database URL configured successfully!");
    fetchTransactions();
  }

  // Working state for create form
  let createEntries = [
    { type: "Dr", account: "", amount: "" },
    { type: "Cr", account: "", amount: "" }
  ];
  let createAttachments = [];

  // Working state for edit modal
  let editEntries = [];
  let editAttachments = [];

  // Pre-fill today's date
  document.addEventListener("DOMContentLoaded", () => {
    const today = new Date().toISOString().split("T")[0];
    document.getElementById("voucherDate").value = today;
    renderEntries();
    fetchTransactions();
  });

  function switchBranch(branch) {
    currentBranch = branch;
    document.getElementById("btnBranchNatwa").classList.toggle("active", branch === "natwa");
    document.getElementById("btnBranchBasahi").classList.toggle("active", branch === "basahi");
    document.getElementById("branchLabel").textContent = branch === "natwa" ? "Branch: Natwa (N)" : "Branch: Basahi (B)";
    updateVoucherPreviewNo();
    fetchTransactions();
  }

  // --- Realtime Firebase API calls ---
  async function fetchTransactions() {
    const container = document.getElementById("vouchersList");
    container.innerHTML = '<p style="text-align: center; color: var(--text-muted); padding: 24px;">Loading transactions from Firebase...</p>';

    try {
      const resp = await fetch(`${'$'}{FIREBASE_BASE_URL}/transactions/${'$'}{currentBranch}.json`);
      if (!resp.ok) throw new Error(`HTTP ${'$'}{resp.status}`);
      const data = await resp.json();

      allVouchers = [];
      if (data) {
        for (const [key, val] of Object.entries(data)) {
          if (val && typeof val === 'object') {
            allVouchers.push({
              key: key,
              txn_id: val.txn_id || "",
              voucher_no: val.voucher_no || "",
              date: val.date || "",
              entries: Array.isArray(val.entries) ? val.entries : [],
              narration: val.narration || "",
              billLinks: Array.isArray(val.billLinks) ? val.billLinks : [],
              timestamp: val.timestamp || 0
            });
          }
        }
      }

      // Sort descending by date and voucher_no
      allVouchers.sort((a, b) => (b.date + "_" + b.voucher_no).localeCompare(a.date + "_" + a.voucher_no));
      updateVoucherPreviewNo();
      renderVouchers();
    } catch (err) {
      console.error(err);
      container.innerHTML = `<p style="text-align: center; color: var(--danger); padding: 24px;">Failed to load transactions: ${'$'}{err.message}</p>`;
    }
  }

  function updateVoucherPreviewNo() {
    const prefix = currentBranch === "natwa" ? "N-" : "B-";
    let nextNum = 1;
    if (allVouchers.length > 0) {
      const numbers = allVouchers.map(v => {
        const match = (v.voucher_no || "").match(/\d+/);
        return match ? parseInt(match[0], 10) : 0;
      });
      nextNum = Math.max(0, ...numbers) + 1;
    }
    const generated = `${'$'}{prefix}${'$'}{String(nextNum).padStart(4, '0')}`;
    document.getElementById("voucherPreviewNo").textContent = `Next No: ${'$'}{generated}`;
    return generated;
  }

  // --- Create Form Table Logic ---
  function renderEntries() {
    const tbody = document.getElementById("entriesBody");
    tbody.innerHTML = "";

    createEntries.forEach((entry, idx) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>
          <select onchange="updateEntryField(${'$'}{idx}, 'type', this.value)" style="padding: 6px; font-weight: 700;">
            <option value="Dr" ${'$'}{entry.type === 'Dr' ? 'selected' : ''}>Dr (Debit)</option>
            <option value="Cr" ${'$'}{entry.type === 'Cr' ? 'selected' : ''}>Cr (Credit)</option>
          </select>
        </td>
        <td>
          <input type="text" value="${'$'}{escapeHtml(entry.account)}" placeholder="Account Ledger Name" oninput="updateEntryField(${'$'}{idx}, 'account', this.value)" />
        </td>
        <td>
          <input type="number" step="0.01" value="${'$'}{entry.amount}" placeholder="0.00" oninput="updateEntryField(${'$'}{idx}, 'amount', this.value)" />
        </td>
        <td style="text-align: center;">
          ${'$'}{createEntries.length > 2 ? `<button class="btn btn-outline btn-sm" style="color: var(--danger); border-color: #fee2e2;" onclick="removeEntryRow(${'$'}{idx})">✕</button>` : ''}
        </td>
      `;
      tbody.appendChild(tr);
    });

    calculateBalance();
  }

  function addEntryRow() {
    createEntries.push({ type: "Cr", account: "", amount: "" });
    renderEntries();
  }

  function removeEntryRow(index) {
    if (createEntries.length > 2) {
      createEntries.splice(index, 1);
      renderEntries();
    }
  }

  function updateEntryField(index, field, value) {
    createEntries[index][field] = value;
    if (field === "amount" || field === "type") {
      calculateBalance();
    }
  }

  function calculateBalance() {
    let totalDr = 0;
    let totalCr = 0;

    createEntries.forEach(e => {
      const amt = parseFloat(e.amount) || 0;
      if (e.type === "Dr") totalDr += amt;
      if (e.type === "Cr") totalCr += amt;
    });

    const isBalanced = Math.abs(totalDr - totalCr) < 0.001 && totalDr > 0;
    const box = document.getElementById("balanceSummary");
    box.className = "balance-box " + (isBalanced ? "balanced" : "unbalanced");

    document.getElementById("totalDrText").textContent = `Total Dr: ₹${'$'}{totalDr.toFixed(2)}`;
    document.getElementById("totalCrText").textContent = `Total Cr: ₹${'$'}{totalCr.toFixed(2)}`;
    document.getElementById("balanceStatusMsg").textContent = isBalanced
      ? "✓ Debits & Credits match perfectly!"
      : (totalDr <= 0 ? "⚠️ Amount cannot be zero" : "⚠️ Total Debits must equal Total Credits!");

    document.getElementById("btnSaveVoucher").disabled = !isBalanced;
  }

  // --- Photo Attachments Handler (Camera & Upload) ---
  async function handleImageSelected(input, target) {
    if (!input.files || input.files.length === 0) return;
    const file = input.files[0];

    try {
      // Compress image client side
      const options = {
        maxSizeMB: 0.25,
        maxWidthOrHeight: 1024,
        useWebWorker: true
      };

      let compressedFile = file;
      if (window.imageCompression) {
        compressedFile = await imageCompression(file, options);
      }

      const reader = new FileReader();
      reader.onload = (e) => {
        const base64Url = e.target.result;
        if (target === 'create') {
          createAttachments.push(base64Url);
          renderAttachments('create');
        } else {
          editAttachments.push(base64Url);
          renderAttachments('edit');
        }
      };
      reader.readAsDataURL(compressedFile);
    } catch (error) {
      console.error("Compression error:", error);
      alert("Could not process photo: " + error.message);
    } finally {
      input.value = "";
    }
  }

  function addManualUrl(target) {
    const inputId = target === 'create' ? "manualUrlInput" : "editManualUrlInput";
    const val = document.getElementById(inputId).value.trim();
    if (val) {
      if (target === 'create') {
        createAttachments.push(val);
        renderAttachments('create');
      } else {
        editAttachments.push(val);
        renderAttachments('edit');
      }
      document.getElementById(inputId).value = "";
    }
  }

  function removeAttachment(target, index) {
    if (target === 'create') {
      createAttachments.splice(index, 1);
      renderAttachments('create');
    } else {
      editAttachments.splice(index, 1);
      renderAttachments('edit');
    }
  }

  function renderAttachments(target) {
    const containerId = target === 'create' ? "createAttachmentsPreview" : "editAttachmentsPreview";
    const container = document.getElementById(containerId);
    const list = target === 'create' ? createAttachments : editAttachments;

    container.innerHTML = "";
    list.forEach((url, idx) => {
      const badge = document.createElement("div");
      badge.className = "attachment-badge";

      const isImg = url.startsWith("data:image") || url.startsWith("http");
      badge.innerHTML = `
        ${'$'}{isImg ? `<img src="${'$'}{escapeHtml(url)}" class="attachment-img" onclick="window.open('${'$'}{escapeHtml(url)}')" title="View image" />` : '<span>📄</span>'}
        <span>Doc #${'$'}{idx + 1}</span>
        <button type="button" class="remove-att-btn" onclick="removeAttachment('${'$'}{target}', ${'$'}{idx})">✕</button>
      `;
      container.appendChild(badge);
    });
  }

  // --- Save Voucher ---
  async function saveVoucher() {
    const date = document.getElementById("voucherDate").value;
    const customNo = document.getElementById("customVoucherNo").value.trim();
    const narration = document.getElementById("voucherNarration").value.trim();

    if (!date) {
      alert("Please select a valid date");
      return;
    }

    // Validate entries
    for (const e of createEntries) {
      if (!e.account.trim()) {
        alert("Please enter account names for all journal rows");
        return;
      }
      if ((parseFloat(e.amount) || 0) <= 0) {
        alert("Each journal row amount must be greater than zero");
        return;
      }
    }

    const finalVoucherNo = customNo || updateVoucherPreviewNo();
    const txnId = "TXN" + Date.now().toString().slice(-8);

    const payload = {
      txn_id: txnId,
      voucher_no: finalVoucherNo,
      date: date,
      entries: createEntries.map(e => ({ type: e.type, account: e.account.trim(), amount: parseFloat(e.amount).toFixed(2) })),
      narration: narration,
      billLinks: createAttachments,
      timestamp: Date.now()
    };

    const btn = document.getElementById("btnSaveVoucher");
    btn.disabled = true;
    btn.textContent = "Saving to Firebase...";

    try {
      const resp = await fetch(`${'$'}{FIREBASE_BASE_URL}/transactions/${'$'}{currentBranch}.json`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!resp.ok) throw new Error(`HTTP ${'$'}{resp.status}`);

      // Reset form
      document.getElementById("customVoucherNo").value = "";
      document.getElementById("voucherNarration").value = "";
      createAttachments = [];
      renderAttachments('create');
      createEntries = [
        { type: "Dr", account: "", amount: "" },
        { type: "Cr", account: "", amount: "" }
      ];
      renderEntries();

      await fetchTransactions();
      alert(`Voucher ${'$'}{finalVoucherNo} saved successfully!`);
    } catch (err) {
      console.error(err);
      alert("Failed to save voucher: " + err.message);
    } finally {
      btn.disabled = false;
      btn.textContent = "💾 Save Voucher Entry";
    }
  }

  // --- Render Vouchers List ---
  function renderVouchers() {
    const fromDate = document.getElementById("filterFromDate").value;
    const toDate = document.getElementById("filterToDate").value;
    const search = document.getElementById("searchInput").value.toLowerCase().trim();

    const filtered = allVouchers.filter(v => {
      if (fromDate && v.date < fromDate) return false;
      if (toDate && v.date > toDate) return false;
      if (search) {
        const matchesNo = (v.voucher_no || "").toLowerCase().includes(search);
        const matchesNarration = (v.narration || "").toLowerCase().includes(search);
        const matchesAccounts = v.entries.some(e => (e.account || "").toLowerCase().includes(search));
        if (!matchesNo && !matchesNarration && !matchesAccounts) return false;
      }
      return true;
    });

    document.getElementById("voucherCount").textContent = filtered.length;
    const container = document.getElementById("vouchersList");
    container.innerHTML = "";

    if (filtered.length === 0) {
      container.innerHTML = '<p style="text-align: center; color: var(--text-muted); padding: 24px;">No vouchers match the criteria.</p>';
      return;
    }

    filtered.forEach(v => {
      const totalDr = v.entries.filter(e => e.type === "Dr").reduce((s, e) => s + (parseFloat(e.amount) || 0), 0);
      const card = document.createElement("div");
      card.className = "voucher-card";

      let entriesHtml = v.entries.map(e => `
        <div style="display: flex; justify-content: space-between; padding: 4px 0; font-size: 0.9rem; border-bottom: 1px dotted #f1f5f9;">
          <div>
            <span class="type-badge ${'$'}{e.type === 'Dr' ? 'badge-dr' : 'badge-cr'}">${'$'}{e.type}</span>
            <span style="font-weight: 600; margin-left: 6px;">${'$'}{escapeHtml(e.account)}</span>
          </div>
          <div style="font-weight: 700; color: ${'$'}{e.type === 'Dr' ? 'var(--dr-color)' : 'var(--cr-color)'};">
            ₹${'$'}{parseFloat(e.amount || 0).toFixed(2)}
          </div>
        </div>
      `).join("");

      let attachmentsHtml = "";
      if (v.billLinks && v.billLinks.length > 0) {
        attachmentsHtml = `
          <div style="margin-top: 8px;">
            <span style="font-size: 0.8rem; font-weight: 700; color: var(--text-muted);">Attachments (${'$'}{v.billLinks.length}):</span>
            <div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 4px;">
              ${'$'}{v.billLinks.map((link, idx) => `
                <img src="${'$'}{escapeHtml(link)}" style="width: 44px; height: 44px; object-fit: cover; border-radius: 4px; border: 1px solid var(--border); cursor: pointer;" onclick="window.open('${'$'}{escapeHtml(link)}')" title="Attachment #${'$'}{idx+1}" />
              `).join("")}
            </div>
          </div>
        `;
      }

      card.innerHTML = `
        <div class="voucher-header">
          <div>
            <span class="voucher-no">${'$'}{escapeHtml(v.voucher_no || 'No Voucher #')}</span>
            <span style="font-size: 0.8rem; color: var(--text-muted); margin-left: 8px;">ID: ${'$'}{escapeHtml(v.txn_id)}</span>
          </div>
          <div class="voucher-date">📅 ${'$'}{escapeHtml(v.date)}</div>
        </div>

        <div style="margin: 8px 0;">${'$'}{entriesHtml}</div>

        <div style="display: flex; justify-content: flex-end; font-weight: 800; font-size: 0.95rem; margin-top: 6px;">
          Total: ₹${'$'}{totalDr.toFixed(2)}
        </div>

        ${'$'}{v.narration ? `<div class="voucher-narration"><strong>Narration:</strong> ${'$'}{escapeHtml(v.narration)}</div>` : ''}

        ${'$'}{attachmentsHtml}

        <div class="voucher-actions">
          <button class="btn btn-outline btn-sm" onclick="openPrintModal('${'$'}{v.key}')">🖨️ Receipt Slip</button>
          <button class="btn btn-outline btn-sm" onclick="openEditModal('${'$'}{v.key}')">✏️ Edit</button>
          <button class="btn btn-danger btn-sm" onclick="deleteVoucher('${'$'}{v.key}')">🗑️ Delete</button>
        </div>
      `;
      container.appendChild(card);
    });
  }

  function clearFilters() {
    document.getElementById("filterFromDate").value = "";
    document.getElementById("filterToDate").value = "";
    document.getElementById("searchInput").value = "";
    renderVouchers();
  }

  // --- Edit Modal Logic ---
  function openEditModal(key) {
    const v = allVouchers.find(x => x.key === key);
    if (!v) return;

    document.getElementById("editVoucherKey").value = v.key;
    document.getElementById("editTxnId").value = v.txn_id;
    document.getElementById("editDate").value = v.date;
    document.getElementById("editVoucherNo").value = v.voucher_no;
    document.getElementById("editNarration").value = v.narration || "";

    editEntries = JSON.parse(JSON.stringify(v.entries || []));
    if (editEntries.length < 2) {
      editEntries = [
        { type: "Dr", account: "", amount: "" },
        { type: "Cr", account: "", amount: "" }
      ];
    }
    editAttachments = [...(v.billLinks || [])];

    renderEditEntries();
    renderAttachments('edit');
    document.getElementById("editModal").style.display = "flex";
  }

  function closeEditModal() {
    document.getElementById("editModal").style.display = "none";
  }

  function renderEditEntries() {
    const tbody = document.getElementById("editEntriesBody");
    tbody.innerHTML = "";

    editEntries.forEach((entry, idx) => {
      const tr = document.createElement("tr");
      tr.innerHTML = `
        <td>
          <select onchange="updateEditEntryField(${'$'}{idx}, 'type', this.value)" style="padding: 6px; font-weight: 700;">
            <option value="Dr" ${'$'}{entry.type === 'Dr' ? 'selected' : ''}>Dr (Debit)</option>
            <option value="Cr" ${'$'}{entry.type === 'Cr' ? 'selected' : ''}>Cr (Credit)</option>
          </select>
        </td>
        <td>
          <input type="text" value="${'$'}{escapeHtml(entry.account)}" oninput="updateEditEntryField(${'$'}{idx}, 'account', this.value)" />
        </td>
        <td>
          <input type="number" step="0.01" value="${'$'}{entry.amount}" oninput="updateEditEntryField(${'$'}{idx}, 'amount', this.value)" />
        </td>
        <td style="text-align: center;">
          ${'$'}{editEntries.length > 2 ? `<button class="btn btn-outline btn-sm" style="color: var(--danger); border-color: #fee2e2;" onclick="removeEditEntryRow(${'$'}{idx})">✕</button>` : ''}
        </td>
      `;
      tbody.appendChild(tr);
    });

    calculateEditBalance();
  }

  function addEditEntryRow() {
    editEntries.push({ type: "Cr", account: "", amount: "" });
    renderEditEntries();
  }

  function removeEditEntryRow(index) {
    if (editEntries.length > 2) {
      editEntries.splice(index, 1);
      renderEditEntries();
    }
  }

  function updateEditEntryField(index, field, value) {
    editEntries[index][field] = value;
    if (field === "amount" || field === "type") {
      calculateEditBalance();
    }
  }

  function calculateEditBalance() {
    let totalDr = 0;
    let totalCr = 0;

    editEntries.forEach(e => {
      const amt = parseFloat(e.amount) || 0;
      if (e.type === "Dr") totalDr += amt;
      if (e.type === "Cr") totalCr += amt;
    });

    const isBalanced = Math.abs(totalDr - totalCr) < 0.001 && totalDr > 0;
    const box = document.getElementById("editBalanceSummary");
    box.className = "balance-box " + (isBalanced ? "balanced" : "unbalanced");

    document.getElementById("editTotalDrText").textContent = `Total Dr: ₹${'$'}{totalDr.toFixed(2)}`;
    document.getElementById("editTotalCrText").textContent = `Total Cr: ₹${'$'}{totalCr.toFixed(2)}`;
    document.getElementById("editBalanceStatusMsg").textContent = isBalanced
      ? "✓ Balanced"
      : "⚠️ Debit and Credit must match!";

    document.getElementById("btnUpdateVoucher").disabled = !isBalanced;
  }

  async function updateVoucher() {
    const key = document.getElementById("editVoucherKey").value;
    const date = document.getElementById("editDate").value;
    const voucherNo = document.getElementById("editVoucherNo").value.trim();
    const narration = document.getElementById("editNarration").value.trim();
    const txnId = document.getElementById("editTxnId").value;

    const payload = {
      txn_id: txnId,
      voucher_no: voucherNo,
      date: date,
      entries: editEntries.map(e => ({ type: e.type, account: e.account.trim(), amount: parseFloat(e.amount).toFixed(2) })),
      narration: narration,
      billLinks: editAttachments,
      timestamp: Date.now()
    };

    const btn = document.getElementById("btnUpdateVoucher");
    btn.disabled = true;
    btn.textContent = "Updating...";

    try {
      const resp = await fetch(`${'$'}{FIREBASE_BASE_URL}/transactions/${'$'}{currentBranch}/${'$'}{key}.json`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload)
      });

      if (!resp.ok) throw new Error(`HTTP ${'$'}{resp.status}`);

      closeEditModal();
      await fetchTransactions();
      alert("Voucher updated successfully!");
    } catch (err) {
      console.error(err);
      alert("Failed to update voucher: " + err.message);
    } finally {
      btn.disabled = false;
      btn.textContent = "Save Changes";
    }
  }

  // --- Delete Voucher ---
  async function deleteVoucher(key) {
    if (!confirm("Are you sure you want to delete this voucher?")) return;

    try {
      const resp = await fetch(`${'$'}{FIREBASE_BASE_URL}/transactions/${'$'}{currentBranch}/${'$'}{key}.json`, {
        method: "DELETE"
      });
      if (!resp.ok) throw new Error(`HTTP ${'$'}{resp.status}`);
      await fetchTransactions();
      alert("Voucher deleted successfully!");
    } catch (err) {
      console.error(err);
      alert("Failed to delete voucher: " + err.message);
    }
  }

  // --- Print Thermal Slip ---
  function openPrintModal(key) {
    const v = allVouchers.find(x => x.key === key);
    if (!v) return;

    document.getElementById("slipBranchName").textContent = currentBranch === "natwa" ? "Branch: Natwa" : "Branch: Basahi";
    document.getElementById("slipVoucherNo").textContent = v.voucher_no || "-";
    document.getElementById("slipTxnId").textContent = v.txn_id || "-";
    document.getElementById("slipDate").textContent = v.date || "-";

    const totalDr = v.entries.filter(e => e.type === "Dr").reduce((s, e) => s + (parseFloat(e.amount) || 0), 0);
    document.getElementById("slipTotalAmount").textContent = "₹" + totalDr.toFixed(2);

    const entriesBox = document.getElementById("slipEntriesList");
    entriesBox.innerHTML = v.entries.map(e => `
      <div style="display: flex; justify-content: space-between; margin: 3px 0;">
        <span>[${'$'}{e.type}] ${'$'}{escapeHtml(e.account)}</span>
        <span>₹${'$'}{parseFloat(e.amount || 0).toFixed(2)}</span>
      </div>
    `).join("");

    const narrBox = document.getElementById("slipNarrationBox");
    if (v.narration) {
      narrBox.innerHTML = `<strong>Remarks:</strong> ${'$'}{escapeHtml(v.narration)}`;
    } else {
      narrBox.innerHTML = "";
    }

    document.getElementById("printModal").style.display = "flex";
  }

  function closePrintModal() {
    document.getElementById("printModal").style.display = "none";
  }

  function escapeHtml(text) {
    if (!text) return "";
    return text.toString()
      .replace(/&/g, "&amp;")
      .replace(/</g, "&lt;")
      .replace(/>/g, "&gt;")
      .replace(/"/g, "&quot;")
      .replace(/'/g, "&#039;");
  }
</script>

</body>
</html>
"""
    }

    /**
     * Directly saves index.html into the user's public Downloads folder without opening share dialog
     */
    fun downloadToDownloadsFolder(context: Context): Pair<Boolean, String> {
        val htmlContent = generateStandaloneHtml()
        val filename = "index.html"

        try {
            // Android Q+ (API 29+): Write directly to Downloads via MediaStore
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "text/html")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(htmlContent.toByteArray(Charsets.UTF_8))
                    }
                    return Pair(true, "✅ index.html directly downloaded to Downloads folder!")
                }
            }

            // Fallback for Android 9 and below or if MediaStore insert returned null
            val publicDownloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!publicDownloadsDir.exists()) {
                publicDownloadsDir.mkdirs()
            }
            val destinationFile = File(publicDownloadsDir, filename)
            FileOutputStream(destinationFile).use { fos ->
                fos.write(htmlContent.toByteArray(Charsets.UTF_8))
            }

            // Also register with DownloadManager so it shows up in system downloads manager & notification bar
            try {
                val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager
                dm?.addCompletedDownload(
                    filename,
                    "Tally Multi-Line Ledger standalone web dashboard",
                    true,
                    "text/html",
                    destinationFile.absolutePath,
                    destinationFile.length(),
                    true
                )
            } catch (dmEx: Exception) {
                dmEx.printStackTrace()
            }

            return Pair(true, "✅ index.html downloaded to ${destinationFile.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to app external files dir if permission restriction
            try {
                val appDownloadsDir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                val fallbackFile = File(appDownloadsDir, filename)
                FileOutputStream(fallbackFile).use { fos ->
                    fos.write(htmlContent.toByteArray(Charsets.UTF_8))
                }
                return Pair(true, "✅ Saved to Downloads: ${fallbackFile.name}")
            } catch (fallbackEx: Exception) {
                fallbackEx.printStackTrace()
                return Pair(false, "❌ Download failed: ${e.localizedMessage ?: e.message}")
            }
        }
    }

    /**
     * Optional Share dialog for users who explicitly want to send index.html via WhatsApp, Gmail, etc.
     */
    fun shareIndexHtml(context: Context): String {
        val htmlContent = generateStandaloneHtml()
        val filename = "index.html"

        try {
            val shareDir = File(context.cacheDir, "shared_html")
            if (!shareDir.exists()) {
                shareDir.mkdirs()
            }
            val targetFile = File(shareDir, filename)
            FileOutputStream(targetFile).use { fos ->
                fos.write(htmlContent.toByteArray(Charsets.UTF_8))
            }

            val fileUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                targetFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/html"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "index.html - Tally Multi-Line Ledger")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share index.html").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
            return "Sharing index.html..."
        } catch (e: Exception) {
            e.printStackTrace()
            return "Error sharing: ${e.localizedMessage ?: e.message}"
        }
    }
}
