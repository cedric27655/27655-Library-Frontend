package kigali.clinic.rw.service;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import kigali.clinic.rw.domain.AppointmentStatus;
import kigali.clinic.rw.repository.AppointmentRepository;
import kigali.clinic.rw.repository.DoctorRepository;
import kigali.clinic.rw.repository.OfficeRepository;
import kigali.clinic.rw.repository.PatientRepository;
import kigali.clinic.rw.repository.PrescriptionRepository;

@Service
public class DashboardService {

    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final OfficeRepository officeRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public DashboardService(PatientRepository patientRepository,
                            DoctorRepository doctorRepository,
                            OfficeRepository officeRepository,
                            AppointmentRepository appointmentRepository,
                            PrescriptionRepository prescriptionRepository) {
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.officeRepository = officeRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    // D4: Clinic dashboard summary
    public Map<String, Long> getDashboardSummary() {
        Map<String, Long> summary = new LinkedHashMap<>();

        long patientsCount = patientRepository.count();
        long doctorsCount = doctorRepository.count();
        long freeOfficesCount = (long) officeRepository.findFreeOffices().size();
        long appointmentsTodayCount = appointmentRepository.countByAppointmentDate(LocalDate.now());
        long scheduledCount = appointmentRepository.countByStatus(AppointmentStatus.SCHEDULED);
        long prescriptionsCount = prescriptionRepository.count();

        summary.put("patients", patientsCount);
        summary.put("doctors", doctorsCount);
        summary.put("freeOffices", freeOfficesCount);
        summary.put("appointmentsToday", appointmentsTodayCount);
        summary.put("scheduled", scheduledCount);
        summary.put("prescriptions", prescriptionsCount);

        return summary;
    }
}
