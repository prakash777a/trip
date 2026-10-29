import {useEffect,useState} from 'react';
export function useApi(loader,initial){const [data,setData]=useState(initial);const [loading,setLoading]=useState(true);useEffect(()=>{let live=true;loader().then(x=>live&&setData(x)).finally(()=>live&&setLoading(false));return()=>{live=false}},[]);return {data,loading,setData}}
