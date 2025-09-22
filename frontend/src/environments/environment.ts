export const environment = {
  production: false,
  apiUrl: (window as any)['env']?.['USER_SERVICE_URL'] || `http://localhost:${(window as any)['env']?.['USER_SERVICE_PORT'] || '8081'}`
};