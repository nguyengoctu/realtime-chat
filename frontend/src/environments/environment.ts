export const environment = {
  production: false,
  apiUrl: (window as any)['env']?.['APP_URL'] ? `${(window as any)['env']['APP_URL']}/apil` : 'http://localhost/api'
};