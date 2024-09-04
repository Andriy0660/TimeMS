import TimeLogList from "../components/TimeLogList.jsx";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {
  Checkbox,
  CircularProgress,
  FormControl,
  FormControlLabel,
  IconButton,
  ListItemText,
  MenuItem,
  Select,
  Switch,
  Tooltip
} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useEffect, useRef, useState} from "react";
import dayjs from "dayjs";
import dateTimeService from "../service/dateTimeService.js";
import MonthPicker from "../components/MonthPicker..jsx";
import WeekPicker from "../components/WeekPicker.jsx";
import SettingsBackupRestoreIcon from '@mui/icons-material/SettingsBackupRestore';
import {useNavigate} from "react-router-dom";
import timeLogProcessingService from "../service/timeLogProcessingService.js";
import {startHourOfDay} from "../config/timeConfig.js";
import TotalTimeLabel from "../components/TotalTimeLabel.jsx";
import DayProgressBar from "../components/DayProgressBar.jsx";
import ClearIcon from '@mui/icons-material/Clear';
import fileService from "../service/fileService.js";
import Button from "@mui/material/Button";
import ImportButton from "../components/ImportButton.jsx";

export default function TimeLogPage() {
  const [timeLogs, setTimeLogs] = useState([]);
  const [hoveredTimeLogIds, setHoveredTimeLogIds] = useState([]);

  const queryParams = new URLSearchParams(location.search);
  const {date, setDate, addAlert} = useAppContext();
  const [mode, setMode] = useState(queryParams.get("mode") || "Day");
  const offset = startHourOfDay;
  const [groupByDescription, setGroupByDescription] = useState(!!queryParams.get("groupByDescription") || false);
  const [filterTickets, setFilterTickets] = useState([""])
  const [selectedTickets, setSelectedTickets] = useState([]);

  const [totalTimeLabel, setTotalTimeLabel] = useState("")
  const queryClient = useQueryClient();

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
  const processedDataRef = useRef([]);
  useEffect(() => {
    const processedData = timeLogProcessingService.processData(data, selectedTickets);
    processedDataRef.current = processedData;

    const filterTickets = getFilterTickets(data);
    updateSelectedTicketsIfNeeded(filterTickets);

    const groupedData = groupAndSortData(processedData, groupByDescription);
    const label = calculateTotalTimeLabel(groupedData, groupByDescription);
    setTimeLogs(groupedData)
    setTotalTimeLabel(label);
  }, [data, groupByDescription, selectedTickets])

  function getFilterTickets(data) {
    const filterTickets = timeLogProcessingService.extractTickets(data);
    filterTickets.push("Without ticket");
    setFilterTickets(filterTickets);
    return filterTickets;
  }

  function updateSelectedTicketsIfNeeded(filterTickets) {
    const updatedTickets = selectedTickets.filter(ticket => filterTickets.includes(ticket));
    if (selectedTickets.toString() !== updatedTickets.toString()) {
      setSelectedTickets(updatedTickets);
    }
  }

  function groupAndSortData(data, groupByDescription) {
    if (groupByDescription) {
      return timeLogProcessingService.group(data, ["date", "ticketAndDescription"]);
    } else {
      return timeLogProcessingService.group(data, ["date"]);
    }
  }

  function calculateTotalTimeLabel(groupedData, groupByDescription) {
    if (groupByDescription) {
      return dateTimeService.formatDuration(dateTimeService.getTotalTimeGroupedByDateAndDescription(groupedData.data));
    } else {
      return dateTimeService.formatDuration(dateTimeService.getTotalTimeGroupedByDate(groupedData.data));
    }
  }

  const {mutateAsync: create} = useMutation({
    mutationFn: (body) => timeLogApi.create(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time log is successfully created",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating time log failed:", error);
    }
  });

  const {mutateAsync: importTimeLogs} = useMutation({
    mutationFn: (body) => timeLogApi.importTimeLogs(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Imported successfully",
        type: "success"
      });

    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Importing failed:", error);
    }
  });

  const {mutateAsync: update} = useMutation({
    mutationFn: (body) => timeLogApi.update(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time log is successfully updated",
        type: "success"
      });
    },
    onError: async (error, body) => {
      queryClient.invalidateQueries(timeLogApi.key);
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
      queryClient.invalidateQueries(timeLogApi.key);
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
      queryClient.invalidateQueries(timeLogApi.key);
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
      queryClient.invalidateQueries(timeLogApi.key);
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

  const saveFile = async () => {
    const formattedText = fileService.convertToTxt(processedDataRef.current);
    const blob = new Blob([formattedText], {type: "text/plain"});
    const a = document.createElement('a');
    a.download = `${dateTimeService.getFormattedDate(date)}-${mode}`
    a.href = URL.createObjectURL(blob);
    a.addEventListener("click", (e) => {
      setTimeout(() => URL.revokeObjectURL(a.href), 30 * 1000);
    });
    a.click();
  };

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
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }

  const modeDatePickerConfig = {
    Week: <WeekPicker
      isLoading={isPlaceholderData}/>,
    Month: <MonthPicker
      isLoading={isPlaceholderData}/>,
    All: null,
  };

  return (
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
              setMode(event.target.value);
            }}
            autoWidth
          >
            <MenuItem value="Day">Day</MenuItem>
            <MenuItem value="Week">Week</MenuItem>
            <MenuItem value="Month">Month</MenuItem>
            <MenuItem value="All">All</MenuItem>
          </Select>

          <FormControl className="mx-2">
            <Select
              size="small"
              multiple
              value={selectedTickets}
              onChange={(event) => setSelectedTickets(event.target.value)}
              renderValue={(selected) => (
                selected.length > 0 ? selected.join(", ") : <em>Select tickets</em>
              )}
              displayEmpty
            >
              {filterTickets.map((ticket) => (
                <MenuItem key={ticket} value={ticket}>
                  <Checkbox size="small" checked={selectedTickets.indexOf(ticket) > -1} />
                  <ListItemText primary={ticket} />
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <IconButton className="mr-2" onClick={() => setSelectedTickets([])}>
            <ClearIcon />
          </IconButton>

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
        <div className="flex justify-between items-center">
          <TotalTimeLabel label={totalTimeLabel} />
          <div className="mt-8">
            <ImportButton className="mr-4" onImport={importTimeLogs}/>
            <Button
              className="mr-4"
              variant="outlined"
              onClick={saveFile}
            >
              Export
            </Button>
          </div>

        </div>
        {mode === "Day" && <DayProgressBar timeLogs={processedDataRef.current} date={date} setHoveredTimeLogIds={setHoveredTimeLogIds} />}

        <TimeLogList
          timeLogs={timeLogs}
          mode={mode}
          onCreate={create}
          onUpdate={update}
          onDelete={deleteTimeLog}
          setGroupDescription={setGroupDescription}
          changeDate={changeDate}
          hoveredTimeLogIds={hoveredTimeLogIds}
        />
      </div>
    </div>
  )
}