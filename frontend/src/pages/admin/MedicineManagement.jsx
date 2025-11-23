import React, { useState, useEffect } from 'react';
import { getAllMedicines, addMedicine } from '../../services/api';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';

const MedicineManagement = () => {
    const [medicines, setMedicines] = useState([]);
    const [form, setForm] = useState({ brandName: '', genericName: '', price: '', type: 'TABLET' });
    const [loading, setLoading] = useState(false);

    useEffect(() => {
        getAllMedicines().then(res => setMedicines(res.data || [])).catch(console.error);
    }, [loading]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await addMedicine({ ...form, price: parseFloat(form.price) });
            setForm({ brandName: '', genericName: '', price: '', type: 'TABLET' });
        } catch (err) {
            alert("Failed to add medicine");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Add Form */}
            <div className="lg:col-span-1 bg-white dark:bg-gray-800 p-6 rounded-xl shadow h-fit">
                <h3 className="text-xl font-bold mb-4 dark:text-white">Add Medicine</h3>
                <form onSubmit={handleSubmit} className="space-y-4">
                    <Input placeholder="Brand Name" value={form.brandName} onChange={e => setForm({...form, brandName: e.target.value})} required />
                    <Input placeholder="Generic Name" value={form.genericName} onChange={e => setForm({...form, genericName: e.target.value})} required />
                    <div className="grid grid-cols-2 gap-2">
                        <Input placeholder="Price" type="number" value={form.price} onChange={e => setForm({...form, price: e.target.value})} required />
                        <select className="border rounded-lg px-2 dark:bg-gray-700 dark:text-white" value={form.type} onChange={e => setForm({...form, type: e.target.value})}>
                            <option value="TABLET">TABLET</option>
                            <option value="SYRUP">SYRUP</option>
                            <option value="INJECTION">INJECTION</option>
                        </select>
                    </div>
                    <Button type="submit" isLoading={loading}>Add to Catalog</Button>
                </form>
            </div>

            {/* List View */}
            <div className="lg:col-span-2 bg-white dark:bg-gray-800 p-6 rounded-xl shadow">
                <h3 className="text-xl font-bold mb-4 dark:text-white">Catalog ({medicines.length})</h3>
                <div className="overflow-y-auto max-h-[500px]">
                    <table className="w-full text-left">
                        <thead className="bg-gray-50 dark:bg-gray-700 sticky top-0">
                            <tr>
                                <th className="p-3 text-sm text-gray-500">Brand</th>
                                <th className="p-3 text-sm text-gray-500">Generic</th>
                                <th className="p-3 text-sm text-gray-500">Type</th>
                                <th className="p-3 text-sm text-gray-500">Price</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y dark:divide-gray-700">
                            {medicines.map(med => (
                                <tr key={med.id}>
                                    <td className="p-3 font-medium dark:text-white">{med.brandName}</td>
                                    <td className="p-3 text-gray-500">{med.genericName}</td>
                                    <td className="p-3 text-gray-500 text-xs font-bold">{med.type}</td>
                                    <td className="p-3 text-green-600 font-bold">₹{med.price}</td>
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default MedicineManagement;