import TimeLogList from "../components/TimeLogList.jsx";
import TimeLogCreateBar from "../components/TimeLogCreateBar.jsx";
import {CircularProgress, FormControlLabel, IconButton, Switch} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import dateTimeService from "../service/dateTimeService.js";
import MonthPicker from "../components/MonthPicker..jsx";
import WeekPicker from "../components/WeekPicker.jsx";
import BigLabel from "../components/BigLabel.jsx";
import DayProgressBar from "../components/DayProgressBar.jsx";
import ClearIcon from '@mui/icons-material/Clear';
import ImportButton from "../components/ImportButton.jsx";
import WorklogList from "../components/WorklogList.jsx";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import {useState} from "react";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";
import {viewMode} from "../consts/viewMode.js";
import {useQuery} from "@tanstack/react-query";
import worklogApi from "../api/worklogApi.js";
import worklogService from "../service/worklogService.js";
import {startHourOfDay} from "../config/timeConfig.js";
import ExportButton from "../components/ExportButton.jsx";
import TimeLogSelectTicketsForm from "../components/TimeLogSelectTicketsForm.jsx";

export default function TimeLogPage() {
  const {isJiraSyncingEnabled, date, mode} = useAppContext();
  const [hoveredTimeLogIds, setHoveredTimeLogIds] = useState([]);
  const [hoveredProgressIntervalId, setHoveredProgressIntervalId] = useState(0);
  const [hoveredConflictedIds, setHoveredConflictedIds] = useState([]);

  const [isJiraEditMode, setIsJiraEditMode] = useState(false);

  const {
    groupByDescription, setGroupByDescription, timeLogs, processedTimeLogsArray, isListing,
    totalTimeLabel, filterTickets, selectedTickets, setSelectedTickets,
  } = useProcessedTimeLogs();

  const timeLogMutations = useTimeLogMutations();

  const {
    data: worklogs,
    isPending: isWorklogsListing,
    error: listWorklogsError,
  } = useQuery({
    queryKey: [worklogApi.key, mode, date, startHourOfDay],
    queryFn: () => {
      return worklogApi.list({mode, date: dateTimeService.getFormattedDate(date)});
    },
    initialData: () => [],
    placeholderData: (prev) => prev,
    retryDelay: 300,
  });


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

  const commonTimeLogListProps = {
    timeLogs,
    mode,
    ...timeLogMutations,
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
        {mode === viewMode.DAY && <DayProgressBar timeLogs={processedTimeLogsArray} date={date} setHoveredTimeLogIds={setHoveredTimeLogIds}
                                                  hoveredProgressIntervalId={hoveredProgressIntervalId} />}

        {!isJiraEditMode ? (
          <TimeLogList {...commonTimeLogListProps} />
        ) : (
          <div className="flex">
            <div className="w-1/2 mr-6">
              <TimeLogList {...commonTimeLogListProps} isJiraEditMode />
            </div>
            <div className="w-1/2 ml-6">
              <WorklogList
                worklogs={worklogService.processData(worklogs, processedTimeLogsArray, selectedTickets)}
                isWorklogsListing={isWorklogsListing}
                listWorklogsError={listWorklogsError}
                isJiraEditMode={isJiraEditMode}
              />
            </div>
          </div>
        )}
      </div>
    </div>
  )
}