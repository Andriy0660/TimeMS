import {createContext, useEffect, useState} from "react";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import dayjs from "dayjs";

const AppContext = createContext();

export const AppProvider = ({children}) => {
  const queryParams = new URLSearchParams(location.search);
  const [mode, setMode] = useState(queryParams.get("mode") || "Day");
  const [date, setDate] = useState(queryParams.get("date") ? dayjs(queryParams.get("date")) : dayjs())
  const [view, setView] = useState(queryParams.get("view") || "Day")

  const [timeLogRefs, setTimeLogRefs] = useState([]);
  useEffect(() => {
    setTimeLogRefs([])
  }, [date]);

  const addAlert = ({type, text}) => {
    return toast[type](text);
  };

  return (
    <>
      <ToastContainer
        position="bottom-left"
        autoClose={5000}
        limit={3}
        hideProgressBar={false}
        newestOnTop={false}
        closeOnClick={false}
        rtl={false}
        pauseOnFocusLoss={false}
        draggable
        pauseOnHover={false}
        theme="light"
        transition: Bounce
      />
      <AppContext.Provider value={{
        date,
        setDate,
        view,
        setView,
        addAlert,
        mode,
        setMode,
        timeLogRefs,
        setTimeLogRefs
      }}>
        {children}
      </AppContext.Provider>
    </>
  );
};

export default AppContext;
