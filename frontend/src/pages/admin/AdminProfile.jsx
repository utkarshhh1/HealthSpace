import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { registerOwnHospital, getHospitalAdminProfile } from '../../services/api';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const AdminProfile = () => {
    const { user } = useAuth();
    const isHospitalAdmin = user?.role === 'HOSPITAL_ADMIN';

    const [profile, setProfile] = useState(null);
    const [formData, setFormData] = useState({
        name: '', address: '', city: '', licenseNumber: '', contactEmail: '', contactPhone: ''
    });
    const [status, setStatus] = useState({ loading: true, msg: '', error: false });

    useEffect(() => {
        if (isHospitalAdmin) {
            getHospitalAdminProfile()
                .then(res => {
                    if (res.data && res.data.hospital) {
                        setProfile(res.data);
                    }
                })
                .catch(() => console.log("No existing profile found"))
                .finally(() => setStatus(prev => ({ ...prev, loading: false })));
        } else {
            setStatus(prev => ({ ...prev, loading: false }));
        }
    }, [isHospitalAdmin]);

    const handleRegister = async (e) => {
        e.preventDefault();
        setStatus({ loading: true, msg: '', error: false });
        try {
            await registerOwnHospital(formData);
            setStatus({ loading: false, msg: 'Registration Submitted! Awaiting Super Admin Approval.', error: false });
            const res = await getHospitalAdminProfile();
            setProfile(res.data);
        } catch (err) {
            const errMsg = err.response?.data || 'Registration Failed. License may be duplicate.';
            setStatus({ loading: false, msg: errMsg, error: true });
        }
    };

    if (status.loading) return <div className="p-10 text-center">Loading Profile...</div>;

    // --- CASE 1: PLATFORM SUPER ADMIN ---
    if (!isHospitalAdmin) {
        return (
            <div className="max-w-2xl mx-auto p-8 bg-white dark:bg-gray-800 rounded-xl shadow border dark:border-gray-700">
                <h2 className="text-2xl font-bold dark:text-white mb-2">Platform Administrator</h2>
                <p className="text-gray-500">Global System Control</p>
            </div>
        );
    }

    // --- CASE 2: HOSPITAL ADMIN (ALREADY LINKED) ---
    if (profile && profile.hospital) {
        const hosp = profile.hospital;
        const isPending = hosp.status === 'PENDING';
        
        return (
            <div className="max-w-3xl mx-auto space-y-6">
                {/* Admin Info Card */}
                <div className="bg-white dark:bg-gray-800 p-8 rounded-xl shadow border dark:border-gray-700">
                    <div className="flex items-center gap-4 mb-6">
                        <div className="w-16 h-16 bg-blue-100 dark:bg-blue-900/30 rounded-full flex items-center justify-center text-2xl">
                            👤
                        </div>
                        <div>
                            <h2 className="text-2xl font-bold dark:text-white">{user?.name}</h2>
                            <p className="text-gray-500">{profile.jobTitle || 'Hospital Administrator'}</p>
                        </div>
                    </div>
                    <div className="grid grid-cols-2 gap-6 text-sm">
                        <div>
                            <p className="text-gray-400 uppercase text-xs">Email</p>
                            <p className="font-medium dark:text-gray-200">{user?.email}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 uppercase text-xs">Role</p>
                            <p className="font-medium dark:text-gray-200">{user?.role}</p>
                        </div>
                    </div>
                </div>

                {/* Hospital Info Card */}
                <div className="bg-white dark:bg-gray-800 p-8 rounded-xl shadow border dark:border-gray-700 relative overflow-hidden">
                    <div className={`absolute top-0 right-0 px-4 py-1 rounded-bl-xl text-xs font-bold ${isPending ? 'bg-yellow-100 text-yellow-700' : 'bg-green-100 text-green-700'}`}>
                        STATUS: {hosp.status}
                    </div>

                    <h3 className="text-xl font-bold text-light-primary mb-4 flex items-center gap-2">
                         {hosp.name}
                    </h3>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <p className="text-gray-400 uppercase text-xs mb-1">License Number</p>
                            <p className="font-mono text-lg dark:text-white">{hosp.licenseNumber}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 uppercase text-xs mb-1">Location</p>
                            <p className="font-medium dark:text-gray-200">{hosp.city}</p>
                            <p className="text-sm text-gray-500">{hosp.address}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 uppercase text-xs mb-1">Contact</p>
                            <p className="dark:text-gray-200">{hosp.contactEmail}</p>
                            <p className="dark:text-gray-200">{hosp.contactPhone}</p>
                        </div>
                        <div>
                            <p className="text-gray-400 uppercase text-xs mb-1">Website</p>
                            <a href={hosp.website} target="_blank" rel="noreferrer" className="text-blue-500 hover:underline">
                                {hosp.website || 'N/A'}
                            </a>
                        </div>
                    </div>

                    {isPending && (
                        <div className="mt-6 p-4 bg-yellow-50 dark:bg-yellow-900/20 rounded border border-yellow-100 dark:border-yellow-800 text-sm text-yellow-700 dark:text-yellow-200">
                             Your facility is awaiting Super Admin approval. Dashboard features are restricted.
                        </div>
                    )}
                </div>
            </div>
        );
    }

    // --- CASE 3: REGISTRATION FORM ---
    return (
        <div className="max-w-3xl mx-auto p-8 bg-white dark:bg-gray-800 rounded-xl shadow border dark:border-gray-700">
            <h2 className="text-3xl font-bold dark:text-white mb-2">Register Your Facility</h2>
            <p className="text-gray-500 mb-8">Establish your hospital in HealthSpace.</p>
            
            {status.msg && (
                <div className={`p-4 mb-6 rounded-lg border text-sm font-medium ${status.error ? 'bg-red-50 text-red-700 border-red-200' : 'bg-green-50 text-green-700 border-green-200'}`}>
                    {status.msg}
                </div>
            )}

            <form onSubmit={handleRegister} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Input label="Hospital Name" value={formData.name} onChange={e => setFormData({...formData, name: e.target.value})} required placeholder="e.g. City General" />
                    <Input label="License Number" value={formData.licenseNumber} onChange={e => setFormData({...formData, licenseNumber: e.target.value})} required />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Input label="City" value={formData.city} onChange={e => setFormData({...formData, city: e.target.value})} required />
                    <Input label="Address" value={formData.address} onChange={e => setFormData({...formData, address: e.target.value})} required />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Input label="Contact Email" type="email" value={formData.contactEmail} onChange={e => setFormData({...formData, contactEmail: e.target.value})} required />
                    <Input label="Contact Phone" value={formData.contactPhone} onChange={e => setFormData({...formData, contactPhone: e.target.value})} required />
                </div>
                
                <Button type="submit" isLoading={status.loading}>Submit Registration</Button>
            </form>
        </div>
    );
};

export default AdminProfile;