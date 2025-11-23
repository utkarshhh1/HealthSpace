import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import api from "../../services/api";
import Input from "../../components/ui/Input";
import Button from "../../components/ui/Button";
import ThemeToggle from "../../components/ui/ThemeToggle"; // Import Toggle

const Register = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    name: "", email: "", password: "", contactPhone: "", role: "PATIENT", 
  });
  
  const [error, setError] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false); // Visibility State

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleRegister = async (e) => {
    e.preventDefault();
    setError(null);
    setIsLoading(true);

    try {
      await api.post("/auth/register", formData);
      alert("Registration Successful! Please Login.");
      navigate("/login");
    } catch (err) {
      console.error("Registration Error:", err);
      const msg = err.response?.data || "Registration failed. Email might be taken.";
      setError(msg);
    } finally {
      setIsLoading(false);
    }
  };

  const selectClasses = "w-full px-4 py-2 border rounded-lg focus:ring-2 focus:outline-none transition-colors bg-white dark:bg-gray-700 text-light-textBody dark:text-gray-100 border-gray-300 dark:border-gray-600 focus:ring-light-primary";

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 dark:bg-gray-900 font-sans transition-colors relative">
      
      {/* Theme Toggle */}
      <div className="absolute top-6 right-6">
        <ThemeToggle />
      </div>

      <div className="w-full max-w-md p-8 bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700">
        
        <div className="text-center mb-6">
          <h1 className="text-3xl font-bold text-light-textMain dark:text-white">Join HealthSpace</h1>
          <p className="text-gray-500 dark:text-gray-400 text-sm">Create your secure account</p>
        </div>

        {error && (
          <div className="p-3 mb-4 text-sm text-red-600 bg-red-100 dark:bg-red-900 dark:text-red-200 rounded-lg border border-red-200">
            {error}
          </div>
        )}

        <form onSubmit={handleRegister} className="space-y-4">
          
          <Input
            label="Full Name"
            name="name"
            type="text"
            required
            onChange={handleChange}
            placeholder="Peter Parker"
          />

          <Input
            label="Email"
            name="email"
            type="email"
            required
            onChange={handleChange}
            placeholder="email@example.com"
          />

          <Input
            label="Password"
            name="password"
            type={showPassword ? "text" : "password"} // Toggle Type
            required
            onChange={handleChange}
            placeholder="••••••••"
            // Eye Icon Button
            endIcon={
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 focus:outline-none"
              >
                {showPassword ? (
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                    <path fillRule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clipRule="evenodd" />
                  </svg>
                ) : (
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                    <path fillRule="evenodd" d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z" clipRule="evenodd" />
                    <path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.742L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.064 7 9.542 7 .847 0 1.669-.105 2.454-.303z" />
                  </svg>
                )}
              </button>
            }
          />

          <Input
            label="Phone Number"
            name="contactPhone"
            type="tel"
            required
            onChange={handleChange}
            placeholder="9876543210"
          />

          <div>
            <label className="block text-sm font-medium text-light-textMain dark:text-gray-300 mb-1">I am a...</label>
            <select
              name="role"
              className={selectClasses}
              onChange={handleChange}
              value={formData.role}
            >
              <option value="PATIENT">Patient (Beneficiary)</option>
              <option value="DOCTOR">Doctor (Provider)</option>
              <option value="HOSPITAL_ADMIN">Hospital Admin (Manager)</option>
              <option value="ADMIN">Platform Super Admin (Owner)</option>
            </select>
          </div>

          <Button type="submit" isLoading={isLoading} className="mt-4">
            Create Account
          </Button>
        </form>

        <div className="mt-4 text-center text-sm text-gray-600 dark:text-gray-400">
          Already have an account?{" "}
          <Link to="/login" className="text-light-primary font-bold hover:underline">
            Login here
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Register;