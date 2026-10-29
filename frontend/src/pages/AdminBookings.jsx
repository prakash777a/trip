import React, { useEffect, useMemo, useState } from 'react';
import { ArrowLeft, CalendarDays, Search, Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import { api } from '../services/api';
import { Fade, SectionTitle } from '../App';

const statuses = ['ALL', 'DRAFT', 'CONFIRMED', 'CANCELLED'];

const formatMoney = value => `₹${Number(value || 0).toLocaleString('en-IN', {
  maximumFractionDigits: 2
})}`;

const formatDate = value => value ? new Date(`${value}T00:00:00`).toLocaleDateString(
  'en-IN', { day: '2-digit', month: 'short', year: 'numeric' }
) : 'Date pending';

export default function AdminBookings() {
  const [bookings, setBookings] = useState([]);
  const [status, setStatus] = useState('ALL');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/admin/bookings/overview')
      .then(({ data }) => setBookings(data.content || []))
      .catch(apiError => setError(
        apiError.response?.status === 401 || apiError.response?.status === 403
          ? 'Your administrator session is no longer valid. Please sign in again.'
          : 'Could not load booking records.'
      ))
      .finally(() => setLoading(false));
  }, []);

  const filtered = useMemo(() => {
    const query = search.trim().toLowerCase();
    return bookings.filter(booking => (
      (status === 'ALL' || booking.status === status) &&
      (!query || JSON.stringify(booking).toLowerCase().includes(query))
    ));
  }, [bookings, search, status]);

  const countFor = currentStatus => currentStatus === 'ALL'
    ? bookings.length
    : bookings.filter(booking => booking.status === currentStatus).length;

  return <Fade><main className="mx-auto max-w-7xl px-5 pb-20 pt-32">
    <Link to="/admin" className="mb-6 inline-flex items-center gap-2 text-sm font-semibold text-primary">
      <ArrowLeft size={17} /> Back to admin workspace
    </Link>
    <div className="flex flex-wrap items-end justify-between gap-5">
      <SectionTitle eyebrow="TripCraft operations" title="Booked trips" copy="Review booking activity, traveller details, totals, and current booking status." />
      <div className="card flex items-center gap-3 px-5 py-4"><Users className="text-primary" size={22} /><div><div className="text-sm text-slate-500">All bookings</div><strong className="text-2xl">{bookings.length}</strong></div></div>
    </div>

    {error && <div role="alert" className="mb-5 rounded-xl bg-red-50 p-4 text-sm text-red-700">{error}</div>}

    <section className="card overflow-hidden">
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-slate-100 p-5">
        <div className="flex flex-wrap gap-2">{statuses.map(item => <button key={item} onClick={() => setStatus(item)} className={`rounded-full px-4 py-2 text-sm font-semibold ${status === item ? 'bg-primary text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'}`}>{item[0] + item.slice(1).toLowerCase()} ({countFor(item)})</button>)}</div>
        <label className="flex items-center gap-2 rounded-xl bg-slate-50 px-3 py-2"><Search size={16} className="text-slate-400" /><input aria-label="Search booked trips" className="bg-transparent text-sm outline-none" placeholder="Search traveller or trip" value={search} onChange={event => setSearch(event.target.value)} /></label>
      </div>

      {loading ? <div className="p-12 text-center text-slate-500">Loading booking records…</div> : <div className="overflow-x-auto"><table className="w-full min-w-[850px] text-left text-sm"><thead className="bg-slate-50 text-xs uppercase text-slate-400"><tr><th className="px-5 py-4">Booking</th><th className="px-5 py-4">Traveller</th><th className="px-5 py-4">Trip</th><th className="px-5 py-4">Travel date</th><th className="px-5 py-4">Guests</th><th className="px-5 py-4">Total</th><th className="px-5 py-4">Status</th></tr></thead><tbody>{filtered.map(booking => <tr className="border-t border-slate-100" key={booking.id}><td className="px-5 py-4"><strong>#{booking.id}</strong><div className="mt-1 text-xs text-slate-400">{booking.bookingCode}</div></td><td className="px-5 py-4"><strong>{booking.travellerName}</strong><div className="mt-1 text-xs text-slate-500">{booking.travellerEmail}</div></td><td className="px-5 py-4"><strong>{booking.packageTitle}</strong><div className="mt-1 text-xs text-slate-500">{booking.destinationName}</div></td><td className="px-5 py-4"><span className="inline-flex items-center gap-2"><CalendarDays size={15} className="text-primary" />{formatDate(booking.travelDate)}</span></td><td className="px-5 py-4">{booking.totalPeople || 0}</td><td className="price px-5 py-4">{formatMoney(booking.totalAmount)}</td><td className="px-5 py-4"><span className={`pill ${booking.status === 'CONFIRMED' ? 'bg-emerald-50 text-emerald-700' : booking.status === 'CANCELLED' ? 'bg-red-50 text-red-700' : 'bg-amber-50 text-amber-700'}`}>{booking.status}</span></td></tr>)}</tbody></table>{!filtered.length && <div className="p-12 text-center text-slate-500">No booking records match the selected filter.</div>}</div>}
    </section>
  </main></Fade>;
}
