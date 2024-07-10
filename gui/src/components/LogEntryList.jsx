import LogEntry from "./LogEntry.jsx";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {LocalizationProvider} from "@mui/x-date-pickers";
import Divider from "@mui/material/Divider";
import CreateLogEntry from "./CreateLogEntry.jsx";
import {useQuery} from "@tanstack/react-query";
import logEntryApi from "../api/logEntryApi.js";
import {CircularProgress} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useEffect} from "react";

export default function LogEntryList({}) {
  const {
    data: logEntries,
    isPending: isListing,
    error: listAllError
  } = useQuery({
    queryKey: [logEntryApi.key],
    queryFn: () => {
      return logEntryApi.listAll();
    },
    retryDelay: 300,
  });

  const {addAlert} = useAppContext();
  useEffect(() => {
    if (listAllError) {
      addAlert({
        text: `${listAllError.displayMessage} Try agail later`,
        type: "error"
      });
    }
  }, [listAllError]);

  if (isListing) {
    return (
      <div className="flex justify-center items-center h-screen">
        <CircularProgress  />
      </div>
    );
  }

  const renderedLogEntries = logEntries?.map((logEntry) => {
    return <div key={logEntry.id}>
      <Divider />
      <LogEntry logEntry={logEntry} />
    </div>
  })

  return (
    <div className="m-4 flex flex-col items-center">
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <div className="w-3/5 overflow-x-auto shadow-md bg-gray-50">
          <CreateLogEntry />
          {renderedLogEntries}
        </div>
      </LocalizationProvider>
    </div>

  );

}
