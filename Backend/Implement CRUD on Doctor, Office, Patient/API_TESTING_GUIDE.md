# Kigali Clinic API - Complete CRUD & Interrogate the Clinic Testing Guide

This project implements:
1. Complete **CRUD operations** on **Doctor**, **Office**, **Patient**, and **Specialization**.
2. **Many-to-Many relationship** between **Doctor** and **Specialization** via a join table (`doctor_specialization`).
3. **Appointment saving** via `AppointmentService.saveAppointment(...)`.
4. **Class Practical Exercise 4 - "Interrogate the Clinic"** queries (Derived, JPQL, Aggregate, JOIN across 2 relationships, and Pagination).
5. Comprehensive automated testing suites for both **Postman** and **Bruno**.

---

## 1. Running the Spring Boot Application

Before testing with Postman or Bruno, ensure the API is running:

1. Ensure PostgreSQL is running locally on port `5432` with database `clinic` (configured in `src/main/resources/application.properties`).
2. Run the startup script:
   ```cmd
   run-clinic.cmd
   ```
   Or via Maven Wrapper:
   ```cmd
   .\mvnw.cmd spring-boot:run
   ```
3. The server starts at `http://localhost:8085`.
4. Console shows all SQL statements in real-time (`spring.jpa.show-sql=true`).

---

## 2. API Endpoints Reference

Base URL: `http://localhost:8085`

### A. Specialization CRUD & Doctor Many-to-Many

| Resource | HTTP Method | Endpoint | Request Body | Success Code | Description |
|---|---|---|---|---|---|
| **Specialization** | `POST` | `/api/specialization` | `{"name": "Cardiology"}` | `201 Created` | Create new specialization |
| | `GET` | `/api/specialization` | None | `200 OK` | List all specializations |
| | `GET` | `/api/specialization/{id}` | None | `200 OK` | Get specialization by UUID |
| | `PUT` | `/api/specialization/{id}` | `{"name": "Pediatric Cardiology"}` | `200 OK` | Update specialization name |
| | `DELETE` | `/api/specialization/{id}` | None | `204 No Content` | Delete specialization |
| | `GET` | `/api/specialization/{id}/doctors` | None | `200 OK` | Get all doctors having this specialization |
| **Many-to-Many** | `POST` | `/api/doctor/{doctorId}/specialization/{specializationId}` | None | `200 OK` | Link specialization to doctor |
| | `DELETE` | `/api/doctor/{doctorId}/specialization/{specializationId}` | None | `200 OK` | Unlink specialization from doctor |
| | `GET` | `/api/doctor/{doctorId}/specializations` | None | `200 OK` | Get all specializations of a doctor |

### B. Doctor, Office, Patient CRUD

| Resource | HTTP Method | Endpoint | Request Body | Success Code | Description |
|---|---|---|---|---|---|
| **Office** | `POST` | `/api/office` | `{"name": "Room 1", "officeNumber": 101}` | `201 Created` | Create office |
| | `GET` | `/api/office` | None | `200 OK` | List offices |
| | `GET` | `/api/office/{id}` | None | `200 OK` | Get office |
| | `PUT` | `/api/office/{id}` | `{"name": "Room 1 (Updated)", "officeNumber": 101}` | `200 OK` | Update office |
| | `DELETE` | `/api/office/{id}` | None | `204 No Content` | Delete office |
| **Doctor** | `POST` | `/api/doctor` | `{"firstName": "Aline", "lastName": "Mukamana", "dateOfBirth": "1988-04-12", "officeId": "<uuid>", "specializationIds": ["<uuid>"]}` | `201 Created` | Create doctor |
| | `GET` | `/api/doctor` | None | `200 OK` | List doctors (includes specializations) |
| | `GET` | `/api/doctor/{id}` | None | `200 OK` | Get doctor |
| | `PUT` | `/api/doctor/{id}` | `{"firstName": "Dr. Aline", ...}` | `200 OK` | Update doctor |
| | `DELETE` | `/api/doctor/{id}` | None | `204 No Content` | Delete doctor |
| **Patient** | `POST` | `/api/patient` | `{"firstName": "Jean", "lastName": "Uwimana", "dateOfBirth": "1995-08-20"}` | `201 Created` | Create patient |
| | `GET` | `/api/patient` | None | `200 OK` | List patients |
| | `GET` | `/api/patient/{id}` | None | `200 OK` | Get patient |
| | `PUT` | `/api/patient/{id}` | `{"firstName": "Jean-Paul", ...}` | `200 OK` | Update patient |
| | `DELETE` | `/api/patient/{id}` | None | `204 No Content` | Delete patient |

### C. Appointment & Practical Exercise 4 Queries

| Exercise Query | HTTP Method | Endpoint | Implementation in Code |
|---|---|---|---|
| **Save Appointment** | `POST` | `/api/appointment/save` | `AppointmentService.saveAppointment(appointment, patientId, doctorId)` |
| **1. Derived Query** | `GET` | `/api/appointment/query/doctor/{doctorId}/pending` | `findByDoctorIdAndStatusOrderByDateAsc(doctorId, PENDING)` |
| **2. JPQL Query** | `GET` | `/api/appointment/query/pending-past-due-report` | `@Query("SELECT patientName, doctorName, date ... WHERE status = PENDING AND date < CURRENT_DATE")` |
| **3. Aggregate Query** | `GET` | `/api/appointment/query/busiest-doctors` | `@Query("SELECT d.id, d.firstName, d.lastName, COUNT(a) ... GROUP BY d.id ORDER BY COUNT(a) DESC")` |
| **4. JOIN 2 Relationships** | `GET` | `/api/appointment/query/by-specialization?name={name}` | `@Query("SELECT a FROM Appointment a JOIN a.doctor d JOIN d.specializations s WHERE ...")` |
| **5. Pagination Query** | `GET` | `/api/appointment/query/doctor/{doctorId}/page?page=1&size=10` | `findByDoctorId(doctorId, PageRequest.of(0, 10, Sort.by("date")))` |

