<%@ page pageEncoding="UTF-8" %>
<%@ include file="cabecera.jspf" %>
<section class="panel narrow"><p class="eyebrow">REVISA LA OPERACIÓN</p><h1>No se pudo completar</h1><p role="alert"><%=html(request.getAttribute("error"))%></p><button type="button" id="volver" class="btn btn-outline-dark">Volver al formulario</button><a class="btn btn-primary" href="<%=base%>/app/catalogo">Ir al catálogo</a></section>
<%@ include file="pie.jspf" %>
