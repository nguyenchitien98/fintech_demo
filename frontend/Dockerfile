# Stage 1: Build stage
FROM node:20-alpine AS builder
WORKDIR /app

# Copy dependency configs
COPY frontend/package*.json ./
RUN npm install

# Copy application source code and build it
COPY frontend/ .
RUN npm run build

# Stage 2: Clean runtime stage
FROM node:20-alpine
WORKDIR /app
ENV NODE_ENV=production

# Copy built resources from builder stage
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

EXPOSE 3000
ENTRYPOINT ["npm", "start"]
