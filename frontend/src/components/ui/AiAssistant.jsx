import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../../services/api.js'; 
import { useAuth } from '../../context/AuthContext.jsx';

const INITIAL_MESSAGE = { 
    role: 'assistant', 
    content: "Hello! I am HealthSpace AI. I have access to the complete medical network records. How can I assist you today?" 
};

// --- HELPER: Custom Markdown Renderer ---
const MessageContent = ({ text }) => {
    if (!text) return null;

    // 1. Check if text contains a Markdown Table
    if (text.includes('|') && text.includes('---')) {
        const lines = text.split('\n');
        const tableRows = [];
        let normalTextBefore = [];
        let normalTextAfter = [];
        let insideTable = false;

        lines.forEach(line => {
            if (line.trim().startsWith('|')) {
                insideTable = true;
                tableRows.push(line);
            } else {
                if (insideTable) normalTextAfter.push(line);
                else normalTextBefore.push(line);
            }
        });

        // Parse Table
        const header = tableRows.length > 0 ? tableRows[0].split('|').filter(c => c.trim()) : [];
        const body = tableRows.length > 2 ? tableRows.slice(2).map(row => row.split('|').filter(c => c.trim())) : [];

        return (
            <div className="space-y-4 w-full">
                <p className="text-base leading-relaxed" dangerouslySetInnerHTML={{ __html: formatText(normalTextBefore.join('\n')) }} />
                
                {/* STUNNING TABLE STYLING */}
                <div className="overflow-x-auto border border-gray-200 dark:border-gray-700 rounded-xl shadow-lg my-4 bg-white dark:bg-gray-900">
                    <table className="w-full text-sm text-left text-gray-700 dark:text-gray-300">
                        <thead className="text-xs font-bold text-white uppercase bg-gradient-to-r from-indigo-600 to-purple-600">
                            <tr>
                                {header.map((h, i) => <th key={i} className="px-6 py-4 whitespace-nowrap tracking-wider">{h}</th>)}
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                            {body.map((row, i) => (
                                <tr key={i} className={`${i % 2 === 0 ? 'bg-white dark:bg-gray-900' : 'bg-gray-50 dark:bg-gray-800'} hover:bg-indigo-50 dark:hover:bg-indigo-900/30 transition-colors duration-150`}>
                                    {row.map((cell, j) => (
                                        <td key={j} className="px-6 py-4 whitespace-nowrap">
                                            <span dangerouslySetInnerHTML={{ __html: formatText(cell) }} />
                                        </td>
                                    ))}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                </div>

                <p className="text-base leading-relaxed" dangerouslySetInnerHTML={{ __html: formatText(normalTextAfter.join('\n')) }} />
            </div>
        );
    }

    // Default: Just formatted text
    return <div dangerouslySetInnerHTML={{ __html: formatText(text) }} className="whitespace-pre-wrap leading-relaxed text-base" />;
};

// Robust Formatter to Hide Stars
const formatText = (text) => {
    return text
        .replace(/\*\*(.*?)\*\*/g, '<strong class="font-bold text-indigo-700 dark:text-indigo-300">$1</strong>') // Bold
        .replace(/\*(.*?)\*/g, '<em class="italic text-gray-600 dark:text-gray-400">$1</em>')             // Italic
        .replace(/\n/g, '<br/>');                          // Line breaks
};


const AiAssistant = () => {
    const navigate = useNavigate();
    const { user, isAuthenticated } = useAuth();
    
    const [isOpen, setIsOpen] = useState(false);
    const [input, setInput] = useState("");
    const [messages, setMessages] = useState([INITIAL_MESSAGE]); 
    const [loading, setLoading] = useState(false);
    const scrollRef = useRef(null);

    // --- HOOKS RUN UNCONDITIONALLY ---
    const shouldShow = isAuthenticated && user && user.role === 'PATIENT';

    useEffect(() => {
        if (shouldShow && user?.userId) {
            const savedKey = `chat_history_${user.userId}`;
            const savedMessages = localStorage.getItem(savedKey);
            if (savedMessages) {
                try { setMessages(JSON.parse(savedMessages)); } 
                catch (e) { setMessages([INITIAL_MESSAGE]); }
            } else {
                setMessages([INITIAL_MESSAGE]);
            }
        }
    }, [shouldShow, user?.userId]);

    useEffect(() => {
        if (shouldShow && user?.userId) {
            const savedKey = `chat_history_${user.userId}`;
            localStorage.setItem(savedKey, JSON.stringify(messages));
        }
    }, [messages, shouldShow, user?.userId]);

    useEffect(() => {
        if (scrollRef.current) scrollRef.current.scrollTop = scrollRef.current.scrollHeight;
    }, [messages, isOpen]);

    const handleSend = async () => {
        if (!input.trim()) return;

        const userMsg = { role: 'user', content: input };
        setMessages(prev => [...prev, userMsg]);
        setInput("");
        setLoading(true);

        try {
            const res = await api.post('/ai/consult', { 
                query: input,
                patientId: user?.userId 
            });
            
            const { text, recommended_doctor_id, recommended_doctor_name, recommended_doctor_specialty } = res.data;
            const docId = recommended_doctor_id ? parseInt(recommended_doctor_id) : null;

            const action = docId ? { 
                label: `Book Appointment with Dr. ${recommended_doctor_name} (${recommended_doctor_specialty})`, 
                id: docId
            } : null;

            const botMsg = { 
                role: 'assistant', 
                content: text,
                action: action
            };
            
            setMessages(prev => [...prev, botMsg]);

        } catch (err) {
            const errorMsg = { role: 'assistant', content: "I'm unable to reach the HealthSpace network. Please check your connection." };
            setMessages(prev => [...prev, errorMsg]);
        } finally {
            setLoading(false);
        }
    };

    const handleBooking = (docId) => {
        navigate('/patient/book', { state: { preselectedDoctorId: docId } });
        setIsOpen(false);
    };

    // --- CONDITIONAL RENDERING ---
    if (!shouldShow) return null;

    return (
        <>
            {/* LAUNCHER */}
            {!isOpen && (
                <div className="fixed bottom-6 right-6 z-50 font-sans animate-fade-in">
                    <button 
                        onClick={() => setIsOpen(true)}
                        className="group flex items-center justify-center w-16 h-16 bg-gradient-to-br from-blue-600 to-indigo-700 rounded-full shadow-2xl hover:scale-110 transition-all duration-300 border-4 border-white dark:border-gray-800"
                    >
                        <span className="text-3xl filter drop-shadow-md">✨</span>
                        <span className="absolute right-full mr-4 bg-gray-900 text-white text-xs font-bold px-3 py-1.5 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none shadow-lg">
                            AI Assistant
                        </span>
                    </button>
                </div>
            )}

            {/* MAIN CHAT INTERFACE */}
            {isOpen && (
                <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 backdrop-blur-md p-0 md:p-6 animate-in fade-in duration-200">
                    
                    <div className="w-full h-full md:max-w-5xl md:h-[85vh] bg-white dark:bg-gray-900 md:rounded-3xl shadow-2xl flex flex-col overflow-hidden relative transition-colors duration-300 border border-gray-100 dark:border-gray-800">
                        
                        {/* Header */}
                        <div className="flex justify-between items-center p-5 border-b border-gray-100 dark:border-gray-800 bg-white/90 dark:bg-gray-900/90 backdrop-blur z-10 absolute top-0 w-full">
                            <div className="flex items-center gap-4">
                                <div className="w-10 h-10 bg-gradient-to-tr from-blue-600 to-indigo-600 rounded-xl flex items-center justify-center text-white text-xl font-bold shadow-md ring-2 ring-blue-50 dark:ring-gray-800">
                                    AI
                                </div>
                                <div>
                                    <h2 className="text-lg font-bold text-gray-900 dark:text-white tracking-tight">HealthSpace Assistant</h2>
                                    <p className="text-xs text-green-600 dark:text-green-400 font-medium flex items-center gap-1.5">
                                        <span className="w-1.5 h-1.5 bg-green-500 rounded-full animate-pulse"></span>
                                        Connected to Medical Graph
                                    </p>
                                </div>
                            </div>
                            <button 
                                onClick={() => setIsOpen(false)}
                                className="p-2.5 rounded-full hover:bg-gray-100 dark:hover:bg-gray-800 text-gray-400 hover:text-gray-600 dark:hover:text-gray-300 transition-all duration-200"
                            >
                                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                                </svg>
                            </button>
                        </div>

                        {/* Chat Area with Padding fix */}
                        <div ref={scrollRef} className="flex-1 overflow-y-auto pt-24 pb-40 px-4 md:px-0 scroll-smooth bg-gradient-to-b from-white to-gray-50 dark:from-gray-900 dark:to-gray-900/50">
                            <div className="max-w-4xl mx-auto space-y-8">
                                {messages.map((msg, idx) => (
                                    <div key={idx} className={`flex gap-4 ${msg.role === 'user' ? 'justify-end' : 'justify-start'} animate-in slide-in-from-bottom-2 duration-300`}>
                                        
                                        {msg.role === 'assistant' && (
                                            <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-blue-600 to-indigo-600 flex-shrink-0 flex items-center justify-center text-white text-xs font-bold shadow-sm mt-1">AI</div>
                                        )}

                                        <div className={`max-w-[95%] md:max-w-[90%] p-5 rounded-2xl text-[15px] leading-7 shadow-sm border ${
                                            msg.role === 'user' 
                                            ? 'bg-blue-600 text-white rounded-br-none border-blue-600' 
                                            : 'bg-white dark:bg-gray-800 text-gray-800 dark:text-gray-100 rounded-bl-none border-gray-200 dark:border-gray-700'
                                        }`}>
                                            <MessageContent text={msg.content} />
                                            
                                            {msg.action && (
                                                <div className="mt-5 pt-4 border-t border-dashed border-gray-300 dark:border-gray-600">
                                                    <button 
                                                        onClick={() => handleBooking(msg.action.id)}
                                                        className="flex items-center justify-center gap-2 w-full px-5 py-3 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white rounded-xl font-bold transition-all shadow-md hover:shadow-lg transform hover:-translate-y-0.5"
                                                    >
                                                        <span>📅</span>
                                                        {msg.action.label}
                                                    </button>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                ))}

                                {loading && (
                                    <div className="flex gap-4 animate-pulse">
                                        <div className="w-8 h-8 rounded-full bg-gray-200 dark:bg-gray-700 flex-shrink-0"></div>
                                        <div className="bg-white dark:bg-gray-800 p-4 rounded-2xl rounded-bl-none flex items-center gap-2 shadow-sm border border-gray-100 dark:border-gray-700">
                                            <div className="flex space-x-1.5">
                                                <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce"></span>
                                                <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce delay-100"></span>
                                                <span className="w-2 h-2 bg-blue-400 rounded-full animate-bounce delay-200"></span>
                                            </div>
                                        </div>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Footer Input */}
                        <div className="absolute bottom-0 left-0 w-full p-6 bg-white/80 dark:bg-gray-900/80 backdrop-blur-lg border-t border-gray-200 dark:border-gray-800 z-20">
                            <div className="max-w-4xl mx-auto relative group">
                                <input 
                                    className="w-full pl-6 pr-14 py-4 bg-gray-50 dark:bg-gray-800 border border-gray-300 dark:border-gray-700 focus:bg-white dark:focus:bg-gray-900 focus:border-blue-500 focus:ring-4 focus:ring-blue-500/10 rounded-full text-gray-900 dark:text-white placeholder-gray-500 outline-none shadow-sm transition-all text-base"
                                    placeholder="Ask about symptoms, doctors, or hospitals..."
                                    value={input}
                                    onChange={e => setInput(e.target.value)}
                                    onKeyPress={e => e.key === 'Enter' && handleSend()}
                                    autoFocus
                                    disabled={loading}
                                />
                                <button 
                                    onClick={handleSend}
                                    disabled={loading || !input.trim()}
                                    className="absolute right-2 top-2 p-2.5 bg-blue-600 text-white rounded-full hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition-all shadow-md hover:shadow-lg transform active:scale-95"
                                >
                                    {loading ? (
                                        <svg className="animate-spin h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                                            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                                            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                                        </svg>
                                    ) : (
                                        <svg xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" strokeWidth={2.5} stroke="currentColor" className="w-6 h-6">
                                            <path strokeLinecap="round" strokeLinejoin="round" d="M4.5 12h15m0 0l-6.75-6.75M19.5 12l-6.75 6.75" />
                                        </svg>
                                    )}
                                </button>
                            </div>
                            <p className="text-center text-[11px] text-gray-400 mt-3 font-medium tracking-wide uppercase">
                                Medical Assistant • Verifies all data from hospital records
                            </p>
                        </div>

                    </div>
                </div>
            )}
        </>
    );
};

export default AiAssistant;