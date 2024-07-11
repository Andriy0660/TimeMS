import LogEntry from "./LogEntry.jsx";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {LocalizationProvider} from "@mui/x-date-pickers";
import Divider from "@mui/material/Divider";
import CreateLogEntry from "./CreateLogEntry.jsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import logEntryApi from "../api/logEntryApi.js";
import {CircularProgress} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useEffect, useState} from "react";

export default function LogEntryList({}) {

  const [logEntries, setLogEntries] = useState([]);

  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {
    data,
    isPending: isListing,
    error: listAllError,
  } = useQuery({
    queryKey: [logEntryApi.key],
    queryFn: () => {
      return logEntryApi.listAll();
    },
    retryDelay: 300,
  });

  useEffect(() => {
    if (data) {
      setLogEntries(data);
    }
  }, [data]);

  const {mutateAsync: create} = useMutation({
    mutationFn: (body) => logEntryApi.create(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(logEntries.key);
      if (body.conflicted) {
        addAlert({
          text: "Log entry is created with time conflicts with other entries",
          type: "warning"
        });
      } else {
        addAlert({
          text: "Log entry is successfully created",
          type: "success"
        });
      }
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating log entry failed:", error);
    }
  });

  const {mutateAsync: update} = useMutation({
    mutationFn: (body) => logEntryApi.update(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(logEntries.key);
      if(body.conflicted) {
        addAlert({
          text: "Log entry is updated with time conflicts with other entries",
          type: "warning"
        });
      } else {
        addAlert({
          text: "Log entry is successfully updated",
          type: "success"
        });
      }
    },
    onError: async (error, body) => {
      queryClient.invalidateQueries(logEntries.key);
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Updating log entry failed:", error);
    }
  });

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
      <LogEntry
        logEntry={logEntry}
        onCreate={create}
        onUpdate={update}
      />
    </div>
  })

  return (
    <div className="m-4 flex flex-col items-center">
      <LocalizationProvider dateAdapter={AdapterDayjs}>
        <div className="w-3/5 overflow-x-auto shadow-md bg-gray-50">
          <CreateLogEntry
            onCreate={create}
          />
          {renderedLogEntries}
        </div>
      </LocalizationProvider>
    </div>

  );

}
