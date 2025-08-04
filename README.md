# Luna - Child-Friendly AI Chat App

Luna is an Android application designed specifically for children to have safe, educational, and fun conversations with an AI assistant powered by the Groq API.

## Features

- **Child-Safe Interface**: Designed with large touch targets and colorful, engaging UI
- **Educational Support**: Help with homework, learning new topics, and creative exploration
- **Content Filtering**: Built-in safety measures to ensure age-appropriate conversations
- **Multiple Themes**: Fun theme options including Rainbow, Ocean, Forest, Space, and Sunset
- **Offline Storage**: Chat history saved locally with privacy protection
- **Parental Controls**: Settings and configuration options for parents

## Architecture

The app follows Clean Architecture principles with MVVM pattern:

- **Presentation Layer**: Jetpack Compose UI with ViewModels
- **Domain Layer**: Use cases and business logic
- **Data Layer**: Repository pattern with Room database and Retrofit API client

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Database**: Room
- **Async**: Coroutines + Flow

## Getting Started

1. Clone the repository
2. Open in Android Studio
3. Configure your Groq API key in the app settings
4. Build and run on Android device (API 24+)

## Requirements

- Android 7.0 (API level 24) or higher
- Internet connection for AI responses
- Groq API key (configured by parent/guardian)

## Safety Features

- Input content filtering
- Response content validation
- No permanent storage of personal information
- Parental oversight capabilities
- Age-appropriate conversation redirection

## License

This project is developed as part of a feature specification for educational purposes.

## Testing

The application includes comprehensive testing for safety features and content filtering to ensure child protection.