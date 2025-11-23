import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getPatientAppointments, getDoctorProfile, getHospitalById } from '../../services/api';

const PatientAppointments = () => {
    const { user } = useAuth();
    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [lookups, setLookups] = useState({ doctors: {}, hospitals: {} }); // Cache

    useEffect(() => {
        const fetchData = async () => {
            try {
                // 1. Get Appointments
                const res = await getPatientAppointments(user.userId);
                const data = res.data || [];
                setAppointments(data);

                // 2. Identify unique IDs to fetch
                const docIds = [...new Set(data.map(a => a.doctorId))];
                const hospIds = [...new Set(data.map(a => a.hospitalId))];

                // 3. Fetch Details in Parallel
                const docPromises = docIds.map(id => getDoctorProfile(id).catch(() => null));
                const hospPromises = hospIds.map(id => getHospitalById(id).catch(() => null));

                const [docs, hosps] = await Promise.all([Promise.all(docPromises), Promise.all(hospPromises)]);

                // 4. Build Lookup Map
                const newLookups = { doctors: {}, hospitals: {} };
                docs.forEach(d => { if(d?.data) newLookups.doctors[d.data.user.id] = d.data.user.name; });
                hosps.forEach(h => { if(h?.data) newLookups.hospitals[h.data.id] = h.data.name; });

                setLookups(newLookups);
            } catch (err) {
                console.error("Error fetching appointments", err);
            } finally {
                setLoading(false);
            }
        };
        if (user?.userId) fetchData();
    }, [user.userId]);

    if (loading) return <div className="p-10 text-center">Loading Appointments...</div>;

    const getStatusColor = (status) => {
        switch(status) {
            case 'BOOKED': return 'bg-blue-100 text-blue-700 border-blue-200';
            case 'COMPLETED': return 'bg-green-100 text-green-700 border-green-200';
            case 'CANCELLED': return 'bg-red-100 text-red-700 border-red-200';
            default: return 'bg-gray-100 text-gray-700';
        }
    };

    return (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-light-textMain dark:text-white">My Appointments</h2>
            
            {appointments.length === 0 ? (
                <div className="p-10 text-center border-2 border-dashed rounded-xl dark:border-gray-700 text-gray-500">No appointments found.</div>
            ) : (
                <div className="grid gap-4">
                    {appointments.map(appt => {
                        const dateObj = new Date(appt.appointmentTime);
                        return (
                            <div key={appt.id} className="p-6 bg-white dark:bg-gray-800 rounded-xl shadow-sm border border-gray-200 dark:border-gray-700 flex justify-between items-center">
                                <div>
                                    <h4 className="text-lg font-bold text-light-primary mb-1">
                                        Dr. {lookups.doctors[appt.doctorId] || 'Loading...'}
                                    </h4>
                                    <p className="text-sm text-gray-600 dark:text-gray-400">
                                        {lookups.hospitals[appt.hospitalId] || 'Unknown Hospital'}
                                    </p>
                                    <p className="mt-2 text-sm italic text-gray-500">"{appt.symptoms}"</p>
                                </div>
                                <div className="text-right">
                                    <div className={`inline-block px-3 py-1 rounded-full text-xs font-bold border mb-2 ${getStatusColor(appt.status)}`}>
                                        {appt.status}
                                    </div>
                                    <p className="text-lg font-semibold dark:text-white">
                                        {dateObj.toLocaleDateString()}
                                    </p>
                                    <p className="text-sm text-gray-500">{dateObj.toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</p>
                                </div>
                            </div>
                        );
                    })}
                </div>
            )}
        </div>
    );
};

export default PatientAppointments;
