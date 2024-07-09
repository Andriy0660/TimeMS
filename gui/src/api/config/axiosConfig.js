import axios from "axios";
import {baseUrl} from "../../config/config.js";

const axiosInstance = axios.create({
  baseURL: baseUrl
});

axiosInstance.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 500) {
      error.displayMessage = 'Server error, try again later...';
    } else {
      const baseMsg = error.config.baseMsg || 'Error occurred';
      const detailMsg = error.response?.data?.detail ?? "";
      error.displayMessage = `${baseMsg}. ${detailMsg}`;
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
