import axios from "axios";

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_BASE_URL
});

axiosInstance.interceptors.request.use(
  config => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = token;
    }
    return config;
  },
  error => Promise.reject(error)
);

axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 500) {
      error.displayMessage = 'Server error, try again later...';
    } else if (error.response?.status === 401) {
      localStorage.removeItem("token");
      window.location.href = "/app/login";
    } else {
      const baseMsg = error.config.baseMsg || 'Error occurred';
      const detailMsg = error.response?.data?.detail ?? "";
      error.displayMessage = `${baseMsg}. ${detailMsg}`;
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
