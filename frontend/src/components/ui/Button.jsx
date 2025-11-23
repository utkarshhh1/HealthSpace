import React from 'react';

const Button = ({ children, variant = 'primary', isLoading, className = '', ...props }) => {
  const baseStyles = "w-full py-3 font-bold rounded-lg transition duration-200 shadow-md flex justify-center items-center disabled:opacity-50 disabled:cursor-not-allowed";
  
  const variants = {
    primary: "text-white bg-light-primary hover:opacity-90",
    secondary: "text-white bg-light-secondary hover:opacity-90",
    danger: "text-red-600 bg-red-50 border border-red-100 hover:bg-red-100 dark:bg-red-900 dark:text-red-200",
    outline: "text-gray-600 border border-gray-300 hover:bg-gray-50 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-800"
  };

  return (
    <button 
      className={`${baseStyles} ${variants[variant]} ${className}`} 
      disabled={isLoading || props.disabled}
      {...props}
    >
      {isLoading ? (
        <span className="animate-spin h-5 w-5 border-2 border-t-transparent border-white rounded-full mr-2"></span>
      ) : null}
      {children}
    </button>
  );
};

export default Button;