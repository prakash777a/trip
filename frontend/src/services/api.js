import axios from 'axios';
export const api=axios.create({baseURL:import.meta.env.VITE_API_URL||'http://localhost:8080/api'});
api.interceptors.request.use(c=>{const t=localStorage.getItem('tripcraft_token');if(t)c.headers.Authorization=`Bearer ${t}`;return c});
const demoDestinations=[{id:1,name:'Coorg',state:'Karnataka',best_season:'October to March',description:'Misty coffee plantations, waterfalls and forest trails.',hero_image_url:'https://images.unsplash.com/photo-1593693397690-362cb9666fc2?auto=format&fit=crop&w=900&q=80'},{id:2,name:'Jaipur',state:'Rajasthan',best_season:'October to March',description:'Royal forts, bazaars and unforgettable cuisine.',hero_image_url:'https://images.unsplash.com/photo-1477587458883-47145ed94245?auto=format&fit=crop&w=900&q=80'},{id:3,name:'Munnar',state:'Kerala',best_season:'September to May',description:'Emerald tea hills, cool valleys and wildlife.',hero_image_url:'https://images.unsplash.com/photo-1597318181409-cf64d0b5d8a2?auto=format&fit=crop&w=900&q=80'},{id:4,name:'Goa',state:'Goa',best_season:'November to February',description:'Sunset beaches, heritage and seafood.',hero_image_url:'https://images.unsplash.com/photo-1512343879784-a960bf40e7f2?auto=format&fit=crop&w=900&q=80'}];
const demoPackages=demoDestinations.map((d,i)=>({id:i+1,title:i===0?'Coorg Mist & Coffee Escape':i===1?'Royal Jaipur Weekend':i===2?'Munnar Tea Hills':'Goa Sun & Heritage',destinationName:d.name,duration_days:3+i%2,price_per_person:8999+i*2000,image_url:d.hero_image_url}));
export async function safe(getter,fallback){
  try{return (await getter()).data}
  catch(error){
    window.dispatchEvent(new CustomEvent('tripcraft:api-error',{detail:{message:error?.response?.data?.message||'The server is unavailable.'}}));
    // Demo data is opt-in for local UI prototyping only. Production workflows
    // must never hide an unavailable backend behind fabricated records.
    if(import.meta.env.VITE_ALLOW_DEMO_FALLBACK==='true') return fallback;
    return null;
  }
}
export {demoDestinations,demoPackages};
