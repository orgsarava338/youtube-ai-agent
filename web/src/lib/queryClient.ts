import { QueryClient } from "@tanstack/react-query";
import axios from "axios";

export const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30 * 1000, // 30 seconds
            gcTime: 5 * 60 * 1000, // 5 minutes
            retry: (failureCount, error) => {
                // Don't retry authentication or client errors automatically.
                if (
                    axios.isAxiosError(error) &&
                    error.response?.status != null &&
                    error.response.status >= 400 &&
                    error.response.status < 500
                ) {
                    return false;
                }

                // Retry transient failures once.
                return failureCount < 1;
            },
            refetchOnWindowFocus: true,
        },
        mutations: {
            retry: 0,
        },
    },
});
