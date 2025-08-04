# Project Context

## Project Overview
- Luna is an Android chatbot application designed specifically for children
- Provides safe, educational AI conversations powered by the Groq API
- Built with Kotlin/Android with child-friendly UI design
- Features content filtering and multiple colorful themes

## Current Status
- Project appears to be in development/testing phase
- Has integration testing checklist and verification scripts
- Build system configured with Gradle
- Final integration summary document exists
- Comprehensive security enhancements implemented (July 2025)
  - Certificate pinning for API communications
  - Runtime integrity checks to detect app tampering
  - Secure logging to prevent sensitive information leakage
  - Security configuration to centralize security settings
  - Content filtering system integrated with chat repository
  - OWASP dependency scanning implemented
- Implementing UI enhancements with experimental Compose APIs
- Using Meta Llama 4 Maverick model via Groq API for improved AI capabilities

## Recent Changes

- [2025-07-17 17:30] Updated Groq API model from "llama3-8b-8192" to "meta-llama/llama-4-maverick-17b-128e-instruct" in ApiConnectivityTester
- [2025-07-17 16:45] Fixed AI thinking indicator behavior in ChatViewModel to properly show when user sends messages and hide when AI responds
- [2025-07-17 15:30] Updated Groq API model from "llama-3.2-90b-vision-preview" to "llama3-8b-8192" in ApiConnectivityTester
- [2025-07-17 14:45] Enhanced security in SettingsViewModel by requiring parent password setup before allowing API key configuration
- [2025-07-17 10:30] Added Kotlin compiler arguments to opt-in to experimental Compose APIs (ExperimentalAnimationApi and ExperimentalFoundationApi) in app/build.gradle
- [2025-07-16 22:57] Modified compile_test.kt
- [2025-07-16 22:56] Modified - [2025-07-16 Testing] Added testing section to README.md documenting comprehensive safety feature testing

## Key Decisions
- Using Groq API for AI functionality
- Kotlin as primary development language
- Android-native approach rather than cross-platform
- Child safety as core design principle
- Using Jetpack Compose with experimental APIs for enhanced UI capabilities
- Upgraded to Meta Llama 4 Maverick model for improved AI capabilities while maintaining child safety

## Next Steps
- Review integration testing status
- Check build and deployment readiness
- Verify safety features implementation
- Test UI components using new experimental Compose APIs
- Evaluate performance and safety of the new Meta Llama 4 Maverick model

## MCP Integration
- SQLite MCP server configured for database debugging and analysis
- Database path: ./luna_database.db (Room database)
- Enables direct querying of chat messages and user preferences
- Supports database migration testing and schema inspection

## Notes
- Last updated: 2025-07-17 17:30
- Contains build scripts and verification tools
- Focus on child safety and educational value
- MCP servers provide enhanced development capabilities for database work
- Model upgrades must maintain child safety compliance per child-safety.md guidelines
