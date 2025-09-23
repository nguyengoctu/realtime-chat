export const environment = {
  production: false,
  apiUrl: (window as any)['env']?.['APP_URL'] ? `${(window as any)['env']['APP_URL']}/api` : 'http://localhost/api'
};