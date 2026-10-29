import React,{useEffect,useState} from 'react'; import {Link,useNavigate} from 'react-router-dom'; import {Save,LogOut,UserCheck} from 'lucide-react'; import {api,safe} from '../services/api'; import {useAuth} from '../context/AuthContext'; import {Fade,SectionTitle} from '../App';
const interests=['BEACH','TEMPLES','ADVENTURE','FOOD','NATURE','HISTORY'];
export default function Profile(){
  const {user: authUser, logout} = useAuth();
  const nav = useNavigate();
  const [user,setUser]=useState({fullName:'',email:'',phone:'',role:'',interests:[]});
  const [saved,setSaved]=useState(false);

  useEffect(()=>{
    safe(()=>api.get('/users/me'),{}).then(x=>{
      if(x&&x.email){
        setUser({...x,interests:x.interests||[]});
      }
    });
  },[]);

  const update=async e=>{
    e.preventDefault();
    const x=await safe(()=>api.put('/users/me',user),user);
    if(x) setUser({...x,interests:x.interests||[]});
    setSaved(true);
    setTimeout(()=>setSaved(false),2500);
  };

  const handleLogout=()=>{
    logout();
    nav('/login');
  };

  const toggle=i=>setUser(x=>({...x,interests:x.interests.includes(i)?x.interests.filter(y=>y!==i):[...x.interests,i]}));

  return (
    <Fade>
      <main className="mx-auto max-w-3xl px-5 pb-20 pt-32">
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 mb-6">
          <SectionTitle eyebrow="Your travel profile" title="Personalize your experience" copy="Your interests help TripCraft tailor itinerary plans and suggestions."/>
          <button type="button" onClick={handleLogout} className="self-start sm:self-auto inline-flex items-center gap-2 rounded-xl border border-rose-200 px-4 py-2.5 text-sm font-semibold text-rose-600 hover:bg-rose-50 transition">
            <LogOut size={16}/> Sign out
          </button>
        </div>

        <form onSubmit={update} className="card p-6 md:p-9 space-y-5">
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-semibold text-slate-700">Email (Account identifier)</label>
              <input disabled className="input mt-2 bg-slate-100 text-slate-500 cursor-not-allowed" value={user.email || authUser?.email || ''}/>
            </div>
            <div>
              <label className="block text-sm font-semibold text-slate-700">Role</label>
              <input disabled className="input mt-2 bg-slate-100 text-slate-500 cursor-not-allowed" value={user.role || authUser?.role || 'ROLE_TOURIST'}/>
            </div>
          </div>

          <div>
            <label className="block text-sm font-semibold text-slate-700">Full name</label>
            <input className="input mt-2" placeholder="Your full name" value={user.fullName||''} onChange={e=>setUser({...user,fullName:e.target.value})}/>
          </div>

          <div>
            <label className="block text-sm font-semibold text-slate-700">Phone</label>
            <input className="input mt-2" placeholder="Phone number" value={user.phone||''} onChange={e=>setUser({...user,phone:e.target.value})}/>
          </div>

          <div className="pt-2">
            <div className="text-sm font-semibold text-slate-700">Interests</div>
            <p className="text-xs text-slate-500 mt-1">Select topics that you love when exploring new places.</p>
            <div className="mt-3 flex flex-wrap gap-2">
              {interests.map(i=>(
                <button type="button" onClick={()=>toggle(i)} className={`rounded-full border px-4 py-2 text-sm font-semibold transition ${user.interests?.includes(i)?'border-primary bg-teal-50 text-primary shadow-xs':'border-slate-200 text-slate-500 hover:border-slate-300'}`} key={i}>
                  {i[0]+i.slice(1).toLowerCase()}
                </button>
              ))}
            </div>
          </div>

          <div className="pt-4 flex items-center gap-4">
            <button className="btn-primary">
              <Save size={17} className="mr-2"/>{saved?'Saved!':'Save profile'}
            </button>
            {saved && <span className="inline-flex items-center text-sm font-medium text-emerald-600 gap-1"><UserCheck size={16}/> Profile updated successfully</span>}
          </div>
        </form>
      </main>
    </Fade>
  );
}

