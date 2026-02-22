# Luna iOS App

## Setup Instructions

### 1. Generate the Shared Framework
```bash
cd /path/to/Luna-Chatbot
./gradlew :shared:assembleSharedXCFramework
```

### 2. Create Xcode Project
1. Open Xcode
2. Create a new iOS App project named "Luna" in the `iosApp/` directory
3. Set Bundle Identifier to `com.luna.chat`
4. Select SwiftUI as the interface
5. Replace generated Swift files with existing `iOSApp.swift` and `ContentView.swift`

### 3. Link Shared Framework
1. In Xcode, go to project settings > General > Frameworks
2. Add the Shared.framework from `shared/build/XCFrameworks/debug/Shared.xcframework`
3. Or add a Run Script phase with:
```
cd "$SRCROOT/.."
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

### 4. Build & Run
- Select an iOS Simulator (iOS 16+)
- Build and run (Cmd+R)

## Permissions
The app requests:
- Microphone (voice input)
- Speech Recognition (voice-to-text)
- Camera (image capture for vision)
- Photo Library (image selection for vision)
