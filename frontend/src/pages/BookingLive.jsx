import React, { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, ArrowRight, Check, LoaderCircle, Minus, Plus, ShieldCheck } from 'lucide-react';
import { api, safe } from '../services/api';
import { Fade, Img } from '../App';

const steps = ['Trip details', 'Places', 'Rooms', 'Vehicles', 'Travelers', 'Review & pay'];
const today = new Date().toISOString().slice(0, 10);
const defaultTravelDate = (() => {
  const d = new Date();
  d.setDate(d.getDate() + 7);
  return d.toISOString().slice(0, 10);
})();
// Package/booking place rows have their own relationship id plus the actual place_id.
// The booking API always expects the actual place id; recommendation rows only have id.
const normalizePlaces = rows => (rows || []).map(place => ({ ...place, id: place.place_id ?? place.id }));

export default function BookingLive() {
  const { id } = useParams();
  const nav = useNavigate();
  const [pkg, setPkg] = useState({});
  const [booking, setBooking] = useState(null);
  const [step, setStep] = useState(1);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [details, setDetails] = useState({ travelDate: defaultTravelDate, adults: 2, children: 0 });
  const [selectedPlaces, setSelectedPlaces] = useState([]);
  const [recommendations, setRecommendations] = useState([]);
  const [rooms, setRooms] = useState([]);
  const [vehicles, setVehicles] = useState([]);
  const [travelers, setTravelers] = useState([]);
  const [preview, setPreview] = useState(null);
  const [paid, setPaid] = useState(false);

  useEffect(() => {
    let cancelled = false;
    safe(() => api.get(`/packages/${id}`), null).then(data => {
      if (!data) return;
      if (!cancelled) {
        setPkg(data);
        setSelectedPlaces(normalizePlaces((data.places || []).filter(p => p.type === 'MANDATORY')));
      }
    });
    if (localStorage.getItem('tripcraft_token')) {
      safe(() => api.get('/bookings'), []).then(async items => {
        const existing = (items || []).find(x => String(x.package_id) === String(id) && x.status === 'DRAFT');
        const data = existing ? (await api.get(`/bookings/${existing.id}`)).data : (await api.post('/bookings', { packageId: id })).data;
        if (cancelled) return;
        setBooking(data);
        if (data.travel_date) setDetails({ travelDate: String(data.travel_date).slice(0, 10), adults: data.adults || 2, children: data.children || 0 });
        if (data.places) setSelectedPlaces(normalizePlaces(data.places));
        if (data.travelers && data.travelers.length > 0) {
          setTravelers(data.travelers.map(t => ({
            fullName: t.full_name || t.fullName || '',
            age: t.age ?? '',
            gender: t.gender || 'PREFER_NOT_TO_SAY',
            idType: t.id_type || t.idType || 'GOVERNMENT_ID',
            idNumber: t.id_number || t.idNumber || '',
          })));
        }
        if (data.id) safe(() => api.get(`/ai/recommendations?bookingId=${data.id}`), { recommendations: [] }).then(r => setRecommendations(r.recommendations || []));
      }).catch(() => setError('We could not start a draft booking. Please try again.'));
    }
    return () => { cancelled = true; };
  }, [id]);

  const people = Number(details.adults || 0) + Number(details.children || 0);
  const nights = Math.max(1, Number(pkg.duration_days || pkg.durationDays || 3) - 1);
  const getBookingId = () => booking?.id ?? booking?.data?.id ?? null;
  const places = normalizePlaces(pkg.places);
  const run = async action => {
    setBusy(true); setError('');
    try {
      const res = await action();
      return res && typeof res === 'object' && 'data' in res ? res.data : res;
    }
    catch (e) {
      setError(e?.response?.data?.message || 'We could not save this step. Please check the details and try again.');
      return null;
    }
    finally { setBusy(false); }
  };

  const nextDetails = async e => {
    e.preventDefault();
    if (!details.travelDate) return setError('Choose a travel date.');
    const bId = getBookingId();
    if (!bId) return setStep(2);
    const saved = await run(() => api.put(`/bookings/${bId}/details`, details));
    if (saved) { setBooking(saved); setStep(2); }
  };
  const addPlace = async place => {
    const placeId = place.place_id ?? place.id;
    if (!placeId) return setError('This place is missing a valid place ID. Please reload the booking.');
    const bId = getBookingId();
    if (!bId) return setSelectedPlaces(current => current.some(x => x.id === placeId) ? current : [...current, { ...place, id: placeId }]);
    const saved = await run(() => api.post(`/bookings/${bId}/places`, { placeId, dayNumber: place.day_number || 1, orderNo: (place.order_no || 1) + selectedPlaces.length }));
    if (saved) { setBooking(saved); setSelectedPlaces(normalizePlaces(saved.places)); }
  };
  const removePlace = async place => {
    if (place.type === 'MANDATORY') return;
    const bId = getBookingId();
    if (!bId) return setSelectedPlaces(current => current.filter(x => x.id !== place.id));
    const saved = await run(() => api.delete(`/bookings/${bId}/places/${place.place_id ?? place.id}`));
    if (saved) { setBooking(saved); setSelectedPlaces(normalizePlaces(saved.places)); }
  };
  const reorderPlaces = async ordered => {
    const bId = getBookingId();
    if (!bId) return;
    const saved = await run(() => api.put(`/bookings/${bId}/places/reorder`, { places: ordered.map((p, i) => ({ placeId: p.id, dayNumber: p.day_number || 1, orderNo: i + 1 })) }));
    if (saved) { setBooking(saved); setSelectedPlaces(normalizePlaces(saved.places)); }
  };
  const reverseOptional = () => {
    const optional = selectedPlaces.filter(p => p.type !== 'MANDATORY').reverse();
    reorderPlaces(selectedPlaces.map(p => p.type === 'MANDATORY' ? p : optional.shift()));
  };
  const extendTrip = async () => {
    const bId = getBookingId();
    if (!bId) return;
    const saved = await run(() => api.post(`/bookings/${bId}/extend-day`));
    if (saved) setBooking(saved);
  };
  const saveRooms = async () => {
    if (!rooms.length) return setError('Choose at least one room.');
    const bId = getBookingId();
    if (!bId) return setStep(4);
    const saved = await run(() => api.post(`/bookings/${bId}/rooms`, { rooms }));
    if (saved) { setBooking(saved); setStep(4); }
  };
  const saveVehicles = async () => {
    if (!vehicles.length) return setError('Choose at least one vehicle.');
    const bId = getBookingId();
    if (!bId) return setStep(5);
    const saved = await run(() => api.post(`/bookings/${bId}/vehicles`, { vehicles }));
    if (saved) { setBooking(saved); setStep(5); }
  };
  const saveTravelers = async (e) => {
    if (e && e.preventDefault) e.preventDefault();
    const list = Array.from({ length: people }).map((_, i) => travelers[i] || {});
    for (let i = 0; i < people; i++) {
      const t = list[i];
      const name = (t.fullName || t.full_name || '').trim();
      if (!name) return setError(`Please enter the full name for Traveler ${i + 1}.`);
      if (t.age === undefined || t.age === '' || Number(t.age) < 0) return setError(`Please enter a valid age for Traveler ${i + 1}.`);
      const idNum = (t.idNumber || t.id_number || '').trim();
      if (!idNum) return setError(`Please enter the ID number for Traveler ${i + 1}.`);
    }
    const payload = list.map(t => ({
      fullName: (t.fullName || t.full_name || '').trim(),
      age: Number(t.age),
      gender: t.gender || 'PREFER_NOT_TO_SAY',
      idType: t.idType || t.id_type || 'GOVERNMENT_ID',
      idNumber: (t.idNumber || t.id_number || '').trim(),
    }));
    const bId = getBookingId();
    if (!bId) return setStep(6);
    const saved = await run(() => api.put(`/bookings/${bId}/travelers`, { travelers: payload }));
    if (saved) { setBooking(saved); setStep(6); }
  };
  useEffect(() => {
    if (step === 6) {
      const bId = getBookingId();
      if (bId) {
        run(() => api.post(`/bookings/${bId}/price-preview`)).then(price => {
          if (price) setPreview(price);
        });
      }
    }
  }, [step]);

  const pay = async () => {
    const bId = getBookingId();
    if (!bId) return setPaid(true);
    const confirmed = await run(() => api.post(`/bookings/${bId}/pay`, { paymentMethod: 'MOCK_CARD' }));
    if (confirmed) { setBooking(confirmed); setPaid(true); }
  };

  if (!pkg.id) return <main className="mx-auto max-w-3xl px-5 pb-20 pt-32"><div className="card p-10 text-center text-slate-500">Loading booking data…</div></main>;
  return <Fade><main className="booking-page mx-auto max-w-7xl px-5 pb-20 pt-32">
    <div className="mb-9 flex items-center gap-2 overflow-x-auto">{steps.map((label, i) => <React.Fragment key={label}><div className={`flex min-w-max items-center gap-2 text-sm font-semibold ${step === i + 1 ? 'text-primary' : 'text-slate-400'}`}><span className={`flex h-9 w-9 items-center justify-center rounded-full ${step > i + 1 ? 'bg-primary text-white' : step === i + 1 ? 'bg-teal-50 text-primary' : 'bg-slate-100'}`}>{step > i + 1 ? <Check size={17} /> : i + 1}</span>{label}</div>{i < 5 && <div className="h-px w-8 bg-slate-200" />}</React.Fragment>)}</div>
    <div className="grid gap-8 lg:grid-cols-[1fr_350px]"><section className="booking-surface card p-6 md:p-9"><div className="mb-8"><div className="text-xs font-bold uppercase tracking-widest text-primary">Step {step} of 6</div><h1 className="mt-2 text-3xl font-bold">{steps[step - 1]}</h1>{error && <div role="alert" className="mt-4 rounded-xl bg-red-50 p-3 text-sm text-red-700">{error}</div>}</div>
      {step === 1 && <form onSubmit={nextDetails} className="grid gap-5 sm:grid-cols-2"><label>Travel date<input aria-label="Travel date" min={today} required type="date" value={details.travelDate} onChange={e => setDetails({ ...details, travelDate: e.target.value })} className="input mt-2" /></label><label>Adults<input aria-label="Adults" min="1" required type="number" value={details.adults} onChange={e => setDetails({ ...details, adults: e.target.value })} className="input mt-2" /></label><label>Children<input aria-label="Children" min="0" type="number" value={details.children} onChange={e => setDetails({ ...details, children: e.target.value })} className="input mt-2" /></label><div className="rounded-xl bg-teal-50 p-4 text-sm text-teal-800">Total travelers: <b>{people}</b></div><button className="btn-primary sm:col-span-2" disabled={busy}>{busy ? <LoaderCircle className="animate-spin" /> : <>Continue <ArrowRight size={18} className="ml-2" /></>}</button></form>}
      {step === 2 && <div><p className="mb-5 text-slate-500">Mandatory stops are included. Add recommendations, remove optional stops, or reorder optional stops.</p>{recommendations.length > 0 && <div className="mb-6 rounded-2xl border border-teal-100 bg-teal-50/60 p-4"><div className="font-bold text-teal-900">Recommended for you</div><div className="mt-3 grid gap-2">{recommendations.map(place => <div className="flex items-center justify-between rounded-xl bg-white p-3" key={place.id}><div><b>{place.name}</b><p className="text-xs text-slate-500">{place.durationMinutes} min · ₹{place.price} · {place.reason}</p></div><button onClick={() => addPlace(place)} disabled={selectedPlaces.some(x => x.id === place.id)} className="rounded-lg bg-primary px-3 py-2 text-xs font-bold text-white disabled:opacity-40">Add</button></div>)}</div></div>}<div className="grid gap-3">{places.map((place, i) => { const added = selectedPlaces.some(x => x.id === place.id); return <div className="flex items-center justify-between rounded-xl border border-slate-200 p-4" key={place.id || i}><div><b>{place.name}</b><p className="text-sm text-slate-500">Day {place.day_number || i % 2 + 1} · {place.type === 'MANDATORY' ? 'Included' : `₹${place.price || 0} add-on`}</p></div>{added ? place.type === 'MANDATORY' ? <span className="pill">Mandatory</span> : <button onClick={() => removePlace(place)} className="rounded-lg bg-orange-50 px-3 py-2 text-xs font-bold text-orange-700">Remove</button> : <button onClick={() => addPlace(place)} className="rounded-lg bg-teal-50 p-2 text-primary"><Plus size={18} /></button>}</div>; })}</div>{selectedPlaces.length > 0 && <div className="mt-6 rounded-xl border border-slate-100 p-4"><div className="flex items-center justify-between"><b>Selected route order</b><button onClick={reverseOptional} className="text-xs font-semibold text-primary">Reverse optional order</button></div><div className="mt-3 flex flex-wrap gap-2">{selectedPlaces.map((place, i) => <span className="pill" key={place.id}>{i + 1}. {place.name}</span>)}</div>{selectedPlaces.filter(place => (place.day_number || 1) === 1).length > 4 && <div className="mt-3 flex items-center justify-between gap-3 rounded-xl bg-amber-50 p-3 text-sm text-amber-800"><span>This day has many stops.</span><button onClick={extendTrip} className="font-bold underline">Extend by one day (+₹1,500/person)</button></div>}</div>}<button onClick={() => { setError(''); setStep(3); }} className="btn-primary mt-8">Continue <ArrowRight size={18} className="ml-2" /></button></div>}
      {step === 3 && <RoomStep pkg={pkg} date={details.travelDate} nights={nights} rooms={rooms} setRooms={setRooms} people={people} onContinue={saveRooms} />}
      {step === 4 && <VehicleStep destinationId={pkg.destination_id ?? pkg.destinationId} date={details.travelDate} vehicles={vehicles} setVehicles={setVehicles} people={people} onContinue={saveVehicles} />}
      {step === 5 && <TravelerStep people={people} travelers={travelers} setTravelers={setTravelers} onContinue={saveTravelers} />}
      {step === 6 && <div>
        <div className="rounded-2xl bg-slate-50 p-6">
          <Line label={`Package × ${people}`} value={`₹${(Number(pkg.price_per_person || pkg.price) * people).toLocaleString('en-IN')}`} />
          <Line label={`Rooms × ${nights} nights`} value={preview ? `₹${Number(preview.rooms || 0).toLocaleString('en-IN')}` : 'Calculating...'} />
          <Line label="Vehicles" value={preview ? `₹${Number(preview.vehicles || 0).toLocaleString('en-IN')}` : 'Calculating...'} />
          <Line label="Extra places" value={preview ? `₹${Number(preview.extraPlaces || 0).toLocaleString('en-IN')}` : 'Calculating...'} />
          <Line label="Taxes" value={preview ? `₹${Number(preview.tax || 0).toLocaleString('en-IN')}` : '5%'} />
          {preview && <Line label="Total" value={`₹${Number(preview.total || 0).toLocaleString('en-IN')}`} strong />}
        </div>
        <p className="my-5 text-sm text-slate-500">Prices are calculated live. Inventory is secured upon payment confirmation.</p>
        {paid && <div className="rounded-xl bg-emerald-100 p-4 text-emerald-800 font-medium">
          <b className="text-emerald-900 block text-base mb-1">🎉 Payment successful!</b>
          Booking <b>{booking?.booking_code || `TC-${id}-DEMO`}</b> is confirmed.
        </div>}
        {!paid && <button onClick={pay} className="btn-accent mt-3 w-full flex items-center justify-center py-3 text-base font-semibold" disabled={busy}>
          {busy ? <LoaderCircle className="animate-spin" /> : <>Pay {preview?.total ? `₹${Number(preview.total).toLocaleString('en-IN')}` : ''} with Mock Card <ShieldCheck size={18} className="ml-2" /></>}
        </button>}
        {paid && <button onClick={() => nav('/bookings')} className="btn-primary mt-7 w-full">View my trips</button>}
      </div>}
      {step > 1 && step < 6 && <button onClick={() => setStep(step - 1)} className="btn mt-8 border border-slate-200"><ArrowLeft size={18} className="mr-2" />Back</button>}
    </section><aside className="card h-fit p-6 lg:sticky lg:top-6"><div className="flex gap-4"><Img src={pkg.image_url || pkg.imageUrl} alt={pkg.title} className="h-20 w-20 rounded-xl object-cover" /><div><h3 className="font-bold">{pkg.title}</h3><p className="text-sm text-slate-500">{pkg.duration_days || pkg.durationDays} days</p></div></div><div className="my-6 border-y border-slate-100 py-5 text-sm"><Line label="Travelers" value={people} /><Line label="Places added" value={selectedPlaces.length} /></div><div className="flex justify-between text-xl font-bold"><span>Package</span><span className="price">₹{(Number(pkg.price_per_person || pkg.price) * people).toLocaleString('en-IN')}</span></div></aside></div>
  </main></Fade>;
}

