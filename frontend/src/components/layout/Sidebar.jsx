import React from 'react';
import { Link, useLocation, useNavigate } from "react-router-dom";

// --- FIX: Corrected Import Paths ---
import { useAuth } from "../../context/AuthContext";   
import { useTheme } from "../../context/ThemeContext"; 
// -----------------------------------

const Sidebar = () => {
    const navigate = useNavigate();
    const location = useLocation();
    const { user, logout } = useAuth();
    const { isDarkMode, toggleTheme } = useTheme();
    const role = user?.role;
    
    const handleLogout = () => {
        logout(); 
        navigate("/login");
    };

    const NavLink = ({ to, children }) => {
        const isActive = location.pathname.startsWith(to);
        const baseClasses = "flex items-center px-4 py-3 rounded-lg transition-all font-medium";
        const activeClasses = "bg-light-primary text-white dark:bg-light-primary dark:text-white shadow-md";
        const hoverClasses = "hover:bg-gray-100 dark:hover:bg-gray-700 text-light-textBody dark:text-gray-200";

        return (
            <Link to={to} className={`${baseClasses} ${isActive ? activeClasses : hoverClasses}`}>
                {children}
            </Link>
        );
    };

    const profilePath = role ? `/${role.toLowerCase().replace('_', '-')}/profile` : '/profile';

    return (
        <div className="h-screen w-64 bg-white dark:bg-gray-800 border-r border-light-border dark:border-gray-700 flex flex-col shadow-lg fixed left-0 top-0 text-light-textBody dark:text-gray-200 transition-colors z-20">
            <div className="p-6 border-b border-light-border dark:border-gray-700 bg-white dark:bg-gray-800">
                <h1 className="text-2xl font-bold text-light-textMain dark:text-white font-sans">HealthSpace</h1>
                <p className="text-xs text-gray-500 mt-1">Medical Vault</p>
            </div>

            <nav className="flex-1 p-4 space-y-2 overflow-y-auto">
                <div className="text-xs font-semibold text-gray-400 uppercase tracking-wider mb-2 px-4">Menu</div>
                
                {role === "PATIENT" && (
                    <>
                        <NavLink to="/patient/appointments">My Appointments</NavLink>
                        <NavLink to="/patient/prescriptions">Medical History</NavLink>
                        <NavLink to="/patient/book">Book Appointment</NavLink>
                    </>
                )}

                {role === "DOCTOR" && (
                    <>
                        <NavLink to="/doctor/schedule">Patient Schedule</NavLink>
                        <NavLink to="/doctor/prescriptions">Issued Prescriptions</NavLink>
                    </>
                )}

                {role === "HOSPITAL_ADMIN" && (
                    <NavLink to="/hospital-admin/pending-doctors">Doctor Verification</NavLink>
                )}

                {role === "ADMIN" && (
                    <>
                        <NavLink to="/admin/hospitals">View Hospitals</NavLink>
                        <NavLink to="/admin/hospitals/register">Register Hospital</NavLink>
                        <NavLink to="/admin/medicines">Medicine Catalog</NavLink>
                    </>
                )}
                
                {user && (
                    <div className="border-t border-light-border dark:border-gray-700 pt-2 mt-2">
                        <NavLink to={profilePath}>Profile / Settings</NavLink>
                    </div>
                )}
            </nav>

            <div className="p-4 border-t border-light-border dark:border-gray-700 bg-white dark:bg-gray-800 space-y-3">
                <div className="flex items-center justify-between px-4 py-1">
                    <span className="text-sm font-medium text-light-textMain dark:text-gray-200">Dark Mode</span>
                    <button onClick={toggleTheme} className={`w-12 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors ${isDarkMode ? 'bg-light-primary' : 'bg-gray-300'}`}>
                        <span className={`h-4 w-4 rounded-full bg-white shadow-md transform transition-transform ${isDarkMode ? 'translate-x-6' : 'translate-x-0'}`}></span>
                    </button>
                </div>
                <button onClick={handleLogout} className="w-full px-4 py-2 bg-red-50 text-red-600 rounded-lg hover:bg-red-100 font-bold text-sm border border-red-100 dark:bg-red-900 dark:text-red-300 dark:hover:bg-red-800">
                    Sign Out
                </button>
            </div>
        </div>
    );
};

export default Sidebar;