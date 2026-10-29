import React from 'react';
export const Button=({children,variant='primary',...props})=><button className={variant==='accent'?'btn-accent':variant==='ghost'?'btn-ghost':'btn-primary'} {...props}>{children}</button>;
export const Badge=({children,tone='teal'})=><span className={`pill ${tone==='green'?'bg-emerald-50 text-emerald-700':tone==='amber'?'bg-amber-50 text-amber-700':''}`}>{children}</span>;
export const EmptyState=({title,copy,action})=><div className="card p-10 text-center"><h3 className="text-xl font-bold">{title}</h3><p className="mt-2 text-slate-500">{copy}</p>{action}</div>;
