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

---

## 📂 Project Structure

Quiz_Application_Backend/
- │── api-gateway/
- │── question/
- │── quiz/
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

## 🔌 Default Ports
Service	Port
- Service Registry	8761
- API Gateway	8080 / custom
- Question Service	8081
- Quiz Service	8090

## 📌 API Endpoints
### Question Service
Method	Endpoint	Description
- GET	/question/allQuestions	Get all questions
- GET	/question/category/{category}	Get questions by category
- POST	/question/add	Add a question
- GET	/question/generate?category=Java&numOfQ=5	Generate quiz questions
- POST	/question/getQuestions	Get questions by IDs
- POST	/question/getScore	Calculate score

### Quiz Service
Method	Endpoint	Description
- POST	/quiz/create	Create quiz
- GET	/quiz/get/{id}	Get quiz questions
- POST	/quiz/submit/{id}	Submit quiz

## 🌟 Features
- Microservices architecture
- RESTful APIs
- Service discovery using Eureka
- API Gateway routing
- Quiz creation with random questions
- Quiz submission and score calculation
- Scalable backend design

## 🔮 Future Enhancements
- JWT Authentication
- Role-based access (Teacher / Student)
- Frontend Integration
- Docker Support
- Kafka for communication
- Leaderboard
- Timer-based quizzes
- Result history

## 👨‍💻 Author
Aniket Shukla

## 📜 License
This project is open-source and available under the MIT License.
