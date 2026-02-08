# Information Systems Risk Assessment Report

## Online Banking REST API — Spring Boot Application (DemoBank_v1)

| Field                  | Detail                                                       |
| ---------------------- | ------------------------------------------------------------ |
| **Application**        | DemoBank_v1 — Spring Boot Online Banking REST API            |
| **Version**            | 0.0.1-SNAPSHOT                                               |
| **Framework**          | Spring Boot 2.7.15 / Java 1.8                                |
| **Date of Assessment** | February 9, 2026                                             |
| **Assessment Type**    | Full IS Risk Assessment (Source Code & Configuration Review) |
| **Classification**     | CONFIDENTIAL                                                 |

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Scope & Methodology](#2-scope--methodology)
3. [System Overview](#3-system-overview)
4. [Asset Identification & Valuation](#4-asset-identification--valuation)
5. [Threat Identification](#5-threat-identification)
6. [Vulnerability Assessment](#6-vulnerability-assessment)
7. [Risk Assessment Matrix](#7-risk-assessment-matrix)
8. [Detailed Risk Register](#8-detailed-risk-register)
9. [Risk Heat Map](#9-risk-heat-map)
10. [Recommended Controls & Mitigation](#10-recommended-controls--mitigation)
11. [Risk Treatment Plan](#11-risk-treatment-plan)
12. [Appendix — Summary Tables](#12-appendix--summary-tables)

---

## 1. Executive Summary

This report presents a comprehensive information systems risk assessment for the **DemoBank_v1 Online Banking REST API**, a Java Spring Boot application that handles user registration, authentication, bank account management, deposits, withdrawals, transfers, and payment operations backed by a MySQL database.

The assessment identified a total of **39 security findings** across the codebase and configuration:

| Severity     | Count  | Percentage |
| ------------ | ------ | ---------- |
| **Critical** | 8      | 20.5%      |
| **High**     | 9      | 23.1%      |
| **Medium**   | 10     | 25.6%      |
| **Low**      | 12     | 30.8%      |
| **Total**    | **39** | **100%**   |

The application is designated as a **financial system handling monetary transactions** and therefore requires the highest level of security assurance. The current state of the codebase presents **unacceptable risk** for a production deployment. Critical vulnerabilities include hardcoded credentials committed to source control, absence of the Spring Security framework, Insecure Direct Object Reference (IDOR) flaws allowing unauthorized financial operations, race conditions on balance updates, and thread-unsafe shared state in controllers that can cause cross-user identity leakage.

**Overall Risk Rating: CRITICAL — Not suitable for production deployment.**

---

## 2. Scope & Methodology

### 2.1 Scope

The assessment covers the entire source code repository including:

- All Java source files (controllers, services, models, repositories, configuration, helpers, interceptors)
- Application configuration files (`application.properties`)
- Build configuration (`pom.xml`)
- Deployment artifacts (`Dockerfile`)
- SQL migration scripts (`fix_user_id.sql`)

### 2.2 Methodology

The assessment follows the **NIST SP 800-30 Rev. 1** risk assessment framework and incorporates:

- **OWASP Top 10 (2021)** vulnerability categories
- **STRIDE** threat modeling (Spoofing, Tampering, Repudiation, Information Disclosure, Denial of Service, Elevation of Privilege)
- **ISO 27005** risk evaluation criteria

### 2.3 Risk Calculation Formula

$$\text{Risk Level} = \text{Likelihood} \times \text{Impact}$$

Where:

- **Likelihood** (1–5): 1 = Rare, 2 = Unlikely, 3 = Possible, 4 = Likely, 5 = Almost Certain
- **Impact** (1–5): 1 = Negligible, 2 = Minor, 3 = Moderate, 4 = Major, 5 = Catastrophic

| Risk Score | Risk Level |
| ---------- | ---------- |
| 1–4        | Low        |
| 5–9        | Medium     |
| 10–15      | High       |
| 16–25      | Critical   |

---

## 3. System Overview

### 3.1 Architecture

```
[Client/Postman] → HTTP → [Spring Boot REST API (Port 8070)]
                              ├── AuthController (/login, /logout)
                              ├── RegisterController (/register)
                              ├── AccountController (/account/*)
                              ├── TransactController (/transact/*)
                              ├── AppController (/app/*)
                              └── IndexController (/verify)
                                      ↓
                              [MySQL Database (demo_bank_v1)]
                                      ↓
                              [Mailtrap SMTP (Email Verification)]
```

### 3.2 Technology Stack

| Component        | Technology               | Version                    |
| ---------------- | ------------------------ | -------------------------- |
| Framework        | Spring Boot              | 2.7.15 (EOL)               |
| Language         | Java                     | 1.8                        |
| Database         | MySQL                    | via mysql-connector-j      |
| Authentication   | Custom JWT + HttpSession | jjwt 0.11.2/0.11.5         |
| Password Hashing | BCrypt                   | via spring-security-crypto |
| Email            | JavaMail / Mailtrap      | spring-boot-starter-mail   |
| Build            | Maven                    | via mvnw wrapper           |
| Deployment       | Docker                   | Custom Dockerfile          |

### 3.3 Key Functional Areas

1. **User Registration** — Email + password signup with email verification
2. **Authentication** — JWT token + session-based dual auth via interceptor
3. **Account Management** — Create bank accounts, view dashboard
4. **Financial Transactions** — Deposit, withdraw, transfer, payment
5. **History** — Transaction and payment history queries

---

## 4. Asset Identification & Valuation

| Asset ID | Asset                      | Classification | Value (1–5) | Description                                           |
| -------- | -------------------------- | -------------- | ----------- | ----------------------------------------------------- |
| A1       | Customer PII (name, email) | Confidential   | 4           | Personal identity data subject to privacy regulations |
| A2       | Authentication Credentials | Secret         | 5           | Passwords, JWT tokens, session IDs                    |
| A3       | Financial Account Data     | Confidential   | 5           | Account numbers, balances                             |
| A4       | Transaction Records        | Confidential   | 5           | Deposit, withdrawal, transfer, payment logs           |
| A5       | JWT Signing Secret         | Secret         | 5           | Used to forge authentication tokens                   |
| A6       | Database Credentials       | Secret         | 5           | Root MySQL credentials                                |
| A7       | SMTP Credentials           | Confidential   | 3           | Email service access                                  |
| A8       | Application Source Code    | Internal       | 3           | Business logic and security architecture              |
| A9       | Infrastructure Config      | Internal       | 4           | Docker, application properties                        |

---

## 5. Threat Identification

### 5.1 Threat Actors

| Threat Actor                  | Motivation                       | Capability                              | Likelihood     |
| ----------------------------- | -------------------------------- | --------------------------------------- | -------------- |
| External Attacker             | Financial gain, data theft       | High (automated tools, OWASP knowledge) | Almost Certain |
| Malicious Insider (Developer) | Financial gain, sabotage         | Very High (source code access)          | Possible       |
| Competitor                    | Service disruption               | Moderate                                | Unlikely       |
| Script Kiddie                 | Notoriety                        | Low–Moderate (public exploit tools)     | Likely         |
| Automated Bot                 | Credential stuffing, brute force | High (scalable)                         | Almost Certain |

### 5.2 STRIDE Threat Mapping

| STRIDE Category            | Applicable Threats                                                                    |
| -------------------------- | ------------------------------------------------------------------------------------- |
| **Spoofing**               | JWT forgery (hardcoded secret); session hijacking; account verification bypass        |
| **Tampering**              | Balance manipulation via IDOR; negative amount injection; race condition double-spend |
| **Repudiation**            | Insufficient audit logging; debug `System.out.println` only                           |
| **Information Disclosure** | Credentials in source; password in registration response; exception messages leaked   |
| **Denial of Service**      | No rate limiting; NPE from malformed tokens; unhandled exceptions                     |
| **Elevation of Privilege** | Thread-unsafe controllers causing cross-user identity; IDOR to any account            |

---

## 6. Vulnerability Assessment

### 6.1 Vulnerability Summary by OWASP Top 10 (2021)

| OWASP Category                     | Findings                                                                         | Severity |
| ---------------------------------- | -------------------------------------------------------------------------------- | -------- |
| **A01: Broken Access Control**     | IDOR on deposit, withdraw, transfer, payment, history; wildcard CORS             | Critical |
| **A02: Cryptographic Failures**    | Hardcoded secrets; no HTTPS; weak verification codes; `double` for currency      | Critical |
| **A03: Injection**                 | Native queries in repositories (mitigated by parameterized `@Query`)             | Low      |
| **A04: Insecure Design**           | No Spring Security; hand-rolled auth; no rate limiting; no transaction atomicity | Critical |
| **A05: Security Misconfiguration** | Debug logging enabled; wildcard CORS; database root user; tests skipped          | High     |
| **A06: Vulnerable Components**     | Spring Boot 2.7.15 (EOL); Java 1.8; mismatched JJWT versions                     | Medium   |
| **A07: Auth Failures**             | Weak verification codes; no password policy; no token revocation; JWT expiry bug | High     |
| **A08: Data Integrity Failures**   | No `@Transactional` on service layer; non-atomic balance updates                 | High     |
| **A09: Logging & Monitoring**      | Auth tokens logged to stdout; no structured security logging; mail debug on      | Medium   |
| **A10: SSRF**                      | Not applicable                                                                   | —        |

### 6.2 Detailed Vulnerability Findings

#### CRITICAL FINDINGS (Risk Score 16–25)

---

**V-01: Hardcoded Credentials & Secrets in Source Code**

| Field          | Detail                                                                       |
| -------------- | ---------------------------------------------------------------------------- |
| **Risk ID**    | V-01                                                                         |
| **Category**   | A02: Cryptographic Failures                                                  |
| **CVSS Base**  | 9.8                                                                          |
| **Likelihood** | 5 (Almost Certain)                                                           |
| **Impact**     | 5 (Catastrophic)                                                             |
| **Risk Score** | **25 (Critical)**                                                            |
| **Location**   | `application.properties` (lines 4–5, 13, 19–20); `JwtService.java` (line 20) |

**Description:** Database root password (`4321@Ys2b7`), JWT signing secret (`helloDarknessMyOldFriendIComeToTalkWithYouAgain`), and Mailtrap SMTP credentials are all hardcoded in the source code and committed to version control.

**Impact:** Anyone with repository access (or if the repo is public) gains full database admin access, can forge JWT tokens for any user, and can access the email service. This represents a **total system compromise**.

**Evidence:**

```properties
spring.datasource.username=root
spring.datasource.password=4321@Ys2b7
demoBank.app.secret=helloDarknessMyOldFriendIComeToTalkWithYouAgain
spring.mail.username=2560454d70fce0
spring.mail.password=47857b0449fa89
```

---

**V-02: Absence of Spring Security Framework**

| Field          | Detail                                               |
| -------------- | ---------------------------------------------------- |
| **Risk ID**    | V-02                                                 |
| **Category**   | A04: Insecure Design                                 |
| **Likelihood** | 5 (Almost Certain)                                   |
| **Impact**     | 5 (Catastrophic)                                     |
| **Risk Score** | **25 (Critical)**                                    |
| **Location**   | `pom.xml` — `spring-boot-starter-security` is absent |

**Description:** The application does not use the Spring Security framework. Only `spring-security-crypto` is included (for BCrypt hashing). The entire authentication/authorization model relies on a custom `AppInterceptor`.

**Impact:** No CSRF protection, no security filter chain, no role-based access control, no session fixation protection, no security response headers (X-Frame-Options, X-Content-Type-Options, Content-Security-Policy, HSTS, etc.).

---

**V-03: Insecure Direct Object Reference (IDOR) — Deposit to Any Account**

| Field          | Detail                                                                                              |
| -------------- | --------------------------------------------------------------------------------------------------- |
| **Risk ID**    | V-03                                                                                                |
| **Category**   | A01: Broken Access Control                                                                          |
| **Likelihood** | 5 (Almost Certain)                                                                                  |
| **Impact**     | 5 (Catastrophic)                                                                                    |
| **Risk Score** | **25 (Critical)**                                                                                   |
| **Location**   | `TransactServiceImpl.java` (deposit method); `AccountRepository.java` (`changeAccountsBalanceById`) |

**Description:** The `deposit()` method accepts an `account_id` from the request body and uses `changeAccountsBalanceById` which updates balance solely by `account_id` — **with no verification that the account belongs to the authenticated user**.

**Impact:** Any authenticated user can deposit arbitrary amounts to any account in the system, inflating balances at will.

---

**V-04: IDOR — Withdraw / Transfer / Pay from Any Account**

| Field          | Detail                                                           |
| -------------- | ---------------------------------------------------------------- |
| **Risk ID**    | V-04                                                             |
| **Category**   | A01: Broken Access Control                                       |
| **Likelihood** | 5 (Almost Certain)                                               |
| **Impact**     | 5 (Catastrophic)                                                 |
| **Risk Score** | **25 (Critical)**                                                |
| **Location**   | `TransactServiceImpl.java` (withdraw, transfer, payment methods) |

**Description:** Same root cause as V-03. The `withdraw()`, `transfer()`, and `payment()` operations do not validate account ownership. `getAccountBalance(userId, accountId)` may return 0 for non-owned accounts, but the balance update query (`changeAccountsBalanceById`) operates on `account_id` alone.

**Impact:** Unauthorized withdrawal from other users' accounts; unauthorized transfers to attacker-controlled accounts; fraudulent payments charged to victims' accounts.

---

**V-05: IDOR — View Any Account's Transaction History**

| Field          | Detail                                                                       |
| -------------- | ---------------------------------------------------------------------------- |
| **Risk ID**    | V-05                                                                         |
| **Category**   | A01: Broken Access Control                                                   |
| **Likelihood** | 5 (Almost Certain)                                                           |
| **Impact**     | 4 (Major)                                                                    |
| **Risk Score** | **20 (Critical)**                                                            |
| **Location**   | `AppServiceImpl.java` (`getAccountTransactionHistory`); `AppController.java` |

**Description:** The `getAccountTransactionHistory` method takes `account_id` directly from the request body and queries all transactions for that account with **zero ownership validation**.

**Impact:** Any authenticated user can view the full transaction history of any account, exposing sensitive financial data.

---

**V-06: JWT Token NPE — Potential Authentication Bypass**

| Field          | Detail                                                              |
| -------------- | ------------------------------------------------------------------- |
| **Risk ID**    | V-06                                                                |
| **Category**   | A07: Authentication Failures                                        |
| **Likelihood** | 4 (Likely)                                                          |
| **Impact**     | 5 (Catastrophic)                                                    |
| **Risk Score** | **20 (Critical)**                                                   |
| **Location**   | `JwtService.java` (`decodeToken`); `AppInterceptor.java` (line ~43) |

**Description:** `decodeToken()` returns `null` on exception (expired/tampered tokens), but `AppInterceptor` calls `claims.getSubject()` without null-checking, causing a `NullPointerException`. Depending on exception handling configuration, this may bypass authentication or crash the server.

---

**V-07: Thread-Unsafe Shared State in Singleton Controllers**

| Field          | Detail                                                                                          |
| -------------- | ----------------------------------------------------------------------------------------------- |
| **Risk ID**    | V-07                                                                                            |
| **Category**   | A01: Broken Access Control                                                                      |
| **Likelihood** | 4 (Likely)                                                                                      |
| **Impact**     | 5 (Catastrophic)                                                                                |
| **Risk Score** | **20 (Critical)**                                                                               |
| **Location**   | `TransactController.java` (line 26: `User user;`); `AppController.java` (line 35: `User user;`) |

**Description:** The `user` variable is declared as an **instance field** on Spring singleton-scoped controllers. When concurrent requests arrive, one thread overwrites the `user` field, causing another thread's financial operation to execute under a different user's identity.

**Impact:** Cross-user identity leakage; User A's deposit/transfer/payment executes as User B. Catastrophic in a banking application — unauthorized financial operations occur silently.

**Evidence:**

```java
@Controller
@RequestMapping("/transact")
public class TransactController {
    User user;  // SHARED MUTABLE STATE ON SINGLETON

    @PostMapping("/deposit")
    public ResponseEntity deposit(...) {
        user = (User) session.getAttribute("user"); // Thread A writes
        // Thread B overwrites user here
        return transactService.deposit(requestMap, user); // Thread A uses Thread B's user
    }
}
```

---

**V-08: JWT Expiration Calculation Bug**

| Field          | Detail                          |
| -------------- | ------------------------------- |
| **Risk ID**    | V-08                            |
| **Category**   | A07: Authentication Failures    |
| **Likelihood** | 4 (Likely)                      |
| **Impact**     | 4 (Major)                       |
| **Risk Score** | **16 (Critical)**               |
| **Location**   | `JwtService.java` (line ~27–28) |

**Description:** `expiresIn = 604800` (intended as seconds = 7 days) is added to `now.getTime()` which returns **milliseconds**. Result: tokens expire in ~10 minutes instead of 7 days.

---

#### HIGH FINDINGS (Risk Score 10–15)

---

**V-09: Race Condition — Non-Atomic Balance Updates (Double-Spend)**

| Field          | Detail                                             |
| -------------- | -------------------------------------------------- |
| **Risk ID**    | V-09                                               |
| **Category**   | A08: Data Integrity Failures                       |
| **Likelihood** | 4 (Likely)                                         |
| **Impact**     | 5 (Catastrophic)                                   |
| **Risk Score** | **20 → High**                                      |
| **Location**   | `TransactServiceImpl.java` (all financial methods) |

**Description:** Balance updates follow a read → compute → write pattern with no database-level locking (`SELECT ... FOR UPDATE`) and no service-level `@Transactional` annotation. Concurrent requests can read the same balance, compute independently, and overwrite each other.

**Impact:** Double-spending (withdraw twice before balance updates), balance inflation (deposit counted multiple times), and money vanishing during transfers if one leg fails. In a banking system, this violates ACID properties and causes financial loss.

---

**V-10: Wildcard CORS with Credentials**

| Field          | Detail                         |
| -------------- | ------------------------------ |
| **Risk ID**    | V-10                           |
| **Category**   | A05: Security Misconfiguration |
| **Likelihood** | 4 (Likely)                     |
| **Impact**     | 4 (Major)                      |
| **Risk Score** | **16 → High**                  |
| **Location**   | `AppConfig.java` (lines 37–45) |

**Description:** CORS is configured with `addAllowedOriginPattern("*")` and `setAllowCredentials(true)`. This allows any website to make authenticated cross-origin requests.

**Impact:** Any malicious website can perform credentialed API requests on behalf of a logged-in user, enabling cross-site financial operations.

**Evidence:**

```java
config.setAllowCredentials(true);
config.addAllowedOriginPattern("*");
config.addAllowedHeader("*");
config.addAllowedMethod("*");
```

---

**V-11: Password Returned in Registration Response**

| Field          | Detail                                   |
| -------------- | ---------------------------------------- |
| **Risk ID**    | V-11                                     |
| **Category**   | A02: Cryptographic Failures              |
| **Likelihood** | 5 (Almost Certain)                       |
| **Impact**     | 3 (Moderate)                             |
| **Risk Score** | **15 (High)**                            |
| **Location**   | `RegisterServiceImpl.java` (line ~57–62) |

**Description:** The registration response includes the full `User` object via `response.put("user", user)`. Since the `User` model has no `@JsonIgnore` on the `password`, `token`, or `code` fields, all of these are serialized to the client.

**Impact:** User's plaintext password (from request), verification token, and verification code are exposed in the HTTP response. If logged, cached by proxy, or intercepted — full account compromise.

---

**V-12: Weak Verification Code — Only ~123 Possible Values**

| Field          | Detail                                   |
| -------------- | ---------------------------------------- |
| **Risk ID**    | V-12                                     |
| **Category**   | A07: Authentication Failures             |
| **Likelihood** | 5 (Almost Certain)                       |
| **Impact**     | 3 (Moderate)                             |
| **Risk Score** | **15 (High)**                            |
| **Location**   | `RegisterServiceImpl.java` (line ~69–73) |

**Description:** Verification code is generated as `code = 123 * rand.nextInt(123)`, producing only 123 unique values (0, 123, 246, ..., 15006). Uses `java.util.Random` (not cryptographically secure).

**Impact:** An attacker can brute-force the verification code in at most 123 attempts, bypassing email verification to activate any account.

---

**V-13: Weak Account Number Generation — Only 1000 Possible Values**

| Field          | Detail                               |
| -------------- | ------------------------------------ |
| **Risk ID**    | V-13                                 |
| **Category**   | A04: Insecure Design                 |
| **Likelihood** | 4 (Likely)                           |
| **Impact**     | 3 (Moderate)                         |
| **Risk Score** | **12 (High)**                        |
| **Location**   | `GenAccountNumber.java` (lines 8–12) |

**Description:** Account numbers are generated as `1000 * random.nextInt(1000)` — only 1000 possible values (0, 1000, 2000, ..., 999000). Uses `java.util.Random`.

**Impact:** Trivially guessable account numbers; high collision rate. Combined with IDOR flaws (V-03/V-04), attackers can enumerate and target any account.

---

**V-14: No HTTPS / TLS Enforcement**

| Field          | Detail                                                     |
| -------------- | ---------------------------------------------------------- |
| **Risk ID**    | V-14                                                       |
| **Category**   | A02: Cryptographic Failures                                |
| **Likelihood** | 4 (Likely)                                                 |
| **Impact**     | 4 (Major)                                                  |
| **Risk Score** | **16 → High**                                              |
| **Location**   | `application.properties` (lines 7–8); `HTML.java` (line 6) |

**Description:** Server binds to `127.0.0.1:8070` with no TLS configuration. Verification emails contain `http://127.0.0.1:8070/verify?token=...` links. All traffic is plaintext HTTP.

**Impact:** Passwords, JWT tokens, session cookies, financial data, and verification tokens transmitted in cleartext. Subject to man-in-the-middle attacks.

---

**V-15: No JWT Token Revocation**

| Field          | Detail                                   |
| -------------- | ---------------------------------------- |
| **Risk ID**    | V-15                                     |
| **Category**   | A07: Authentication Failures             |
| **Likelihood** | 3 (Possible)                             |
| **Impact**     | 4 (Major)                                |
| **Risk Score** | **12 (High)**                            |
| **Location**   | `AuthServiceImpl.java` (`logout` method) |

**Description:** Logout only invalidates the server-side session. The JWT token remains valid until natural expiration. There is no token blacklist or revocation mechanism.

**Impact:** Stolen JWT tokens remain usable indefinitely (within expiry window). Cannot force-logout compromised accounts.

---

**V-16: No CSRF Protection**

| Field          | Detail                                                 |
| -------------- | ------------------------------------------------------ |
| **Risk ID**    | V-16                                                   |
| **Category**   | A01: Broken Access Control                             |
| **Likelihood** | 4 (Likely)                                             |
| **Impact**     | 3 (Moderate)                                           |
| **Risk Score** | **12 (High)**                                          |
| **Location**   | Application-wide (no Spring Security = no CSRF tokens) |

**Description:** Without Spring Security, there is no CSRF token mechanism. All state-changing endpoints (`/login`, `/register`, `/transact/*`, `/account/*`) accept requests without anti-CSRF tokens.

**Impact:** Combined with wildcard CORS (V-10), attackers can forge financial transactions from malicious websites when a user is logged in.

---

**V-17: User Data Serialized Without Field Filtering**

| Field          | Detail                                                         |
| -------------- | -------------------------------------------------------------- |
| **Risk ID**    | V-17                                                           |
| **Category**   | A01: Broken Access Control / Information Disclosure            |
| **Likelihood** | 4 (Likely)                                                     |
| **Impact**     | 3 (Moderate)                                                   |
| **Risk Score** | **12 (High)**                                                  |
| **Location**   | `User.java` — no `@JsonIgnore` annotations on sensitive fields |

**Description:** The `User` model exposes `password`, `token` (verification token), and `code` (verification code) fields whenever serialized. Multiple endpoints return `User` objects.

**Impact:** Verification tokens/codes and hashed (or plaintext) passwords leak in API responses.

---

#### MEDIUM FINDINGS (Risk Score 5–9)

---

**V-18: No Input Validation on Financial Amounts — Negative Values**

| Field          | Detail                                          |
| -------------- | ----------------------------------------------- |
| **Risk ID**    | V-18                                            |
| **Category**   | A04: Insecure Design                            |
| **Likelihood** | 4 (Likely)                                      |
| **Impact**     | 4 (Major)                                       |
| **Risk Score** | **16 → Medium**                                 |
| **Location**   | `TransactServiceImpl.java` (validation methods) |

**Description:** Financial amount validation only checks for zero; negative values are not rejected. A negative deposit effectively becomes a withdrawal; a negative withdrawal becomes a deposit.

**Impact:** Attackers can manipulate account balances by sending negative transaction amounts, bypassing intended business rules.

---

**V-19: `double` Used for Financial Calculations**

| Field          | Detail                                                                                      |
| -------------- | ------------------------------------------------------------------------------------------- |
| **Risk ID**    | V-19                                                                                        |
| **Category**   | A08: Data Integrity Failures                                                                |
| **Likelihood** | 5 (Almost Certain)                                                                          |
| **Impact**     | 2 (Minor)                                                                                   |
| **Risk Score** | **10 (Medium)**                                                                             |
| **Location**   | `TransactServiceImpl.java`; `AccountRepository.java` (`getAccountBalance` returns `double`) |

**Description:** All balance and amount calculations use `double` primitive type. IEEE 754 floating-point representation cannot accurately represent all decimal fractions.

**Impact:** Rounding errors accumulate over time. Example: `0.1 + 0.2 = 0.30000000000000004`. In banking, this violates financial accuracy requirements. Must use `BigDecimal`.

---

**V-20: Exception Messages Leaked to Clients**

| Field          | Detail                                                                   |
| -------------- | ------------------------------------------------------------------------ |
| **Risk ID**    | V-20                                                                     |
| **Category**   | A09: Security Logging & Monitoring Failures                              |
| **Likelihood** | 3 (Possible)                                                             |
| **Impact**     | 3 (Moderate)                                                             |
| **Risk Score** | **9 (Medium)**                                                           |
| **Location**   | `AuthServiceImpl.java`; `AccountServiceImpl.java`; `AppServiceImpl.java` |

**Description:** Error responses include raw exception messages: `"Something went wrong: " + e.getMessage()`. These may contain SQL error details, stack information, or internal class names.

**Impact:** Information disclosure aids attacker reconnaissance — reveals database structure, query syntax, and internal architecture.

---

**V-21: Debug Logging of Authentication Tokens**

| Field          | Detail                                                                                                         |
| -------------- | -------------------------------------------------------------------------------------------------------------- |
| **Risk ID**    | V-21                                                                                                           |
| **Category**   | A09: Security Logging & Monitoring Failures                                                                    |
| **Likelihood** | 3 (Possible)                                                                                                   |
| **Impact**     | 3 (Moderate)                                                                                                   |
| **Risk Score** | **9 (Medium)**                                                                                                 |
| **Location**   | `AppInterceptor.java` (multiple `System.out.println` statements); `application.properties` (`mail.debug=true`) |

**Description:** The interceptor uses `System.out.println` to log full Authorization headers (JWT tokens) and user objects. Email debug mode logs SMTP authentication.

**Impact:** Sensitive tokens and credentials written to application logs. If logs are accessed by unauthorized parties, all active sessions are compromised.

---

**V-22: JwtService Not Spring-Managed in AppInterceptor**

| Field          | Detail                                                                      |
| -------------- | --------------------------------------------------------------------------- |
| **Risk ID**    | V-22                                                                        |
| **Category**   | A05: Security Misconfiguration                                              |
| **Likelihood** | 3 (Possible)                                                                |
| **Impact**     | 3 (Moderate)                                                                |
| **Risk Score** | **9 (Medium)**                                                              |
| **Location**   | `AppInterceptor.java` — `private JwtService jwtService = new JwtService();` |

**Description:** The `JwtService` instance in the interceptor is manually instantiated (`new JwtService()`) instead of being `@Autowired`. Spring's `@Value` property injection does not apply to this instance.

**Impact:** If the JWT secret or expiry is ever managed solely through Spring configuration, the interceptor will use different values than the auth service, causing authentication failures or using a weaker/default secret.

---

**V-23: No Duplicate Registration Prevention**

| Field          | Detail                     |
| -------------- | -------------------------- |
| **Risk ID**    | V-23                       |
| **Category**   | A04: Insecure Design       |
| **Likelihood** | 3 (Possible)               |
| **Impact**     | 3 (Moderate)               |
| **Risk Score** | **9 (Medium)**             |
| **Location**   | `RegisterServiceImpl.java` |

**Description:** No check is performed to verify whether an email address already exists before creating a new user record.

**Impact:** Duplicate registrations may cause database constraint violations (ungraceful errors), overwrite existing accounts, or enable denial-of-service through mass registration.

---

**V-24: No Rate Limiting on Verification Endpoint**

| Field          | Detail                                       |
| -------------- | -------------------------------------------- |
| **Risk ID**    | V-24                                         |
| **Category**   | A07: Authentication Failures                 |
| **Likelihood** | 4 (Likely)                                   |
| **Impact**     | 3 (Moderate)                                 |
| **Risk Score** | **12 → Medium**                              |
| **Location**   | `IndexServiceImpl.java` (`/verify` endpoint) |

**Description:** The verification endpoint accepts unlimited requests with no rate limiting. Combined with V-12 (only 123 possible verification codes), brute-force verification is trivial.

**Impact:** Attacker can verify any email address in under 123 requests, gaining full access to accounts they do not own.

---

**V-25: No Service-Level @Transactional — Partial Commits**

| Field          | Detail                                       |
| -------------- | -------------------------------------------- |
| **Risk ID**    | V-25                                         |
| **Category**   | A08: Data Integrity Failures                 |
| **Likelihood** | 3 (Possible)                                 |
| **Impact**     | 4 (Major)                                    |
| **Risk Score** | **12 → Medium**                              |
| **Location**   | `TransactServiceImpl.java` (transfer method) |

**Description:** Transfer operations perform two separate `changeAccountsBalanceById` calls (debit source, credit target) and log a transaction. Each is auto-committed independently with no wrapping `@Transactional`.

**Impact:** If the credit operation fails after the debit succeeds, money vanishes from the source account without reaching the target. No rollback capability. Violates financial ACID requirements.

---

**V-26: Bean Validation Bypass in Registration**

| Field          | Detail                                 |
| -------------- | -------------------------------------- |
| **Risk ID**    | V-26                                   |
| **Category**   | A03: Injection / Input Validation      |
| **Likelihood** | 3 (Possible)                           |
| **Impact**     | 3 (Moderate)                           |
| **Risk Score** | **9 (Medium)**                         |
| **Location**   | `RegisterController.java` (line 27–28) |

**Description:** The validation check `if(bindingResult.hasErrors() && confirmPassword.isEmpty())` only returns errors when **both** conditions are true. If `confirmPassword` is non-empty but the `User` object has validation errors, registration proceeds with invalid data.

**Impact:** All bean validation constraints (`@NotEmpty`, `@Email`, `@Size`) can be bypassed by providing any non-empty `confirm_password` value.

**Evidence:**

```java
if(bindingResult.hasErrors() && confirmPassword.isEmpty()){
    // errors returned ONLY if BOTH conditions true
}
// Otherwise, registration proceeds with invalid data
```

---

**V-27: Docker Image Copies Entire target/ Directory**

| Field          | Detail                         |
| -------------- | ------------------------------ |
| **Risk ID**    | V-27                           |
| **Category**   | A05: Security Misconfiguration |
| **Likelihood** | 2 (Unlikely)                   |
| **Impact**     | 3 (Moderate)                   |
| **Risk Score** | **6 (Medium)**                 |
| **Location**   | `Dockerfile` (line 6)          |

**Description:** `COPY target/ /app/` copies the entire Maven build output into the Docker image including test classes, build metadata, and potentially sensitive configuration.

**Impact:** Unnecessary attack surface in production; test code and development artifacts exposed.

---

#### LOW FINDINGS (Risk Score 1–4)

---

**V-28: End-of-Life Spring Boot Version**

| Field          | Detail                                                |
| -------------- | ----------------------------------------------------- |
| **Risk ID**    | V-28                                                  |
| **Category**   | A06: Vulnerable & Outdated Components                 |
| **Risk Score** | **4 (Low)**                                           |
| **Location**   | `pom.xml` line 8: `spring-boot-starter-parent 2.7.15` |

Spring Boot 2.7.x has reached end-of-life. No further security patches will be released.

---

**V-29: Mismatched JJWT Library Versions**

| Field          | Detail                                                                     |
| -------------- | -------------------------------------------------------------------------- |
| **Risk ID**    | V-29                                                                       |
| **Category**   | A06: Vulnerable & Outdated Components                                      |
| **Risk Score** | **3 (Low)**                                                                |
| **Location**   | `pom.xml`: `jjwt-api:0.11.5` vs `jjwt-impl:0.11.2` / `jjwt-jackson:0.11.2` |

Mismatched library versions may cause runtime incompatibilities or miss security patches.

---

**V-30: Java 8 Target**

| Field          | Detail                                                |
| -------------- | ----------------------------------------------------- |
| **Risk ID**    | V-30                                                  |
| **Category**   | A06: Vulnerable & Outdated Components                 |
| **Risk Score** | **3 (Low)**                                           |
| **Location**   | `pom.xml` line 19: `<java.version>1.8</java.version>` |

Java 8 is in extended support only. Missing modern security APIs and performance improvements.

---

**V-31: All Tests Skipped in Build**

| Field          | Detail                                                 |
| -------------- | ------------------------------------------------------ |
| **Risk ID**    | V-31                                                   |
| **Category**   | Code Quality                                           |
| **Risk Score** | **4 (Low)**                                            |
| **Location**   | `pom.xml` lines 101–103: `<skipTests>true</skipTests>` |

No automated tests are executed during the build process, meaning security regressions will not be caught.

---

**V-32: Database Root User**

| Field          | Detail                                                             |
| -------------- | ------------------------------------------------------------------ |
| **Risk ID**    | V-32                                                               |
| **Category**   | A05: Security Misconfiguration                                     |
| **Risk Score** | **4 (Low)**                                                        |
| **Location**   | `application.properties` line 3: `spring.datasource.username=root` |

Application connects to MySQL as `root`. Any SQL injection or ORM vulnerability grants full DBA privileges.

---

**V-33: No Password Complexity Policy**

| Field          | Detail                                                  |
| -------------- | ------------------------------------------------------- |
| **Risk ID**    | V-33                                                    |
| **Category**   | A07: Authentication Failures                            |
| **Risk Score** | **4 (Low)**                                             |
| **Location**   | `User.java` — only `@NotEmpty` / `@NotNull` on password |

No minimum length, complexity, or strength requirements. Users can register with single-character passwords.

---

**V-34: Hardcoded Verification Email URL**

| Field          | Detail                                                 |
| -------------- | ------------------------------------------------------ |
| **Risk ID**    | V-34                                                   |
| **Category**   | A05: Security Misconfiguration                         |
| **Risk Score** | **2 (Low)**                                            |
| **Location**   | `HTML.java` line 6: `http://127.0.0.1:8070/verify?...` |

Verification URL is hardcoded to localhost. Will not function in any deployed environment.

---

**V-35: Deprecated JWT API Usage**

| Field          | Detail                                                                     |
| -------------- | -------------------------------------------------------------------------- |
| **Risk ID**    | V-35                                                                       |
| **Category**   | A02: Cryptographic Failures                                                |
| **Risk Score** | **3 (Low)**                                                                |
| **Location**   | `JwtService.java` line 34: `signWith(SignatureAlgorithm.HS256, appSecret)` |

Uses deprecated `signWith` overload that accepts a raw string as a signing key without proper HMAC key derivation.

---

**V-36: No Authorization Header Format Validation**

| Field          | Detail                                         |
| -------------- | ---------------------------------------------- |
| **Risk ID**    | V-36                                           |
| **Category**   | A07: Authentication Failures                   |
| **Risk Score** | **3 (Low)**                                    |
| **Location**   | `JwtService.java` (`getAccessTokenFromHeader`) |

Splits on space and accesses `parts[1]` with no validation. Malformed headers cause `ArrayIndexOutOfBoundsException`.

---

**V-37: SQL File Committed to Repository**

| Field          | Detail                 |
| -------------- | ---------------------- |
| **Risk ID**    | V-37                   |
| **Category**   | Information Disclosure |
| **Risk Score** | **2 (Low)**            |
| **Location**   | `fix_user_id.sql`      |

Schema details committed to repository aids attacker reconnaissance.

---

**V-38: GlobalExceptionHandler Re-throws Exceptions**

| Field          | Detail                         |
| -------------- | ------------------------------ |
| **Risk ID**    | V-38                           |
| **Category**   | A05: Security Misconfiguration |
| **Risk Score** | **2 (Low)**                    |
| **Location**   | `GlobalExceptionHandler.java`  |

Catches `CustomError` then immediately re-throws in all cases, providing no actual error handling.

---

**V-39: Deprecated Servlet API Version**

| Field          | Detail                                                              |
| -------------- | ------------------------------------------------------------------- |
| **Risk ID**    | V-39                                                                |
| **Category**   | A06: Vulnerable & Outdated Components                               |
| **Risk Score** | **2 (Low)**                                                         |
| **Location**   | `pom.xml`: `javax.servlet-api:3.1.0`, `javax.servlet.jsp-api:2.3.3` |

Using legacy `javax.servlet` API instead of `jakarta.servlet`.

---

## 7. Risk Assessment Matrix

|                        | **Negligible (1)** | **Minor (2)**  | **Moderate (3)**                 | **Major (4)**              | **Catastrophic (5)**            |
| ---------------------- | ------------------ | -------------- | -------------------------------- | -------------------------- | ------------------------------- |
| **Almost Certain (5)** | 5                  | 10             | V-11, V-12 (15)                  | V-14 (20)                  | **V-01, V-02, V-03, V-04** (25) |
| **Likely (4)**         | 4                  | 8              | V-13, V-16, V-17, V-18 (12)      | V-09, V-10 (16)            | **V-06, V-07** (20)             |
| **Possible (3)**       | 3                  | 6              | V-20, V-21, V-22, V-23, V-26 (9) | V-15, V-24, V-25 (12)      |                                 |
| **Unlikely (2)**       | 2                  | 4              | V-27 (6)                         |                            |                                 |
| **Rare (1)**           | V-39 (1)           | V-34, V-37 (2) | V-29, V-30, V-35, V-36 (3)       | V-28, V-31, V-32, V-33 (4) |                                 |

---

## 8. Detailed Risk Register

| Risk ID | Risk Title                                     | Likelihood | Impact | Risk Score | Severity | Owner       | Status |
| ------- | ---------------------------------------------- | ---------- | ------ | ---------- | -------- | ----------- | ------ |
| V-01    | Hardcoded credentials in source code           | 5          | 5      | 25         | Critical | DevOps      | Open   |
| V-02    | No Spring Security framework                   | 5          | 5      | 25         | Critical | Dev Lead    | Open   |
| V-03    | IDOR — Deposit to any account                  | 5          | 5      | 25         | Critical | Backend Dev | Open   |
| V-04    | IDOR — Withdraw/Transfer/Pay from any account  | 5          | 5      | 25         | Critical | Backend Dev | Open   |
| V-05    | IDOR — View any account's history              | 5          | 4      | 20         | Critical | Backend Dev | Open   |
| V-06    | JWT NPE — Auth bypass risk                     | 4          | 5      | 20         | Critical | Backend Dev | Open   |
| V-07    | Thread-unsafe controllers — cross-user leakage | 4          | 5      | 20         | Critical | Backend Dev | Open   |
| V-08    | JWT expiration calculation bug                 | 4          | 4      | 16         | Critical | Backend Dev | Open   |
| V-09    | Race condition — non-atomic balance updates    | 4          | 5      | 20         | High     | Backend Dev | Open   |
| V-10    | Wildcard CORS with credentials                 | 4          | 4      | 16         | High     | Backend Dev | Open   |
| V-11    | Password returned in registration response     | 5          | 3      | 15         | High     | Backend Dev | Open   |
| V-12    | Weak verification code (~123 values)           | 5          | 3      | 15         | High     | Backend Dev | Open   |
| V-13    | Weak account number generation (~1000 values)  | 4          | 3      | 12         | High     | Backend Dev | Open   |
| V-14    | No HTTPS / TLS enforcement                     | 4          | 4      | 16         | High     | DevOps      | Open   |
| V-15    | No JWT token revocation                        | 3          | 4      | 12         | High     | Backend Dev | Open   |
| V-16    | No CSRF protection                             | 4          | 3      | 12         | High     | Backend Dev | Open   |
| V-17    | User data serialized without filtering         | 4          | 3      | 12         | High     | Backend Dev | Open   |
| V-18    | Negative financial amounts accepted            | 4          | 4      | 16         | Medium   | Backend Dev | Open   |
| V-19    | `double` used for currency calculations        | 5          | 2      | 10         | Medium   | Backend Dev | Open   |
| V-20    | Exception messages leaked to clients           | 3          | 3      | 9          | Medium   | Backend Dev | Open   |
| V-21    | Debug logging of auth tokens                   | 3          | 3      | 9          | Medium   | DevOps      | Open   |
| V-22    | JwtService not Spring-managed in interceptor   | 3          | 3      | 9          | Medium   | Backend Dev | Open   |
| V-23    | No duplicate registration prevention           | 3          | 3      | 9          | Medium   | Backend Dev | Open   |
| V-24    | No rate limiting on verification               | 4          | 3      | 12         | Medium   | Backend Dev | Open   |
| V-25    | No service-level @Transactional                | 3          | 4      | 12         | Medium   | Backend Dev | Open   |
| V-26    | Bean validation bypass in registration         | 3          | 3      | 9          | Medium   | Backend Dev | Open   |
| V-27    | Docker copies full target/ directory           | 2          | 3      | 6          | Medium   | DevOps      | Open   |
| V-28    | EOL Spring Boot 2.7.15                         | 1          | 4      | 4          | Low      | Dev Lead    | Open   |
| V-29    | Mismatched JJWT versions                       | 1          | 3      | 3          | Low      | Backend Dev | Open   |
| V-30    | Java 8 target                                  | 1          | 3      | 3          | Low      | Dev Lead    | Open   |
| V-31    | Tests skipped in build                         | 1          | 4      | 4          | Low      | QA          | Open   |
| V-32    | Database root user                             | 1          | 4      | 4          | Low      | DBA         | Open   |
| V-33    | No password complexity policy                  | 1          | 4      | 4          | Low      | Backend Dev | Open   |
| V-34    | Hardcoded localhost verification URL           | 1          | 2      | 2          | Low      | Backend Dev | Open   |
| V-35    | Deprecated JWT signing API                     | 1          | 3      | 3          | Low      | Backend Dev | Open   |
| V-36    | No auth header format validation               | 1          | 3      | 3          | Low      | Backend Dev | Open   |
| V-37    | SQL migration file in repo                     | 1          | 2      | 2          | Low      | DevOps      | Open   |
| V-38    | Exception handler re-throws all errors         | 1          | 2      | 2          | Low      | Backend Dev | Open   |
| V-39    | Legacy javax.servlet API                       | 1          | 1      | 1          | Low      | Dev Lead    | Open   |

---

## 9. Risk Heat Map

```
                        I M P A C T
               Negligible  Minor  Moderate  Major  Catastrophic
              ┌──────────┬──────┬─────────┬──────┬─────────────┐
Almost Certain│          │      │  V-11   │ V-14 │ V-01, V-02  │
     (5)      │          │      │  V-12   │      │ V-03, V-04  │
              ├──────────┼──────┼─────────┼──────┼─────────────┤
   Likely     │          │      │  V-13   │ V-09 │ V-06, V-07  │
     (4)      │          │      │V-16,V-17│ V-10 │             │
              │          │      │  V-18   │      │             │
              ├──────────┼──────┼─────────┼──────┼─────────────┤
  Possible    │          │      │V-20,V-21│ V-15 │             │
     (3)      │          │      │V-22,V-23│ V-24 │             │
              │          │      │  V-26   │ V-25 │             │
              ├──────────┼──────┼─────────┼──────┼─────────────┤
  Unlikely    │          │      │  V-27   │      │             │
     (2)      │          │      │         │      │             │
              ├──────────┼──────┼─────────┼──────┼─────────────┤
    Rare      │  V-39    │ V-34 │V-29,V-30│ V-28 │             │
     (1)      │          │ V-37 │V-35,V-36│ V-31 │             │
              │          │      │         │V-32  │             │
              │          │      │         │V-33  │             │
              └──────────┴──────┴─────────┴──────┴─────────────┘
                         LOW      MEDIUM     HIGH     CRITICAL
```

---

## 10. Recommended Controls & Mitigation

### 10.1 Immediate Actions (Critical — within 48 hours)

| #   | Action                                                                                                                                                           | Addresses        | Effort |
| --- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------- | ------ |
| 1   | **Externalize all secrets** — Use environment variables, Spring Cloud Config, or HashiCorp Vault. Rotate all exposed credentials immediately.                    | V-01             | 4h     |
| 2   | **Integrate Spring Security** — Add `spring-boot-starter-security` with proper SecurityFilterChain, CSRF, security headers, session management.                  | V-02, V-16       | 16h    |
| 3   | **Fix all IDOR vulnerabilities** — Add ownership validation: every query that modifies or reads account data must verify `WHERE account_id = ? AND user_id = ?`. | V-03, V-04, V-05 | 8h     |
| 4   | **Fix thread-safety** — Change instance-level `User user` to local variables in all controller methods.                                                          | V-07             | 1h     |
| 5   | **Add null-check for JWT claims** — Validate `decodeToken()` return value before calling `getSubject()`.                                                         | V-06             | 1h     |

### 10.2 Short-Term Actions (High — within 2 weeks)

| #   | Action                                                                                                                                    | Addresses  | Effort |
| --- | ----------------------------------------------------------------------------------------------------------------------------------------- | ---------- | ------ |
| 6   | **Add `@Transactional` with `SELECT ... FOR UPDATE`** — Wrap financial operations in service-level transactions with pessimistic locking. | V-09, V-25 | 8h     |
| 7   | **Restrict CORS** — Replace wildcard with specific allowed origins.                                                                       | V-10       | 1h     |
| 8   | **Add `@JsonIgnore`** on sensitive User fields (password, token, code).                                                                   | V-11, V-17 | 1h     |
| 9   | **Use `SecureRandom`** with large random space for verification codes and account numbers.                                                | V-12, V-13 | 2h     |
| 10  | **Enable HTTPS** — Configure TLS certificates and redirect HTTP to HTTPS.                                                                 | V-14       | 4h     |
| 11  | **Implement token revocation** — Add a blacklist (Redis/DB) checked on each request.                                                      | V-15       | 8h     |
| 12  | **Fix JWT expiration** — Multiply by 1000 to convert seconds to milliseconds.                                                             | V-08       | 0.5h   |

### 10.3 Medium-Term Actions (Medium — within 1 month)

| #   | Action                                                                                         | Addresses | Effort                   |
| --- | ---------------------------------------------------------------------------------------------- | --------- | ------------------------ | ---- | ---- |
| 13  | **Validate financial amounts** — Reject negative, zero, and unreasonably large values.         | V-18      | 2h                       |
| 14  | **Replace `double` with `BigDecimal`** across all financial logic.                             | V-19      | 8h                       |
| 15  | **Sanitize error responses** — Return generic error messages; log details server-side only.    | V-20      | 2h                       |
| 16  | **Remove debug logging** — Replace `System.out.println` with SLF4J; disable mail debug.        | V-21      | 2h                       |
| 17  | **@Autowire JwtService** in AppInterceptor.                                                    | V-22      | 1h                       |
| 18  | **Add duplicate email check** in registration flow.                                            | V-23      | 1h                       |
| 19  | **Implement rate limiting** — Use Spring's `RateLimiter` or bucket4j on auth/verify endpoints. | V-24      | 4h                       |
| 20  | **Fix validation logic** — Change `&&` to `                                                    |           | `in`RegisterController`. | V-26 | 0.5h |
| 21  | **Optimize Dockerfile** — Use multi-stage build; copy only the JAR.                            | V-27      | 2h                       |

### 10.4 Long-Term Actions (Low — within 3 months)

| #   | Action                                                              | Addresses        | Effort |
| --- | ------------------------------------------------------------------- | ---------------- | ------ |
| 22  | **Upgrade to Spring Boot 3.x** and Java 17+.                        | V-28, V-30, V-39 | 24h    |
| 23  | **Align JJWT dependency versions.**                                 | V-29             | 1h     |
| 24  | **Enable and write security tests.**                                | V-31             | 16h    |
| 25  | **Create dedicated database user** with minimal privileges.         | V-32             | 2h     |
| 26  | **Add password policy** — minimum 8 chars, complexity requirements. | V-33             | 2h     |
| 27  | **Make verification URL configurable** via properties.              | V-34             | 1h     |
| 28  | **Update JWT signing to use `Keys.hmacShaKeyFor()`**.               | V-35, V-36       | 2h     |

---

## 11. Risk Treatment Plan

| Risk ID          | Treatment Strategy                                     | Residual Risk After Treatment |
| ---------------- | ------------------------------------------------------ | ----------------------------- |
| V-01             | **Mitigate** — Externalize + rotate secrets            | Low                           |
| V-02             | **Mitigate** — Implement Spring Security               | Low                           |
| V-03, V-04, V-05 | **Mitigate** — Add ownership validation                | Low                           |
| V-06             | **Mitigate** — Null-check JWT claims                   | Negligible                    |
| V-07             | **Mitigate** — Use local variables                     | Negligible                    |
| V-08             | **Mitigate** — Fix calculation                         | Negligible                    |
| V-09             | **Mitigate** — Database locking + @Transactional       | Low                           |
| V-10             | **Mitigate** — Restrict CORS origins                   | Low                           |
| V-11, V-17       | **Mitigate** — @JsonIgnore on sensitive fields         | Negligible                    |
| V-12, V-13       | **Mitigate** — SecureRandom + larger space             | Low                           |
| V-14             | **Mitigate** — Enable TLS                              | Low                           |
| V-15             | **Mitigate** — Token blacklist                         | Low                           |
| V-16             | **Mitigate** — CSRF tokens via Spring Security         | Low                           |
| V-18             | **Mitigate** — Input validation                        | Negligible                    |
| V-19             | **Mitigate** — BigDecimal                              | Negligible                    |
| V-20             | **Mitigate** — Generic error messages                  | Low                           |
| V-21             | **Mitigate** — Structured logging, remove debug output | Low                           |
| V-22–V-27        | **Mitigate** — Configuration fixes                     | Low                           |
| V-28–V-39        | **Accept/Mitigate** — Upgrades & housekeeping          | Low                           |

---

## 12. Appendix — Summary Tables

### A. Findings by Category

| Category                           | Critical | High  | Medium | Low    | Total  |
| ---------------------------------- | -------- | ----- | ------ | ------ | ------ |
| Broken Access Control (A01)        | 4        | 2     | 0      | 0      | 6      |
| Cryptographic Failures (A02)       | 1        | 2     | 0      | 1      | 4      |
| Injection / Input Validation (A03) | 0        | 0     | 1      | 0      | 1      |
| Insecure Design (A04)              | 1        | 1     | 2      | 0      | 4      |
| Security Misconfiguration (A05)    | 0        | 1     | 2      | 3      | 6      |
| Vulnerable Components (A06)        | 0        | 0     | 0      | 4      | 4      |
| Authentication Failures (A07)      | 2        | 2     | 1      | 2      | 7      |
| Data Integrity Failures (A08)      | 0        | 1     | 2      | 0      | 3      |
| Logging & Monitoring (A09)         | 0        | 0     | 2      | 0      | 2      |
| Other (Code Quality)               | 0        | 0     | 0      | 2      | 2      |
| **Total**                          | **8**    | **9** | **10** | **12** | **39** |

### B. Compliance Gap Analysis

| Standard / Requirement                    | Status        | Key Gaps                                                                             |
| ----------------------------------------- | ------------- | ------------------------------------------------------------------------------------ |
| **PCI DSS** (Payment Card Industry)       | Non-Compliant | Hardcoded credentials, no encryption in transit, no access control, no audit logging |
| **OWASP ASVS Level 1**                    | Non-Compliant | Fails authentication, access control, input validation, cryptography sections        |
| **GDPR Art. 32** (Security of Processing) | Non-Compliant | Inadequate technical measures, password exposure, no data protection by design       |
| **SOC 2 Type II**                         | Non-Compliant | No access controls, no change management (tests skipped), no monitoring              |

### C. Estimated Remediation Effort

| Priority             | Items          | Estimated Effort |
| -------------------- | -------------- | ---------------- |
| Immediate (Critical) | 5 actions      | ~30 hours        |
| Short-Term (High)    | 7 actions      | ~24.5 hours      |
| Medium-Term (Medium) | 9 actions      | ~22.5 hours      |
| Long-Term (Low)      | 7 actions      | ~48 hours        |
| **Total**            | **28 actions** | **~125 hours**   |

---

_End of Risk Assessment Report_
