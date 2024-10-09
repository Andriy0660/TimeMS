import {createContext, useEffect, useState} from "react";
import {toast, ToastContainer} from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import dayjs from "dayjs";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import worklogApi from "../api/worklogApi.js";
import timeLogApi from "../api/timeLogApi.js";
import {viewMode} from "../consts/viewMode.js";
import syncApi from "../api/syncApi.js";

const AppContext = createContext();

export const AppProvider = ({children}) => {
  const queryParams = new URLSearchParams(location.search);
  const [mode, setMode] = useState(queryParams.get("mode") || viewMode.DAY);
  const [date, setDate] = useState(queryParams.get("date") ? dayjs(queryParams.get("date")) : dayjs())
  const isJiraSyncingEnabled = true;

  const queryClient = useQueryClient();
  const [timeLogRefs, setTimeLogRefs] = useState([]);

  useEffect(() => {
    setTimeLogRefs([])
  }, [date]);

  const [worklogRefs, setWorklogRefs] = useState([]);

  useEffect(() => {
    setWorklogRefs([])
  }, [date]);

  const addAlert = ({type, text}) => {
    return toast[type](text);
  };

  const {mutateAsync: syncWorklogs, isPending: isSyncingLaunched} = useMutation({
    mutationFn: () => syncApi.syncAllWorklogs(),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "You have successfully synchronized worklogs",
        type: "success"
      });
    },
    onError: (error) => {
      queryClient.setQueryData([worklogApi.key, "progress"], {progress: 0});
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("synchronizing worklogs failed:", error);
    }
  });

  const {
    data: progressInfo,
  } = useQuery({
    queryKey: [worklogApi.key, "progress"],
    queryFn: () => syncApi.getProgress(),
    initialData: () => 0,
    refetchInterval: (data) => isSyncingLaunched || data.state.data.inProgress ? 300 : false,
    refetchOnWindowFocus: false,
    retryDelay: 300
  });


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
        isJiraSyncingEnabled,
        date,
        setDate,
        addAlert,
        mode,
        setMode,
        timeLogRefs,
        setTimeLogRefs,
        worklogRefs,
        setWorklogRefs,
        syncWorklogs,
        progressInfo,
        isSyncingLaunched,
        isSyncingRunning: progressInfo.inProgress && progressInfo.progress > 0,
      }}>
        {children}
      </AppContext.Provider>
    </>
  );
};

export default AppContext;
