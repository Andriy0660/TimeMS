import TimeLogList from "../components/TimeLogList.jsx";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {FormControlLabel, IconButton, Switch} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import MonthPicker from "../components/MonthPicker..jsx";
import WeekPicker from "../components/WeekPicker.jsx";
import BigLabel from "../components/BigLabel.jsx";
import DayProgressBar from "../components/DayProgressBar.jsx";
import ClearIcon from '@mui/icons-material/Clear';
import ImportButton from "../components/ImportButton.jsx";
import WorklogList from "../components/WorklogList.jsx";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import {useState} from "react";
import {viewMode} from "../consts/viewMode.js";
import worklogService from "../service/worklogService.js";
import ExportButton from "../components/ExportButton.jsx";
import TimeLogSelectTicketsForm from "../components/TimeLogSelectTicketsForm.jsx";
import LoadingPage from "../components/LoadingPage.jsx";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";
import useSync from "../hooks/useSync.js";
import useWorklogMutations from "../hooks/useWorklogMutations.js";
import {isJiraSyncingEnabled} from "../config/config.js";

export default function TimeLogPage() {
  const {date, mode} = useAppContext();
  const [hoveredTimeLogIds, setHoveredTimeLogIds] = useState([]);
  const [hoveredProgressIntervalId, setHoveredProgressIntervalId] = useState(0);
  const [hoveredConflictedIds, setHoveredConflictedIds] = useState([]);

  const [isJiraEditMode, setIsJiraEditMode] = useState(false);

  const {
    groupByDescription, setGroupByDescription, timeLogs, processedTimeLogsArray, isListing,
    worklogs, isWorklogsListing,
    totalTimeLabel, filterTickets, selectedTickets, setSelectedTickets,
  } = useProcessedTimeLogs();

  const timeLogMutations = useTimeLogMutations();
  const {onCreate: onWorklogCreate}  = useWorklogMutations();
  const syncMutations = useSync();

  if (isListing) {
    return <LoadingPage />
  }

  const modeDatePickerConfig = {
    Week: <WeekPicker buttonColor="blue"/>,
    Month: <MonthPicker buttonColor="blue"/>,
    All: null,
  };

  const commonTimeLogListProps = {
    timeLogs,
    mode,
    ...timeLogMutations,
    onWorklogCreate,
    ...syncMutations,
    hoveredTimeLogIds,
    setHoveredProgressIntervalId,
    hoveredConflictedIds,
    setHoveredConflictedIds
  };

  return (
    <div>
      <div className="w-3/5 mx-auto">
        <TimeLogCreateBar
          onCreate={timeLogMutations.onCreate}
          date={date}
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

            {isJiraSyncingEnabled && (
              <>
                <TimeLogSelectTicketsForm filterTickets={filterTickets} selectedTickets={selectedTickets}
                                          setSelectedTickets={setSelectedTickets} />
                <IconButton className="mr-2" onClick={() => setSelectedTickets([])}>
                  <ClearIcon />
                </IconButton>
              </>
            )}

            {isJiraSyncingEnabled && <FormControlLabel
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
            }
            {modeDatePickerConfig[mode]}

          </div>
          <div className="flex justify-between items-center">
            <BigLabel className="ml-4 mt-4">{date.format("dddd")}</BigLabel>
            <BigLabel className="ml-4 mt-4">{totalTimeLabel}</BigLabel>
            <div className="flex items-center mt-8">
              <ImportButton className="mr-4" onImport={timeLogMutations.onImport} />
              <ExportButton className="mr-4" processedTimeLogsArray={processedTimeLogsArray}/>
            </div>

          </div>
        </div>
      </div>

      <div className={`${isJiraEditMode ? "w-4/5" : "w-3/5"} mx-auto`}>
        {mode === viewMode.DAY &&
          <DayProgressBar timeLogs={processedTimeLogsArray} date={date} setHoveredTimeLogIds={setHoveredTimeLogIds}
                          hoveredProgressIntervalId={hoveredProgressIntervalId} />}

        {!isJiraEditMode && (
          <TimeLogList {...commonTimeLogListProps} />
        )}

        {isJiraSyncingEnabled && isJiraEditMode && (
          <div className="flex">
            <div className="w-1/2 mr-6">
              <TimeLogList {...commonTimeLogListProps} isJiraEditMode />
            </div>
            <div className="w-1/2 ml-6">
              <WorklogList
                worklogs={worklogService.processData(worklogs, processedTimeLogsArray, selectedTickets)}
                isWorklogsListing={isWorklogsListing}
                isJiraEditMode={isJiraEditMode}
              />
            </div>
          </div>
        )}

      </div>

    </div>
  )
}