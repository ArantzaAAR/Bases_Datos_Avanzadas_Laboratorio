SELECT emp.nombre AS Empresa, COUNT(e.id_estacion) AS Total_Maritimas
FROM estacion e
JOIN empresa emp ON e.id_empresa = emp.id_empresa
WHERE e.tipo = 'MARITIMA'
GROUP BY emp.nombre
ORDER BY Total_Maritimas DESC
LIMIT 1;