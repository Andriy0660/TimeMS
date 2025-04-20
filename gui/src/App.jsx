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
          element: <Navigate replace to="/app/timelog" />,
        },
        {
          path: "/app/timelog",
          element: <TimeLogPage />,
        },
        {
          path: "/app/weekview",
          element: <WeekPage />,
        },
        {
          path: "/app/monthview",
          element: <MonthPage />,
        },
        {
          path: "/app/syncWorklogs",
          element: <SyncPage />,
        },
        // {
        //   path: "/app/info",
        //   element: <InfoPage />,
        // },
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
          element: <ConfigPage />,
        },
      ],
    },
  ]);
  return (
    <RouterProvider router={router} />
  );
}
export default App;
