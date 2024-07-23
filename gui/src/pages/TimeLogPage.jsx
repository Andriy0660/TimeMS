import TimeLogList from "../components/TimeLogList.jsx";
import DayPicker from "../components/DayPicker.jsx";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {LocalizationProvider} from "@mui/x-date-pickers";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {CircularProgress} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useEffect, useState} from "react";
import dayjs from "dayjs";
import dateTimeService from "../utils/dateTimeService.js";
export default function TimeLogPage() {
  const [timeLogs, setTimeLogs] = useState([]);
  const [date, setDate] = useState(dayjs());

  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const {
    data,
    isPending: isListing,
    error: listAllError,
  } = useQuery({
    queryKey: [timeLogApi.key, date],
    queryFn: () => {
      return timeLogApi.list({day: dateTimeService.getFormattedDate(date)});
    },
    retryDelay: 300,
  });

  useEffect(() => {
    if (data) {
      setTimeLogs(data);
    }
  }, [data]);

  const {mutateAsync: create} = useMutation({
    mutationFn: (body) => timeLogApi.create(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogs.key);
      if (body.conflicted) {
        addAlert({
          text: "Time log is created with time conflicts with other time logs",
          type: "warning"
        });
      } else {
        addAlert({
          text: "Time log is successfully created",
          type: "success"
        });
      }
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating time log failed:", error);
    }
  });

  const {mutateAsync: update} = useMutation({
    mutationFn: (body) => timeLogApi.update(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogs.key);
      if (body.conflicted) {
        addAlert({
          text: "Time log is updated with time conflicts with other time logs",
          type: "warning"
        });
      } else {
        addAlert({
          text: "Time log is successfully updated",
          type: "success"
        });
      }
    },
    onError: async (error, body) => {
      queryClient.invalidateQueries(timeLogs.key);
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Updating time log failed:", error);
    }
  });

  const {mutateAsync: deleteTimeLog} = useMutation({
    mutationFn: (id) => timeLogApi.delete(id),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogs.key);
      addAlert({
        text: "You have successfully deleted time log",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Deleting time log failed:", error);
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
        <CircularProgress />
      </div>
    );
  }
  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <div>
        <TimeLogCreateBar
          onCreate={create}
        />
        <div className="flex flex-col">
        <DayPicker
          date={date}
          setDate={setDate}
        />
        <TimeLogList
          timeLogs={timeLogs}
          onCreate={create}
          onUpdate={update}
          onDelete={deleteTimeLog}
        />
        </div>
      </div>
    </LocalizationProvider>
  )
}