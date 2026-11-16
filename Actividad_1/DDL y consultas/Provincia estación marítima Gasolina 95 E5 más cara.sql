SELECT p.nombre AS Provincia, e.localidad, emp.nombre AS Empresa, pr.precio
FROM precio pr
JOIN estacion e ON pr.id_estacion = e.id_estacion
JOIN municipio m ON e.id_municipio = m.id_municipio
JOIN provincia p ON m.id_provincia = p.id_provincia
JOIN empresa emp ON e.id_empresa = emp.id_empresa
JOIN carburante c ON pr.id_carburante = c.id_carburante
WHERE e.tipo = 'MARITIMA' AND c.nombre = 'Gasoleo A'
ORDER BY pr.precio DESC
LIMIT 1;
