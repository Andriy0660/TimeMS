import {createBrowserRouter, Navigate, RouterProvider} from "react-router-dom";
import InfoPage from "./pages/InfoPage.jsx";
import TimeLogPage from "./pages/TimeLogPage.jsx";
import Root from "./Root.jsx";
import WeekPage from "./pages/WeekPage.jsx";
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
