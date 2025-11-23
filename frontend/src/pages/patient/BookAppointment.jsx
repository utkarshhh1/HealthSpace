import React, { useState, useEffect, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getAllHospitals, getAllVerifiedDoctors, bookAppointment } from '../../services/api';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

// Helper to get today's date in YYYY-MM-DD format
const getTodayString = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); 
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};

const BookAppointment = () => {
    const navigate = useNavigate();
    const { user } = useAuth();
    
    const [doctors, setDoctors] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [error, setError] = useState(null);

    // --- Filters State ---
    const [searchTerm, setSearchTerm] = useState(''); 
    const [selectedCity, setSelectedCity] = useState(''); // Location Filter
    const [selectedSpecialty, setSelectedSpecialty] = useState('');

    // --- Booking Form State ---
    const [selectedDoctor, setSelectedDoctor] = useState(null);
    const [bookingData, setBookingData] = useState({ date: '', time: '', symptoms: '' });

    // 1. Fetch Master Data
    useEffect(() => {
        const loadData = async () => {
            try {
                const [docRes, hospRes] = await Promise.all([getAllVerifiedDoctors(), getAllHospitals()]);
                
                // Create a map for O(1) lookup of hospital details
                const hospitalMap = new Map(hospRes.data.map(h => [h.id, h]));
                
                // Flatten the data structure for easier filtering/sorting
                const enrichedDocs = docRes.data.map(doc => ({
                    ...doc,
                    hospitalName: hospitalMap.get(doc.hospital.id)?.name || 'Unknown Hospital',
                    city: hospitalMap.get(doc.hospital.id)?.city || 'Unknown City',
                    doctorName: doc.user.name,
                    doctorId: doc.user.id
                }));

                setDoctors(enrichedDocs);
            } catch (err) {
                setError('Failed to load provider network.');
            } finally {
                setLoading(false);
            }
        };
        loadData();
    }, []);

    // 2. Extract Unique Filter Options (Sorted Alphabetically)
    const uniqueCities = useMemo(() => 
        [...new Set(doctors.map(d => d.city))].sort(), 
    [doctors]);

    const uniqueSpecialties = useMemo(() => 
        [...new Set(doctors.map(d => d.specialty))].sort(), 
    [doctors]);

    // 3. Filter & Sort Logic (Dictionary Order)
    const filteredAndSortedDoctors = useMemo(() => {
        // A. Filter
        let result = doctors.filter(doc => {
            const matchesSearch = doc.doctorName.toLowerCase().includes(searchTerm.toLowerCase()) || 
                                  doc.hospitalName.toLowerCase().includes(searchTerm.toLowerCase());
            
            const matchesCity = selectedCity ? doc.city === selectedCity : true;
            const matchesSpecialty = selectedSpecialty ? doc.specialty === selectedSpecialty : true;

            return matchesSearch && matchesCity && matchesSpecialty;
        });

        // B. Sort (Dictionary Order)
        return result.sort((a, b) => {
            // Primary Sort: By Hospital Name (A-Z)
            const hospitalComparison = a.hospitalName.localeCompare(b.hospitalName);
            
            if (hospitalComparison !== 0) {
                return hospitalComparison;
            }
            
            // Secondary Sort: By Doctor Name (A-Z) if hospitals are the same
            return a.doctorName.localeCompare(b.doctorName);
        });

    }, [doctors, searchTerm, selectedCity, selectedSpecialty]);

    // 4. Handle Booking Submission
    const handleBook = async (e) => {
        e.preventDefault();
        if (!selectedDoctor) return;
        setSubmitting(true);

        try {
            const payload = {
                patientId: user.userId,
                doctorId: selectedDoctor.doctorId,
                hospitalId: selectedDoctor.hospital.id,
                appointmentTime: `${bookingData.date}T${bookingData.time}:00`,
                symptoms: bookingData.symptoms
            };
            await bookAppointment(payload);
            alert('Appointment booked successfully!');
            navigate('/patient/appointments');
        } catch (err) {
            setError(err.response?.data || 'Booking failed.');
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="p-10 text-center">Loading Network...</div>;

    return (
        <div className="space-y-6">
            <h2 className="text-3xl font-bold text-light-textMain dark:text-white">Find a Doctor</h2>
            
            {/* --- Search & Filter Bar --- */}
            <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow-sm border dark:border-gray-700 grid gap-4 md:grid-cols-3">
                
                {/* 1. Text Search */}
                <Input 
                    placeholder="Search Doctor or Hospital Name..." 
                    value={searchTerm} 
                    onChange={(e) => setSearchTerm(e.target.value)} 
                />

                {/* 2. Location (City) Filter */}
                <select 
                    className="px-4 py-2 border rounded-lg bg-white dark:bg-gray-700 dark:text-white dark:border-gray-600 focus:ring-2 focus:ring-light-primary focus:outline-none"
                    value={selectedCity}
                    onChange={(e) => setSelectedCity(e.target.value)}
                >
                    <option value="">All Locations</option>
                    {uniqueCities.map(city => <option key={city} value={city}>{city}</option>)}
                </select>

                {/* 3. Specialty Filter */}
                <select 
                    className="px-4 py-2 border rounded-lg bg-white dark:bg-gray-700 dark:text-white dark:border-gray-600 focus:ring-2 focus:ring-light-primary focus:outline-none"
                    value={selectedSpecialty}
                    onChange={(e) => setSelectedSpecialty(e.target.value)}
                >
                    <option value="">All Specialties</option>
                    {uniqueSpecialties.map(s => <option key={s} value={s}>{s}</option>)}
                </select>
            </div>

            {/* --- Result Count --- */}
            <p className="text-sm text-gray-500 dark:text-gray-400 text-right">
                Found {filteredAndSortedDoctors.length} matching providers
            </p>

            {/* --- Results List --- */}
            {!selectedDoctor ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {filteredAndSortedDoctors.map(doc => (
                        <div key={doc.id} className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow border dark:border-gray-700 hover:shadow-md transition flex flex-col justify-between">
                            <div>
                                <div className="flex justify-between items-start">
                                    <h3 className="text-xl font-bold text-light-primary">Dr. {doc.doctorName}</h3>
                                    <span className="bg-blue-50 text-blue-700 text-xs font-bold px-2 py-1 rounded border border-blue-100">
                                        {doc.specialty}
                                    </span>
                                </div>
                                <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">{doc.degree} • {doc.experienceYears} Yrs Exp.</p>
                                
                                <div className="mt-4 pt-4 border-t dark:border-gray-700">
                                    <p className="font-semibold text-gray-800 dark:text-gray-200">{doc.hospitalName}</p>
                                    <p className="text-sm text-gray-500"> {doc.city}</p>
                                </div>
                            </div>
                            
                            <div className="mt-4 flex justify-between items-center">
                                <span className="text-green-600 font-bold">₹{doc.consultationFee}</span>
                                <Button variant="outline" className="w-auto px-4 py-1 text-sm" onClick={() => setSelectedDoctor(doc)}>
                                    Book Visit
                                </Button>
                            </div>
                        </div>
                    ))}
                    
                    {filteredAndSortedDoctors.length === 0 && (
                        <div className="col-span-full text-center py-10 text-gray-500 border-2 border-dashed rounded-xl">
                            No doctors found matching your criteria.
                        </div>
                    )}
                </div>
            ) : (
                // --- Booking Form ---
                <div className="max-w-2xl mx-auto bg-white dark:bg-gray-800 p-8 rounded-xl shadow-lg border dark:border-gray-700 animate-fade-in">
                    <div className="flex justify-between items-center mb-6 border-b pb-4 dark:border-gray-700">
                        <div>
                            <h3 className="text-2xl font-bold dark:text-white">Booking Confirmation</h3>
                            <p className="text-gray-500">Dr. {selectedDoctor.doctorName} at {selectedDoctor.hospitalName}</p>
                        </div>
                        <button onClick={() => setSelectedDoctor(null)} className="text-sm text-red-500 hover:underline font-semibold">Cancel</button>
                    </div>
                    
                    {error && <div className="p-3 mb-4 text-sm bg-red-100 text-red-700 rounded">{error}</div>}

                    <form onSubmit={handleBook} className="space-y-4">
                        <div className="grid grid-cols-2 gap-4">
                            <Input 
                                label="Date" 
                                type="date" 
                                required 
                                value={bookingData.date} 
                                min={getTodayString()} 
                                onChange={e => {
                                    if (e.target.value < getTodayString()) {
                                        alert("You cannot book a date in the past.");
                                        return;
                                    }
                                    setBookingData({...bookingData, date: e.target.value})
                                }} 
                            />
                            <Input 
                                label="Time" 
                                type="time" 
                                required 
                                value={bookingData.time} 
                                onChange={e => setBookingData({...bookingData, time: e.target.value})} 
                            />
                        </div>
                        <Input 
                            label="Symptoms / Reason" 
                            value={bookingData.symptoms} 
                            onChange={e => setBookingData({...bookingData, symptoms: e.target.value})} 
                            required 
                            placeholder="e.g., Fever, Migraine"
                        />
                        <Button type="submit" isLoading={submitting}>Confirm Appointment</Button>
                    </form>
                </div>
            )}
        </div>
    );
};

export default BookAppointment;