import React from 'react'; import {Navigate} from 'react-router-dom';
export default function ProtectedRoute({children,role}){const user=JSON.parse(localStorage.getItem('tripcraft_user')||'null');if(!localStorage.getItem('tripcraft_token'))return <Navigate to="/login" replace/>;if(role&&user?.role!==role)return <Navigate to="/" replace/>;return <>{children}</>}
