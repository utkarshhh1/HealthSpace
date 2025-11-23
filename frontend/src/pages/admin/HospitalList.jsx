import React, { useState, useEffect } from 'react';
import { getAllHospitalsAdmin, approveHospital } from '../../services/api'; // Uses Admin endpoint
import Button from '../../components/ui/Button';

const HospitalList = () => {
    const [hospitals, setHospitals] = useState([]);
    const [loading, setLoading] = useState(true);

    const fetchAll = () => {
        getAllHospitalsAdmin()
            .then(res => setHospitals(res.data))
            .catch(err => console.error(err))
            .finally(() => setLoading(false));
    };

    useEffect(() => { fetchAll(); }, []);

    const handleApprove = async (id) => {
        if(!window.confirm("Approve this hospital? It will become visible to patients.")) return;
        try {
            await approveHospital(id);
            fetchAll(); // Refresh list to show updated status
        } catch(e) { 
            alert("Approval failed"); 
        }
    };

    if (loading) return <div className="p-10 text-center">Loading Network...</div>;

    return (
        <div className="bg-white dark:bg-gray-800 rounded-xl shadow overflow-hidden">
            <div className="p-6 border-b dark:border-gray-700 flex justify-between items-center">
                <h2 className="text-xl font-bold dark:text-white">Network Management</h2>
                <span className="text-sm text-gray-500">{hospitals.length} Facilities</span>
            </div>
            <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                    <thead className="bg-gray-50 dark:bg-gray-700">
                        <tr>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">License</th>
                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">City</th>
                            <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Action</th>
                        </tr>
                    </thead>
                    <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                        {hospitals.map(h => (
                            <tr key={h.id} className={h.status === 'PENDING' ? 'bg-yellow-50 dark:bg-yellow-900/10' : 'hover:bg-gray-50 dark:hover:bg-gray-700/50'}>
                                <td className="px-6 py-4 font-medium dark:text-white">{h.name}</td>
                                <td className="px-6 py-4">
                                    <span className={`px-2 py-1 rounded text-xs font-bold border ${
                                        h.status === 'ACTIVE' 
                                        ? 'bg-green-100 text-green-700 border-green-200' 
                                        : 'bg-yellow-100 text-yellow-700 border-yellow-200'
                                    }`}>
                                        {h.status}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-sm text-gray-500 font-mono">{h.licenseNumber}</td>
                                <td className="px-6 py-4 text-sm text-gray-500">{h.city}</td>
                                <td className="px-6 py-4 text-right">
                                    {h.status === 'PENDING' ? (
                                        <Button 
                                            onClick={() => handleApprove(h.id)} 
                                            className="w-auto px-3 py-1 text-xs bg-green-600 hover:bg-green-700 text-white"
                                        >
                                            Approve
                                        </Button>
                                    ) : (
                                        <span className="text-xs text-gray-400">Active</span>
                                    )}
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            </div>
        </div>
    );
};

export default HospitalList;