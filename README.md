# NUM Community Portal - Spring Boot Project

A comprehensive social networking and event management platform designed for the NUM (National University of Management) community. This application allows students and administrators to connect, share posts, and engage in discussions.

## 🚀 Overview

This project is a full-stack web application built with **Spring Boot** (Java) and **Thymeleaf**. It features a robust authentication system with email OTP verification, role-based access control (Student, Admin, and Super Admin), and media integration using **Pinata (IPFS)** for decentralized file storage.

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
* **Interactions**: Like/Vote, Save (Bookmark), and Comment on posts.
* **Nested Comments**: Threaded replies to comments.
* **Feed**: Chronological feed of community posts.
* **Search**: Filter posts and users by keywords.
* **Notifications**: Alerts for likes, comments, and replies.

### 🛡️ Administrative Roles

* **Student**: Standard access to feed, posting, and profile.
* **Sub Admin**: 
    * Read-only access to the Admin Dashboard and User Lists.
    * Global Content Moderation.
* **Super Admin**:
    * Global Dashboard (User and Post statistics).
    * User Management (Suspend/Ban users, Edit Roles, Delete Users).
    * Global Content Moderation.

## ⚙️ Configuration & Setup

### Prerequisites

* JDK 17 or higher
* PostgreSQL installed and running
* Maven (or use the provided `./mvnw` wrapper)

### 1. Clone the Repository

```bash
git clone <repository-url>
cd spring_num_project-reach