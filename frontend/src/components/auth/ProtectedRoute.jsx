import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../../context/authContext"; // UPDATED PATH

const ProtectedRoute = ({ allowedRoles = [] }) => { 
  const { isAuthenticated, user, isLoading } = useAuth();

  if (isLoading) {
    return <div className="p-8 text-center text-xl text-gray-700 dark:text-gray-200">Loading Session...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (allowedRoles.length > 0 && (!user || !allowedRoles.includes(user.role))) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
};

export default ProtectedRoute;