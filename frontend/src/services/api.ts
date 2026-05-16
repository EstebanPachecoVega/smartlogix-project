import axios from 'axios';

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para añadir token JWT (más adelante)
api.interceptors.request.use((config) => {
  // const token = localStorage.getItem('token');
  // if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Interceptor para manejar errores de ProblemDetail
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.data) {
      const problemDetail = error.response.data;
      // Lanza un error con el mensaje detail o el genérico
      throw new Error(problemDetail.detail || problemDetail.title || 'Error en la petición');
    }
    throw error;
  }
);

export default api;