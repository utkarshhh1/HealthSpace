import React, { Suspense, lazy } from 'react';
import { Routes, Route } from 'react-router-dom';

// --- Imports ---
import Layout from './components/layout/Layout';
import ProtectedRoute from './components/auth/ProtectedRoute';
import AiAssistant from './components/ui/AiAssistant'; 

// --- Lazy Load Pages ---
// Auth Pages
const Login = lazy(() => import('./pages/auth/Login'));
const Register = lazy(() => import('./pages/auth/Register'));

// Core Pages
const Unauthorized = lazy(() => import('./pages/core/Unauthorized'));
const DashboardRouter = lazy(() => import('./pages/core/DashboardRouter'));

// Feature Pages - Admin
const AdminProfile = lazy(() => import('./pages/admin/AdminProfile'));
const HospitalList = lazy(() => import('./pages/admin/HospitalList'));
const RegisterHospital = lazy(() => import('./pages/admin/RegisterHospital'));
const MedicineManagement = lazy(() => import('./pages/admin/MedicineManagement'));

// Feature Pages - Hospital Admin
const HospitalAdminDashboard = lazy(() => import('./pages/hospitalAdmin/HospitalAdminDashboard'));

// Feature Pages - Doctor
const DoctorProfile = lazy(() => import('./pages/doctor/DoctorProfile'));
const DoctorSchedule = lazy(() => import('./pages/doctor/DoctorSchedule'));
const CreatePrescription = lazy(() => import('./pages/doctor/CreatePrescription'));
const DoctorPrescriptions = lazy(() => import('./pages/doctor/DoctorPrescriptions'));

// Feature Pages - Patient
const PatientProfile = lazy(() => import('./pages/patient/PatientProfile'));
const BookAppointment = lazy(() => import('./pages/patient/BookAppointment'));
const PatientAppointments = lazy(() => import('./pages/patient/PatientAppointments'));
const PatientPrescriptions = lazy(() => import('./pages/patient/PatientPrescriptions'));

// --- Loading Component ---
const LoadingFallback = () => (
  <div className="flex items-center justify-center h-screen bg-gray-50 dark:bg-gray-900">
    <div className="text-xl font-semibold text-light-primary animate-pulse">
      Loading HealthSpace...
    </div>
  </div>
);

function App() {
  return (
    <Suspense fallback={<LoadingFallback />}>
      
      {/* FIX: AiAssistant must be OUTSIDE of <Routes> to persist globally */}
      <AiAssistant />

      <Routes>
        {/* Public Routes */}
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/unauthorized" element={<Unauthorized />} />

        {/* Root Redirect */}
        <Route path="/" element={<DashboardRouter />} />
        <Route path="/dashboard" element={<DashboardRouter />} />

        {/* Protected Routes */}
        <Route element={<Layout />}>
          
          {/* ADMIN */}
          <Route element={<ProtectedRoute allowedRoles={['ADMIN']} />}>
            <Route path="/admin/dashboard" element={<div>Platform Admin Overview</div>} />
            <Route path="/admin/hospitals" element={<HospitalList />} />
            <Route path="/admin/hospitals/register" element={<RegisterHospital />} />
            <Route path="/admin/medicines" element={<MedicineManagement />} />
            <Route path="/admin/profile" element={<AdminProfile />} />
          </Route>

          {/* HOSPITAL ADMIN */}
          <Route element={<ProtectedRoute allowedRoles={['HOSPITAL_ADMIN']} />}>
            <Route path="/hospital-admin/dashboard" element={<div>Hospital Admin Overview</div>} />
            <Route path="/hospital-admin/pending-doctors" element={<HospitalAdminDashboard />} />
            <Route path="/hospital-admin/profile" element={<AdminProfile />} />
          </Route>

          {/* DOCTOR */}
          <Route element={<ProtectedRoute allowedRoles={['DOCTOR']} />}>
            <Route path="/doctor/profile" element={<DoctorProfile />} />
            <Route path="/doctor/schedule" element={<DoctorSchedule />} />
            <Route path="/doctor/create-prescription/:appointmentId" element={<CreatePrescription />} />
            <Route path="/doctor/prescriptions" element={<DoctorPrescriptions />} />
          </Route>

          {/* PATIENT */}
          <Route element={<ProtectedRoute allowedRoles={['PATIENT']} />}>
            <Route path="/patient/profile" element={<PatientProfile />} />
            <Route path="/patient/appointments" element={<PatientAppointments />} />
            <Route path="/patient/book" element={<BookAppointment />} />
            <Route path="/patient/prescriptions" element={<PatientPrescriptions />} />
          </Route>

          <Route path="*" element={<div>Page Not Found</div>} />
        </Route>
      </Routes>
    </Suspense>
  );
}

export default App;