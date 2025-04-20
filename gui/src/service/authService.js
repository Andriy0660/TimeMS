import authApi from "../api/authApi.js";

const authService = {
  handleUnauthorized: () => {
    localStorage.removeItem("token");
    if (!window.location.href.match("login")) {
      window.location.href = "/app/login";
    }
  },

  logOut: async () => {
    await authApi.logOut();
    localStorage.removeItem("token");
  },

  getCurrentUser: async () => {
    return await authApi.getCurrentUser();
  },

  hasRole: (user, role) => {
    if (!user || !user.roles) return false;

    return user.roles.some(userRole =>
      userRole === role ||
      (role === "ROLE_MANAGER" && userRole === "ROLE_ADMIN")
    );
  }
}

export default authService;