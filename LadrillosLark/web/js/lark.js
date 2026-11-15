"use strict";
document.querySelectorAll('[data-confirm]').forEach(b=>b.addEventListener('click',e=>{if(!confirm(b.dataset.confirm))e.preventDefault();}));
const back=document.getElementById('volver');if(back)back.addEventListener('click',()=>history.back());
const printButton=document.getElementById('imprimir');if(printButton)printButton.addEventListener('click',()=>window.print());
const envio=document.getElementById('envio'),descuento=document.getElementById('descuento'),total=document.getElementById('total');
function calcular(){if(!total)return;const sub=Number(total.dataset.subtotal),cost=envio.value==='1'?25:0,desc=descuento?Number(descuento.value):0;total.textContent=(sub-Math.round(sub*desc)/100+cost).toFixed(2);document.getElementById('deliveryCosto').textContent=cost.toFixed(2);document.querySelector('[name="direccion"]').required=envio.value==='1';document.querySelector('[name="distrito"]').required=envio.value==='1';}
if(envio){envio.addEventListener('change',calcular);if(descuento)descuento.addEventListener('input',calcular);calcular();}
if(document.querySelector('[data-refresh]'))setInterval(()=>{if(!document.hidden&&!['INPUT','SELECT','TEXTAREA'].includes(document.activeElement.tagName))location.reload();},30000);
