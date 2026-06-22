# NexusHR Backend

AI-enabled enterprise HR and workforce intelligence backend built with Java 21 and Spring Boot 3.3.

## Capabilities

- Employee lifecycle management with onboarding/offboarding approval workflows, profile JSONB data, role assignment, and document metadata.
- Attendance and leave management with biometric punch simulation, approval workflow, dashboard metrics, and balance calculation.
- Payroll processing with automated gross/net salary calculation, tax deduction, run approval, and CSV payslip export.
- Performance management with goals, reviews, 360-degree weighted ratings, scorecards, and trend-ready history.
- AI workforce insights with attrition risk, engagement scoring, skill gap analysis, and recommendations.
- Admin/manager dashboards with role-based metrics and CSV export.
- Notification queue for email/SMS/in-app delivery simulation with success-rate tracking.
- JWT authentication, role-based access control, Redis cache, PostgreSQL JSONB, Prometheus metrics, Sentry hook, Docker, Helm, and GitHub Actions.

## Run Locally

```bash
docker compose up --build
```

Default admin:

```text
email: admin@nexushr.local
password: ChangeMe123!
```

Login:

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@nexushr.local","password":"ChangeMe123!"}'
```

## Useful Endpoints

- `POST /api/v1/employees`
- `PUT /api/v1/employees/{id}/role`
- `POST /api/v1/employees/{id}/offboarding`
- `POST /api/v1/attendance/biometric-punch`
- `POST /api/v1/attendance/leave-requests`
- `POST /api/v1/payroll/runs`
- `GET /api/v1/payroll/runs/{id}/export.csv`
- `POST /api/v1/performance/reviews`
- `GET /api/v1/insights/organization`
- `GET /api/v1/dashboard/metrics`
- `GET /actuator/prometheus`

## Deploy

```bash
helm upgrade --install nexushr ./helm/nexushr
```
