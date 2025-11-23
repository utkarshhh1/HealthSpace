import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const DashboardRouter = () => {
    const { user, isAuthenticated, isLoading } = useAuth();

    if (isLoading) {
        return <div className="p-8 text-center text-xl text-gray-700 dark:text-gray-200">Loading user session...</div>;
    }

    if (!isAuthenticated || !user?.role) {
        return <Navigate to="/login" replace />;
    }

    // Role-Based Redirection logic
    switch (user.role) {
        case 'PATIENT':
            return <Navigate to="/patient/appointments" replace />;
        case 'DOCTOR':
            return <Navigate to="/doctor/schedule" replace />;
        case 'HOSPITAL_ADMIN':
            return <Navigate to="/hospital-admin/pending-doctors" replace />;
        case 'ADMIN': 
            return <Navigate to="/admin/hospitals" replace />;
        default:
            return <Navigate to="/unauthorized" replace />;
    }
};

export default DashboardRouter;