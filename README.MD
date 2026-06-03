# MoneyLite - Personal Finance Tracker

**MoneyLite** is a beautiful, offline-first personal finance tracking application built with **Compose Multiplatform (CMP)** and **Clean Architecture**. It is designed for fast transactional entries, sleek Material 3 Expressive UI, and simple monthly budget insights.

---

## 🚀 MVP Features

### 1. 📊 Dashboard
* **Dynamic Monthly Balance**: View current balance, total income, and total expenses for the active month.
* **Monthly Budget Progress**: A visually striking progress indicator showing budget utilization.
* **Recent Transactions**: Lists the 5 most recent transactions of the current month with quick click-to-edit actions.
* **Quick Action FAB**: Add new transactions instantly.

### 2. 🧾 Transactions Ledger
* **Grouped Ledger**: All transactions grouped by date headers.
* **Quick Filters**: Filter between *All*, *Income*, and *Expense* types.
* **Instant Search**: Live, client-side search by notes or description text.
* **Easy Management**: Click transactions to edit them or delete entries directly from the ledger.

### 3. ➕ Add / Edit Transactions
* **Expressive Entry**: Large digit input for transaction amounts.
* **Flexible Config**: Toggle type (Income/Expense), playful grid-based category picker, description text, and date picker (defaults to today).
* **Validation**: Restricts savings if amount $\le 0$ or if no category is selected.
* **Edit Mode**: Intelligently loads transaction values by ID using custom use cases and updates them in-place in the SQLite database.

### 4. 📈 Circular Budget Tracking
* **Progress Analytics**: Circular visual indicator of current monthly expense vs. the set budget limit.
* **Alert States**: Contextual warnings such as **Aman** (Safe), **Hati-Hati** (Warning), and **Overbudget**.

### 5. ⚙️ Settings & Administration
* **Preferences**: Persisted Dark Theme toggle via preferences DataStore.
* **Currency**: Hardcoded default to IDR (Rupiah).
* **Reset Database**: Destructive reset of all transaction data from the SQLite ledger.

---

## 🛠️ Technology Stack & Libraries

* **Framework**: [Compose Multiplatform (CMP) 1.10.0](https://www.jetbrains.com/lp/compose-multiplatform/) - 100% Shared UI for Android & iOS.
* **Language**: [Kotlin 2.3.0](https://kotlinlang.org/) (bundled compiler: `2.2.20`).
* **Database**: [Room 2.8.4](https://developer.android.com/jetpack/androidx/releases/room) - Local, multiplatform SQLite database engine.
* **DI**: [Koin 4.1.1](https://insert-koin.io/) - Dependency Injection with compose-viewmodel binding.
* **Navigation**: [Navigation 3 (androidx.navigation3)](https://developer.android.com/jetpack/compose/navigation) - Core type-safe multiplatform navigation.
* **Key-Value Store**: [Preferences DataStore](https://developer.android.com/topic/libraries/architecture/datastore) - Storage of theme preferences.
* **Datetime**: [Kotlinx-Datetime 0.8.0](https://github.com/Kotlin/kotlinx-datetime) - Modern date-time manipulation integrated directly with standard library `kotlin.time.Clock` and `kotlin.time.Instant`.

---

## 📂 Project Architecture

MoneyLite adheres strictly to **Clean Architecture** and the **MVI (Model-View-Intent)** presentation pattern across multiple modules:

```text
├── androidApp       # Android launcher module (Thin wrapper)
├── iosApp           # Xcode iOS Project (Thin wrapper)
├── sharedUI         # Main app navigation host (Nav3) and global theme setup
├── features/        # Feature presentation modules (Screens, ViewModels, States)
│   ├── home         # Dashboard / Summary Screen
│   ├── transactions # Ledger list and Add/Edit transactions form
│   ├── schedule     # Circular Budget screen
│   └── profile      # Dark mode settings and data reset action
└── core/            # Shared business logic and infrastructures
    ├── domain       # Domain Entities, Use Cases, Repository interfaces
    ├── data         # Room DB builders, Entity schemas, DAOs, Prefs, Repo implementations
    └── common       # DateTime extensions and shared utils
```

---

## 🏗️ Room Database Entities

### 1. Transaction
Represents a financial transaction:
```kotlin
data class Transaction(
    val id: String,
    val type: TransactionType, // Income, Expense
    val amount: Long,
    val categoryId: String,
    val note: String?,
    val date: LocalDate,
    val createdAt: Instant
)
```

### 2. Category
Seeded with 8 standard categories (Salary, Freelance, Food, Shopping, Transport, Utilities, Entertainment, Health):
```kotlin
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val color: String,
    val type: TransactionType
)
```

### 3. Budget
Monthly spending budgets:
```kotlin
data class Budget(
    val id: String,
    val limitAmount: Long,
    val month: Int,
    val year: Int
)
```

---

## ⚡ Build & Compile Commands

To build the debug APK, compile targets, or run tests:

```bash
# Assemble Android Debug APK
./gradlew :androidApp:assembleDebug

# Run Domain Unit Tests
./gradlew :core:domain:desktopTest

# Perform Lint checks
./gradlew lint
```

The compiled Android debug APK is generated at:
`androidApp/build/outputs/apk/debug/androidApp-debug.apk`

---

## 📜 License

Distributed under the **GNU GPLv3** License. See `LICENSE` for details.