function Line({ label, value, strong = false }) { const isPrice = String(value).includes('₹'); return <div className={`flex justify-between ${strong ? 'mt-4 border-t pt-4 text-xl font-bold' : 'mt-3'}`}><span>{label}</span><b className={isPrice ? 'price' : ''}>{value}</b></div>; }

function RoomStep({ pkg, date, nights, rooms, setRooms, people, onContinue }) {
  const [items, setItems] = useState([]);
  const hotelId = pkg.hotel_id ?? pkg.hotelId;
  useEffect(() => { if (hotelId && date) { const checkout = new Date(`${date}T00:00:00`); checkout.setDate(checkout.getDate() + nights); const checkoutDate = checkout.toISOString().slice(0, 10); api.get(`/hotels/${hotelId}/rooms?checkin=${encodeURIComponent(date)}&checkout=${encodeURIComponent(checkoutDate)}`).then(({ data }) => setItems(data)).catch(() => setItems([])); } }, [hotelId, date, nights]);
  const capacity = rooms.reduce((total, room) => total + (room.capacity || 0) * (room.quantity || 0), 0);
  const change = (room, delta) => { const current = rooms.find(x => x.roomId === room.id); const quantity = (current?.quantity || 0) + delta; if (quantity <= 0) return setRooms(rooms.filter(x => x.roomId !== room.id)); if (quantity > (room.availableUnits ?? room.total_units)) return; setRooms([{ roomId: room.id, quantity, capacity: room.capacity }, ...rooms.filter(x => x.roomId !== room.id)]); };
  return <div><p className="mb-5 text-slate-500">Choose rooms for {people} people across {nights} nights.</p><div className="grid gap-3">{items.map(room => { const current = rooms.find(x => x.roomId === room.id); return <div className="flex items-center justify-between rounded-xl border border-slate-200 p-4" key={room.id}><div><b>{room.room_type}</b><p className="text-sm text-slate-500">Sleeps {room.capacity} · ₹{room.price_per_night}/night · {room.availableUnits ?? room.total_units} available</p></div>{current ? <div className="flex items-center gap-2"><button aria-label={`Remove ${room.room_type}`} onClick={() => change(room, -1)} className="rounded-lg bg-slate-100 p-2"><Minus size={16} /></button><b>{current.quantity}</b><button aria-label={`Add ${room.room_type}`} onClick={() => change(room, 1)} className="rounded-lg bg-teal-50 p-2 text-primary"><Plus size={16} /></button></div> : <button onClick={() => change(room, 1)} className="rounded-lg bg-teal-50 px-3 py-2 text-sm font-bold text-primary">Select</button>}</div>; })}</div><CapacityMessage covered={capacity} required={people} noun="beds" /><button disabled={capacity < people} onClick={onContinue} className="btn-primary mt-7">Continue <ArrowRight size={18} className="ml-2" /></button></div>;
}

