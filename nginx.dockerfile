# nginx with a custom config
FROM nginx

# Copy from local working directory
COPY ./nginx.conf /etc/nginx/nginx.conf
COPY ./vercel-static/athens/ /usr/share/nginx/html
