import React from 'react';
import { useTheme } from '../../context/ThemeContext';

const ThemeToggle = ({ className = "" }) => {
    const { isDarkMode, toggleTheme } = useTheme();

    return (
        <div className={`flex items-center gap-2 ${className}`}>
            <span className="text-xs font-medium text-gray-500 dark:text-gray-400 uppercase">
                {isDarkMode ? 'Dark' : 'Light'}
            </span>
            <button 
                onClick={toggleTheme} 
                className={`w-12 h-6 flex items-center rounded-full p-1 cursor-pointer transition-colors ${isDarkMode ? 'bg-light-primary' : 'bg-gray-300'}`}
                aria-label="Toggle Dark Mode"
                type="button"
            >
                <span 
                    className={`h-4 w-4 rounded-full bg-white shadow-md transform transition-transform duration-200 ease-in-out ${isDarkMode ? 'translate-x-6' : 'translate-x-0'}`}
                ></span>
            </button>
        </div>
    );
};

export default ThemeToggle;