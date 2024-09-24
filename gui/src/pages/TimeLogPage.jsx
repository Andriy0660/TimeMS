import TimeLogList from "../components/TimeLogList.jsx";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {useMutation, useQuery, useQueryClient} from "@tanstack/react-query";
import timeLogApi from "../api/timeLogApi.js";
import {Checkbox, CircularProgress, FormControl, FormControlLabel, IconButton, ListItemText, MenuItem, Select, Switch} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";
import MonthPicker from "../components/MonthPicker..jsx";
import WeekPicker from "../components/WeekPicker.jsx";
import Label from "../components/Label.jsx";
import DayProgressBar from "../components/DayProgressBar.jsx";
import ClearIcon from '@mui/icons-material/Clear';
import fileService from "../service/fileService.js";
import Button from "@mui/material/Button";
import ImportButton from "../components/ImportButton.jsx";
import worklogApi from "../api/worklogApi.js";
import WorklogList from "../components/WorklogList.jsx";

export default function TimeLogPage() {
  const {
    date, addAlert, mode, groupByDescription, setGroupByDescription, filterTickets, selectedTickets, setSelectedTickets,
    timeLogs, processedDataRef, isListing, isPlaceholderData, totalTimeLabel,
    create, divide, importTimeLogs, update, createWorklogFromTimeLog, deleteTimeLog,
    setGroupDescription, changeDate, syncWorklogsForIssue,
    hoveredTimeLogIds, setHoveredTimeLogIds, setHoveredProgressIntervalId, hoveredConflictedIds,
    setHoveredConflictedIds, hoveredProgressIntervalId,
  } = useAppContext();
  const queryClient = useQueryClient();

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

  if (isListing) {
    return (
      <div className="absolute inset-1/2">
        <CircularProgress />
      </div>
    );
  }

  const modeDatePickerConfig = {
    Week: <WeekPicker buttonColor="blue" isActive={isPlaceholderData} />,
    Month: <MonthPicker buttonColor="blue" isActive={isPlaceholderData} />,
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
                onChange={(event) => setGroupByDescription((event.target.checked))}
              />
            }
            label="Group"
            labelPlacement="start"
            className="mx-2"
          />

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
          hoveredConflictedIds={hoveredConflictedIds}
          setHoveredConflictedIds={setHoveredConflictedIds}
        />
        <WorklogList mode={mode} date={date} selectedTickets={selectedTickets}/>

      </div>
    </div>
  )
}