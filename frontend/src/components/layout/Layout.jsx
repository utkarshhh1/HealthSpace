import React from "react";
import { Outlet } from "react-router-dom";
import Sidebar from "./Sidebar";

const Layout = () => {
  return (
    <div className="flex min-h-screen bg-white dark:bg-gray-900 font-sans text-gray-900 dark:text-gray-100 transition-colors">
      <Sidebar />
      <main className="flex-1 ml-64 p-8 overflow-y-auto h-screen bg-gray-50 dark:bg-gray-900 transition-colors">
        <div className="max-w-7xl mx-auto">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default Layout;