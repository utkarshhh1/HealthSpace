import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getPrescriptionsForPatient } from '../../services/api';

const PatientPrescriptions = () => {
    const { user } = useAuth();
    const [prescriptions, setPrescriptions] = useState([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!user?.userId) return;
        getPrescriptionsForPatient(user.userId)
            .then(res => setPrescriptions(res.data || []))
            .catch(err => console.error(err))
            .finally(() => setLoading(false));
    }, [user.userId]);

    if (loading) return <div className="p-10 text-center">Loading Records...</div>;

    return (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-light-textMain dark:text-white">Medical History</h2>
            
            {prescriptions.length === 0 ? (
                <div className="p-10 text-center border-2 border-dashed rounded-xl dark:border-gray-700 text-gray-500">
                    No prescriptions found.
                </div>
            ) : (
                <div className="grid gap-6">
                    {prescriptions.map(rx => (
                        <div key={rx.id} className="bg-white dark:bg-gray-800 rounded-xl shadow border dark:border-gray-700 overflow-hidden">
                            <div className="p-4 bg-gray-50 dark:bg-gray-700 border-b dark:border-gray-600 flex justify-between">
                                <span className="font-bold text-light-primary dark:text-white">Diagnosis: {rx.diagnosis}</span>
                                <span className="text-sm text-gray-500 dark:text-gray-300">{new Date(rx.createdAt).toLocaleDateString()}</span>
                            </div>
                            <div className="p-4">
                                <div className="mb-4">
                                    <p className="text-sm font-semibold text-gray-500 uppercase">Medicines:</p>
                                    <ul className="mt-2 space-y-2">
                                        {rx.medicines.map((med, idx) => (
                                            <li key={idx} className="flex items-center text-sm dark:text-gray-200">
                                                <span className="w-2 h-2 bg-blue-500 rounded-full mr-3"></span>
                                                <span className="font-medium mr-2">{med.medicineName}</span>
                                                <span className="text-gray-500">({med.dosage}, {med.frequency} for {med.duration})</span>
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                                {rx.notes && (
                                    <div className="text-sm bg-yellow-50 dark:bg-gray-900 p-3 rounded border border-yellow-100 dark:border-gray-700 text-gray-700 dark:text-gray-400 italic">
                                        "Note: {rx.notes}"
                                    </div>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default PatientPrescriptions;