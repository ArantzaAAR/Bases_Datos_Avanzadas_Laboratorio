SELECT emp.nombre AS Empresa, COUNT(e.id_estacion) AS Total_Terrestres
FROM estacion e
JOIN empresa emp ON e.id_empresa = emp.id_empresa
WHERE e.tipo = 'TERRESTRE'
GROUP BY emp.nombre
ORDER BY Total_Terrestres DESC
LIMIT 1;