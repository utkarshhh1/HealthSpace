import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
    getPendingDoctors, 
    approveDoctor, 
    rejectDoctor, 
    getHospitalAppointments, 
    getHospitalPrescriptions,
    getDoctorProfile,     
    getPatientProfile     
} from '../../services/api';
import Button from '../../components/ui/Button';

const HospitalAdminDashboard = () => {
    const [activeTab, setActiveTab] = useState('APPOINTMENTS'); 
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);
    
    const [nameCache, setNameCache] = useState({ doctors: {}, patients: {} });

    useEffect(() => {
        loadData();
    }, [activeTab]);

    const resolveNames = async (items) => {
        const docIds = [...new Set(items.map(i => i.doctorId).filter(Boolean))];
        const patIds = [...new Set(items.map(i => i.patientId).filter(Boolean))];

        const newCache = { ...nameCache };
        
        await Promise.all([
            ...docIds.map(async (id) => {
                if (!newCache.doctors[id]) {
                    try { const res = await getDoctorProfile(id); newCache.doctors[id] = res.data; } catch(e){}
                }
            }),
            ...patIds.map(async (id) => {
                if (!newCache.patients[id]) {
                    try { const res = await getPatientProfile(id); newCache.patients[id] = res.data; } catch(e){}
                }
            })
        ]);
        setNameCache(newCache);
    };

    const loadData = async () => {
        setLoading(true);
        setError(null);
        try {
            let res;
            if (activeTab === 'DOCTORS') res = await getPendingDoctors();
            else if (activeTab === 'APPOINTMENTS') res = await getHospitalAppointments();
            else if (activeTab === 'PRESCRIPTIONS') res = await getHospitalPrescriptions();
            
            const rawData = res.data || [];
            setData(rawData);

            if (activeTab !== 'DOCTORS' && rawData.length > 0) {
                await resolveNames(rawData);
            }

        } catch (err) {
            if (err.response?.status === 403) setError("Profile Not Linked");
            else setError("Failed to fetch data.");
        } finally {
            setLoading(false);
        }
    };

    const handleDecision = async (id, decision) => {
        setData(prev => prev.filter(d => d.id !== id));
        try {
            if (decision === 'APPROVE') await approveDoctor(id);
            else await rejectDoctor(id);
        } catch (err) {
            alert("Action failed.");
            loadData();
        }
    };

    const getDayLabel = (dateStr) => {
        const d = new Date(dateStr);
        const today = new Date();
        const tomorrow = new Date(today);
        tomorrow.setDate(tomorrow.getDate() + 1);

        if (d.toDateString() === today.toDateString()) return "Today";
        if (d.toDateString() === tomorrow.toDateString()) return "Tomorrow";
        return d.toLocaleDateString();
    };

    if (error === "Profile Not Linked") {
        return (
            <div className="p-10 text-center bg-yellow-50 rounded-xl border border-yellow-200">
                <h3 className="text-xl font-bold text-yellow-800 mb-2">Setup Required</h3>
                <p className="mb-4">Link your account to a hospital first.</p>
                <Link to="/hospital-admin/profile" className="text-blue-600 hover:underline font-bold">Go to Profile Settings</Link>
            </div>
        );
    }

    return (
        <div className="flex flex-col h-[calc(100vh-100px)] space-y-6">
            <div className="flex justify-between items-center">
                <h2 className="text-3xl font-bold dark:text-white">Hospital Dashboard</h2>
                <div className="flex space-x-2 bg-white dark:bg-gray-800 p-1 rounded-lg border dark:border-gray-700">
                    {['APPOINTMENTS', 'PRESCRIPTIONS', 'DOCTORS'].map(tab => (
                        <button 
                            key={tab}
                            onClick={() => setActiveTab(tab)}
                            className={`px-4 py-2 text-sm font-bold rounded-md transition-all ${
                                activeTab === tab 
                                ? 'bg-light-primary text-white shadow' 
                                : 'text-gray-500 hover:bg-gray-100 dark:hover:bg-gray-700'
                            }`}
                        >
                            {tab}
                        </button>
                    ))}
                </div>
            </div>

            {loading && <div className="text-center p-10 text-gray-500">Loading Data...</div>}

            {!loading && (
                <div className="flex-1 overflow-hidden bg-white dark:bg-gray-800 rounded-xl shadow border dark:border-gray-700">
                    
                    {/* === 1. APPOINTMENT ANALYSIS === */}
                    {activeTab === 'APPOINTMENTS' && (
                        <div className="h-full flex flex-col">
                            <div className="p-4 border-b dark:border-gray-700 bg-gray-50 dark:bg-gray-700/50">
                                <h3 className="font-bold text-gray-700 dark:text-gray-200">Patient Flow Analysis</h3>
                            </div>
                            <div className="flex-1 overflow-y-auto">
                                <table className="w-full text-left text-sm">
                                    <thead className="bg-white dark:bg-gray-800 sticky top-0 z-10 border-b dark:border-gray-700">
                                        <tr className="text-gray-500 text-xs uppercase">
                                            <th className="p-4">Timing</th>
                                            <th className="p-4">Patient</th>
                                            <th className="p-4">Doctor / Specialty</th>
                                            <th className="p-4">Reason</th>
                                            <th className="p-4">Status</th>
                                        </tr>
                                    </thead>
                                    <tbody className="divide-y dark:divide-gray-700">
                                        {data.map(appt => {
                                            const doc = nameCache.doctors[appt.doctorId];
                                            const pat = nameCache.patients[appt.patientId];
                                            return (
                                                <tr key={appt.id} className="hover:bg-gray-50 dark:hover:bg-gray-700/30">
                                                    <td className="p-4">
                                                        <span className="font-bold text-blue-600">{getDayLabel(appt.appointmentTime)}</span>
                                                        <div className="text-xs text-gray-500">{new Date(appt.appointmentTime).toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'})}</div>
                                                    </td>
                                                    <td className="p-4 font-medium dark:text-white">{pat?.user?.name || 'Unknown'}</td>
                                                    <td className="p-4">
                                                        <div className="font-semibold dark:text-gray-200">{doc?.user?.name || 'Unknown'}</div>
                                                        <div className="text-xs text-gray-500 bg-gray-100 dark:bg-gray-700 px-2 py-0.5 rounded inline-block mt-1">{doc?.specialty || 'General'}</div>
                                                    </td>
                                                    <td className="p-4 italic text-gray-600 dark:text-gray-400">"{appt.symptoms}"</td>
                                                    <td className="p-4">
                                                        <span className={`px-2 py-1 rounded text-xs font-bold border ${
                                                            appt.status==='COMPLETED' ? 'bg-green-100 text-green-700 border-green-200' :
                                                            appt.status==='CANCELLED' ? 'bg-red-100 text-red-700 border-red-200' :
                                                            'bg-blue-100 text-blue-700 border-blue-200'
                                                        }`}>
                                                            {appt.status}
                                                        </span>
                                                    </td>
                                                </tr>
                                            );
                                        })}
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    )}

                    {/* === 2. PRESCRIPTION MASTER LIST === */}
                    {activeTab === 'PRESCRIPTIONS' && (
                        <div className="h-full flex flex-col">
                            <div className="p-4 border-b dark:border-gray-700 bg-gray-50 dark:bg-gray-700/50 flex justify-between">
                                <h3 className="font-bold text-gray-700 dark:text-gray-200">Master Prescription Register</h3>
                                <span className="text-xs font-mono bg-gray-200 dark:bg-gray-600 px-2 py-1 rounded">Total: {data.length}</span>
                            </div>
                            <div className="flex-1 overflow-y-auto p-4 space-y-4">
                                {data.map(rx => {
                                    const doc = nameCache.doctors[rx.doctorId];
                                    const pat = nameCache.patients[rx.patientId];
                                    return (
                                        <div key={rx.id} className="border dark:border-gray-700 rounded-lg p-4 hover:shadow-md transition bg-white dark:bg-gray-800">
                                            <div className="flex justify-between items-start mb-3">
                                                <div>
                                                    <h4 className="font-bold text-lg text-red-600 dark:text-red-400">{rx.diagnosis}</h4>
                                                    <p className="text-xs text-gray-500">{new Date(rx.createdAt).toLocaleString()}</p>
                                                </div>
                                                <div className="text-right text-sm">
                                                    <p><span className="text-gray-500">Dr.</span> <strong>{doc?.user?.name}</strong></p>
                                                    <p><span className="text-gray-500">Pt.</span> <strong>{pat?.user?.name}</strong></p>
                                                </div>
                                            </div>
                                            
                                            <div className="bg-gray-50 dark:bg-gray-700/50 p-3 rounded border dark:border-gray-700">
                                                <p className="text-xs font-bold text-gray-500 uppercase mb-2">Medicines</p>
                                                <div className="flex flex-wrap gap-2">
                                                    {/* SAFE MAPPING FIX */}
                                                    {(rx.medicines || []).map((m, i) => (
                                                        <span key={i} className="text-sm bg-white dark:bg-gray-600 border dark:border-gray-500 px-2 py-1 rounded shadow-sm dark:text-gray-200">
                                                            {m.medicineName} <span className="text-xs text-gray-500 dark:text-gray-400">({m.dosage})</span>
                                                        </span>
                                                    ))}
                                                </div>
                                            </div>
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    )}

                    {/* === 3. DOCTOR VERIFICATION === */}
                    {activeTab === 'DOCTORS' && (
                        <div className="p-6 grid gap-4">
                            {data.length === 0 && <p className="text-center text-gray-500">No pending verification requests.</p>}
                            {data.map(doc => (
                                <div key={doc.id} className="bg-gray-50 dark:bg-gray-700 p-4 rounded-lg flex justify-between items-center border dark:border-gray-600">
                                    <div>
                                        <h3 className="text-lg font-bold text-light-primary">{doc.user?.name || 'Unknown'}</h3>
                                        <p className="text-sm text-gray-600 dark:text-gray-300">{doc.specialty} • {doc.degree}</p>
                                    </div>
                                    <div className="flex gap-2">
                                        <Button variant="danger" onClick={() => handleDecision(doc.id, 'REJECT')} className="px-3 py-1 text-xs w-auto">Reject</Button>
                                        <Button onClick={() => handleDecision(doc.id, 'APPROVE')} className="px-3 py-1 text-xs w-auto bg-green-600 hover:bg-green-700">Approve</Button>
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};

export default HospitalAdminDashboard;