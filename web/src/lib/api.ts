import axios, { AxiosError } from "axios";

import config from "@/config";

export const api = axios.create({
  baseURL: config.api.url,
  withCredentials: true,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Central place to add request-wide behavior.
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      // The session may be missing or expired.
      // Let the calling feature decide how to handle it.
    }

    return Promise.reject(error);
  }
);