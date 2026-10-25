# Vaastu Care - Property Care Management System (Java Swing + JDBC + MySQL)

Desktop app for property owners, service providers and an admin. The Swing screens are unchanged;
data now lives in a MySQL database. Passwords are stored as PBKDF2 hashes, SQL uses PreparedStatement,
and role permissions are enforced in `AppController`, not only by hiding buttons.

## Windows setup (step by step)

Run every command from the project folder (the one that contains `src`, `scripts` and `pom.xml`).
In PowerShell put `.\` in front of script names, e.g. `.\scripts\build.bat`.

### 1. Install JDK 25 or newer
Install a Java 25 LTS JDK (for example Eclipse Temurin 25). Check:
    java -version
    javac -version
Both must work. A JRE alone is not enough.

### 2. Install and start MySQL
1. Download **MySQL Installer for Windows** from mysql.com, choose **MySQL Server 8.x** (Server only is enough).
2. During setup choose "Use Strong Password Encryption", set a **root password** and remember it.
3. Keep the default port 3306 and "Start the MySQL Server at System Startup".
4. Check it is running: Start menu -> **Services** -> `MySQL80` should say *Running*.

### 3. Configure the database connection
    copy config\db.properties.example config\db.properties
Open `config\db.properties` in Notepad and replace `CHANGE_ME` with your MySQL root password
(or use another MySQL user). The file is not shared or committed. Instead of the file you can set the
environment variables `VAASTU_DB_URL`, `VAASTU_DB_USER`, `VAASTU_DB_PASSWORD`.

### 4. Download the MySQL driver (one time)
    mkdir lib
    Invoke-WebRequest https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/8.4.0/mysql-connector-j-8.4.0.jar -OutFile lib\mysql-connector-j-8.4.0.jar
(PowerShell command. Or download that jar in a browser and put it in a folder named `lib`.)

### 5. Compile
    scripts\build.bat
You should see `Build OK.`

### 6. Create the database, tables and sample data
    scripts\setup-db.bat
This runs `database\schema.sql` and adds the sample accounts. It only adds sample data when the database
is empty. To wipe everything and start again: `scripts\setup-db.bat --reset` (deletes all data!).

Manual alternative: open **MySQL 8.0 Command Line Client** and run
`source C:/path/to/vaastu-care/database/schema.sql;` then run `scripts\setup-db.bat` for the sample data.

### 7. Run the application
    scripts\run.bat

### 8. Log in with the sample accounts
| Role             | Username   | Password     |
|------------------|------------|--------------|
| Owner            | owner      | owner123     |
| Service Provider | provider   | provider123  |
| Admin            | admin      | admin123     |

More owners: meera, rajesh, sneha, kiran. More providers: safeguard, nightwatch, aquafix, voltcare, heritage,
sparklepro, pipemasters (approved) and eyespy, trustywatch (pending approval). All use password `demo123`.
These are demo accounts only; change or delete them before any real use. Passwords are stored hashed.

### 9. Check that data survives a restart
1. Log in as `owner`, open **My Properties**, click **+ Add property**, save "Test Villa".
2. Close the window completely, run `scripts\run.bat` again, log in as `owner`: "Test Villa" is still there.
3. Optional: in the MySQL Command Line Client run
   `USE vaastu_care; SELECT property_id, name FROM properties;` and `SELECT user_id, username, LEFT(password_hash, 20) FROM users;`

### Maven alternative (if you have Maven installed)
    mvn package
    java -cp target\vaastu-care.jar com.vaastucare.db.DbSetup --seed
    java -jar target\vaastu-care.jar
(`pom.xml` builds one jar that includes the MySQL driver. Uploaded photos go to an `uploads` folder next to where you start the app.)

### IDE
Open the folder as a Maven project (IntelliJ / Eclipse / NetBeans), set the working directory to the project
folder, and run `com.vaastucare.Main`. Run `com.vaastucare.db.DbSetup` with program argument `--seed` once.

### Troubleshooting
| Message | Fix |
|---|---|
| Access denied for user | Wrong password in `config\db.properties` |
| Communications link failure / Cannot connect | MySQL service is not running (Services -> MySQL80 -> Start) |
| No suitable driver / ClassNotFound | `lib\mysql-connector-j-8.4.0.jar` is missing (step 4) |
| Database is empty / tables missing | Run `scripts\setup-db.bat` |
| Unknown database | Run `scripts\setup-db.bat` (creates it) |
| `--mock` | `scripts\run.bat --mock` starts with temporary in-memory data (nothing saved), useful if MySQL is not available |

## Project structure
    com.vaastucare
      Main                       starts the app (MySQL by default, --mock for in-memory data)
      model/                     User, Provider, Property, ServiceRequest, Category, Review, Notification, StatusEntry
      view/                      Swing screens (unchanged look and navigation)
      controller/AppController   login, permissions, workflow rules, transactions, notifications
      data/DataService           interface used by controller and views
      data/jdbc/                 JdbcDataService + UserDao, PropertyDao, RequestDao, ReviewDao, CategoryDao, NotificationDao
      data/MockDataService       in-memory sample data (used by --mock and as the source of the seed data)
      db/                        Db (JDBC helper + transactions), AppConfig, DataAccessException, DbSetup, SampleData
      security/PasswordHasher    PBKDF2-HMAC-SHA256 hashing
    database/schema.sql          MySQL schema   |   config/db.properties.example   |   scripts/*.bat

## Database tables
users, providers (extra columns for provider accounts), categories, properties, service_requests (owner comes from the
property), request_status_history, request_photos, reviews (one review per request, rating 1-5), notifications.
All have primary and foreign keys; statuses are ENUMs with exactly Scheduled / In Progress / Completed / Rejected.

## Features implemented and how to test them
| Feature | How to test |
|---|---|
| Login with hashed passwords, email or username | Log in as `owner`; wrong password shows an error; 5 wrong tries lock that login for 30 s |
| Remember me | Tick it, log out, the username is prefilled |
| Registration (owner / provider) | Login screen -> Register. Provider accounts start as *Pending*; duplicate email/username refused |
| Owner: properties add / edit / view / search | My Properties; changes persist after restart |
| Owner: provider directory (only approved and not suspended) with search, category, rating, sort | Find Providers; `eyespy` is absent until the admin approves it |
| Owner: request a service | Request service on a provider; appears in Service Requests as Scheduled / Awaiting response |
| Provider: accept / reject, start work, upload proof photo, complete | Log in as `provider` (or the provider you chose); Incoming Requests -> Accept; My Jobs -> Start work -> open request -> Upload photo -> Mark completed |
| Photo proof stored | "Upload photo..." copies the picture into `uploads\`; "Add demo photo" adds a generated sample image |
| Owner: review after completion only | Completed request -> Rate provider; second review for the same request is blocked; average rating updates |
| Status history and notifications saved | Tracking screen history; bell icon; mark read persists |
| Provider profile edit, own reviews | My Profile, Ratings & Reviews (cannot change own approval) |
| Admin: approve / reject / suspend / reinstate providers | Provider Approvals / Provider Listings |
| Admin: categories add / edit / enable / disable / delete | Service Categories (a category in use cannot be deleted) |
| Admin: users and all requests, dashboard counts | Users, Service Requests, Dashboard (numbers come from the database) |
| Permissions enforced in code | E.g. an owner cannot accept requests or view another owner's request even if the UI were bypassed |
| Errors do not crash the app | Stop the MySQL service and click something: a message box appears |

## Tests
Run in my build environment (JDK 25, no MySQL server available there):
- Whole project compiles without errors.
- 51 automated checks of the controller rules on the in-memory implementation: password hashing and verification,
  login lockout, ownership and role checks, the full request workflow, review rules, admin actions, registration rules,
  notifications, photo upload copying. All passed.
- All screens for the three roles render without exceptions.

NOT run here, you must run locally (steps 5-9 above): `schema.sql` against a real MySQL 8 server, every SQL statement in the
`data/jdbc` classes, `DbSetup`, Maven build, and the Windows `.bat` scripts. If something fails, copy the full error text.

## Not implemented / limits
- No payments, pricing or time-slot booking: the original UI has none, so there are no tables for them.
- No delete for properties or users and no password reset: the UI never offered them.
- Photos are files in the `uploads` folder on the same computer; the database stores only the file path.
- Database calls run on the Swing thread, so a slow database can briefly freeze the window.
- Verification of providers is an admin decision inside the app; no documents or identity checks are performed.