function VehicleStep({ destinationId, date, vehicles, setVehicles, people, onContinue }) {
  const [items, setItems] = useState([]);
  useEffect(() => { if (destinationId) safe(() => api.get(`/vehicles?destinationId=${destinationId}&date=${date || ''}&seats=${people}`), []).then(setItems); }, [destinationId, date, people]);
  const seats = vehicles.reduce((total, vehicle) => total + (vehicle.seatCapacity || 0) * (vehicle.quantity || 0), 0);
  const change = (vehicle, delta) => { const current = vehicles.find(x => x.vehicleId === vehicle.id); const quantity = (current?.quantity || 0) + delta; if (quantity <= 0) return setVehicles(vehicles.filter(x => x.vehicleId !== vehicle.id)); if (quantity > (vehicle.units_available ?? 1)) return; setVehicles([{ vehicleId: vehicle.id, quantity, seatCapacity: vehicle.seat_capacity }, ...vehicles.filter(x => x.vehicleId !== vehicle.id)]); };
  return <div><p className="mb-5 text-slate-500">Choose vehicles with at least {people} seats in total.</p><div className="grid gap-3">{items.map(vehicle => { const current = vehicles.find(x => x.vehicleId === vehicle.id); return <div className="flex items-center justify-between rounded-xl border border-slate-200 p-4" key={vehicle.id}><div><b>{vehicle.model}</b><p className="text-sm text-slate-500">{vehicle.seat_capacity} seats · ₹{vehicle.price_per_day}/day · {vehicle.units_available ?? 'available'} units</p></div>{current ? <div className="flex items-center gap-2"><button aria-label={`Remove ${vehicle.model}`} onClick={() => change(vehicle, -1)} className="rounded-lg bg-slate-100 p-2"><Minus size={16} /></button><b>{current.quantity}</b><button aria-label={`Add ${vehicle.model}`} onClick={() => change(vehicle, 1)} className="rounded-lg bg-teal-50 p-2 text-primary"><Plus size={16} /></button></div> : <button onClick={() => change(vehicle, 1)} className="rounded-lg bg-teal-50 px-3 py-2 text-sm font-bold text-primary">Select</button>}</div>; })}</div><CapacityMessage covered={seats} required={people} noun="vehicle seats" /><button disabled={seats < people} onClick={onContinue} className="btn-primary mt-7">Continue <ArrowRight size={18} className="ml-2" /></button></div>;
}

