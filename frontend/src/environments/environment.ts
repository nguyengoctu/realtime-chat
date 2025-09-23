export const environment = {
  production: false,
  apiUrl: (window as any)['env']?.['API_GATEWAY_URL'] || `http://localhost:${(window as any)['env']?.['API_GATEWAY_PORT'] || '9080'}`
};