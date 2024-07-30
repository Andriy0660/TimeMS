import TimeLogList from "../components/TimeLogList.jsx";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {LocalizationProvider} from "@mui/x-date-pickers";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {CircularProgress, IconButton, MenuItem, Select, Tooltip} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useEffect, useState} from "react";
import dayjs from "dayjs";
import dateTimeService from "../utils/dateTimeService.js";
import DayPicker from "../components/DayPicker.jsx";
import MonthPicker from "../components/MonthPicker..jsx";
import WeekPicker from "../components/WeekPicker.jsx";
import SettingsBackupRestoreIcon from '@mui/icons-material/SettingsBackupRestore';
import {useNavigate} from "react-router-dom";

export default function TimeLogPage() {
  const [timeLogs, setTimeLogs] = useState({});

  const queryParams = new URLSearchParams(location.search);
  const [date, setDate] = useState(queryParams.get("date") ? dayjs(queryParams.get("date")) : dayjs());
  const [mode, setMode] = useState(queryParams.get("mode") || "Day");

  const queryClient = useQueryClient();
  const {addAlert} = useAppContext();

  const navigate = useNavigate();
  useEffect(() => {
    const params = new URLSearchParams();
    if (mode && mode !== "Day") {
      params.set("mode", mode);
    }
    if (date && !dayjs().isSame(date, "day")) {
      params.set("date", dateTimeService.getFormattedDateTime(date));
    }
    navigate({search: params.toString()});
  }, [mode, date]);
  const {
    data,
    isPending: isListing,
    error: listAllError,
  } = useQuery({
    queryKey: [timeLogApi.key, mode, date],
    queryFn: () => {
      return timeLogApi.list({mode, date: dateTimeService.getFormattedDate(date)});
    },
    placeholderData: (prev) => prev,
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
      if(mode !== "Day" || !dayjs().isSame(date, "day")) {
        setDate(dayjs());
        setMode("Day");
      }
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

  const modeDatePickerConfig = {
    Day: <DayPicker date={date} setDate={setDate}/>,
    Week: <WeekPicker date={date} setDate={setDate}/>,
    Month: <MonthPicker date={date} setDate={setDate}/>,
    All: null,
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <div>
        <TimeLogCreateBar
          onCreate={create}
        />
        <div className="flex flex-col">
          <div className="flex justify-center">
            <Select
              className="mx-2"
              size="small"
              inputProps={{"aria-label": "Without label"}}
              value={mode}
              onChange={(event) => {
                setDate(dayjs());
                setMode(event.target.value);
              }}
              autoWidth
            >
              <MenuItem value="Day">Day</MenuItem>
              <MenuItem value="Week">Week</MenuItem>
              <MenuItem value="Month">Month</MenuItem>
              <MenuItem value="All">All</MenuItem>
            </Select>
              {modeDatePickerConfig[mode]}
              {mode !== "All" &&
                <Tooltip title="reset">
                  <IconButton
                    onClick={() => setDate(dayjs())}
                    variant="outlined"
                    color="primary"
                  >
                    <SettingsBackupRestoreIcon />
                  </IconButton>
                </Tooltip>

              }
          </div>
          <TimeLogList
            timeLogs={timeLogs}
            mode={mode}
            onCreate={create}
            onUpdate={update}
            onDelete={deleteTimeLog}
          />
        </div>
      </div>
    </LocalizationProvider>
  )
}