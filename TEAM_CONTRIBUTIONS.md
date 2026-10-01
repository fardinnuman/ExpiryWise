# 👥 Team Contribution and Architectural Responsibility Breakdown of [ExpiryWise](https://github.com/fardinnuman/ExpiryWise)

## 📌 Overview

**ExpiryWise** was developed collaboratively as a desktop-based food inventory and expiry management application. The system was divided into clearly defined technical and functional responsibilities. Each team member was responsible for specific components of the application while contributing to the integration and overall development of the system.

```text
                              ┌──────────────────────────┐
                              │        ExpiryWise        │
                              └────────────┬─────────────┘
                                           │
                    ┌──────────────────────┴──────────────────────┐
                    ▼                                             ▼
      ┌──────────────────────────┐                    ┌─────────────────────────┐
      │  Fardin Bin Aslam Numan  │                    │     Adita Anan Orin     │
      │                          │                    │                         │
      │   Lead Architect & Core  │                    │     Automation & UX     │
      │      Systems Engineer    │                    │         Engineer        │
      ├──────────────────────────┤                    ├─────────────────────────┤
      │ • Project Infrastructure │                    │ • Database Management   │
      │ • Authentication         │                    │ • Food Data Access      │
      │ • Food Model & Service   │                    │ • Shopping List         │
      │ • Application Shell      │                    │ • Expiry Calendar       │
      │ • Dashboard              │                    │ • Notifications         │
      │ • Analytics              │                    │ • Theme Management      │
      │ • Food Dialog & Media    │                    │ • Settings & CSV        │
      └──────────────────────────┘                    └─────────────────────────┘
```

---

## 🧑‍💻 Team Structure & Primary Responsibilities

| Team Member                | Role                                       | Primary Responsibilities                                                                                                                                          |
| -------------------------- | ------------------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Fardin Bin Aslam Numan** | **Lead Architect & Core Systems Engineer** | Project architecture, build configuration, authentication, food models and services, application navigation, dashboard, analytics and food management interface. |
| **Adita Anan Orin**        | **Automation & UX Engineer**               | Database management, food DAO operations, shopping list, expiry calendar, notifications, theme management, settings and CSV export.                              |

---

# 🛠️ Detailed Technical Responsibilities

## Part I - Fardin Bin Aslam Numan

### 1. Project Infrastructure & Build Configuration

**Files:** `pom.xml`

* Configured the Maven project structure and Java 21 environment.
* Integrated JavaFX, SQLite JDBC, JUnit and related project dependencies.
* Configured the JavaFX Maven plugin for application execution.
* Established the initial project structure and development environment.

### 2. Authentication & User Management

**Files:** `User.java`, `UserDAO.java`, `LoginController.java`

* Implemented user registration and login functionality.
* Added SHA-256 password hashing using Java's `MessageDigest`.
* Implemented user authentication using email and password verification.
* Added demo account initialization for application demonstrations.
* Managed active user session information and logout functionality.

### 3. Food Model & Expiry Management

**Files:** `Food.java`, `FoodService.java`

* Developed the food inventory data model.
* Implemented expiry-date calculations using Java's date and time API.
* Developed the following expiry classifications:
  * **Expired**
  * **Expires Today**
  * **Use Soon**
  * **Safe**
* Implemented freshness calculations based on purchase and expiry dates.
* Generated user-friendly expiry information such as *"Expires in 2 days"*.

### 4. Main Application Shell & Navigation

**Files:** `MainController.java`, `main.fxml`

* Developed the main application layout and navigation structure.
* Implemented dynamic loading of application views.
* Added sidebar navigation and active-user context.
* Implemented page transition effects.
* Integrated notification count synchronization across the application.

### 5. Inventory Dashboard

**Files:** `DashboardController.java`, `FoodCard.java`

* Developed the main inventory dashboard.
* Implemented responsive food-card layouts.
* Added live search functionality.
* Implemented category and expiry-status filtering.
* Added sorting options including expiry date, alphabetical order and favorites.
* Integrated inventory data with the food management system.

### 6. Analytics Dashboard

**Files:** `AnalyticsController.java`, `analytics.fxml`

* Developed the inventory analytics interface.
* Implemented inventory health calculations based on expiry status.
* Added JavaFX charts for category distribution and upcoming expirations.
* Implemented summary statistics for inventory monitoring.
* Presented inventory information through visual dashboards.

### 7. Food Management & Image Processing

**Files:** `FoodDialogController.java`, `ImageUtil.java`

* Developed the add/edit food dialog.
* Implemented validation for required fields and date relationships.
* Added support for quantity and storage-location information.
* Implemented food-image upload and local image storage.
* Added image resizing while maintaining aspect ratio.

---

# Part II - Adita Anan Orin

### 1. Database Management

**File:** `DatabaseManager.java`

