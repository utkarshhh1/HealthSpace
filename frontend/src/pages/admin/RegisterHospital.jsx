import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { registerHospital } from '../../services/api';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const RegisterHospital = () => {
    const location = useLocation();
    const navigate = useNavigate();
    
    // Check if we were passed a hospital object to edit
    const hospitalToEdit = location.state?.hospital;
    const isEditing = !!hospitalToEdit;

    const [formData, setFormData] = useState({
        name: '', address: '', city: '', licenseNumber: '', contactEmail: '', contactPhone: '', website: ''
    });
    
    const [status, setStatus] = useState({ loading: false, msg: '' });

    // Load data if editing
    useEffect(() => {
        if (hospitalToEdit) {
            setFormData({
                id: hospitalToEdit.id, // Critical for backend update
                name: hospitalToEdit.name || '',
                address: hospitalToEdit.address || '',
                city: hospitalToEdit.city || '',
                licenseNumber: hospitalToEdit.licenseNumber || '',
                contactEmail: hospitalToEdit.contactEmail || '',
                contactPhone: hospitalToEdit.contactPhone || '',
                website: hospitalToEdit.website || ''
            });
        }
    }, [hospitalToEdit]);

    const handleChange = (e) => setFormData({ ...formData, [e.target.name]: e.target.value });

    const handleSubmit = async (e) => {
        e.preventDefault();
        setStatus({ loading: true, msg: '' });
        
        try {
            // The backend 'register' endpoint handles upsert (update if ID exists)
            await registerHospital(formData);
            
            setStatus({ loading: false, msg: isEditing ? 'Hospital Updated Successfully!' : 'Hospital Registered Successfully!' });
            
            if (!isEditing) {
                setFormData({ name: '', address: '', city: '', licenseNumber: '', contactEmail: '', contactPhone: '', website: '' });
            } else {
                // Optional: Redirect back to list after update
                setTimeout(() => navigate('/admin/hospitals'), 1500);
            }
        } catch (err) {
            setStatus({ loading: false, msg: 'Operation Failed. Check console.' });
            console.error(err);
        }
    };

    return (
        <div className="max-w-4xl mx-auto p-8 bg-white dark:bg-gray-800 rounded-xl shadow border dark:border-gray-700">
            <div className="flex justify-between items-center mb-6">
                <h2 className="text-2xl font-bold dark:text-white">
                    {isEditing ? 'Edit Hospital Details' : 'Register New Hospital'}
                </h2>
                {isEditing && (
                    <button onClick={() => navigate('/admin/hospitals')} className="text-sm text-blue-500 hover:underline">
                        Back to List
                    </button>
                )}
            </div>

            {status.msg && (
                <div className={`p-3 mb-4 rounded ${status.msg.includes('Success') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
                    {status.msg}
                </div>
            )}
            
            <form onSubmit={handleSubmit} className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Input label="Hospital Name" name="name" value={formData.name} onChange={handleChange} required />
                    <Input 
                        label="License Number" 
                        name="licenseNumber" 
                        value={formData.licenseNumber} 
                        onChange={handleChange} 
                        required 
                        disabled={isEditing} // DISABLED IN EDIT MODE
                    />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <Input label="City" name="city" value={formData.city} onChange={handleChange} required />
                    <Input label="Street Address" name="address" value={formData.address} onChange={handleChange} required />
                </div>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <Input label="Email" type="email" name="contactEmail" value={formData.contactEmail} onChange={handleChange} />
                    <Input label="Phone" type="tel" name="contactPhone" value={formData.contactPhone} onChange={handleChange} />
                    <Input label="Website" type="url" name="website" value={formData.website} onChange={handleChange} />
                </div>
                
                <Button type="submit" isLoading={status.loading}>
                    {isEditing ? 'Update Hospital' : 'Register Facility'}
                </Button>
            </form>
        </div>
    );
};

export default RegisterHospital;