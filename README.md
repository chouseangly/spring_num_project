# NUM Community Portal - Spring Boot Project

A comprehensive social networking and event management platform designed for the NUM (National University of Management) community. This application allows students, faculty, and administrators to connect, share posts, manage events, and engage in discussions.

## 🚀 Overview

This project is a full-stack web application built with **Spring Boot** (Java) and **Thymeleaf**. It features a robust authentication system with email OTP verification, role-based access control (Student, Faculty Admin, Super Admin), and media integration using **Pinata (IPFS)** for decentralized file storage.

## 🛠️ Tech Stack

### Backend

  * **Java 17**
  * **Spring Boot 3.5.7**
  * **Spring Security 6** (JWT + Session-based hybrid auth)
  * **Spring Data JPA** (Hibernate)
  * **PostgreSQL** (Database)
  * **Java Mail Sender** (SMTP for OTP & Reset Password)

### Frontend

  * **Thymeleaf** (Server-side templating)
  * **Tailwind CSS** (Styling via CDN)
  * **JavaScript** (Vanilla + Fancybox for media galleries)
  * **FontAwesome** (Icons)

### External Services

  * **Pinata (IPFS)**: For decentralized image and video storage.

## ✨ Key Features

### 👤 User Module

  * **Authentication**: Register, Login, Logout with JWT security.
  * **OTP Verification**: Email-based One-Time Password verification upon registration.
  * **Profile Management**: Edit bio, display name, and upload avatars (stored on IPFS).
  * **Password Recovery**: Forgot/Reset password flow via email tokens.

### 📝 Content & Social

  * **Posts**: Create text, image, and video posts.
  * **Interactions**: Like, Save (Bookmark), and Comment on posts.
  * **Nested Comments**: Threaded replies to comments.
  * **Feed**: Chronological feed of community posts.
  * **Search**: Filter posts and users by keywords.
  * **Notifications**: Real-time alerts for likes and comments.

### 🛡️ Administrative Roles

  * **Student**: Standard access to feed, posting, and profile.
  * **Faculty Admin**: Manages specific faculty pages and moderates posts within their faculty.
  * **Super Admin**:
      * Global Dashboard (User/Post/Faculty counts).
      * User Management (Suspend/Ban users, Edit Roles).
      * Faculty Management (Create/Delete Faculties).
      * Global Content Moderation.

## ⚙️ Configuration & Setup

### Prerequisites

  * JDK 17 or higher
  * PostgreSQL installed and running
  * Maven (or use the provided `./mvnw` wrapper)

### 1\. Clone the Repository

```bash
git clone <repository-url>
cd spring_num_project-reach
```

### 2\. Database Setup

Create a PostgreSQL database named `spring_num_app`:

```sql
CREATE DATABASE spring_num_app;
```

### 3\. Environment Configuration

Open `src/main/resources/application.properties` and configure your credentials.

**Database:**

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/spring_num_app
spring.datasource.username=YOUR_DB_USERNAME
spring.datasource.password=YOUR_DB_PASSWORD
```

**Email (SMTP - Required for OTP):**

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_EMAIL@gmail.com
spring.mail.password=YOUR_APP_PASSWORD
```

**Pinata (IPFS - Required for File Uploads):**

```properties
pinata.jwt.token=YOUR_PINATA_JWT_TOKEN
pinata.api.url=https://api.pinata.cloud/pinning/pinFileToIPFS
ipfs.gateway.url=https://YOUR_GATEWAY.mypinata.cloud/ipfs/
```

**Security:**

```properties
# Generate a secure 256-bit secret key
jwt.secret.key=YOUR_SECURE_RANDOM_STRING
```

### 4\. Run the Application

You can run the application using the Maven wrapper:

**Linux/Mac:**

```bash
./mvnw spring-boot:run
```

**Windows:**

```cmd
mvnw.cmd spring-boot:run
```

The application will start at `http://localhost:8081`.

## 📂 Project Structure

```
src/main/java/com/example/spring_project_mid
├── config/          # Security filters (JwtAuthenticationFilter, UserStatusFilter)
├── controller/      # Web controllers (Admin, Post, Auth, User, etc.)
├── dto/             # Data Transfer Objects (RegisterRequest, LoginRequest)
├── model/           # JPA Entities (User, Post, Comment, Notification, etc.)
├── repository/      # Spring Data Repositories
└── service/         # Business Logic (AuthService, PinataService, EmailService)

src/main/resources
├── templates/       # Thymeleaf HTML views
│   ├── admin/       # Admin dashboard views
│   ├── form/        # Login/Register forms
│   ├── fragments/   # Reusable components (navbar, sidebar)
│   └── ...
└── application.properties
```

## 🔒 Roles and Permissions

| Role | Permissions |
| :--- | :--- |
| **STUDENT** | View feed, Create posts, Comment, Like, Manage own profile. |
| **FACULTY\_ADMIN** | All Student permissions + Access Faculty Dashboard, Delete posts within assigned faculty. |
| **SUPER\_ADMIN** | Full system access. Manage Faculties, Users (Ban/Suspend), and global content moderation. |

## 🤝 Contributing

1.  Fork the repository.
2.  Create your feature branch (`git checkout -b feature/AmazingFeature`).
3.  Commit your changes (`git commit -m 'Add some AmazingFeature'`).
4.  Push to the branch (`git push origin feature/AmazingFeature`).
5.  Open a Pull Request.

## 📄 License

This project is licensed under the Apache License 2.0 - see the `mvnw` header for details.