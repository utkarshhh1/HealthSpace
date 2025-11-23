import React, { createContext, useContext, useState, useEffect } from 'react';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    // Load persisted session
    const storedToken = localStorage.getItem('jwtToken');
    const storedRole = localStorage.getItem('userRole');
    const storedUserId = localStorage.getItem('userId');
    const storedName = localStorage.getItem('userName');

    if (storedToken && storedRole && storedUserId) {
      setToken(storedToken);
      setUser({
        userId: parseInt(storedUserId, 10),
        role: storedRole,
        name: storedName,
      });
    }
    setIsLoading(false);
  }, []);

  const login = (authData) => {
    // Expects authData: { token, role, userId, email, name? }
    localStorage.setItem('jwtToken', authData.token);
    localStorage.setItem('userRole', authData.role);
    localStorage.setItem('userId', authData.userId);
    if (authData.name) localStorage.setItem('userName', authData.name);

    setToken(authData.token);
    setUser({
      userId: authData.userId,
      role: authData.role,
      name: authData.name,
    });
  };

  const logout = () => {
    localStorage.clear();
    setToken(null);
    setUser(null);
  };

  const isAuthenticated = !!token;

  return (
    <AuthContext.Provider value={{ user, token, isAuthenticated, login, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);