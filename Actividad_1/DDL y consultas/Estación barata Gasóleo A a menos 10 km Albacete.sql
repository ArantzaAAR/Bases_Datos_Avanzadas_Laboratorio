SELECT e.direccion AS Localizacion, e.localidad, emp.nombre AS Empresa, e.margen, pr.precio
FROM precio pr
JOIN estacion e ON pr.id_estacion = e.id_estacion
JOIN empresa emp ON e.id_empresa = emp.id_empresa
JOIN carburante c ON pr.id_carburante = c.id_carburante
WHERE c.nombre = 'Gasoleo A' 
AND SQRT( POW((e.latitud - 38.9942) * 111, 2) + POW((e.longitud - (-1.8585)) * 111 * COS(RADIANS(38.9942)), 2) ) <= 10
ORDER BY pr.precio ASC
LIMIT 1;