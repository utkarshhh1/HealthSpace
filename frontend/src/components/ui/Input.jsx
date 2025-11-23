import React from 'react';

const Input = ({ label, error, endIcon, ...props }) => {
  return (
    <div className="w-full">
      {label && (
        <label className="block text-sm font-medium text-light-textMain dark:text-gray-300 mb-1">
          {label}
        </label>
      )}
      <div className="relative">
        <input
          className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:outline-none transition-colors 
            bg-white dark:bg-gray-700 text-light-textBody dark:text-gray-100
            ${error 
              ? 'border-red-500 focus:ring-red-200' 
              : 'border-gray-300 dark:border-gray-600 focus:ring-light-primary'
            }
            ${props.disabled ? 'bg-gray-100 dark:bg-gray-800 cursor-not-allowed' : ''}
            ${endIcon ? 'pr-10' : ''} 
          `}
          {...props}
        />
        {/* Render the icon if provided */}
        {endIcon && (
          <div className="absolute inset-y-0 right-0 flex items-center pr-3">
            {endIcon}
          </div>
        )}
      </div>
      {error && <p className="text-xs text-red-500 mt-1">{error}</p>}
    </div>
  );
};

export default Input;