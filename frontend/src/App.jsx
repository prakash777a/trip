import React,{lazy,Suspense,useEffect,useState} from 'react';
import {Routes,Route,Link} from 'react-router-dom';
import {Menu,X,Sun,Moon,Compass} from 'lucide-react';
import {motion} from 'framer-motion';
import {Destinations,Bookings,Login,ReviewForm} from './pages';
import HomeLive from './pages/HomeLive';
const BookingLive=lazy(()=>import('./pages/BookingLive'));
const PackageDetailsLive=lazy(()=>import('./pages/PackageDetailsLive'));
const AdminLive=lazy(()=>import('./pages/AdminLive'));
const AdminBookings=lazy(()=>import('./pages/AdminBookings'));
const Profile=lazy(()=>import('./pages/Profile'));
const Register=lazy(()=>import('./pages/Register'));
const BudgetPlannerLive=lazy(()=>import('./pages/BudgetPlannerLive'));
import ProtectedRoute from './routes/ProtectedRoute';
import {useAuth} from './context/AuthContext';

export default function App(){
  const [dark,setDark]=useState(localStorage.theme==='dark'); const [open,setOpen]=useState(false); const [fallback,setFallback]=useState(false); const {user}=useAuth();
  useEffect(()=>{const onError=()=>setFallback(true);window.addEventListener('tripcraft:api-error',onError);return()=>window.removeEventListener('tripcraft:api-error',onError)},[]);
  const toggle=()=>{setDark(!dark);document.documentElement.classList.toggle('dark');localStorage.theme=!dark?'dark':'light'};
  const gated=(element,role)=><ProtectedRoute role={role}>{element}</ProtectedRoute>;
  return <div className={dark?'dark':''}><div className="min-h-screen dark:bg-slate-950 dark:text-slate-100">{fallback&&<div role="status" className="fixed bottom-4 left-1/2 z-50 -translate-x-1/2 rounded-full bg-amber-100 px-4 py-2 text-sm font-semibold text-amber-900 shadow-lg">Live data is temporarily unavailable. Please reconnect and retry. <button className="ml-3 underline" onClick={()=>setFallback(false)}>Dismiss</button></div>}
    <header className="absolute z-30 w-full bg-slate-950/25 backdrop-blur-sm"><nav className="mx-auto flex max-w-7xl items-center justify-between px-5 py-5">
      <Link to="/" className="flex items-center gap-2 text-xl font-extrabold text-white"><span className="rounded-xl bg-white/20 p-2"><Compass size={21}/></span>TripCraft</Link>
      <div className={`${open?'absolute left-4 right-4 top-20 flex':'hidden'} flex-col gap-4 rounded-2xl bg-white p-5 text-slate-700 shadow-xl md:static md:flex md:flex-row md:items-center md:bg-transparent md:p-0 md:text-white md:shadow-none`}>
        <Link onClick={()=>setOpen(false)} to="/destinations">Explore</Link><Link onClick={()=>setOpen(false)} to="/planner">AI planner</Link><Link onClick={()=>setOpen(false)} to="/bookings">My trips</Link>{user?.role==='ROLE_ADMIN'&&<><Link onClick={()=>setOpen(false)} to="/admin">Admin</Link><Link onClick={()=>setOpen(false)} to="/admin/bookings">Booked trips</Link></>}<Link className="md:hidden" to={user?'/profile':'/login'}>{user?'Profile':'Log in'}</Link>
      </div>
      <div className="flex items-center gap-2"><button onClick={toggle} className="rounded-xl bg-white/15 p-3 text-white">{dark?<Sun size={18}/>:<Moon size={18}/>}</button><Link to={user?'/profile':'/login'} className="hidden rounded-xl bg-white px-4 py-2 font-semibold text-slate-900 md:block">{user?'Profile':'Log in'}</Link><button onClick={()=>setOpen(!open)} className="rounded-xl bg-white/15 p-3 text-white md:hidden">{open?<X size={18}/>:<Menu size={18}/>}</button></div>
    </nav></header>
    <Suspense fallback={<main className="flex min-h-screen items-center justify-center text-slate-500">Loading TripCraft…</main>}><Routes><Route path="/" element={<HomeLive/>}/><Route path="/destinations" element={<Destinations/>}/><Route path="/packages/:id" element={<PackageDetailsLive/>}/><Route path="/book/:id" element={gated(<BookingLive/>)}/><Route path="/bookings" element={gated(<Bookings/>)}/><Route path="/review/:packageId/:bookingId" element={gated(<ReviewForm/>)}/><Route path="/profile" element={gated(<Profile/>)}/><Route path="/planner" element={<BudgetPlannerLive/>}/><Route path="/admin" element={gated(<AdminLive/>,'ROLE_ADMIN')}/><Route path="/admin/bookings" element={gated(<AdminBookings/>,'ROLE_ADMIN')}/><Route path="/login" element={<Login/>}/><Route path="/register" element={<Register/>}/></Routes></Suspense>
    <footer className="bg-slate-950 px-5 py-12 text-slate-400"><div className="mx-auto flex max-w-7xl flex-col justify-between gap-8 md:flex-row"><div><div className="mb-3 flex items-center gap-2 text-xl font-bold text-white"><Compass/>TripCraft</div><p>Thoughtful trips, beautifully planned.</p></div><div className="flex gap-8 text-sm"><Link to="/destinations">Destinations</Link><Link to="/planner">AI planner</Link><Link to="/bookings">My bookings</Link></div></div></footer>
  </div></div>
}
export const Fade=({children})=><motion.div initial={{opacity:0,y:12}} animate={{opacity:1,y:0}} transition={{duration:.35}}>{children}</motion.div>;
export const SectionTitle=({eyebrow,title,copy})=><div className="mb-8 max-w-2xl"><div className="mb-2 text-sm font-bold uppercase tracking-widest text-primary">{eyebrow}</div><h2 className="text-3xl font-bold md:text-4xl">{title}</h2>{copy&&<p className="mt-3 text-slate-500">{copy}</p>}</div>;
export const Img=({src,alt,className=''})=><img loading="lazy" src={src} alt={alt} className={className}/>;
