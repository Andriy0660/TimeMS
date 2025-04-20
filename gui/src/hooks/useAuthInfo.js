import { useState, useEffect } from "react";
import authService from "../service/authService.js";
import useAppContext from "../context/useAppContext.js";

export default function useAuthInfo() {
  const [user, setUser] = useState(null);
  const [isLoading, setIsLoading] = useState(true);
  const { addAlert } = useAppContext();

  useEffect(() => {
    const fetchCurrentUser = async () => {
      try {
        setIsLoading(true);
        const token = localStorage.getItem("token");

        if (!token) {
          setUser(null);
          return;
        }

        const userData = await authService.getCurrentUser();
        setUser(userData);
      } catch (error) {
        console.error("Error fetching user info:", error);
        addAlert({
          text: "Failed to load user information",
          type: "error"
        });
        setUser(null);
      } finally {
        setIsLoading(false);
      }
    };
    fetchCurrentUser();
  }, []);

  return { user, isLoading };
}