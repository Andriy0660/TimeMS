import {createBrowserRouter, Navigate, RouterProvider} from "react-router-dom";
import InfoPage from "./pages/InfoPage.jsx";
import TimeLogPage from "./pages/TimeLogPage.jsx";
import Root from "./Root.jsx";
import WeekPage from "./pages/WeekPage.jsx";
import MonthPage from "./pages/MonthPage.jsx";
import SyncPage from "./pages/SyncPage.jsx";
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
        {
          path: "/app/info",
          element: <InfoPage />,
        },
      ],
    },
  ]);
  return (
    <RouterProvider router={router} />
  );
}
export default App;
