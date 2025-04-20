import { Navigate } from "react-router-dom";
import { useEffect, useState } from "react";
import useAuthInfo from "../../hooks/useAuthInfo.js";
import LoadingPage from "../general/LoadingPage.jsx";

export default function ProtectedRoute({ children, role }) {
  const { user, isLoading } = useAuthInfo();
  const [checkedAccess, setCheckedAccess] = useState(false);
  const [hasAccess, setHasAccess] = useState(false);

  useEffect(() => {
    if (!isLoading && user) {
      if (!role) {
        setHasAccess(true);
      } else {
        const hasRole = user.roles.some(userRole =>
          userRole === role
          ||
          (role === "ROLE_MANAGER" && userRole === "ROLE_ADMIN")
        );
        setHasAccess(hasRole);
      }
      setCheckedAccess(true);
    }
  }, [user, isLoading, role]);

  if (isLoading || !checkedAccess) {
    return <LoadingPage />;
  }

  if (!user) {
    return <Navigate to="/app/login" />;
  }

  if (!hasAccess) {
    return <Navigate to="/app/not-authorized" />;
  }

  return children;
}
