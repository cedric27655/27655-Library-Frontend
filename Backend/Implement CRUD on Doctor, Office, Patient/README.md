# Kigali Clinic Backend — Quiz 2: Completing the Backend

Spring Boot backend application implementing comprehensive clinic operations for Doctors, Offices, Patients, Appointments, and Prescriptions.

## Key Features & Quiz 2 Endpoints

- **Part A (Doctors & Offices)**:
  - `PUT /api/doctors/{doctorId}/specializations/{specializationId}`: Add specialization to doctor (Owning side update).
  - `DELETE /api/doctors/{doctorId}/specializations/{specializationId}`: Remove specialization link from doctor.
  - `GET /api/offices/free`: JPQL query finding offices without an assigned doctor.
  - `PATCH /api/doctors/{doctorId}/office?officeNumber=...`: Move doctor to an office with occupancy validation.

- **Part B (Appointment Rules)**:
  - `POST /api/appointments/save`: Safe booking (rejects past dates, forces `SCHEDULED` status).
  - `PATCH /api/appointments/{id}/status?status=...`: Enforces allowed finite state transitions.
  - `PATCH /api/appointments/{id}/reschedule?newDate=...`: Reschedules appointments with double-booking checks.

- **Part C (Prescriptions)**:
  - `POST /api/prescriptions/save?appointmentId=...`: Create prescription (restricted to `COMPLETED` appointments).
  - `GET /api/prescriptions/appointment/{appointmentId}`: Derived query for appointment prescriptions.
  - `GET /api/prescriptions/patient/{ssNumber}`: JPQL single-valued path navigation for patient prescription history.
  - `PUT /api/prescriptions/update/{id}` & `DELETE /api/prescriptions/delete/{id}`: CRUD operations.
  - `DELETE /api/appointments/{id}`: Protected deletion (blocks appointment deletion if prescriptions exist with HTTP 409).

- **Part D (Agenda & Reports)**:
  - `GET /api/doctors/{doctorId}/agenda?date=...`: Doctor agenda excluding cancelled appointments.
  - `GET /api/patients/{ssNumber}/upcoming`: Upcoming appointments ordered soonest first.
  - `GET /api/doctors/search?name=...`: Case-insensitive doctor search by first or last name.
  - `GET /api/dashboard/summary`: Clinic statistics summary returned as an ordered `LinkedHashMap`.

- **Bonus Queries**:
  - `GET /api/prescriptions/stats/top-medicines`: JPQL group by count of prescribed medicines.
  - `GET /api/doctors/workload?start=...&end=...`: JPQL doctor workload over date range.

## How to Run the Application

```powershell
.\run-clinic.cmd
```
The server starts on port `8085` (`http://localhost:8085`).

## API Testing with Postman

The repository contains dedicated Postman test collections for each quiz stage:

### Quiz 2: Completing the Backend (`clinic-quiz2.postman_collection.json`)
1. Open Postman and import `clinic-quiz2.postman_collection.json` located at the root of the project.
2. Run the folder **Step 0 - Seed Data** to populate test data and extract variables.
3. Run the requests in **Part A (A1–A4)**, **Part B (B1–B3)**, **Part C (C1–C4)**, **Part D (D1–D4)**, and **Part E (Bonus 1 & 2)**.

### Quiz 1: JPA & JPQL In-Class Quiz (`clinic-quiz.postman_collection.json`)
- Use `clinic-quiz.postman_collection.json` for testing earlier Quiz 1 requirements (derived queries, JPQL with joins, and aggregate reports).

