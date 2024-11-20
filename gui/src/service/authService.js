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
  }
}

export default authService;