# Quiz Application Backend (Microservices Architecture)

A backend project for a Quiz Application built using **Microservices Architecture** with **Spring Boot** and **Spring Cloud**.  
This project allows teachers/admins to create quizzes, manage questions, and users to attempt quizzes and get scores.

---

## 🚀 Tech Stack

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Cloud
- Eureka Server
- API Gateway
- OpenFeign
- MySQL
- Maven

---

## 🏗️ Microservices Included

### 1️⃣ Service Registry
- Eureka Server for service discovery.

### 2️⃣ API Gateway
- Single entry point for all client requests.
- Routes requests to appropriate microservices.

### 3️⃣ Question Service
Handles all question-related operations:
- Add question
- Get all questions
- Get questions by category
- Generate random questions for quiz
- Calculate score

### 4️⃣ Quiz Service
Handles quiz-related operations:
- Create quiz
- Fetch quiz questions
- Submit quiz

### 5️⃣ Auth Service
Handles authentications:
- login
- register
- logout

### 6️⃣ Profile Service
Handles User Profile:
- view profile
- update profile
- view quiz history

### 7️⃣ Resukt Service
Handles Results:
- history
- attempt
- score summary

---

## 📂 Project Structure

Quiz_Application_Backend/
- │── api-gateway/
- │── authService/
- │── profile-service/
- │── question/
- │── quiz/
- │── result-service/
- │── service-registry/


---

## ⚙️ How to Run the Project

### Step 1: Clone Repository
```bash
git clone https://github.com/aniketShukla1605/Quiz_App_Backend.git
cd Quiz_App_Backend
```
### Step 2: Start Services in Order
- Run service-registry
- Run api-gateway
- Run question
- Run quiz
- Run profile-service
- Run authService
- Run result-service

## 🔌 Default Ports
Service	Port
- Service Registry	8761
- API Gateway	8762 / custom
- Question Service	8081
- Quiz Service	8080
- Auth Service 8083
- Result Service 8084
- Profile Service 8070

## 🌟 Features
- Microservices architecture
- RESTful APIs
- Service discovery using Eureka
- API Gateway routing
- Quiz creation with random questions
- Quiz submission and score calculation
- Scalable backend design
- User Profile
- Result History

## 🔮 Future Enhancements
- Frontend Integration
- Docker Support
- Kafka for communication
- Leaderboard
- Timer-based quizzes using websocket

## 👨‍💻 Author
Aniket Shukla

## 📜 License
This project is open-source and available under the MIT License.
