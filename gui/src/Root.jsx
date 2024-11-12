import {Outlet, useLocation} from "react-router-dom";
import NavBar from "./components/NavBar.jsx";

export default function Root() {
  const location = useLocation();
  const isAuthPage = location.pathname.match("login") || location.pathname.match("signup");

  return (
    <div>
      {!isAuthPage && <NavBar />}
      <Outlet />
    </div>
  );
}