# 💼 Job Portal Management System

A full-stack **Job Portal Web Application** built with **Spring Boot**, **Thymeleaf**, and **MySQL** — supporting three distinct user roles: **Admin**, **Employer (Recruiter)**, and **Student (Job Seeker)**.

---

## 🚀 Features

### 👤 Authentication & Security
- Secure login with **Spring Security** (BCrypt password hashing)
- **Google OAuth2** social login
- OTP-based **email verification** on registration
- **Forgot Password** with OTP reset via Gmail SMTP
- Role-based access control (`ROLE_ADMIN`, `ROLE_EMPLOYER`, `ROLE_STUDENT`)

### 🛡️ Admin Panel
- View all registered users
- Manage user accounts (enable/disable)
- Platform-wide dashboard overview

### 🏢 Employer (Recruiter)
- Post new job listings
- Edit and manage existing job posts
- View all applications submitted for their jobs
- Employer dashboard with job statistics

### 🎓 Student (Job Seeker)
- Browse and search available jobs
- Apply to jobs with resume upload
- Track application status
- Update profile (skills, education, resume URL)

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Backend** | Java 21, Spring Boot 3.5.16 |
| **Web MVC** | Spring MVC, Thymeleaf |
| **Security** | Spring Security 6, OAuth2 Client |
| **Database** | MySQL 8.0, Spring Data JPA (Hibernate) |
| **Mail** | Spring Boot Mail (Gmail SMTP) |
| **Build** | Maven, JaCoCo (code coverage) |
| **Frontend** | HTML5, CSS3, Thymeleaf Templates |

---

## 📁 Project Structure

```
job-portal/
├── src/main/java/com/example/jobportal/
│   ├── config/                  # App configuration
│   ├── controller/
│   │   ├── AdminController.java
│   │   ├── AuthController.java       # Login, Register, OTP, Forgot Password
│   │   ├── EmployerController.java   # Job posting & applications
│   │   ├── StudentController.java    # Profile & job applications
│   │   ├── JobController.java        # Public job browsing
│   │   └── ApplicationRestController.java
│   ├── entity/
│   │   ├── User.java            # User entity (all roles)
│   │   ├── Job.java             # Job listing entity
│   │   └── Application.java     # Job application entity
│   ├── repository/              # Spring Data JPA repositories
│   ├── service/                 # Business logic layer
│   ├── security/
│   │   ├── SecurityConfig.java            # Security rules & route protection
│   │   ├── CustomUserDetailsService.java  # DB-backed authentication
│   │   ├── CustomUserDetails.java
│   │   └── CustomOAuth2UserService.java   # Google OAuth2 handler
│   ├── dto/                     # Data Transfer Objects
│   └── exception/               # Custom exception handlers
│
├── src/main/resources/
│   ├── templates/
│   │   ├── admin/               # Admin dashboard & users view
│   │   ├── employer/            # Job post, edit, applications views
│   │   ├── student/             # Profile, applications, dashboard
│   │   ├── emails/              # Email HTML templates
│   │   ├── fragments/           # Reusable Thymeleaf fragments (navbar, footer)
│   │   ├── login.html
│   │   ├── register.html
│   │   ├── jobs.html            # Public job listings
│   │   ├── forgot-password.html
│   │   ├── verify-otp.html
│   │   └── reset-password.html
│   ├── static/                  # CSS, JS, images
│   └── application.properties   # App configuration
│
└── pom.xml
```

---

## ⚙️ Setup & Installation

### Prerequisites
- Java 21+
- Maven 3.6+
- MySQL 8.0+

### 1. Clone the Repository
```bash
git clone <your-repo-url>
cd job-portal
```

### 2. Configure the Database
Create the database (auto-created on first run if MySQL user has permissions):
```sql
CREATE DATABASE job_portal_db;
```

Update `src/main/resources/application.properties` with your credentials:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/job_portal_db
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password
```

### 3. Configure Gmail SMTP (for OTP & Password Reset)
In `application.properties`, set your Gmail App Password:
```properties
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
```
> **Note:** Generate an [App Password](https://myaccount.google.com/apppasswords) from your Google Account (requires 2FA enabled).

### 4. Configure Google OAuth2 (optional)
```properties
spring.security.oauth2.client.registration.google.client-id=YOUR_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_CLIENT_SECRET
```

### 5. Run the Application
```bash
mvn spring-boot:run
```

The app will start at: **http://localhost:8080**

---

## 👥 Default User Roles

| Role | Access |
|---|---|
| `ROLE_ADMIN` | Full platform control — user management, dashboard |
| `ROLE_EMPLOYER` | Post jobs, view & manage applications |
| `ROLE_STUDENT` | Browse jobs, apply, manage profile |

> Admin and test accounts are seeded directly into the database. No default credentials are stored in code.

---

## 📌 Key Endpoints

| URL | Description |
|---|---|
| `/` | Home / Landing page |
| `/login` | Login page |
| `/register` | Student registration |
| `/jobs` | Browse public job listings |
| `/admin/dashboard` | Admin dashboard (ROLE_ADMIN only) |
| `/employer/dashboard` | Employer dashboard (ROLE_EMPLOYER only) |
| `/employer/post-job` | Post a new job |
| `/student/dashboard` | Student dashboard (ROLE_STUDENT only) |
| `/student/profile` | Update student profile |
| `/student/applications` | View submitted applications |
| `/forgot-password` | Initiate password reset |
| `/actuator/health` | App health check |

---

## 🔒 Security Notes

- All passwords are stored as **BCrypt hashes** — never in plaintext
- Session-based authentication with CSRF protection
- Route-level security enforced via Spring Security rules
- OAuth2 tokens handled by Spring Security OAuth2 client

---

## 📄 License

This project is developed for educational purposes as part of a Full Stack development course.

---

*Built with ❤️ by **Konakanchi Kotesh***
