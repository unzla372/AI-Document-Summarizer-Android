# 📄 AI Document Summarizer (Android Application)

## 📌 Project Overview
AI Document Summarizer is an Android application that helps users quickly understand long documents by generating concise summaries using Artificial Intelligence.  
The application also supports asking questions directly from the document content.

The system is designed with a **multi-layer processing approach** to ensure reliability even when AI services are unavailable.

---

## 🎯 Objectives
- To reduce time required to read lengthy documents
- To provide AI-based document summarization
- To support multiple summary styles and adjustable length
- To enable document-based Question & Answer functionality
- To ensure reliable output using fallback mechanisms

---

## 🛠 Tools & Technologies Used
- **Android Studio (Narwhal 2025)**
- **Kotlin (2.0)**
- **Ollama (phi3 AI model)**
- **FastAPI (API Server – optional)**
- **SharedPreferences (local authentication)**
- **Android Emulator / Real Android Device**

---

## 🔐 Authentication System
- The application includes **Login and Signup functionality**
- User credentials are stored **locally on the device**
- Passwords are stored in **hashed form**
- Login state is preserved even after app restart

> Note: This authentication system is designed for educational purposes.

---

## ⚙️ Application Features

### ✅ Document Selection
- Users can load a sample document
- The document text is prepared for summarization and Q&A

### ✅ Multiple Summary Styles
Users can choose from:
- Concise
- Detailed
- Bullet Points
- Executive Summary

### ✅ Adjustable Summary Length
- Summary length can be controlled using a slider
- Allows short or detailed summaries based on user preference

### ✅ AI-based Summarization
- Uses **phi3 AI model via Ollama**
- Generates accurate and meaningful summaries

### ✅ Question & Answer (Q&A)
- Users can ask questions directly from the document
- Answers are generated based on document context

### ✅ Save & Share
- Generated summaries can be saved locally
- Summaries can be shared via text-based sharing options

---

## 🤖 AI Processing Strategy (Most Important Part)

The application follows a **three-layer fallback strategy**:

### 1️⃣ Direct Ollama (Primary Option)
- Android app directly communicates with Ollama
- Best performance on Android Emulator
- Uses phi3 AI model

### 2️⃣ API Server (Second Option)
- If direct Ollama is unavailable, the app sends a request to an API server
- API server internally communicates with Ollama
- Enables scalability and device compatibility

### 3️⃣ Local Fallback (Final Option)
- If both AI options fail, the app uses local logic
- No AI or internet required
- Ensures the user always receives an output

> This design guarantees reliability and continuous functionality.

---

## 📱 Emulator vs Real Device Behavior
- **Emulator:** Can access local Ollama service directly
- **Real Mobile Device:** Cannot access laptop localhost, so local fallback is used

This behavior is expected and demonstrates a real-world system design approach.

---

## 🧠 Why This Project Is Valuable
- Solves a real-world problem
- Demonstrates AI integration with Android
- Includes offline and fallback support
- User-friendly and scalable design
- Suitable for academic presentations and demonstrations

---

## 🚀 Future Enhancements
- Support for PDF upload from device storage
- Cloud-based authentication
- Deployment of API server on public cloud
- Improved AI models for higher accuracy
- PDF export instead of text file

---

## 🎓 Academic Note
This project is developed for **educational and demonstration purposes** to showcase Android development and AI integration concepts.

---

## 👤 Developer
**Student Project – BS Computer Science**

---

## ✅ How to Run the Project
1. Open the project in Android Studio
2. Sync Gradle files
3. Run Ollama with phi3 model (for AI features)
4. Launch the app using Android Emulator or real device
5. Login or Sign up and start using the application

---
