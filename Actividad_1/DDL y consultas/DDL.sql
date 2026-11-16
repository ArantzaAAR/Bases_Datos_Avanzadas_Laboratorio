CREATE DATABASE IF NOT EXISTS gasolineras_db;
USE gasolineras_db;

CREATE TABLE provincia (
    id_provincia INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE municipio (
    id_municipio INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    id_provincia INT NOT NULL,
    FOREIGN KEY (id_provincia) REFERENCES provincia(id_provincia)
);

CREATE TABLE empresa (
    id_empresa INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL UNIQUE
);

CREATE TABLE carburante (
    id_carburante INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE estacion (
    id_estacion INT AUTO_INCREMENT PRIMARY KEY,
    id_municipio INT NOT NULL,
    id_empresa INT NOT NULL,
    localidad VARCHAR(150),
    codigo_postal VARCHAR(10),
    direccion VARCHAR(255),
    margen VARCHAR(10), 
    latitud DECIMAL(10,6),  
    longitud DECIMAL(10,6),
    horario VARCHAR(255),
    fecha_actualizacion DATETIME,
    tipo ENUM('TERRESTRE', 'MARITIMA') DEFAULT 'TERRESTRE',
    FOREIGN KEY (id_municipio) REFERENCES municipio(id_municipio),
    FOREIGN KEY (id_empresa) REFERENCES empresa(id_empresa)
);

CREATE TABLE precio (
    id_estacion INT NOT NULL,
    id_carburante INT NOT NULL,
    precio DECIMAL(6,3) NOT NULL, -- Ej: 1.659
    fecha_actualizacion DATETIME NOT NULL,
    PRIMARY KEY (id_estacion, id_carburante, fecha_actualizacion),
    FOREIGN KEY (id_estacion) REFERENCES estacion(id_estacion),
    FOREIGN KEY (id_carburante) REFERENCES carburante(id_carburante)
);