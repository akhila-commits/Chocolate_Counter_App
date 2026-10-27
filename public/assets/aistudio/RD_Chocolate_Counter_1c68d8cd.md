# Requirements Document (RD) — Chocolate Counter App
For: Revello Speciality Outlet (handcrafted chocolates), Kalutara
Target build tool: Google AI Studio ("Build" app mode, Gemini-powered)

> Note on platform: Google AI Studio's Build feature generates a **web app** (runs in a browser, works fine on an Android phone, can be installed as a PWA) rather than a compiled native `.apk`. This document is written for that web-app build. If a native Android app from the Play Store is required instead, use the Android Studio + Antigravity path discussed earlier — the feature requirements below are the same either way.

## 1. Purpose
Staff at the outlet need a fast way to tally a tray or box of assorted handcrafted chocolates: take one photo, get each chocolate type identified and counted, and see the total price automatically, without manually weighing or counting each piece.

## 2. Users
- Primary: shop staff at the counter, preparing orders or checking stock.
- Secondary: the owner, for setting up products/prices and reviewing scan history.
No customer-facing login is required for v1; assume a single shared device at the counter.

## 3. Goals (v1)
1. Maintain a product catalog of chocolate types with price per piece.
2. Capture a photo of a group of chocolates.
3. Identify each chocolate type present and count how many of each.
4. Calculate and display the total price.
5. Show a short, human-readable summary of what's in the photo.
6. Let staff correct any miscounted item before finalizing.

## 4. Out of Scope (v1)
- Barcode/QR scanning (chocolates are unwrapped/handcrafted, not individually barcoded).
- Multi-user accounts, roles, or login.
- Payments or integration with a POS/till.
- Inventory stock deduction (counting only, not stock management).
- Offline use (requires internet for the Gemini API call).

## 5. Core Features

### 5.1 Product Catalog
- Add / edit / delete a product: name, price (LKR), 1–3 reference photos.
- Reference photos should show the chocolate clearly against a plain background — these are what the AI compares against, so quality here directly affects recognition accuracy.
- Product list view: name, price, thumbnail.
- Validation: name required and unique (case-insensitive), price must be a positive number.

### 5.2 Photo Capture
- Button to open the device camera (via browser `<input capture>` or `getUserMedia`) or upload an existing photo.
- Show a preview before submitting; allow retake.
- Client-side resize/compress the image before sending (long side ≈1200–1600px, JPEG) to keep upload fast and within API limits.

### 5.3 Recognition (Gemini)
- Send the photo together with the current product catalog (names + reference photos) to the Gemini API.
- Ask the model to return **structured JSON only**, e.g.:
  ```json
  {
    "items": [
      { "productName": "Hazelnut Truffle", "count": 5, "confidence": 0.92 }
    ],
    "unrecognizedCount": 1,
    "notes": "one piece partially hidden"
  }
  ```
- Instruct the model to: count only chocolates matching the catalog; never invent a product not in the catalog; count partially visible/overlapping pieces as best as it can rather than skipping them; report anything it can't match under `unrecognizedCount`.
- The app must parse this defensively (missing fields, extra text around the JSON, unknown product names) and never crash on a bad response.

### 5.4 Result & Totals
- Show one row per detected product: name, counted quantity (editable with +/- or direct input), unit price, line total.
- An "Unrecognized items" row if `unrecognizedCount > 0`, with a manual "add as" picker.
- An "add a product staff noticed but AI missed" option.
- **All price math (line totals, grand total) is calculated in app code from catalog prices, never by the AI** — the AI only supplies names and counts.
- Grand total shown prominently.
- Auto-generated summary line, built from the final corrected counts, e.g.: "11 pieces across 3 types. Total: LKR 2,750.00."
- Save / Discard buttons.

### 5.5 Scan History
- List of saved scans: date/time, thumbnail, total.
- Tap to reopen a saved result (read-only).

## 6. Data Model
- **Product**: id, name, price, referenceImageUrls[]
- **ScanRecord**: id, timestamp, photoUrl, items[], total, summary
- **ScanItem** (inside ScanRecord): productName, unitPrice, count, confidence
Store price snapshots on each ScanItem so editing a product's price later doesn't change past scan totals.

## 7. Non-Functional Requirements
- Recognition response should return within ~10 seconds on a normal connection; show a loading state during the call.
- Clear error states: no internet, API failure/timeout, empty catalog (nothing to recognize against), low-confidence results.
- Mobile-first layout (this will mainly be used on a phone/tablet at the counter); large touch targets for count +/- buttons.
- Currency fixed to LKR for v1 (no multi-currency needed).
- No sensitive personal data is collected; photos are of chocolates only.

## 8. Known Limitation
Counting many similar-looking handcrafted pieces from one photo won't be 100% accurate, especially with overlapping or partially hidden pieces. The result screen must always allow correction before saving — this is a requirement, not a nice-to-have.

## 9. Success Criteria for v1
- Staff can add at least 5 real Revello products with reference photos.
- A photo of 8–12 mixed chocolates produces a result that needs only minor manual correction.
- The displayed total matches a manual count/price check exactly after corrections.
- A saved scan can be reopened later from History.

## 10. Open Questions (confirm before/while building)
- Should the app support more than one "batch" open at once (e.g., two trays queued), or is one scan at a time enough for v1?
- Should reference photos be required before a product can be recognized, or is text-only (name) matching acceptable as a fallback?
- Any branding to match (Amore/Revello black-and-gold look) for this internal tool, or is a plain functional UI fine for v1?