function CapacityMessage({ covered, required, noun }) { return <div className={`mt-5 rounded-xl p-4 text-sm ${covered >= required ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-700'}`}>{covered >= required ? `Capacity covered: ${covered} ${noun}.` : `You need ${required - covered} more ${noun} for ${required} people.`}</div>; }

function TravelerStep({ people, travelers, setTravelers, onContinue }) {
  const update = (index, key, value) => setTravelers(current => {
    const next = [...current];
    next[index] = { ...(next[index] || {}), [key]: value };
    return next;
  });

  return (
    <form onSubmit={onContinue}>
      <p className="mb-5 text-slate-500">Enter details for each traveler. IDs are securely stored with your booking.</p>
      <div className="grid gap-5">
        {Array.from({ length: people }).map((_, i) => {
          const traveler = travelers[i] || {};
          return (
            <div className="rounded-2xl border border-slate-200 bg-slate-50/70 p-5 shadow-sm" key={i}>
              <div className="mb-4 flex items-center justify-between border-b border-slate-200 pb-3">
                <span className="font-bold text-slate-900">Traveler {i + 1}</span>
                <span className="rounded-full bg-teal-50 px-2.5 py-0.5 text-xs font-semibold text-primary">
                  {i === 0 ? 'Primary Traveler' : 'Companion'}
                </span>
              </div>
              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-1.5">
                    Full Name <span className="text-red-500">*</span>
                  </label>
                  <input
                    required
                    aria-label={`Traveler ${i + 1} full name`}
                    className="input w-full bg-white"
                    placeholder="e.g. John Doe"
                    value={traveler.fullName || traveler.full_name || ''}
                    onChange={e => update(i, 'fullName', e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-1.5">
                    Age <span className="text-red-500">*</span>
                  </label>
                  <input
                    required
                    aria-label={`Traveler ${i + 1} age`}
                    className="input w-full bg-white"
                    placeholder="e.g. 28"
                    type="number"
                    min="0"
                    max="120"
                    value={traveler.age !== undefined ? traveler.age : ''}
                    onChange={e => update(i, 'age', e.target.value)}
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-1.5">
                    Gender
                  </label>
                  <select
                    aria-label={`Traveler ${i + 1} gender`}
                    className="input w-full bg-white"
                    value={traveler.gender || 'PREFER_NOT_TO_SAY'}
                    onChange={e => update(i, 'gender', e.target.value)}
                  >
                    <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
                    <option value="FEMALE">Female</option>
                    <option value="MALE">Male</option>
                    <option value="NON_BINARY">Non-binary</option>
                  </select>
                </div>
                <div>
                  <label className="block text-xs font-bold uppercase tracking-wider text-slate-600 mb-1.5">
                    Govt ID / Passport <span className="text-red-500">*</span>
                  </label>
                  <input
                    required
                    aria-label={`Traveler ${i + 1} ID number`}
                    className="input w-full bg-white"
                    placeholder="e.g. AADHAAR / Passport / DL"
                    value={traveler.idNumber || traveler.id_number || ''}
                    onChange={e => update(i, 'idNumber', e.target.value)}
                  />
                </div>
              </div>
            </div>
          );
        })}
      </div>
      <button type="submit" className="btn-primary mt-7">
        Review price <ArrowRight size={18} className="ml-2" />
      </button>
    </form>
  );
}
