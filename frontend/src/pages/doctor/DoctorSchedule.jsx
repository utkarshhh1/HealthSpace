import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getDoctorAppointments, getPatientProfile, updateAppointmentStatus } from '../../services/api';
import Button from '../../components/ui/Button';

const DoctorSchedule = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [appointments, setAppointments] = useState([]);
    const [patientCache, setPatientCache] = useState({});
    const [loading, setLoading] = useState(true);

    const fetchSchedule = async () => {
        try {
            const res = await getDoctorAppointments(user.userId);
            const data = res.data || [];
            setAppointments(data);

            // Resolve Patient Names
            const patientIds = [...new Set(data.map(a => a.patientId))];
            const newCache = { ...patientCache };
            
            await Promise.all(patientIds.map(async (pid) => {
                if (!newCache[pid]) {
                    try {
                        const pRes = await getPatientProfile(pid);
                        newCache[pid] = pRes.data.user.name; // Get Name from User obj
                    } catch {
                        newCache[pid] = 'Unknown Patient';
                    }
                }
            }));
            setPatientCache(newCache);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (user?.userId) fetchSchedule();
    }, [user?.userId]);

    const handleCancel = async (id) => {
        if (window.confirm("Cancel this appointment?")) {
            await updateAppointmentStatus(id, "CANCELLED");
            fetchSchedule();
        }
    };

    if (loading) return <div className="p-10 text-center">Loading Schedule...</div>;

    return (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-light-textMain dark:text-white">Patient Schedule</h2>
            
            {appointments.length === 0 ? (
                <div className="p-10 text-center border-2 border-dashed rounded-xl text-gray-500">No upcoming appointments.</div>
            ) : (
                <div className="grid gap-4">
                    {appointments.map(appt => (
                        <div key={appt.id} className="p-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm border dark:border-gray-700 flex justify-between items-center">
                            <div>
                                <div className="text-lg font-bold text-light-primary">
                                    {new Date(appt.appointmentTime).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                                    <span className="text-gray-400 mx-2">|</span>
                                    {patientCache[appt.patientId] || 'Loading...'}
                                </div>
                                <div className="text-sm text-gray-500 mt-1">
                                    {new Date(appt.appointmentTime).toLocaleDateString()} - {appt.symptoms}
                                </div>
                            </div>
                            <div className="flex gap-3">
                                <Button variant="danger" onClick={() => handleCancel(appt.id)} className="px-4 py-2 text-sm w-auto">Cancel</Button>
                                <Button onClick={() => navigate(`/doctor/create-prescription/${appt.id}`)} className="px-4 py-2 text-sm w-auto">
                                    Issue Prescription
                                </Button>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default DoctorSchedule;