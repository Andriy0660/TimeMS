import TimeLogList from "../components/TimeLogList.jsx";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {Checkbox, CircularProgress, FormControl, FormControlLabel, IconButton, ListItemText, MenuItem, Select, Switch} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import {useEffect, useRef, useState} from "react";
import dateTimeService from "../service/dateTimeService.js";
import MonthPicker from "../components/MonthPicker..jsx";
import WeekPicker from "../components/WeekPicker.jsx";
import {useNavigate} from "react-router-dom";
import timeLogProcessingService from "../service/timeLogProcessingService.js";
import {startHourOfDay} from "../config/timeConfig.js";
import Label from "../components/Label.jsx";
import DayProgressBar from "../components/DayProgressBar.jsx";
import ClearIcon from '@mui/icons-material/Clear';
import fileService from "../service/fileService.js";
import Button from "@mui/material/Button";
import ImportButton from "../components/ImportButton.jsx";
import worklogApi from "../api/worklogApi.js";
import WorklogList from "../components/WorklogList.jsx";

export default function TimeLogPage() {
  const [timeLogs, setTimeLogs] = useState([]);
  const [hoveredTimeLogIds, setHoveredTimeLogIds] = useState([]);
  const [hoveredProgressIntervalId, setHoveredProgressIntervalId] = useState(0);

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
    const params = new URLSearchParams(location.search);
    if (mode && mode !== "Day") {
      params.set("mode", mode);
    } else {
      params.delete("mode")
    }
    if(groupByDescription) {
      params.set("groupByDescription", true);
    } else {
      params.delete("groupByDescription");
    }
    navigate({search: params.toString()});
  }, [mode, groupByDescription]);

  const {
    data,
    isPending: isListing,
    error: listAllError,
    isPlaceholderData
  } = useQuery({
    queryKey: [timeLogApi.key, mode, date, offset],
    queryFn: () => {
      return timeLogApi.list({mode, date: dateTimeService.getFormattedDate(date)});
    },
    // placeholderData: (prev) => prev,
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

  const {mutateAsync: createWorklogFromTimeLog} = useMutation({
    mutationFn: (body) => worklogApi.create(body),
    onSuccess: async (body) => {
      queryClient.invalidateQueries(worklogApi.key);
      addAlert({
        text: "Worklog is successfully created",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Creating worklog failed:", error);
    }
  });

  const {mutateAsync: divide} = useMutation({
    mutationFn: (id) => timeLogApi.divide(id),
    onSuccess: async () => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: "Time logs is successfully divided",
        type: "success"
      });
    },
    onError: async (error, body) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      })
      console.error("Dividing time logs failed:", error);
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

  const {mutateAsync: syncWorklogs, isPending: isSyncing} = useMutation({
    mutationFn: (body) => worklogApi.syncWorklogs(),
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

  const {mutateAsync: syncWorklogsForIssue} = useMutation({
    mutationFn: (issueKey) => worklogApi.syncWorklogsForIssue(issueKey),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries(timeLogApi.key);
      addAlert({
        text: `You have successfully synchronized worklogs for issue ${variables}`,
        type: "success"
      });
    },
    onError: (error) => {
      addAlert({
        text: error.displayMessage,
        type: "error"
      });
      console.error("synchronizing worklogs for issue failed:", error);
    }
  });

  const {
    data: progressInfo,
  } = useQuery({
    queryKey: [worklogApi.key, "progress"],
    queryFn: () => worklogApi.getProgress(),
    initialData: () => 0,
    refetchInterval: (data) => isSyncing || data.state.data.progress > 0 ? 300 : false,
    refetchOnWindowFocus: false,
    retryDelay: 300
  });

  const progress = progressInfo.progress;
  const isSyncingRunning = isSyncing || progress > 0;

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
    Week: <WeekPicker buttonColor="blue" isActive={isPlaceholderData}/>,
    Month: <MonthPicker buttonColor="blue" isActive={isPlaceholderData}/>,
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

        </div>
        <div className="flex justify-between items-center">
          <Label label={date.format("dddd")}/>
          <Label label={totalTimeLabel} />
          <div className="flex items-center mt-8">
            <Button className="mr-4" disabled={isSyncing || progressInfo.progress > 0} variant="outlined" onClick={syncWorklogs}>
              {isSyncingRunning
                ? (
                  <>
                    {progress > 0 ? `${Math.floor(progress)}%` : <CircularProgress size={25} />}
                    <CircularProgress className="ml-1" variant="determinate" size={25} value={progress} />
                  </>
                )
                : "synchronize worklogs"}
            </Button>

            <ImportButton className="mr-4" onImport={importTimeLogs} />
            <Button
              className="mr-4"
              variant="outlined"
              onClick={saveFile}
            >
              Export
            </Button>
          </div>

        </div>
        {progress > 0 &&
          <div className="m-4 flex justify-center">
            <div className="text-center p-2 h-16 w-full overflow-x-auto shadow-md bg-gray-50">
              <div className="text-center">{progressInfo.ticketOfCurrentWorklog} {progressInfo.commentOfCurrentWorklog}</div>
            </div>
          </div>
        }
        {mode === "Day" && <DayProgressBar timeLogs={processedDataRef.current} date={date} setHoveredTimeLogIds={setHoveredTimeLogIds}
                                           hoveredProgressIntervalId={hoveredProgressIntervalId}/>}

        <TimeLogList
          timeLogs={timeLogs}
          mode={mode}
          onCreate={create}
          onDivide={divide}
          onUpdate={update}
          onWorklogCreate={createWorklogFromTimeLog}
          onDelete={deleteTimeLog}
          setGroupDescription={setGroupDescription}
          changeDate={changeDate}
          onSync={syncWorklogsForIssue}
          hoveredTimeLogIds={hoveredTimeLogIds}
          setHoveredProgressIntervalId={setHoveredProgressIntervalId}
        />
        <WorklogList mode={mode} date={date} selectedTickets={selectedTickets}/>

      </div>
    </div>
  )
}