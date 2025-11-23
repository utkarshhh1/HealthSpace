import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

const Unauthorized = () => {
  const { user } = useAuth();

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900 p-6 transition-colors">
      <div className="text-center p-10 bg-white dark:bg-gray-800 rounded-xl shadow-2xl border border-red-200 dark:border-red-900 max-w-lg">
        <div className="text-8xl mb-4 text-red-500">🚫</div>
        <h1 className="text-3xl font-bold text-light-textMain dark:text-white mb-2">Access Denied (403)</h1>
        <p className="text-lg text-gray-700 dark:text-gray-300 mb-6">
          Your current role <strong>{user?.role || 'Unknown'}</strong> does not have permission to view this page.
        </p>
        <Link 
          to="/" 
          className="px-6 py-3 text-white font-bold bg-light-primary rounded-lg hover:opacity-90 transition duration-200 inline-block"
        >
          Go to Dashboard
        </Link>
      </div>
    </div>
  );
};

export default Unauthorized;