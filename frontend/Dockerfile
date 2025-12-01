# Stage 1: Build
FROM node:20-alpine AS build
WORKDIR /app

# Copiar archivos de dependencias
COPY frontend/package*.json ./
RUN npm ci --legacy-peer-deps

# Copiar código fuente
COPY frontend/ ./

# Build de producción
RUN npm run build

# Stage 2: Servir con nginx
FROM nginx:stable-alpine

# Copiar build al directorio de nginx
COPY --from=build /app/dist /usr/share/nginx/html

# Copiar configuración personalizada de nginx (opcional)
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

# Exponer puerto
EXPOSE 80

# Comando de inicio
CMD ["nginx", "-g", "daemon off;"]
