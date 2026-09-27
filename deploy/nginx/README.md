# Nginx configuration

Host-level Nginx configs for EVA.

Install:

sudo cp deploy/nginx/*.conf /etc/nginx/sites-available/

Enable a site:
sudo ln -s \
  /etc/nginx/sites-available/eva-miniapp.conf \
  /etc/nginx/sites-enabled/eva-miniapp.conf

Validate and reload:
sudo nginx -t
sudo systemctl reload nginx

TLS certificates are managed separately by Certbot and are not stored in Git.
