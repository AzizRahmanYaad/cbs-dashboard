export const environment = {
  production: false,
  // Use empty string so requests go to same origin and proxy (proxy.conf.json) forwards to backend — avoids CORS in dev.
  apiUrl: ''
};
