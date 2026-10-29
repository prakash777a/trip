import React,{useState} from 'react'; import {Link,useNavigate} from 'react-router-dom'; import {api} from '../services/api'; import {useAuth} from '../context/AuthContext'; import {Users} from 'lucide-react';
const choices=['BEACH','TEMPLES','ADVENTURE','FOOD','NATURE','HISTORY'];
export default function Register(){
  const [form,setForm]=useState({fullName:'',email:'',password:'',interests:[]});
  const [err,setErr]=useState('');
  const [busy,setBusy]=useState(false);
  const {login}=useAuth();
  const nav=useNavigate();
  const toggle=x=>setForm(f=>({...f,interests:f.interests.includes(x)?f.interests.filter(i=>i!==x):[...f.interests,x]}));
  const submit=async e=>{
    e.preventDefault();
    setErr('');
    setBusy(true);
    try{
      const r=await api.post('/auth/register',form);
      login(r.data.user,r.data.accessToken);
      nav('/');
    }catch(e){
      setErr(e?.response?.data?.message || e?.message || 'Registration failed. Please verify your details.');
    }finally{
      setBusy(false);
    }
  };
  return (
    <main className="flex min-h-[760px] items-center justify-center px-5 pt-20">
      <form onSubmit={submit} className="card w-full max-w-md p-8">
        <div className="mb-7 text-center">
          <div className="mx-auto flex h-12 w-12 items-center justify-center rounded-2xl bg-teal-50 text-primary"><Users/></div>
          <h1 className="mt-4 text-3xl font-bold">Create your travel profile</h1>
          <p className="mt-2 text-slate-500">Tell us what makes a trip feel worth it.</p>
        </div>
        {err && <div className="mb-4 rounded-xl border border-rose-200 bg-rose-50 p-3 text-sm font-medium text-rose-600">{err}</div>}
        <input required className="input" placeholder="Full name" value={form.fullName} onChange={e=>setForm({...form,fullName:e.target.value})}/>
        <input required type="email" className="input mt-3" placeholder="Email" value={form.email} onChange={e=>setForm({...form,email:e.target.value})}/>
        <input required minLength="6" type="password" className="input mt-3" placeholder="Password (minimum 6 characters)" value={form.password} onChange={e=>setForm({...form,password:e.target.value})}/>
        <div className="mt-5 text-sm font-semibold">Choose interests</div>
        <div className="mt-3 flex flex-wrap gap-2">
          {choices.map(x=>(
            <button type="button" onClick={()=>toggle(x)} className={`rounded-full border px-3 py-2 text-xs font-semibold ${form.interests.includes(x)?'border-primary bg-teal-50 text-primary':'border-slate-200 text-slate-500'}`} key={x}>
              {x}
            </button>
          ))}
        </div>
        <button disabled={busy} className="btn-primary mt-7 w-full disabled:opacity-60">
          {busy ? 'Creating account...' : 'Create account'}
        </button>
        <p className="mt-5 text-center text-sm text-slate-500">Already have an account? <Link className="font-semibold text-primary" to="/login">Log in</Link></p>
      </form>
    </main>
  );
}
