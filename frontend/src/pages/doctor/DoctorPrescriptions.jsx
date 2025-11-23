import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getPrescriptionsForDoctor, getPatientProfile } from '../../services/api';

const DoctorPrescriptions = () => {
    const { user } = useAuth();
    const [prescriptions, setPrescriptions] = useState([]);
    const [patientNames, setPatientNames] = useState({});
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const loadData = async () => {
            try {
                const res = await getPrescriptionsForDoctor(user.userId);
                const data = res.data || [];
                setPrescriptions(data);

                // Resolve Names
                const pids = [...new Set(data.map(p => p.patientId))];
                const names = {};
                await Promise.all(pids.map(async (id) => {
                    try {
                        const pRes = await getPatientProfile(id);
                        names[id] = pRes.data.user.name;
                    } catch { names[id] = 'Unknown'; }
                }));
                setPatientNames(names);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        if (user?.userId) loadData();
    }, [user.userId]);

    if (loading) return <div className="p-10 text-center">Loading History...</div>;

    return (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold dark:text-white">Issued History</h2>
            {prescriptions.length === 0 ? (
                <p className="text-gray-500">No prescriptions issued yet.</p>
            ) : (
                <div className="grid gap-6">
                    {prescriptions.map(rx => (
                        <div key={rx.id} className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow border dark:border-gray-700">
                            <div className="flex justify-between border-b pb-2 mb-4 dark:border-gray-700">
                                <h3 className="font-bold text-lg text-light-primary">Patient: {patientNames[rx.patientId]}</h3>
                                <span className="text-sm text-gray-500">{new Date(rx.createdAt).toLocaleDateString()}</span>
                            </div>
                            <p className="mb-2"><span className="font-semibold">Diagnosis:</span> {rx.diagnosis}</p>
                            <div className="space-y-1">
                                {rx.medicines.map((m, i) => (
                                    <div key={i} className="text-sm bg-gray-50 dark:bg-gray-700 p-2 rounded flex justify-between">
                                        <span className="font-medium">{m.medicineName}</span>
                                        <span className="text-gray-500">{m.dosage} | {m.frequency} | {m.duration}</span>
                                    </div>
                                ))}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default DoctorPrescriptions;