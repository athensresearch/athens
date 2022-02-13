FROM httpd:2.4
COPY ./vercel-static/athens/ /usr/local/apache2/htdocs/
