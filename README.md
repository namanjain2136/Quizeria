# Quizeria

A web-based quiz application built with Spring Boot that allows users to create, take, and manage quizzes. The platform includes user authentication, quiz management, and performance tracking features.

## Features

- **User Authentication**: Secure login and registration system
- **Quiz Management**: Create, edit, and delete quizzes
- **Question Bank**: Maintain a collection of questions for different quizzes
- **User Dashboard**: Track quiz attempts and performance
- **Notes Feature**: Users can create and maintain study notes
- **Feedback System**: Collect and manage user feedback
- **Admin Panel**: Comprehensive admin dashboard for content management

## Technology Stack

- **Backend**: Spring Boot (Java)
- **Frontend**: Thymeleaf templates, HTML/CSS
- **Security**: Spring Security
- **Build Tool**: Maven
- **Java Version**: 17

## Getting Started

### Prerequisites
- JDK 17 or higher
- Maven 3.x
- Your favorite IDE (Spring Tool Suite, IntelliJ IDEA, or VS Code)

### Local Development
1. Clone the repository:
   ```bash
   git clone https://github.com/namanjain2136/Quizeria.git
   ```

2. Navigate to project directory:
   ```bash
   cd Quizeria
   ```

3. Build the project:
   ```bash
   mvn clean package -DskipTests
   ```

4. Run the application:
   ```bash
   java -jar target/quizeria-0.0.1-SNAPSHOT.jar
   ```

5. Access the application at:
   ```
   http://localhost:10000
   ```

## Project Structure

```
src/main/
├── java/com/quizeria/
│   ├── config/         # Configuration classes
│   ├── controller/     # MVC Controllers
│   ├── model/         # Entity classes
│   └── repository/    # Data access layer
└── resources/
    ├── static/        # Static resources
    └── templates/     # Thymeleaf templates
```

## Live Demo

The application is deployed on Render and can be accessed at [https://quizeria.onrender.com](https://quizeria.onrender.com)

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is open source and available under the [MIT License](LICENSE).