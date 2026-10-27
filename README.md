[README.md](https://github.com/user-attachments/files/34014764/README.md)
# Chocolate Order Calculator

**One-line repo description (for the GitHub "About" field):**
> Android app for browsing a chocolate shop's product catalog and calculating order totals on the spot.

---

## Overview
Chocolate Order Calculator is a native Android app built for **Revello Speciality Outlet**, a handcrafted chocolate shop in Kalutara, Sri Lanka. Counter staff search or browse the product catalog, tap to add items to an order, and see the running total instantly — no manual price lookups, no cash register needed.

## Features
- 🔍 **Search & filter** — live search bar plus category filter chips (Chocolate Pieces, Beverages, Slabs, Gift Boxes, Assortment Packs, Cakes, Savory Items, Ice Cream).
- ➕ **One-tap ordering** — add a product to the current order directly from its catalog card.
- 🧮 **Live order calculator** — a persistent order panel shows quantity, line totals, and a running grand total as items are added or adjusted.
- 🗂️ **Catalog management** — add, edit, and delete products (name, category, size, price); products sharing a name but differing in size/weight are kept as distinct entries.
- 🧼 **Simple, uncluttered UI** — one list, a search bar, filter chips — designed to be fast to use during service.

## Tech Stack
- Kotlin, Jetpack Compose, Material 3
- Room (local storage)
- MVVM, Hilt, Coroutines/Flow

## Status
🚧 In development — single-device, local-only app (no backend, no network calls required). See `RD_Chocolate_Counter.md` for the full requirements document.

## Setup
1. Clone the repo and open it in Android Studio.
2. Build and run on an emulator or device (minSdk 26).

## License
MIT © 2026 MARK-TECH — see [LICENSE](LICENSE).
