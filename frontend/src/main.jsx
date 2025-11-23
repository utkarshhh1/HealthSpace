import React from 'react';
import ReactDOM from 'react-dom/client';
import { BrowserRouter as Router } from 'react-router-dom';
import App from './App.jsx';
import { AuthProvider } from './context/AuthContext';   // UPDATED PATH
import { ThemeProvider } from './context/ThemeContext'; // UPDATED PATH
import './index.css'; 

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <Router>
      <ThemeProvider>
        <AuthProvider> 
          <App />
        </AuthProvider>
      </ThemeProvider>
    </Router>
  </React.StrictMode>,
);