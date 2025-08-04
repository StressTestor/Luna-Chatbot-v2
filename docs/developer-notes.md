# Luna Chat — Developer Notes

These notes summarize the current model, API host, authorization, network security toggles, and where to change them. Scope is informational; no behavior changes.

## Model and API

- Model: `deepseek/deepseek-chat-v3-0324:free` via OpenRouter
- Base URL: `https://api.openrouter.ai/v1/`
  - Defined in NetworkModule (see `BASE_URL` reference there)

## Authorization

- Caller supplies: `Authorization: Bearer <API_KEY>`
- Interceptors are best-effort helpers only; they do not replace the requirement for a valid API key.
- Logs are redacted and disabled in release builds to avoid leaking sensitive data.

## Certificate Pinning

- Host pinned: `api.openrouter.ai`
- Primary SPKI pin: present in code (CertificatePinningManager)
- Backup pin: TODO — must be populated with a verified SPKI pin before release (placeholder is present, do not ship without populating)

## Integrity Policy

- Modes (via SecurityConfig):
  - `ALLOW_WITH_WARNING` (default)
  - `LIMITED_MODE`
  - `BLOCK_STARTUP`
- Signature verification: `EXPECTED_SIGNATURE_HASH` must be injected per keystore/variant.
- Select the policy in SecurityConfig; app integrity checker enforces the selected mode.

## Client-Side Filtering

- Pre-send input and post-receive output filtering enabled.
- Respects parental controls.

## Where to Change Settings

- Security and integrity toggles: see `SecurityConfig` keys and references across the `security/` package.
- Network base URL and client config: see `NetworkModule` (DI), which defines `BASE_URL`, interceptors, logging configuration, and certificate pinning hookup.
- Authorization: ensure caller provides `Bearer <API_KEY>`; interceptors will add/validate headers only as best-effort.

## Release Hardening Checklist (one paragraph)

Before release, populate the backup SPKI pin with a verified value for `api.openrouter.ai`, set `EXPECTED_SIGNATURE_HASH` for each keystore/variant in `SecurityConfig`, confirm the integrity policy (prefer `LIMITED_MODE` or `BLOCK_STARTUP` for stricter environments), and verify logging configuration is redacted and disabled in release so no secrets or PII are emitted. Validate that the `BASE_URL` remains `https://api.openrouter.ai/v1/` and that authorization uses `Bearer <API_KEY>` provided by the caller.