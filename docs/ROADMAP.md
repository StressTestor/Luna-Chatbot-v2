# Luna Chat Roadmap

This roadmap outlines planned milestones and tasks for stabilizing and evolving the Luna Chat Android app after migration to OpenRouter with the deepseek/deepseek-chat-v3-0324:free model.

## Milestone 0 — Baseline Verification (Week 1)
- Runtime verification
  - Enter OpenRouter API key via Settings → API Configuration
  - Validate connectivity using in-app tester [Kotlin.function()](app/src/main/java/com/luna/chat/data/remote/api/ApiConnectivityTester.kt:27)
  - Confirm model usage is deepseek/deepseek-chat-v3-0324:free across all calls
- Quality gates
  - Run Gradle unit tests, androidTest (if available), lint, and ktlint
  - Fix any failures introduced by migration
- Logging and security posture
  - Confirm Authorization header is injected and redacted; logging level NONE on release
  - Verify [Kotlin.declaration()](app/src/main/java/com/luna/chat/di/NetworkModule.kt:25) uses BASE_URL=https://api.openrouter.ai/v1/

Deliverable: “Baseline Verification Report” section appended to [Markdown.section()](context.md:1) with outcomes and any risks.

## Milestone 1 — Certificate Pinning Hardening (Week 1–2)
- Current state: Primary SPKI pin for api.openrouter.ai is set in [Kotlin.declaration()](app/src/main/java/com/luna/chat/data/network/CertificatePinningManager.kt:19)
- Tasks
  - Obtain backup SPKI pin from OpenRouter or extract during rotation/staging
  - Add backup pin alongside primary:
    - .add("api.openrouter.ai", "sha256/<BACKUP_PIN_BASE64>")
  - Document rotation procedure and monitoring
- Risk: Single pin can break on rotation; mitigate by adding backup pin ASAP

Deliverable: Updated CertificatePinningManager.kt with two pins and rotation doc note in context.md.

## Milestone 2 — CI Memory Triggers (Week 2)
- Implement GitHub Actions workflow to store memory facts/insights on material changes:
  - Paths: network/security/policy (e.g., [Kotlin.declaration()](app/src/main/java/com/luna/chat/di/NetworkModule.kt:25), [Kotlin.declaration()](app/src/main/java/com/luna/chat/data/network/CertificatePinningManager.kt:19), [Markdown.section()](context.md:66))
  - Use CI Secrets for MCP credentials
  - Events captured: model/base URL changes, pin updates, security toggles, successful tests post-security change
- Optional: local git hooks for post-commit/merge (developer convenience)

Deliverable: .github/workflows/memory-triggers.yml + scripts with secure secret usage.

## Milestone 3 — Content Safety & Parental Controls Validation (Week 2–3)
- Re-validate filtering logic against new model behaviors
  - Review [Kotlin.declaration()](app/src/main/java/com/luna/chat/security/ContentFilteringService.kt:1) and repository use
  - Expand test coverage for inappropriate content detection and redirection
- Parental controls
  - Confirm Settings gating and password setup flow
  - UX copy audit to ensure clarity for guardians

Deliverable: Test additions in security/content filter area and updated documentation.

## Milestone 4 — Accessibility & UX Polish (Week 3)
- Window insets handling and system bar overlap checks in [Kotlin.declaration()](app/src/main/java/com/luna/chat/MainActivity.kt:15) and screens
- TalkBack/Screen Reader labels, content descriptions, and focus order validation
- Performance: Verify recomposition behavior in ViewModels and heavy lists (apply distinctUntilChanged/stateIn where relevant)

Deliverable: Accessibility checklist results and patches.

## Milestone 5 — Release Readiness (Week 4)
- Build/signing and R8/ProGuard rules audit
- App privacy statements and policy updates reflecting OpenRouter usage
- Store assets and “What’s New” (migration note, safety improvements)
- Telemetry (if any): ensure GDPR/COPPA compliance and disable in child context or anonymize

Deliverable: Release checklist and tagged release candidate branch.

## Backlog and Enhancements
- Model guardrails/prompts tuned for age-appropriate guidance
- Offline-first improvements for history access
- Update subsystem (downloader/installer) integration plan with OpenRouter policy compatibility
- Observability dashboards (non-PII) for error rates and connectivity tests
- Optional: Add endpoint health-check and rate-limit backoff visualization

## Governance
- Policy file: [Markdown.section()](context.md:66) governs model/provider usage; all changes require explicit approval
- Security exceptions/change log captured via CI memory triggers
- Critical fixes are prioritized over feature work

## References
- Network: [Kotlin.declaration()](app/src/main/java/com/luna/chat/di/NetworkModule.kt:25)
- Pinning: [Kotlin.declaration()](app/src/main/java/com/luna/chat/data/network/CertificatePinningManager.kt:19)
- Connectivity tester: [Kotlin.function()](app/src/main/java/com/luna/chat/data/remote/api/ApiConnectivityTester.kt:27)
- Settings (API key UI): [Kotlin.composable()](app/src/main/java/com/luna/chat/presentation/ui/screen/SettingsScreen.kt:1034)
- Policy: [Markdown.section()](context.md:66)

## Milestone X — Vision Governance Activation (post-approval)
- Governance toggle activation steps:
  - Enable vision after explicit approval by setting [Kotlin.function()](app/src/main/java/com/luna/chat/security/SecurityConfig.kt:200) via setVisionModelApproved(true).
- Follow-up hardening:
  - Add backup SPKI pin for api.openrouter.ai prior to enabling vision in production; see [Kotlin.declaration()](app/src/main/java/com/luna/chat/data/network/CertificatePinningManager.kt:24) placeholder/TODO.