---

## 3. SQL Statements Generated by Hibernate in Console

When the 5 exercise queries are invoked, Hibernate executes the following SQL statements in the application console:

### 1. Derived: All PENDING appointments of a doctor, ordered by date
```sql
select a1_0.id, a1_0.appointment_date, a1_0.doctor_id, a1_0.patient_id, a1_0.reason, a1_0.status 
from appointment a1_0 
where a1_0.doctor_id=? and a1_0.status=? 
order by a1_0.appointment_date;
```

### 2. JPQL: PENDING appointments whose date has passed
```sql
select (p1_0.first_name||' '||p1_0.last_name), (d1_0.first_name||' '||d1_0.last_name), a1_0.appointment_date 
from appointment a1_0 
join patient p1_0 on p1_0.id=a1_0.patient_id 
join doctor d1_0 on d1_0.id=a1_0.doctor_id 
where a1_0.status='PENDING' and a1_0.appointment_date<current_date;
```

### 3. Aggregate: Number of appointments per doctor, busiest first
```sql
select d1_0.id, d1_0.first_name, d1_0.last_name, count(a1_0.id) 
from doctor d1_0 
left join appointment a1_0 on d1_0.id=a1_0.doctor_id 
group by d1_0.id, d1_0.first_name, d1_0.last_name 
order by count(a1_0.id) desc;
```

### 4. JOIN across 2 relationships: Appointments of doctors having a given specialization
```sql
select a1_0.id, a1_0.appointment_date, a1_0.doctor_id, a1_0.patient_id, a1_0.reason, a1_0.status 
from appointment a1_0 
join doctor d1_0 on d1_0.id=a1_0.doctor_id 
join doctor_specialization s1_0 on d1_0.id=s1_0.doctor_id 
join specialization s1_1 on s1_1.id=s1_0.specialization_id 
where lower(s1_1.name)=lower(?);
```

### 5. Pagination: Page 1 of a doctor's appointments, 10 per page, sorted by date
```sql
select a1_0.id, a1_0.appointment_date, a1_0.doctor_id, a1_0.patient_id, a1_0.reason, a1_0.status 
from appointment a1_0 
where a1_0.doctor_id=? 
order by a1_0.appointment_date 
fetch first ? rows only;
```

---

## 4. Testing with Postman

Files located in [`postman/`](file:///c:/Users/NCUD/OneDrive/Desktop/WEB%20TECH%20Assignment/Implement%20CRUD%20on%20Doctor,%20Office,%20Patient/postman):
- **Collection**: `Clinic_API.postman_collection.json`
- **Environment**: `Clinic_API_Environment.postman_environment.json`

### Import & Run:
1. Open **Postman** -> Click **Import** (top-left).
2. Select both files: `Clinic_API.postman_collection.json` and `Clinic_API_Environment.postman_environment.json`.
3. Select **Clinic API - Local Environment** from the top-right environment selector.
4. Run requests individually or right-click the collection -> **Run collection**.

---

## 5. Testing with Bruno

Directory structure located in [`bruno/`](file:///c:/Users/NCUD/OneDrive/Desktop/WEB%20TECH%20Assignment/Implement%20CRUD%20on%20Doctor,%20Office,%20Patient/bruno):
```
bruno/
├── bruno.json
├── environments/
│   └── Local.bru
├── Office/
│   ├── 1-Create-Office.bru
│   ├── ...
├── Doctor/
│   ├── 1-Create-Doctor.bru
│   ├── ...
├── Patient/
│   ├── 1-Create-Patient.bru
│   ├── ...
├── Specialization/
│   ├── 1-Create-Specialization.bru
│   ├── 2-Get-All-Specializations.bru
│   ├── 3-Get-Specialization-By-ID.bru
│   ├── 4-Update-Specialization.bru
│   └── 5-Delete-Specialization.bru
├── Doctor-Specialization/
│   ├── 1-Assign-Specialization-To-Doctor.bru
│   ├── 2-Get-Doctor-Specializations.bru
│   ├── 3-Get-Doctors-With-Specialization.bru
│   └── 4-Remove-Specialization-From-Doctor.bru
├── Exercise-4-Queries/
│   ├── 1-Save-Appointment-Past.bru
│   ├── 2-Save-Appointment-Future.bru
│   ├── 3-Query1-Pending-Doctor-Appointments.bru
│   ├── 4-Query2-Pending-Past-Due.bru
│   ├── 5-Query3-Busiest-Doctors.bru
│   ├── 6-Query4-Appointments-By-Specialization.bru
│   └── 7-Query5-Doctor-Appointments-Page.bru
└── Edge-Cases/
    ├── 1-Duplicate-Office-Number.bru
    ├── 2-Occupied-Office-Conflict.bru
    └── 3-Non-Existent-Office.bru
```

### Open & Run:
1. Open **Bruno** -> Click **Open Collection** -> Select folder `bruno`.
2. Select **Local** environment (top-right).
3. Run requests or use the folder/collection runner. All variables (`doctorId`, `patientId`, `specializationId`, `officeId`, `appointmentId`) are chained automatically.
