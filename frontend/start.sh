#!/bin/sh

# Create assets directory if it doesn't exist
mkdir -p /usr/share/nginx/html/assets

# Generate env.js file with environment variables
cat > /usr/share/nginx/html/assets/env.js << EOF
(function (window) {
  window.env = window.env || {};

  // Environment variables
  window.env.APP_URL = '${APP_URL}';
  window.env.USER_SERVICE_URL = '${USER_SERVICE_URL}';
  window.env.USER_SERVICE_PORT = '${USER_SERVICE_PORT}';

  // Debug info
  console.log('Runtime env loaded:', {
    APP_URL: window.env.APP_URL,
    USER_SERVICE_URL: window.env.USER_SERVICE_URL,
    USER_SERVICE_PORT: window.env.USER_SERVICE_PORT,
    current_origin: window.location.origin
  });
})(this);
EOF

# Start nginx
nginx -g "daemon off;"