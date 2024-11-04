import TimeLogList from "../components/timeLog/TimeLogList.jsx";
import TimeLogCreateBar from "../components/timeLog/TimeLogCreateBar.jsx";
import {CircularProgress, FormControlLabel, IconButton, Switch} from "@mui/material";
import useAppContext from "../context/useAppContext.js";
import BigLabel from "../components/general/BigLabel.jsx";
import DayProgressBar from "../components/day/DayProgressBar.jsx";
import ClearIcon from '@mui/icons-material/Clear';
import ImportButton from "../components/timeLog/ImportButton.jsx";
import WorklogList from "../components/worklog/WorklogList.jsx";
import useTimeLogMutations from "../hooks/useTimeLogMutations.js";
import {useEffect, useState} from "react";
import {viewMode} from "../consts/viewMode.js";
import worklogService from "../service/worklogService.js";
import ExportButton from "../components/timeLog/ExportButton.jsx";
import TimeLogSelectTicketsForm from "../components/timeLog/TimeLogSelectTicketsForm.jsx";
import LoadingPage from "../components/general/LoadingPage.jsx";
import useProcessedTimeLogs from "../hooks/useProcessedTimeLogs.js";
import useJiraSync from "../hooks/useJiraSync.js";
import useWorklogMutations from "../hooks/useWorklogMutations.js";
import {isJiraSyncingEnabled, isUpworkSyncingEnabled, upworkTimeCf} from "../config/config.js";
import timeLogService from "../service/timeLogService.js";
import dateTimeService from "../service/dateTimeService.js";
import SyncInfoLabel from "../components/sync/SyncInfoLabel.jsx";
import TimeLogUpworkSyncer from "../components/timeLog/TimeLogUpworkSyncer.jsx";
import {useQuery} from "@tanstack/react-query";
import syncUpworkApi from "../api/syncUpworkApi.js";

export default function TimeLogPage() {
  const {date, mode, addAlert} = useAppContext();
  const [hoveredTimeLogIds, setHoveredTimeLogIds] = useState([]);
  const [hoveredProgressIntervalId, setHoveredProgressIntervalId] = useState(0);
  const [hoveredConflictedIds, setHoveredConflictedIds] = useState([]);

  const [isJiraEditMode, setIsJiraEditMode] = useState(false);

  const {
    data: upworkStatus,
    isPending: isGettingUpworkStatus,
    error: upworkStatusError
  } = useQuery({
    queryKey: [date],
    queryFn: () => syncUpworkApi.getStatus(dateTimeService.getFormattedDate(date)),
    retryDelay: 300,
  });

  useEffect(() => {
    if (upworkStatusError) {
      addAlert({
        text: `${upworkStatusError.displayMessage} Try again later`,
        type: "error"
      });
    }
  }, [upworkStatusError]);

  const {
    groupByDescription, setGroupByDescription, timeLogs, processedTimeLogsArray, isListing,
    worklogs, isWorklogsListing,
    totalTimeLabel, filterTickets, selectedTickets, setSelectedTickets,
  } = useProcessedTimeLogs();

  const timeLogMutations = useTimeLogMutations();
  const {onCreate: onWorklogCreate}  = useWorklogMutations();
  const syncMutations = useSync();
  const syncMutations = useJiraSync();

  if (isListing) {
    return <LoadingPage />
  }

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

            {isJiraSyncingEnabled && (
              <SyncInfoLabel className="ml-8" color="blue">
                <FormControlLabel
                  control={
                    <Switch
                      checked={isJiraEditMode}
                      onChange={(event) => setIsJiraEditMode((event.target.checked))}
                    />
                  }
                  label="Jira Edit Mode"
                  labelPlacement="start"
                />
              </SyncInfoLabel>
            )}

            {isUpworkSyncingEnabled && (
              <>
                <SyncInfoLabel className="ml-8" color="green">
                  Upwork: {dateTimeService.formatMinutesToHM(
                  timeLogService.getTotalMinutesForTimeLogsArray(processedTimeLogsArray, upworkTimeCf)
                )}
                  {isGettingUpworkStatus ? <CircularProgress className="ml-2" size={25} /> : <TimeLogUpworkSyncer
                    status={upworkStatus}
                    date={date}
                    timeSpentSeconds={timeLogService.getTotalSecondsForTimeLogsArray(processedTimeLogsArray)}
                  />
                  }
                </SyncInfoLabel>
              </>
            )}
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