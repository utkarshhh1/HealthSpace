import axios from "axios";

const API_BASE_URL = "http://localhost:8080/api";

const api = axios.create({
    baseURL: API_BASE_URL,
    headers: {
        "Content-Type": "application/json",
    },
});

// Request Interceptor
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem("jwtToken");
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

// Response Interceptor
api.interceptors.response.use(
    (response) => response,
    (error) => Promise.reject(error)
);

// ================= API ENDPOINTS =================

// --- AUTH ---
export const registerUser = (userData) => api.post("/auth/register", userData);
export const loginUser = (authData) => api.post("/auth/login", authData);

// --- PROFILES ---
export const getPatientProfile = (userId) => api.get(`/profiles/patient/${userId}`);
export const createPatientProfile = (userId, profileData) => api.post(`/profiles/patient/${userId}`, profileData);

export const getDoctorProfile = (userId) => api.get(`/profiles/doctor/${userId}`);
export const createDoctorProfile = (userId, profileData) => api.post(`/profiles/doctor/${userId}`, profileData);
export const getAllVerifiedDoctors = () => api.get("/profiles/doctor/verified");

// --- HOSPITAL ADMIN WORKFLOW ---
// Register their own hospital (Creates Hospital + Links Profile)
export const registerOwnHospital = (hospitalData) => api.post("/hospital-admin/register-hospital", hospitalData);
// Get status (Pending/Active)
export const getHospitalAdminProfile = () => api.get("/hospital-admin/my-profile");

// --- HOSPITALS (SUPER ADMIN) ---
export const getAllHospitals = () => api.get("/hospitals"); // Public Active List
export const getAllHospitalsAdmin = () => api.get("/hospitals/all"); // Admin All List
export const getHospitalById = (hospitalId) => api.get(`/hospitals/${hospitalId}`);
export const registerHospital = (hospitalData) => api.post("/hospitals/register", hospitalData);
export const approveHospital = (id) => api.put(`/hospitals/approve/${id}`);

// --- MEDICINES ---
export const getAllMedicines = () => api.get("/medicines");
export const searchMedicines = (query) => api.get(`/medicines/search?query=${query}`);
export const addMedicine = (medicineData) => api.post("/medicines/add", medicineData);

// --- APPOINTMENTS ---
export const bookAppointment = (apptData) => api.post("/appointments/book", apptData);
export const getPatientAppointments = (patientId) => api.get(`/appointments/patient/${patientId}`);
export const getDoctorAppointments = (doctorId) => api.get(`/appointments/doctor/${doctorId}`);
export const getAppointmentById = (apptId) => api.get(`/appointments/${apptId}`);
export const updateAppointmentStatus = (apptId, status) => api.put(`/appointments/cancel/${apptId}`, { status }); 

// --- PRESCRIPTIONS ---
export const createPrescription = (rxData) => api.post("/prescriptions/create", rxData);
export const getPrescriptionsForPatient = (patientId) => api.get(`/prescriptions/patient/${patientId}`);
export const getPrescriptionsForDoctor = (doctorId) => api.get(`/prescriptions/doctor/${doctorId}`);
export const getPatientsForDoctor = (doctorId) => api.get(`/prescriptions/doctor/${doctorId}/patients`);

// --- HOSPITAL ADMIN DASHBOARD ---
export const getPendingDoctors = () => api.get("/hospital-admin/pending-doctors");
export const approveDoctor = (profileId) => api.put(`/hospital-admin/approve-doctor/${profileId}`);
export const rejectDoctor = (profileId) => api.put(`/hospital-admin/reject-doctor/${profileId}`);
export const getHospitalAppointments = () => api.get("/hospital-admin/appointments");
export const getHospitalPrescriptions = () => api.get("/hospital-admin/prescriptions");

export default api;