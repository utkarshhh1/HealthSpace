import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { getAppointmentById, getPatientProfile, getPrescriptionsForPatient, createPrescription, updateAppointmentStatus } from '../../services/api';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const CreatePrescription = () => {
    const { appointmentId } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();

    // Data State
    const [appt, setAppt] = useState(null);
    const [patient, setPatient] = useState(null);
    const [history, setHistory] = useState([]); // Previous records
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);

    // Form State
    const [diagnosis, setDiagnosis] = useState('');
    const [notes, setNotes] = useState('');
    const [medicines, setMedicines] = useState([{ medicineName: '', dosage: '', frequency: '', duration: '' }]);

    useEffect(() => {
        const loadContext = async () => {
            try {
                // 1. Get Appointment Details
                const apptRes = await getAppointmentById(appointmentId);
                const appointmentData = apptRes.data;
                setAppt(appointmentData);
                
                // 2. Use Patient ID from Appointment to fetch Profile & History
                const pid = appointmentData.patientId || appointmentData.patient?.id;
                
                if (pid) {
                    const [pRes, hRes] = await Promise.all([
                        getPatientProfile(pid).catch(() => null),
                        getPrescriptionsForPatient(pid).catch(() => ({ data: [] }))
                    ]);
                    
                    if (pRes) setPatient(pRes.data);
                    if (hRes) setHistory(hRes.data);
                }
            } catch (err) {
                console.error("Context Load Error", err);
                alert("Failed to load appointment context. Check console.");
            } finally {
                setLoading(false);
            }
        };
        loadContext();
    }, [appointmentId]);

    // Dynamic Medicines Logic
    const updateMedicine = (index, field, value) => {
        const newMeds = [...medicines];
        newMeds[index][field] = value;
        setMedicines(newMeds);
    };

    const addRow = () => setMedicines([...medicines, { medicineName: '', dosage: '', frequency: '', duration: '' }]);
    const removeRow = (idx) => setMedicines(medicines.filter((_, i) => i !== idx));

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSubmitting(true);
        try {
            await createPrescription({
                appointmentId: parseInt(appointmentId),
                doctorId: user.userId,
                patientId: appt.patientId || appt.patient.id,
                diagnosis,
                notes,
                medicines
            });
            
            // Attempt to mark as completed
            try { await updateAppointmentStatus(appointmentId, "COMPLETED"); } catch(e){}

            alert("Prescription Issued Successfully!");
            navigate('/doctor/prescriptions');
        } catch (err) {
            alert("Failed to issue prescription.");
        } finally {
            setSubmitting(false);
        }
    };

    if (loading) return <div className="p-10 text-center">Loading Patient Context...</div>;

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 h-[calc(100vh-100px)]">
            
            {/* LEFT COLUMN: PRESCRIPTION FORM (Scrollable) */}
            <div className="lg:col-span-2 overflow-y-auto pr-2 space-y-6">
                <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow border dark:border-gray-700">
                    <h2 className="text-2xl font-bold dark:text-white mb-4">Issue Prescription</h2>
                    <div className="mb-4 p-3 bg-gray-50 dark:bg-gray-700 rounded text-sm">
                        <span className="font-bold text-gray-700 dark:text-gray-200">Current Visit Reason:</span> 
                        <span className="ml-2 italic text-gray-600 dark:text-gray-300">"{appt?.symptoms}"</span>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-6">
                        <Input label="Diagnosis" value={diagnosis} onChange={e => setDiagnosis(e.target.value)} required placeholder="e.g., Acute Bronchitis" />
                        
                        <div>
                            <label className="block text-sm font-medium mb-2 dark:text-gray-300">Medications</label>
                            {medicines.map((med, idx) => (
                                <div key={idx} className="flex flex-wrap md:flex-nowrap gap-2 mb-2 items-start">
                                    <div className="w-full md:w-1/3">
                                        <Input placeholder="Medicine Name" value={med.medicineName} onChange={e => updateMedicine(idx, 'medicineName', e.target.value)} required />
                                    </div>
                                    <div className="w-1/4 md:w-1/5">
                                        <Input placeholder="Dose" value={med.dosage} onChange={e => updateMedicine(idx, 'dosage', e.target.value)} required />
                                    </div>
                                    <div className="w-1/4 md:w-1/5">
                                        <Input placeholder="Freq" value={med.frequency} onChange={e => updateMedicine(idx, 'frequency', e.target.value)} required />
                                    </div>
                                    <div className="w-1/4 md:w-1/5">
                                        <Input placeholder="Dur." value={med.duration} onChange={e => updateMedicine(idx, 'duration', e.target.value)} required />
                                    </div>
                                    {medicines.length > 1 && (
                                        <button type="button" onClick={() => removeRow(idx)} className="mt-2 text-red-500 px-2 font-bold text-xl">×</button>
                                    )}
                                </div>
                            ))}
                            <button type="button" onClick={addRow} className="text-sm text-blue-600 font-bold hover:underline mt-1">+ Add Another Medicine</button>
                        </div>

                        <Input label="Clinical Notes / Advice" value={notes} onChange={e => setNotes(e.target.value)} placeholder="e.g., Drink plenty of fluids, rest for 2 days." />

                        <Button type="submit" isLoading={submitting} className="bg-green-600 hover:bg-green-700">Finalize & Complete Visit</Button>
                    </form>
                </div>
            </div>

            {/* RIGHT COLUMN: PATIENT CONTEXT (Fixed/Scrollable) */}
            <div className="space-y-6 overflow-y-auto pr-1 h-full">
                
                {/* 1. Vitals Card */}
                <div className="bg-blue-50 dark:bg-gray-800 p-6 rounded-xl border border-blue-100 dark:border-gray-700 shadow-sm">
                    <h3 className="font-bold text-lg mb-3 text-blue-900 dark:text-blue-300 border-b border-blue-200 dark:border-gray-600 pb-2">Patient Vitals</h3>
                    {patient ? (
                        <div className="grid grid-cols-2 gap-4 text-sm">
                            <div>
                                <p className="text-gray-500 dark:text-gray-400 text-xs uppercase">Name</p>
                                <p className="font-semibold text-gray-800 dark:text-white">{patient.user?.name || 'Unknown'}</p>
                            </div>
                            <div>
                                <p className="text-gray-500 dark:text-gray-400 text-xs uppercase">Gender / Age</p>
                                <p className="font-semibold text-gray-800 dark:text-white">{patient.gender} / {patient.dob ? new Date().getFullYear() - new Date(patient.dob).getFullYear() : 'N/A'} Yrs</p>
                            </div>
                            <div>
                                <p className="text-gray-500 dark:text-gray-400 text-xs uppercase">Blood Group</p>
                                <p className="font-bold text-red-600">{patient.bloodGroup || 'N/A'}</p>
                            </div>
                            <div>
                                <p className="text-gray-500 dark:text-gray-400 text-xs uppercase">Height / Weight</p>
                                <p className="font-semibold text-gray-800 dark:text-white">{patient.height} cm / {patient.weight} kg</p>
                            </div>
                            <div className="col-span-2 mt-2 pt-2 border-t border-blue-200 dark:border-gray-600">
                                <p className="text-gray-500 dark:text-gray-400 text-xs uppercase">Emergency Contact</p>
                                <p className="font-medium text-gray-800 dark:text-white">{patient.emergencyContactName} ({patient.emergencyContactPhone})</p>
                            </div>
                        </div>
                    ) : (
                        <p className="text-sm text-gray-500 italic">Patient profile not completed.</p>
                    )}
                </div>

                {/* 2. Medical History Card */}
                <div className="bg-white dark:bg-gray-800 p-6 rounded-xl shadow border dark:border-gray-700">
                    <h3 className="font-bold text-lg mb-3 dark:text-white flex justify-between items-center">
                        Medical History
                        <span className="text-xs font-normal bg-gray-200 dark:bg-gray-600 px-2 py-1 rounded-full">{history.length} Records</span>
                    </h3>
                    
                    <div className="space-y-4 max-h-[400px] overflow-y-auto pr-1 custom-scrollbar">
                        {history.length === 0 ? (
                            <p className="text-sm text-gray-500 text-center py-4">No past prescriptions found.</p>
                        ) : (
                            history.map(record => (
                                <div key={record.id} className="text-sm border-l-4 border-blue-400 pl-3 py-1 bg-gray-50 dark:bg-gray-700/30 rounded-r">
                                    <div className="flex justify-between mb-1">
                                        <span className="font-bold text-gray-800 dark:text-gray-200">{record.diagnosis}</span>
                                        <span className="text-xs text-gray-500">{new Date(record.createdAt).toLocaleDateString()}</span>
                                    </div>
                                    <p className="text-xs text-gray-600 dark:text-gray-400 truncate">
                                        {record.medicines?.map(m => m.medicineName).join(', ')}
                                    </p>
                                </div>
                            ))
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default CreatePrescription;