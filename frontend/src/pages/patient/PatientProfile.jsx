import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import api, { getPatientProfile, createPatientProfile } from '../../services/api';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const PatientProfile = () => {
    const { user } = useAuth();
    const userId = user?.userId;

    const [formData, setFormData] = useState({
        dob: '',
        gender: 'MALE',
        bloodGroup: '',
        height: '',
        weight: '',
        address: '',
        emergencyContactName: '',
        emergencyContactPhone: '',
    });
    
    const [status, setStatus] = useState({ loading: true, submitting: false, error: null, success: null });

    // Load existing profile
    useEffect(() => {
        if (!userId) return;
        
        getPatientProfile(userId)
            .then(res => {
                const data = res.data;
                if (data) {
                    setFormData({
                        dob: data.dob || '',
                        gender: data.gender || 'MALE',
                        bloodGroup: data.bloodGroup || '',
                        height: data.height || '',
                        weight: data.weight || '',
                        address: data.address || '',
                        emergencyContactName: data.emergencyContactName || '',
                        emergencyContactPhone: data.emergencyContactPhone || '',
                    });
                }
                setStatus(prev => ({ ...prev, loading: false }));
            })
            .catch(err => {
                // 404 is expected for new users
                console.log("Profile fetch info:", err);
                setStatus(prev => ({ ...prev, loading: false }));
            });
    }, [userId]);

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setStatus({ ...status, submitting: true, error: null, success: null });

        const payload = {
            ...formData,
            height: parseFloat(formData.height) || null,
            weight: parseFloat(formData.weight) || null,
        };

        try {
            await createPatientProfile(userId, payload);
            setStatus({ ...status, submitting: false, success: 'Profile updated successfully!' });
        } catch (err) {
            setStatus({ ...status, submitting: false, error: 'Failed to save profile.' });
        }
    };

    if (status.loading) return <div className="p-8 text-center">Loading Profile...</div>;

    // Styling for Select to match Input component
    const selectClass = "w-full px-4 py-2 border rounded-lg focus:ring-2 focus:outline-none transition-colors bg-white dark:bg-gray-700 text-light-textBody dark:text-gray-100 border-gray-300 dark:border-gray-600 focus:ring-light-primary";

    return (
        <div className="max-w-3xl mx-auto p-8 bg-white dark:bg-gray-800 rounded-xl shadow-lg border border-gray-200 dark:border-gray-700 transition-colors">
            <h2 className="text-2xl font-bold text-light-textMain dark:text-white mb-2">My Medical Profile</h2>
            <p className="text-gray-500 dark:text-gray-400 mb-6">Keep your health metrics up to date.</p>

            {status.error && <div className="p-3 mb-4 text-sm bg-red-100 text-red-700 rounded-lg">{status.error}</div>}
            {status.success && <div className="p-3 mb-4 text-sm bg-green-100 text-green-700 rounded-lg">{status.success}</div>}

            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Input label="Date of Birth" type="date" name="dob" value={formData.dob} onChange={handleChange} required />
                    
                    <div>
                        <label className="block text-sm font-medium text-light-textMain dark:text-gray-300 mb-1">Gender</label>
                        <select name="gender" value={formData.gender} onChange={handleChange} className={selectClass}>
                            <option value="MALE">Male</option>
                            <option value="FEMALE">Female</option>
                            <option value="OTHER">Other</option>
                        </select>
                    </div>

                    <Input label="Blood Group" name="bloodGroup" value={formData.bloodGroup} onChange={handleChange} placeholder="e.g. O+" />
                    <div className="grid grid-cols-2 gap-4">
                        <Input label="Height (cm)" type="number" name="height" value={formData.height} onChange={handleChange} />
                        <Input label="Weight (kg)" type="number" name="weight" value={formData.weight} onChange={handleChange} />
                    </div>
                </div>

                <Input label="Address" name="address" value={formData.address} onChange={handleChange} placeholder="Full residential address" />

                <div className="border-t pt-4 dark:border-gray-700">
                    <h3 className="text-lg font-semibold mb-4 text-light-textMain dark:text-white">Emergency Contact</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <Input label="Contact Name" name="emergencyContactName" value={formData.emergencyContactName} onChange={handleChange} required />
                        <Input label="Contact Phone" type="tel" name="emergencyContactPhone" value={formData.emergencyContactPhone} onChange={handleChange} required />
                    </div>
                </div>

                <Button type="submit" isLoading={status.submitting}>Save Profile</Button>
            </form>
        </div>
    );
};

export default PatientProfile;