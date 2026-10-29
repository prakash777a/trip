import React,{createContext,useContext,useMemo,useState} from 'react';
const AuthContext=createContext(null);
export function AuthProvider({children}){const [user,setUser]=useState(()=>JSON.parse(localStorage.getItem('tripcraft_user')||'null'));const login=(u,t)=>{localStorage.setItem('tripcraft_token',t);localStorage.setItem('tripcraft_user',JSON.stringify(u));setUser(u)};const logout=()=>{localStorage.removeItem('tripcraft_token');localStorage.removeItem('tripcraft_user');setUser(null)};return <AuthContext.Provider value={useMemo(()=>({user,login,logout}),[user])}>{children}</AuthContext.Provider>}
export const useAuth=()=>useContext(AuthContext);
