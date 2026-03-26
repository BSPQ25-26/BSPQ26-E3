# Frontend - React App

Single-page application that provides the user interface for registration, login, and dashboard functionality.

## Tech Stack

- **React 19** (Create React App)
- **CSS** for styling

## Project Structure

```
src/
├── App.js           # Main app component and routing
├── Login.js         # Login page
├── Register.js      # Registration page
├── Dashboard.js     # User dashboard
├── assets/          # Static assets (images, icons)
└── index.js         # Entry point
```

## Running Locally

1. Install dependencies:

   ```bash
   npm install
   ```

2. Start the development server:

   ```bash
   npm start
   ```

3. The app will be available at `http://localhost:3000`

The development server proxies API requests (`/api/*`) to `http://localhost:8080`, so make sure the backend is running.

## Building for Production

```bash
npm run build
```

This creates an optimized build in the `build/` folder, ready to be served by a static file server (nginx in Docker).
