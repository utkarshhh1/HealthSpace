import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { getDoctorProfile, createDoctorProfile, getAllHospitals } from '../../services/api';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

// --- Predefined Lists for Dropdowns ---
const SPECIALTIES = [
    "Cardiology",
    "Dermatology",
    "Endocrinology",
    "Gastroenterology",
    "General Medicine",
    "Gynecology",
    "Neurology",
    "Oncology",
    "Ophthalmology",
    "Orthopedics",
    "Pediatrics",
    "Psychiatry",
    "Pulmonology",
    "Urology"
];

const DEGREES = [
    "MBBS",
    "MD",
    "MS",
    "DNB",
    "BDS",
    "MDS",
    "BAMS",
    "BHMS",
    "PhD"
];

const DoctorProfile = () => {
    const { user } = useAuth();
    const userId = user?.userId;

    const [profile, setProfile] = useState(null);
    const [hospitals, setHospitals] = useState([]);
    
    const [formData, setFormData] = useState({
        hospitalId: '',
        specialty: '',
        degree: '',
        experienceYears: '',
        consultationFee: '',
    });
    
    const [status, setStatus] = useState({ loading: true, submitting: false, error: null, success: null });

    useEffect(() => {
        const loadData = async () => {
            try {
                const [hospRes, profRes] = await Promise.all([
                    getAllHospitals(),
                    getDoctorProfile(userId).catch(() => ({ data: null })) // Handle 404 gracefully
                ]);

                setHospitals(hospRes.data);

                if (profRes.data) {
                    setProfile(profRes.data);
                    setFormData({
                        hospitalId: profRes.data.hospital?.id || '',
                        specialty: profRes.data.specialty || '',
                        degree: profRes.data.degree || '',
                        experienceYears: profRes.data.experienceYears || '',
                        consultationFee: profRes.data.consultationFee || '',
                    });
                }
            } catch (err) {
                console.error("Error loading profile data", err);
            } finally {
                setStatus(prev => ({ ...prev, loading: false }));
            }
        };
        if (userId) loadData();
    }, [userId]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setStatus({ ...status, submitting: true, error: null, success: null });

        const payload = {
            hospital: { id: parseInt(formData.hospitalId) },
            specialty: formData.specialty,
            degree: formData.degree,
            experienceYears: parseInt(formData.experienceYears),
            consultationFee: parseFloat(formData.consultationFee),
        };

        try {
            const res = await createDoctorProfile(userId, payload);
            setProfile(res.data);
            setStatus({ ...status, submitting: false, success: 'Profile saved! Awaiting Admin verification.' });
        } catch (err) {
            setStatus({ ...status, submitting: false, error: 'Failed to save profile.' });
        }
    };

    const getStatusBadge = (s) => {
        const colors = {
            VERIFIED: 'bg-green-100 text-green-800 border-green-200',
            REJECTED: 'bg-red-100 text-red-800 border-red-200',
            PENDING: 'bg-yellow-100 text-yellow-800 border-yellow-200'
        };
        return <span className={`px-3 py-1 rounded-full text-xs font-bold border ${colors[s] || colors.PENDING}`}>{s || 'PENDING'}</span>;
    };

    // Shared class for Select inputs to match the Input component style
    const selectClasses = "w-full px-4 py-2 border rounded-lg bg-white dark:bg-gray-700 dark:text-white dark:border-gray-600 focus:ring-2 focus:ring-light-primary focus:outline-none transition-colors";

    if (status.loading) return <div className="p-10 text-center">Loading Profile...</div>;

    return (
        <div className="max-w-3xl mx-auto p-8 bg-white dark:bg-gray-800 rounded-xl shadow-lg border dark:border-gray-700">
            <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold dark:text-white">Professional Profile</h2>
                {profile && getStatusBadge(profile.affiliationStatus)}
            </div>

            {status.success && <div className="p-3 mb-4 bg-green-100 text-green-700 rounded">{status.success}</div>}
            {status.error && <div className="p-3 mb-4 bg-red-100 text-red-700 rounded">{status.error}</div>}

            <form onSubmit={handleSubmit} className="space-y-6">
                
                {/* Hospital Dropdown */}
                <div>
                    <label className="block text-sm font-medium mb-1 dark:text-gray-300">Affiliated Hospital</label>
                    <select 
                        className={selectClasses}
                        value={formData.hospitalId}
                        onChange={e => setFormData({...formData, hospitalId: e.target.value})}
                        required
                    >
                        <option value="">Select Hospital</option>
                        {hospitals.map(h => <option key={h.id} value={h.id}>{h.name} ({h.city})</option>)}
                    </select>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    {/* Specialty Dropdown */}
                    <div>
                        <label className="block text-sm font-medium mb-1 dark:text-gray-300">Specialty</label>
                        <select 
                            className={selectClasses}
                            value={formData.specialty}
                            onChange={e => setFormData({...formData, specialty: e.target.value})}
                            required
                        >
                            <option value="">Select Specialty</option>
                            {SPECIALTIES.map(s => <option key={s} value={s}>{s}</option>)}
                        </select>
                    </div>

                    {/* Degree Dropdown */}
                    <div>
                        <label className="block text-sm font-medium mb-1 dark:text-gray-300">Primary Qualification</label>
                        <select 
                            className={selectClasses}
                            value={formData.degree}
                            onChange={e => setFormData({...formData, degree: e.target.value})}
                            required
                        >
                            <option value="">Select Degree</option>
                            {DEGREES.map(d => <option key={d} value={d}>{d}</option>)}
                        </select>
                    </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Input 
                        label="Experience (Years)" 
                        type="number" 
                        value={formData.experienceYears} 
                        onChange={e => setFormData({...formData, experienceYears: e.target.value})} 
                        required 
                        min="0"
                    />
                    <Input 
                        label="Consultation Fee (₹)" 
                        type="number" 
                        value={formData.consultationFee} 
                        onChange={e => setFormData({...formData, consultationFee: e.target.value})} 
                        required 
                        min="0"
                        step="0.01"
                    />
                </div>

                <Button type="submit" isLoading={status.submitting}>Update Credentials</Button>
            </form>
        </div>
    );
};

export default DoctorProfile;