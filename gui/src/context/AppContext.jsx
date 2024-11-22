import {createContext, useEffect, useState} from "react";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import dayjs from "dayjs";
import {viewMode} from "../consts/viewMode.js";

const AppContext = createContext();

export const AppProvider = ({children}) => {
  const queryParams = new URLSearchParams(location.search);
  const [mode, setMode] = useState(queryParams.get("mode") || viewMode.DAY);
  const [date, setDate] = useState(queryParams.get("date") ? dayjs(queryParams.get("date")) : dayjs())

  const [counter, setCounter] = useState(0);
  const forceRender = () => setCounter((prev) => prev + 1);

  const [timeLogRefs, setTimeLogRefs] = useState([]);
  const [externalTimeLogRefs, setExternalTimeLogRefs] = useState([]);

  useEffect(() => {
    setTimeLogRefs([]);
    setExternalTimeLogRefs([]);
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
        forceRender,
        date,
        setDate,
        addAlert,
        mode,
        setMode,
        timeLogRefs,
        setTimeLogRefs,
        externalTimeLogRefs,
        setExternalTimeLogRefs
      }}>
        <div key={counter}>{children}</div>
      </AppContext.Provider>
    </>
  );
};

export default AppContext;
