import TimeLogList from "../components/TimeLogList.jsx";
import {AdapterDayjs} from "@mui/x-date-pickers/AdapterDayjs";
import {LocalizationProvider} from "@mui/x-date-pickers";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {CircularProgress, FormControlLabel, IconButton, MenuItem, Select, Switch, Tooltip} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useEffect, useState} from "react";
import dayjs from "dayjs";
import dateTimeService from "../service/dateTimeService.js";
import DayPicker from "../components/DayPicker.jsx";
import MonthPicker from "../components/MonthPicker..jsx";
import WeekPicker from "../components/WeekPicker.jsx";
import SettingsBackupRestoreIcon from '@mui/icons-material/SettingsBackupRestore';
import {useNavigate} from "react-router-dom";
import timeLogProcessingService from "../service/timeLogProcessingService.js";
import {startHourOfDay} from "../config/timeConfig.js";
import TotalTimeLabel from "../components/TotalTimeLabel.jsx";

export default function TimeLogPage() {
  const [timeLogs, setTimeLogs] = useState([]);

  const queryParams = new URLSearchParams(location.search);
  const [date, setDate] = useState(queryParams.get("date") ? dayjs(queryParams.get("date")) : dayjs());
  const [mode, setMode] = useState(queryParams.get("mode") || "Day");
  const offset = startHourOfDay;
  const [groupByDescription, setGroupByDescription] = useState(!!queryParams.get("groupByDescription") || false);

  const [totalTimeLabel, setTotalTimeLabel] = useState("")
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
    if(groupByDescription) {
      params.set("groupByDescription", true);
    }
    navigate({search: params.toString()});
  }, [mode, date, groupByDescription]);

  const {
    data,
    isPending: isListing,
    error: listAllError,
    isPlaceholderData
  } = useQuery({
    queryKey: [timeLogApi.key, mode, date, offset],
    queryFn: () => {
      return timeLogApi.list({mode, date: dateTimeService.getFormattedDate(date), offset});
    },
    placeholderData: (prev) => prev,
    retryDelay: 300,
  });

  useEffect(() => {
    let dataNotNull = data ? JSON.parse(JSON.stringify(data)) : [];
    dataNotNull = timeLogProcessingService.processTimeLogDateTime(dataNotNull);
    let groupedAndSortedData;
    if (!groupByDescription) {
      groupedAndSortedData = timeLogProcessingService.group(dataNotNull, ["date"])
      setTotalTimeLabel(dateTimeService.getTotalTimeGroupedByDate(groupedAndSortedData));
    } else {
      groupedAndSortedData = timeLogProcessingService.group(dataNotNull, ["date", "description"])
      setTotalTimeLabel(dateTimeService.getTotalTimeGroupedByDateAndDescription(groupedAndSortedData));
    }
    setTimeLogs(groupedAndSortedData)
  }, [data, groupByDescription])

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
  const {mutateAsync: setGroupDescription} = useMutation({
    mutationFn: (body) => timeLogApi.setGroupDescription(body),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogs.key);
      addAlert({
        text: "You have successfully set description",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Setting group description failed:", error);
    }
  });
  const {mutateAsync: changeDate} = useMutation({
    mutationFn: (body) => timeLogApi.changeDate(body),
    onSuccess: () => {
      queryClient.invalidateQueries(timeLogs.key);
      addAlert({
        text: "You have successfully changed date",
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("Changing date failed:", error);
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
    Day: <DayPicker date={date} setDate={setDate} isPlaceholderData={isPlaceholderData}/>,
    Week: <WeekPicker date={date} setDate={setDate} isPlaceholderData={isPlaceholderData}/>,
    Month: <MonthPicker date={date} setDate={setDate} isPlaceholderData={isPlaceholderData}/>,
    All: null,
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDayjs}>
      <div className="w-3/5 mx-auto">
        <TimeLogCreateBar
          onCreate={create}
          date={date}
          canCreate={mode === "Day"}
        />
        <div className="flex flex-col">
          <div className="flex justify-center">
            <FormControlLabel
              control={
                <Switch
                  checked={groupByDescription}
                  onChange={() => setGroupByDescription((event.target.checked))}
                />
              }
              label="Group"
              labelPlacement="start"
              className="mx-2"
            />
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
          <TotalTimeLabel label={totalTimeLabel} />
          <TimeLogList
            timeLogs={timeLogs}
            mode={mode}
            onCreate={create}
            onUpdate={update}
            onDelete={deleteTimeLog}
            setGroupDescription={setGroupDescription}
            changeDate={changeDate}
          />
        </div>
      </div>
    </LocalizationProvider>
  )
}