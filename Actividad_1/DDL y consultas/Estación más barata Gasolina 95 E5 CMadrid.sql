SELECT e.direccion AS Localizacion, e.localidad, emp.nombre AS Empresa, e.margen, pr.precio
FROM precio pr
JOIN estacion e ON pr.id_estacion = e.id_estacion
JOIN municipio m ON e.id_municipio = m.id_municipio
JOIN provincia p ON m.id_provincia = p.id_provincia
JOIN empresa emp ON e.id_empresa = emp.id_empresa
JOIN carburante c ON pr.id_carburante = c.id_carburante
WHERE p.nombre = 'Madrid' AND c.nombre = 'Gasolina 95 E5'
ORDER BY pr.precio ASC
LIMIT 1;