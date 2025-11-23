/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      fontFamily: {
        sans: ['"Roboto Slab"', 'serif'], 
      },
      colors: {
        light: {
          bg: '#FFFFFF',
          surface: '#EFF0F4',     
          border: '#D3D6DB',      
          primary: '#415F9D',     
          textMain: '#233B6E',    
          textBody: '#1F2937',    
        },
        dark: {
          bg: '#1A1D23',          
          surface: '#252931',     
          surfaceLight: '#2E333D',
          primary: '#6B8FCF',     
          secondary: '#8BA8DA',   
          border: '#3A3F4B',      
          textMain: '#E5E7EB',    
          textMuted: '#9CA3AF',   
        }
      },
    },
  },
  plugins: [],
}