* Implemented SQLite database connectivity.
* Created the required database tables and schema.
* Added database initialization during application startup.
* Implemented compatibility handling for database schema updates.
* Managed database connections using safe resource-handling practices.

### 2. Food Data Access Layer

**File:** `FoodDAO.java`

* Implemented database operations for food inventory items.
* Added create, read, update and delete operations.
* Used parameterized SQL queries for safer database interaction.
* Handled optional and nullable food attributes.
* Integrated database-generated IDs with application objects.

### 3. Shopping List & Restock Automation

**Files:** `ShoppingListController.java`, `ShoppingListService.java`, `ShoppingItem.java`, `ShoppingListDAO.java`

* Developed the shopping list functionality.
* Implemented automatic identification of expired and soon-to-expire items.
* Added an **Auto-Add Expiring** feature for restocking.
* Prevented duplicate shopping-list entries.
* Implemented checklist functionality and completion tracking.
* Added progress indicators for shopping-list completion.

### 4. Monthly Expiry Calendar

**Files:** `CalendarController.java`, `calendar.fxml`

* Developed the monthly expiry calendar.
* Implemented dynamic calendar generation using Java's date and time API.
* Displayed food items according to their expiry dates.
* Added visual indicators for the current date and expiry events.
* Implemented detailed item inspection for selected dates.

### 5. Expiry Notifications

**Files:** `NotificationsController.java`, `notifications.fxml`

* Developed the expiry notification interface.
* Displayed items requiring attention based on expiry status.
* Added quick actions for:
  * Adding items to the shopping list
  * Marking items as used
  * Editing food information
* Integrated notification counts with the main application interface.

### 6. Theme Management

**Files:** `ThemeManager.java`, `dark.css`, `style.css`

* Implemented application theme management.
* Added Light Mode and Dark Mode support.
* Enabled runtime theme switching without restarting the application.
* Persisted the selected theme using Java Preferences.
* Managed CSS stylesheets across application views and dialogs.

### 7. Settings & CSV Export

**Files:** `SettingsController.java`, `settings.fxml`

* Developed the application settings interface.
* Added user preference and appearance controls.
* Implemented inventory data export to CSV format.
* Added support for exporting inventory information for external use.
* Implemented demo-data population for application demonstrations.

---

# 📁 Source Code Responsibility Matrix

| File / Component                           | Primary Author | Secondary Contributor | Core Responsibility                          |
| ------------------------------------------ | :------------: | :-------------------: | -------------------------------------------- |
| `pom.xml`                                  |   Numan   |           -           | Project configuration and dependencies       |
| `ExpiryWiseApp.java`                       |   Numan   |         Orin         | Application startup and stage initialization |
| `models/User.java`                         |   Numan   |           -           | User data model                              |
| `models/Food.java`                         |   Numan   |         Orin         | Food inventory data model                    |
| `models/ShoppingItem.java`                 |    Orin   |         Numan        | Shopping-list data model                     |
| `database/DatabaseManager.java`            |    Orin   |           -           | SQLite connection and schema management      |
| `database/UserDAO.java`                    |   Numan   |           -           | User authentication and database operations  |
| `database/FoodDAO.java`                    |    Orin   |           -           | Food inventory CRUD operations               |
| `database/ShoppingListDAO.java`            |    Orin   |           -           | Shopping-list database operations            |
| `services/FoodService.java`                |   Numan   |           -           | Expiry and freshness calculations            |
| `services/ShoppingListService.java`        |    Orin   |           -           | Shopping-list business logic                 |
| `controllers/MainController.java`          |   Numan   |           -           | Main application shell and navigation        |
| `controllers/LoginController.java`         |   Numan   |           -           | Login and registration                       |
| `controllers/DashboardController.java`     |   Numan   |           -           | Inventory dashboard and filtering            |
| `controllers/AnalyticsController.java`     |   Numan   |           -           | Inventory analytics and charts               |
| `controllers/FoodDialogController.java`    |   Numan   |         Orin         | Add/edit food interface and validation       |
| `controllers/CalendarController.java`      |    Orin   |           -           | Monthly expiry calendar                      |
| `controllers/NotificationsController.java` |    Orin   |           -           | Expiry notifications and actions             |
| `controllers/ShoppingListController.java`  |    Orin   |           -           | Shopping-list interface and automation       |
| `controllers/SettingsController.java`      |    Orin   |           -           | Settings and CSV export                      |
| `utils/ThemeManager.java`                  |    Orin   |           -           | Runtime theme management                     |
| `utils/ImageUtil.java`                     |   Numan   |           -           | Image resizing and storage                   |
| `css/style.css`                            |   Numan   |         Orin         | Main application styling                     |
| `css/dark.css`                             |    Orin   |           -           | Dark-mode styling                            |
