import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ArrowRight, Check, MapPin, Star } from 'lucide-react';
import { api, safe } from '../services/api';
import { Fade, Img, SectionTitle } from '../App';

export default function PackageDetailsLive() {
  const { id } = useParams();
  const [pkg, setPkg] = useState(null);
  const [reviews, setReviews] = useState([]);
  const [tab, setTab] = useState('Overview');

  useEffect(() => {
    safe(() => api.get(`/packages/${id}`), null).then(data => data && setPkg(data));
    safe(() => api.get(`/packages/${id}/reviews`), null).then(data => data && setReviews(data));
  }, [id]);

  if (!pkg) return <main className="mx-auto max-w-3xl px-5 pb-20 pt-32"><div className="card p-10 text-center text-slate-500">Package details are unavailable. Please try again.</div></main>;
  const places = pkg.places || [];
  return <Fade><main className="mx-auto max-w-7xl px-5 pb-20 pt-28">
    <div className="grid gap-8 lg:grid-cols-[1.4fr_.6fr]"><div>
      <div className="grid gap-3 md:grid-cols-[1.5fr_.7fr]"><Img src={pkg.image_url || pkg.imageUrl} alt={pkg.title} className="h-[420px] w-full rounded-3xl object-cover" /><div className="hidden gap-3 md:grid">{places.slice(0, 2).map((place, i) => <Img key={place.id || i} src={place.imageUrl || pkg.image_url || pkg.imageUrl} alt={place.name} className="h-[203px] w-full rounded-2xl object-cover" />)}</div></div>
      <div className="mt-8"><div className="flex items-center gap-2 text-primary"><MapPin size={17} />{pkg.destinationName}</div><h1 className="mt-3 text-4xl font-extrabold md:text-5xl">{pkg.title}</h1><p className="mt-4 max-w-2xl text-slate-500">{pkg.description || 'A thoughtful itinerary with the freedom to add the moments that matter most to you.'}</p><div className="mt-8 flex flex-wrap gap-3"><span className="pill">{pkg.duration_days || pkg.durationDays} days</span><span className="pill">Breakfast included</span><span className="pill">Free customization</span></div></div>
      <div className="mt-10 flex gap-2 overflow-x-auto border-b border-slate-200">{['Overview', 'Itinerary', 'Inclusions', 'Reviews'].map(item => <button key={item} onClick={() => setTab(item)} className={`whitespace-nowrap border-b-2 px-4 py-3 text-sm font-semibold ${tab === item ? 'border-primary text-primary' : 'border-transparent text-slate-500'}`}>{item}</button>)}</div>
      {tab === 'Overview' && <div className="mt-8 grid gap-4 sm:grid-cols-2"><Feature title="Flexible dates" /><Feature title="Personalized places" /><Feature title="Local stays" /><Feature title="Mock payment protection" /></div>}
      {tab === 'Itinerary' && <div className="mt-10"><SectionTitle eyebrow="The route" title="A day-by-day story" />{places.map((place, i) => <div className="relative flex gap-5 border-l-2 border-teal-100 pb-8 pl-7" key={place.id || i}><div className="absolute -left-[9px] top-0 h-4 w-4 rounded-full border-4 border-white bg-primary" /><div><div className="text-sm font-bold text-primary">DAY {place.day_number || i + 1}</div><h3 className="mt-1 text-xl font-bold">{place.name}</h3><p className="mt-1 text-slate-500">{place.description || 'A memorable stop selected for pace and variety.'}</p></div></div>)}</div>}
      {tab === 'Inclusions' && <div className="mt-8 rounded-2xl bg-slate-50 p-6"><p className="whitespace-pre-wrap text-slate-600">{pkg.inclusions || 'Accommodation, breakfast, curated itinerary, and booking support.'}</p></div>}
      {tab === 'Reviews' && <div className="mt-8 grid gap-4">{reviews.length ? reviews.map((review, i) => <article className="rounded-2xl border border-slate-100 p-5" key={i}><div className="flex items-center gap-1 text-orange-500">{Array.from({ length: review.rating }).map((_, n) => <Star size={15} fill="currentColor" key={n} />)}</div><p className="mt-3 text-slate-600">{review.reviewText}</p><p className="mt-3 text-sm font-semibold text-slate-400">{review.author}</p></article>) : <div className="rounded-2xl bg-slate-50 p-8 text-center text-slate-500">No reviews yet. Be the first to share your experience after your trip.</div>}</div>}
    </div><aside><div className="card sticky top-6 p-6"><div className="text-sm text-slate-500">From</div><div className="text-3xl"><span className="price">₹{Number(pkg.price_per_person || pkg.price).toLocaleString('en-IN')}</span> <span className="price-unit text-sm">/ person</span></div><div className="my-6 grid gap-3 border-y border-slate-100 py-5 text-sm"><Feature title="Flexible dates" /><Feature title="Personalized places" /><Feature title="Secure mock payment" /></div><Link to={`/book/${pkg.id}`} className="btn-accent w-full">Book this trip <ArrowRight size={18} className="ml-2" /></Link><p className="mt-4 text-center text-xs text-slate-400">You can save and resume your booking anytime.</p></div></aside></div>
  </main></Fade>;
}

function Feature({ title }) { return <div className="flex items-center gap-2 text-slate-600"><Check className="text-primary" size={18} />{title}</div>; }
