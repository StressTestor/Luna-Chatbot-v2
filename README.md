# Luna-Chatbot 
 
## Overview 
Luna-Chatbot is an Android application that delivers secure, privacy-aware conversational AI with optional vision analysis. It is built with Kotlin, Jetpack Compose, Hilt for dependency injection, Retrofit/OkHttp for networking, Room for local persistence, and a modular update system. Recent work adds a vision analysis pipeline, refactors networking, and enhances security and logging. 
 
## Features 
- Chat with AI via modern Compose UI 
- Vision analysis pipeline: 
  - Image validation, Base64 encoding, PII scrubbing 
  - Vision API service and repository 
  - UI: image attachment and preview components 
- Networking refactor: 
  - Centralized OkHttp client with auth, logging, and redaction interceptors 
  - Connectivity tester and diagnostics 
  - Retrofit services: GroqApiService, VisionApiService 
- Security and privacy: 
  - SecureLogger with sanitization 
  - AppIntegrityChecker and IntegrityPolicy 
  - SecurityConfig with safe defaults 
- Updates module and manager with Hilt wiring 
- Comprehensive tests for vision flow, utilities, repositories, and use cases 
 
## Architecture 
- Presentation: Jetpack Compose screens and components, ViewModels 
- Domain: entities, repositories (interfaces), use cases (e.g., ProcessImageUseCase) 
- Data: Retrofit API services, DTOs, repository implementations 
- DI: Hilt modules (e.g., NetworkModule, RepositoryModule) 
- Security: SecureLogger, policies, integrity checks 
- Update system: UpdateManager, receiver, and utilities 
 
Key files: 
- app/src/main/java/com/luna/chat/di/NetworkModule.kt 
- app/src/main/java/com/luna/chat/data/remote/api/ApiConnectivityTester.kt 
- app/src/main/java/com/luna/chat/data/remote/api/GroqApiService.kt 
- app/src/main/java/com/luna/chat/data/remote/api/VisionApiService.kt 
- app/src/main/java/com/luna/chat/data/repository/VisionRepositoryImpl.kt 
- app/src/main/java/com/luna/chat/domain/usecase/ProcessImageUseCase.kt 
- app/src/main/java/com/luna/chat/domain/util/{Base64Encoder,ImageValidator,PiiScrubber}.kt 
- app/src/main/java/com/luna/chat/presentation/ui/components/{ImageAttachmentButton,ImagePreviewDialog}.kt 
- app/src/main/java/com/luna/chat/security/{SecureLogger,AppIntegrityChecker,IntegrityPolicy,SecurityConfig}.kt 
- app/src/main/java/com/luna/chat/update/** 
 
## Setup 
Prerequisites: 
- Android Studio (latest stable) 
- JDK 17+ 
- Android SDK and platform tools 
 
Clone and prepare: 
```bash 
git clone https://github.com/StressTestor/Luna-Chatbot.git 
cd Luna-Chatbot 
``` 
 
Important: this repository history was rewritten to remove large vendored Gradle artifacts. If you cloned earlier, re-clone or run: 
```bash 
git fetch --all --prune 
git reset --hard origin/master 
``` 
 
## Build & Run 
From Android Studio: 
1. Open the project folder 
2. Let Gradle sync 
3. Select a device/emulator 
4. Run the app (Shift+F10) 
 
From command line: 
```bash 
./gradlew assembleDebug 
./gradlew installDebug 
``` 
On Windows: 
```bat 
gradlew.bat assembleDebug 
gradlew.bat installDebug 
``` 
 
## Configuration (API keys) 
Provide your API key(s) via one of the supported providers: 
- SecureApiKeyProvider / SimpleApiKeyProvider (see data/repository) 
- Environment/gradle properties or encrypted storage 
- Settings screen workflow where applicable 
 
Sensitive values should not be committed to VCS. Ensure local.properties or gradle.properties is ignored if used. 
 
## Testing 
Run unit tests: 
```bash 
./gradlew test 
``` 
Key tests include: 
- VisionChatDtoTest 
- VisionRepositoryImplTest 
- ProcessImageUseCaseTest 
- ImageValidatorTest 
- PiiScrubberTest 
 
## Security 
- SecureLogger sanitizes messages to avoid leaking PII/secrets 
- Integrity checks via AppIntegrityChecker and IntegrityPolicy 
- Redaction interceptor removes sensitive headers and payload fields 
- Follow least-privilege and do not hardcode secrets; use providers 
 
## Troubleshooting 
- Build artifacts or large files rejected by GitHub: 
  - Do not vendor Gradle distributions or large jars/zips 
  - Only commit Gradle Wrapper: gradlew, gradlew.bat, gradle/wrapper/* 
- Network issues: 
  - Use ApiConnectivityTester for diagnostics 
- Crashes on startup: 
  - Verify API keys and permissions 
  - Check logcat for security/integrity warnings 
- Clean builds: 
  ```bash 
  ./gradlew clean 
  ``` 
 
## License 
This project is licensed under the MIT License. See LICENSE for details. 
