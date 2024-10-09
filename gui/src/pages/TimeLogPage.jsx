import TimeLogList from "../components/TimeLogList.jsx";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
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
import WorklogList from "../components/WorklogList.jsx";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import {useState} from "react";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";
import {viewMode} from "../consts/viewMode.js";

export default function TimeLogPage() {
  const {date, mode} = useAppContext();

  const [hoveredTimeLogIds, setHoveredTimeLogIds] = useState([]);
  const [hoveredProgressIntervalId, setHoveredProgressIntervalId] = useState(0);
  const [hoveredConflictedIds, setHoveredConflictedIds] = useState([]);

  const [isJiraEditMode, setIsJiraEditMode] = useState(false);

  const {
    groupByDescription, setGroupByDescription, timeLogs, processedTimeLogsArray, isListing,
    totalTimeLabel, filterTickets, selectedTickets, setSelectedTickets,
  } = useProcessedTimeLogs();

  const timeLogMutations = useTimeLogMutations();

  const saveFile = async () => {
    const formattedText = fileService.convertToTxt(processedTimeLogsArray);
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
    Week: <WeekPicker buttonColor="blue"/>,
    Month: <MonthPicker buttonColor="blue"/>,
    All: null,
  };

  return (
    <div>
      <div className="w-3/5 mx-auto">
        <TimeLogCreateBar
          onCreate={timeLogMutations.onCreate}
          date={date}
          canCreate={mode === viewMode.DAY}
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
            <FormControlLabel
              control={
                <Switch
                  checked={isJiraEditMode}
                  onChange={(event) => setIsJiraEditMode((event.target.checked))}
                />
              }
              label="Jira Edit Mode"
              labelPlacement="start"
              className="ml-12"
            />
            {modeDatePickerConfig[mode]}

          </div>
          <div className="flex justify-between items-center">
            <Label label={date.format("dddd")} />
            <Label label={totalTimeLabel} />
            <div className="flex items-center mt-8">
              <ImportButton className="mr-4" onImport={timeLogMutations.onImport} />
              <Button
                className="mr-4"
                variant="outlined"
                onClick={saveFile}
              >
                Export
              </Button>
            </div>

          </div>
        </div>
      </div>
      <div className={`${isJiraEditMode ? "w-4/5" : "w-3/5"} mx-auto`}>
        {mode === viewMode.DAY && <DayProgressBar timeLogs={processedTimeLogsArray} date={date} setHoveredTimeLogIds={setHoveredTimeLogIds}
                                           hoveredProgressIntervalId={hoveredProgressIntervalId} />}

        {!isJiraEditMode && (
          <TimeLogList
            timeLogs={timeLogs}
            mode={mode}
            {...timeLogMutations}
            hoveredTimeLogIds={hoveredTimeLogIds}
            setHoveredProgressIntervalId={setHoveredProgressIntervalId}
            hoveredConflictedIds={hoveredConflictedIds}
            setHoveredConflictedIds={setHoveredConflictedIds}
            processedTimeLogsArray={processedTimeLogsArray}
          />
        )}
        {isJiraEditMode && (
          <>
            <div className="flex">
              <div className="w-1/2 mr-6">
                <TimeLogList
                  timeLogs={timeLogs}
                  mode={mode}
                  isJiraEditMode
                  {...timeLogMutations}
                  hoveredTimeLogIds={hoveredTimeLogIds}
                  setHoveredProgressIntervalId={setHoveredProgressIntervalId}
                  hoveredConflictedIds={hoveredConflictedIds}
                  setHoveredConflictedIds={setHoveredConflictedIds}
                  processedTimeLogsArray={processedTimeLogsArray}
                />
              </div>
              <div className="w-1/2 ml-6">
                <WorklogList isJiraEditMode={isJiraEditMode} mode={mode} date={date} selectedTickets={selectedTickets} timeLogs={processedTimeLogsArray}/>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  )
}