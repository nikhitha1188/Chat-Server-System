# Chat-Server-System
# Secret Spy Chat 🕵️‍♂️💬

A **JavaFX-based secure chat client** that allows users to send and receive messages with a simple and modern UI.  
The project also includes **emoji support** and **custom UI themes** to make chatting fun and engaging.


## 🚀 Features
- Real-time chat communication  
- Secure messaging  
- Emoji support 😀😂❤️  
- Custom UI themes 🎨  
- Built with **JavaFX**  


## 🛠️ Tech Stack
- **Java**  
- **JavaFX**  
- **FXML** for UI design  


## 📂 Project Structure
- `client/` → Main chat client source code  
- `resources/` → Images, icons, and other assets  
- `javafx-sdk-24/` → JavaFX library (not included in repo, download separately)  


## How to Run
1. Install **Java JDK 11 or later**  
2. Download and configure **JavaFX SDK**  
3. Compile and run the client:  

   ```bash
   javac --module-path javafx-sdk-24/lib --add-modules javafx.controls,javafx.fxml client/*.java
   java --module-path javafx-sdk-24/lib --add-modules javafx.controls,javafx.fxml client.Main
