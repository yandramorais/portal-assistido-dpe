import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0f6f2',
          100: '#dcebe1',
          200: '#b8d6c3',
          300: '#8bbb9e',
          400: '#5a9a74',
          500: '#357a52',
          600: '#2a6b46',
          700: '#1f5c3a',
          800: '#184a2f',
          900: '#113622',
        },
        neutral: {
          50: '#f7f7f6',
          100: '#efefed',
          200: '#e2e2df',
          300: '#cbcbc7',
          400: '#a3a39e',
          500: '#6b6b66',
          600: '#55554f',
          700: '#41413c',
          800: '#2b2b27',
          900: '#1a1a17',
        },
        danger: {
          50: '#fef3f2',
          100: '#fee4e2',
          200: '#fecdca',
          600: '#b42318',
          700: '#912018',
        },
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', '"Segoe UI"', 'Roboto', 'sans-serif'],
      },
      borderRadius: {
        sm: '4px',
        DEFAULT: '4px',
        md: '6px',
      },
    },
  },
} satisfies Config;
