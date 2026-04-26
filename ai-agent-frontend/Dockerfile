# Frontend build stage
FROM node:20-alpine AS build
WORKDIR /app
COPY . .
RUN npm install
RUN npm run build

# Runtime stage: serve static files with nginx
FROM nginx:alpine
# Copy build output to nginx static file directory
COPY --from=build /app/dist /usr/share/nginx/html
# Replace the default nginx config with the custom config
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Expose HTTP port
EXPOSE 80
# Start nginx
CMD ["nginx", "-g", "daemon off;"]
