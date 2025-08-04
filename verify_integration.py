#!/usr/bin/env python3
"""
Integration verification script for Luna Chat App
Checks that all key components are properly integrated
"""

import os
import re
from pathlib import Path

def check_file_exists(file_path, description):
    """Check if a file exists and report status"""
    if os.path.exists(file_path):
        print(f"✅ {description}: {file_path}")
        return True
    else:
        print(f"❌ {description}: {file_path} - NOT FOUND")
        return False

def check_file_contains(file_path, pattern, description):
    """Check if a file contains a specific pattern"""
    try:
        with open(file_path, 'r', encoding='utf-8') as f:
            content = f.read()
            if re.search(pattern, content):
                print(f"✅ {description}")
                return True
            else:
                print(f"❌ {description} - Pattern not found")
                return False
    except Exception as e:
        print(f"❌ {description} - Error reading file: {e}")
        return False

def main():
    print("🔍 Luna Chat App Integration Verification")
    print("=" * 50)
    
    # Check core application files
    print("\n📱 Core Application Files:")
    core_files = [
        ("app/src/main/AndroidManifest.xml", "Android Manifest"),
        ("app/src/main/java/com/luna/chat/MainActivity.kt", "Main Activity"),
        ("app/src/main/java/com/luna/chat/LunaApplication.kt", "Application Class"),
    ]
    
    for file_path, description in core_files:
        check_file_exists(file_path, description)
    
    # Check navigation integration
    print("\n🧭 Navigation Integration:")
    nav_files = [
        ("app/src/main/java/com/luna/chat/presentation/navigation/LunaNavigation.kt", "Navigation Component"),
    ]
    
    for file_path, description in nav_files:
        check_file_exists(file_path, description)
    
    # Check screen components
    print("\n🖥️ Screen Components:")
    screen_files = [
        ("app/src/main/java/com/luna/chat/presentation/ui/screen/ChatScreen.kt", "Chat Screen"),
        ("app/src/main/java/com/luna/chat/presentation/ui/screen/SettingsScreen.kt", "Settings Screen"),
    ]
    
    for file_path, description in screen_files:
        check_file_exists(file_path, description)
    
    # Check UI components
    print("\n🎨 UI Components:")
    ui_files = [
        ("app/src/main/java/com/luna/chat/presentation/ui/components/MessageBubble.kt", "Message Bubble"),
        ("app/src/main/java/com/luna/chat/presentation/ui/components/MessageInput.kt", "Message Input"),
        ("app/src/main/java/com/luna/chat/presentation/ui/components/TypingIndicator.kt", "Typing Indicator"),
    ]
    
    for file_path, description in ui_files:
        check_file_exists(file_path, description)
    
    # Check ViewModels
    print("\n🧠 ViewModels:")
    vm_files = [
        ("app/src/main/java/com/luna/chat/presentation/viewmodel/ChatViewModel.kt", "Chat ViewModel"),
        ("app/src/main/java/com/luna/chat/presentation/viewmodel/SettingsViewModel.kt", "Settings ViewModel"),
    ]
    
    for file_path, description in vm_files:
        check_file_exists(file_path, description)
    
    # Check data layer
    print("\n💾 Data Layer:")
    data_files = [
        ("app/src/main/java/com/luna/chat/data/repository/ChatRepositoryImpl.kt", "Chat Repository"),
        ("app/src/main/java/com/luna/chat/data/repository/ApiKeyProvider.kt", "API Key Provider Interface"),
        ("app/src/main/java/com/luna/chat/data/repository/SecureApiKeyProvider.kt", "Secure API Key Provider"),
        ("app/src/main/java/com/luna/chat/data/remote/api/GroqApiService.kt", "Groq API Service"),
    ]
    
    for file_path, description in data_files:
        check_file_exists(file_path, description)
    
    # Check domain layer
    print("\n🏗️ Domain Layer:")
    domain_files = [
        ("app/src/main/java/com/luna/chat/domain/repository/ChatRepository.kt", "Chat Repository Interface"),
        ("app/src/main/java/com/luna/chat/domain/entity/ChatMessage.kt", "Chat Message Entity"),
        ("app/src/main/java/com/luna/chat/domain/usecase/SendMessageUseCase.kt", "Send Message Use Case"),
    ]
    
    for file_path, description in domain_files:
        check_file_exists(file_path, description)
    
    # Check dependency injection
    print("\n💉 Dependency Injection:")
    di_files = [
        ("app/src/main/java/com/luna/chat/di/DatabaseModule.kt", "Database Module"),
        ("app/src/main/java/com/luna/chat/di/NetworkModule.kt", "Network Module"),
        ("app/src/main/java/com/luna/chat/di/RepositoryModule.kt", "Repository Module"),
    ]
    
    for file_path, description in di_files:
        check_file_exists(file_path, description)
    
    # Check theme system
    print("\n🎨 Theme System:")
    theme_files = [
        ("app/src/main/java/com/luna/chat/presentation/theme/Theme.kt", "Theme Implementation"),
        ("app/src/main/java/com/luna/chat/presentation/theme/ColorSchemes.kt", "Color Schemes"),
    ]
    
    for file_path, description in theme_files:
        check_file_exists(file_path, description)
    
    # Check integration patterns
    print("\n🔗 Integration Patterns:")
    
    # Check MainActivity uses navigation
    check_file_contains(
        "app/src/main/java/com/luna/chat/MainActivity.kt",
        r"LunaNavigation",
        "MainActivity uses navigation system"
    )
    
    # Check Hilt integration
    check_file_contains(
        "app/src/main/java/com/luna/chat/LunaApplication.kt",
        r"@HiltAndroidApp",
        "Application class has Hilt annotation"
    )
    
    # Check dependency injection in ViewModels
    check_file_contains(
        "app/src/main/java/com/luna/chat/presentation/viewmodel/ChatViewModel.kt",
        r"@Inject|hiltViewModel",
        "Chat ViewModel uses dependency injection"
    )
    
    # Check testing integration
    print("\n🧪 Testing Integration:")
    test_files = [
        ("app/src/androidTest/java/com/luna/chat/integration/FullAppIntegrationTest.kt", "Full App Integration Test"),
        ("INTEGRATION_TESTING_CHECKLIST.md", "Integration Testing Checklist"),
    ]
    
    for file_path, description in test_files:
        check_file_exists(file_path, description)
    
    print("\n" + "=" * 50)
    print("✅ Integration verification complete!")
    print("📋 Review the checklist in INTEGRATION_TESTING_CHECKLIST.md")
    print("🧪 Run integration tests with: ./gradlew connectedAndroidTest")

if __name__ == "__main__":
    main()