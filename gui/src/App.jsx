// gui/src/App.jsx
import {createBrowserRouter, Navigate, RouterProvider} from "react-router-dom";
import InfoPage from "./pages/InfoPage.jsx";
import TimeLogPage from "./pages/TimeLogPage.jsx";
import Root from "./Root.jsx";
import WeekPage from "./pages/WeekPage.jsx";
import MonthPage from "./pages/MonthPage.jsx";
import SyncPage from "./pages/SyncPage.jsx";
import SignUpPage from "./pages/SignUpPage.jsx";
import LogInPage from "./pages/LogInPage.jsx";
import ConfigPage from "./pages/ConfigPage.jsx";
import NotAuthorizedPage from "./pages/NotAuthorizedPage.jsx";
import ProtectedRoute from "./components/auth/ProtectedRoute.jsx";
import ManagerPage from "./pages/ManagerPage.jsx";
import AdminPage from "./pages/AdminPage.jsx";
import useAuthInfo from "./hooks/useAuthInfo.js";
import LoadingPage from "./pages/LoadingPage.jsx";

function RoleBasedRedirect() {
  const { user, isLoading } = useAuthInfo(); // Отримуємо дані користувача через ваш хук
  // Повертаємо завантажувальний стан, якщо дані ще не отримані
  if (isLoading) {
    return <LoadingPage />;
  }

  // Перевіряємо роль користувача і перенаправляємо відповідно
  if (user?.roles?.includes("ROLE_ADMIN")) {
    return <Navigate replace to="/app/admin" />;
  } else if (user?.roles?.includes("ROLE_MANAGER")) {
    return <Navigate replace to="/app/manager" />;
  } else {
    // Для звичайних користувачів або якщо роль не визначена
    return <Navigate replace to="/app/timelog" />;
  }
}

function App() {
  const router = createBrowserRouter([
    {
      path: "/",
      element: <Navigate replace to="/app" />,
    },
    {
      path: "/app",
      element: <Root />,
      children: [
        {
          path: "/app",
          element: <RoleBasedRedirect />, // Використовуємо наш новий компонент
        },
        {
          path: "/app/timelog",
          element: <ProtectedRoute role="ROLE_USER"><TimeLogPage /></ProtectedRoute>, // Захищаємо для звичайних користувачів
        },
        {
          path: "/app/weekview",
          element: <ProtectedRoute role="ROLE_USER"><WeekPage /></ProtectedRoute>,
        },
        {
          path: "/app/monthview",
          element: <ProtectedRoute role="ROLE_USER"><MonthPage /></ProtectedRoute>,
        },
        {
          path: "/app/syncWorklogs",
          element: <ProtectedRoute role="ROLE_USER"><SyncPage /></ProtectedRoute>,
        },
        {
          path: "/app/login",
          element: <LogInPage />,
        },
        {
          path: "/app/signup",
          element: <SignUpPage />,
        },
        {
          path: "/app/config",
          element: <ProtectedRoute><ConfigPage /></ProtectedRoute>, // Загальний доступ для авторизованих
        },
        {
          path: "/app/admin",
          element: <ProtectedRoute role="ROLE_ADMIN"><AdminPage /></ProtectedRoute>,
        },
        {
          path: "/app/manager",
          element: <ProtectedRoute role="ROLE_MANAGER"><ManagerPage /></ProtectedRoute>,
        },
        {
          path: "/app/not-authorized",
          element: <NotAuthorizedPage />,
        },
      ],
    },
  ]);
  return (
    <RouterProvider router={router} />
  );
}
export default App;